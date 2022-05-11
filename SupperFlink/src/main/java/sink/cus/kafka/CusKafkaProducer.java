package sink.cus.kafka;

import org.apache.commons.lang3.StringUtils;
import org.apache.flink.annotation.VisibleForTesting;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeutils.SimpleTypeSerializerSnapshot;
import org.apache.flink.api.common.typeutils.TypeSerializerSnapshot;
import org.apache.flink.api.common.typeutils.base.TypeSerializerSingleton;
import org.apache.flink.api.java.ClosureCleaner;
import org.apache.flink.core.memory.DataInputView;
import org.apache.flink.core.memory.DataOutputView;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.shaded.guava18.com.google.common.collect.Lists;
import org.apache.flink.streaming.api.functions.sink.TwoPhaseCommitSinkFunction;
import org.apache.flink.streaming.api.operators.StreamingRuntimeContext;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.streaming.connectors.kafka.KafkaSerializationSchema;
import org.apache.flink.streaming.connectors.kafka.internals.FlinkKafkaInternalProducer;
import org.apache.flink.streaming.connectors.kafka.internals.TransactionalIdsGenerator;
import org.apache.flink.streaming.connectors.kafka.partitioner.FlinkKafkaPartitioner;
import org.apache.flink.streaming.util.serialization.KeyedSerializationSchema;
import org.apache.flink.util.TemporaryClassLoaderContext;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;

import javax.annotation.Nullable;
import java.io.IOException;
import java.time.Duration;
import java.util.*;

import static org.apache.flink.util.Preconditions.checkNotNull;
import static org.apache.flink.util.Preconditions.checkState;

public class CusKafkaProducer<IN> extends TwoPhaseCommitSinkFunction<
        IN,
        CusKafkaProducer.KafkaTransactionState,
        CusKafkaProducer.KafkaTransactionContext> {

    private String defaultTopicId;
    @Nullable private final KeyedSerializationSchema<IN> keyedSchema;
    private final KafkaSerializationSchema kafkaSchema;
    private final FlinkKafkaPartitioner<IN> flinkKafkaPartitioner;
    protected final Properties producerConfig;
    private CusKafkaProducer.Semantic semantic;
    public static final Time DEFAULT_KAFKA_TRANSACTION_TIMEOUT = Time.hours(1);
    /** Partitions of each topic. */
    protected final Map<String, int[]> topicPartitionsMap;
    /**
     * Max number of producers in the pool. If all producers are in use, snapshoting state will
     * throw an exception.
     */
    private final int kafkaProducersPoolSize;

    private static final short maxTaskNameSize = 1_000;

    /** Generator for Transactional IDs. */
    private transient TransactionalIdsGenerator transactionalIdsGenerator;

    public static final int SAFE_SCALE_DOWN_FACTOR = 5;



    private static final ListStateDescriptor<CusKafkaProducer.NextTransactionalIdHint>
            NEXT_TRANSACTIONAL_ID_HINT_DESCRIPTOR_V2 =
            new ListStateDescriptor<>(
                    "next-transactional-id-hint-v2",
                    new CusKafkaProducer.NextTransactionalIdHintSerializer());


    private static final ListStateDescriptor<CusKafkaProducer.NextTransactionalIdHint>
            NEXT_TRANSACTIONAL_ID_HINT_DESCRIPTOR =
            new ListStateDescriptor<>(
                    "next-transactional-id-hint",
                    TypeInformation.of(CusKafkaProducer.NextTransactionalIdHint.class));

    private transient ListState<CusKafkaProducer.NextTransactionalIdHint>
            nextTransactionalIdHintState;

    /** Hint for picking next transactional id. */
    private transient CusKafkaProducer.NextTransactionalIdHint nextTransactionalIdHint;

    private CusKafkaProducer(
            String defaultTopic,
            KeyedSerializationSchema<IN> keyedSchema,
            FlinkKafkaPartitioner<IN> customPartitioner,
            KafkaSerializationSchema<IN> kafkaSchema,
            Properties producerConfig,
            CusKafkaProducer.Semantic semantic,
            int kafkaProducersPoolSize) {
        super(
                new CusKafkaProducer.TransactionStateSerializer(),
                new CusKafkaProducer.ContextStateSerializer());

        this.defaultTopicId = checkNotNull(defaultTopic, "defaultTopic is null");

        if (kafkaSchema != null) {
            this.keyedSchema = null;
            this.kafkaSchema = kafkaSchema;
            this.flinkKafkaPartitioner = null;
            ClosureCleaner.clean(
                    this.kafkaSchema, ExecutionConfig.ClosureCleanerLevel.RECURSIVE, true);

            if (customPartitioner != null) {
                throw new IllegalArgumentException(
                        "Customer partitioner can only be used when"
                                + "using a KeyedSerializationSchema or SerializationSchema.");
            }
        } else if (keyedSchema != null) {
            this.kafkaSchema = null;
            this.keyedSchema = keyedSchema;
            this.flinkKafkaPartitioner = customPartitioner;
            ClosureCleaner.clean(
                    this.flinkKafkaPartitioner,
                    ExecutionConfig.ClosureCleanerLevel.RECURSIVE,
                    true);
            ClosureCleaner.clean(
                    this.keyedSchema, ExecutionConfig.ClosureCleanerLevel.RECURSIVE, true);
        } else {
            throw new IllegalArgumentException(
                    "You must provide either a KafkaSerializationSchema or a"
                            + "KeyedSerializationSchema.");
        }

        this.producerConfig = checkNotNull(producerConfig, "producerConfig is null");
        this.semantic = checkNotNull(semantic, "semantic is null");
        this.kafkaProducersPoolSize = kafkaProducersPoolSize;
        checkState(kafkaProducersPoolSize > 0, "kafkaProducersPoolSize must be non empty");

        // set the producer configuration properties for kafka record key value serializers.
        if (!producerConfig.containsKey(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG)) {
            this.producerConfig.put(
                    ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                    ByteArraySerializer.class.getName());
        } else {
           /* LOG.warn(
                    "Overwriting the '{}' is not recommended",
                    ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG);*/
        }

        if (!producerConfig.containsKey(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG)) {
            this.producerConfig.put(
                    ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                    ByteArraySerializer.class.getName());
        } else {
            /*LOG.warn(
                    "Overwriting the '{}' is not recommended",
                    ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG);*/
        }

        // eagerly ensure that bootstrap servers are set.
        if (!this.producerConfig.containsKey(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG)) {
            throw new IllegalArgumentException(
                    ProducerConfig.BOOTSTRAP_SERVERS_CONFIG
                            + " must be supplied in the producer config properties.");
        }

        if (!producerConfig.containsKey(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG)) {
            long timeout = DEFAULT_KAFKA_TRANSACTION_TIMEOUT.toMilliseconds();
            checkState(
                    timeout < Integer.MAX_VALUE && timeout > 0,
                    "timeout does not fit into 32 bit integer");
            this.producerConfig.put(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, (int) timeout);
            /*LOG.warn(
                    "Property [{}] not specified. Setting it to {}",
                    ProducerConfig.TRANSACTION_TIMEOUT_CONFIG,
                    DEFAULT_KAFKA_TRANSACTION_TIMEOUT);*/
        }

        // Enable transactionTimeoutWarnings to avoid silent data loss
        // See KAFKA-6119 (affects versions 0.11.0.0 and 0.11.0.1):
        // The KafkaProducer may not throw an exception if the transaction failed to commit
        if (semantic == CusKafkaProducer.Semantic.EXACTLY_ONCE) {
            final Object object =
                    this.producerConfig.get(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG);
            final long transactionTimeout;
            if (object instanceof String && StringUtils.isNumeric((String) object)) {
                transactionTimeout = Long.parseLong((String) object);
            } else if (object instanceof Number) {
                transactionTimeout = ((Number) object).longValue();
            } else {
                throw new IllegalArgumentException(
                        ProducerConfig.TRANSACTION_TIMEOUT_CONFIG
                                + " must be numeric, was "
                                + object);
            }
            super.setTransactionTimeout(transactionTimeout);
            super.enableTransactionTimeoutWarnings(0.8);
        }

        this.topicPartitionsMap = new HashMap<>();
    }




    public CusKafkaProducer(
            String topicId,
            SerializationSchema<IN> serializationSchema,
            Properties producerConfig,
            @Nullable FlinkKafkaPartitioner<IN> customPartitioner,
            CusKafkaProducer.Semantic semantic,
            int kafkaProducersPoolSize) {
        this(
                topicId,
                null,
                null,
                new CusKafkaSerializationSchemaWrapper<>(
                        topicId, customPartitioner, false, serializationSchema),
                producerConfig,
                semantic,
                kafkaProducersPoolSize);
    }


    @Override
    public void initializeState(FunctionInitializationContext context) throws Exception {
        if (semantic != CusKafkaProducer.Semantic.NONE
                && !((StreamingRuntimeContext) this.getRuntimeContext()).isCheckpointingEnabled()) {
          /*  LOG.warn(
                    "Using {} semantic, but checkpointing is not enabled. Switching to {} semantic.",
                    semantic,
                    FlinkKafkaProducer.Semantic.NONE);*/
            semantic = CusKafkaProducer.Semantic.NONE;
        }

        nextTransactionalIdHintState =
                context.getOperatorStateStore()
                        .getUnionListState(NEXT_TRANSACTIONAL_ID_HINT_DESCRIPTOR_V2);

        if (context.getOperatorStateStore()
                .getRegisteredStateNames()
                .contains(NEXT_TRANSACTIONAL_ID_HINT_DESCRIPTOR)) {
            migrateNextTransactionalIdHindState(context);
        }

        String taskName = getRuntimeContext().getTaskName();
        // Kafka transactional IDs are limited in length to be less than the max value of a short,
        // so we truncate here if necessary to a more reasonable length string.
        if (taskName.length() > maxTaskNameSize) {
            taskName = taskName.substring(0, maxTaskNameSize);
           /* LOG.warn(
                    "Truncated task name for Kafka TransactionalId from {} to {}.",
                    getRuntimeContext().getTaskName(),
                    taskName);*/
        }
        transactionalIdsGenerator =
                new TransactionalIdsGenerator(
                        taskName
                                + "-"
                                + ((StreamingRuntimeContext) getRuntimeContext())
                                .getOperatorUniqueID(),
                        getRuntimeContext().getIndexOfThisSubtask(),
                        getRuntimeContext().getNumberOfParallelSubtasks(),
                        kafkaProducersPoolSize,
                        SAFE_SCALE_DOWN_FACTOR);

        if (semantic != CusKafkaProducer.Semantic.EXACTLY_ONCE) {
            nextTransactionalIdHint = null;
        } else {
            ArrayList<CusKafkaProducer.NextTransactionalIdHint> transactionalIdHints =
                    Lists.newArrayList(nextTransactionalIdHintState.get());
            if (transactionalIdHints.size() > 1) {
                throw new IllegalStateException(
                        "There should be at most one next transactional id hint written by the first subtask");
            } else if (transactionalIdHints.size() == 0) {
                nextTransactionalIdHint = new CusKafkaProducer.NextTransactionalIdHint(0, 0);

                // this means that this is either:
                // (1) the first execution of this application
                // (2) previous execution has failed before first checkpoint completed
                //
                // in case of (2) we have to abort all previous transactions
                abortTransactions(transactionalIdsGenerator.generateIdsToAbort());
            } else {
                nextTransactionalIdHint = transactionalIdHints.get(0);
            }
        }

        super.initializeState(context);
    }

    private void abortTransactions(final Set<String> transactionalIds) {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        transactionalIds
                .parallelStream()
                .forEach(
                        transactionalId -> {
                            // The parallelStream executes the consumer in a separated thread pool.
                            // Because the consumer(e.g. Kafka) uses the context classloader to
                            // construct some class
                            // we should set the correct classloader for it.
                            try (TemporaryClassLoaderContext ignored =
                                         TemporaryClassLoaderContext.of(classLoader)) {
                                // don't mess with the original configuration or any other
                                // properties of the
                                // original object
                                // -> create an internal kafka producer on our own and do not rely
                                // on
                                //    initTransactionalProducer().
                                final Properties myConfig = new Properties();
                                myConfig.putAll(producerConfig);
                                initTransactionalProducerConfig(myConfig, transactionalId);
                                FlinkKafkaInternalProducer<byte[], byte[]> kafkaProducer = null;
                                try {
                                    kafkaProducer = new FlinkKafkaInternalProducer<>(myConfig);
                                    // it suffices to call initTransactions - this will abort any
                                    // lingering transactions
                                    kafkaProducer.initTransactions();
                                } finally {
                                    if (kafkaProducer != null) {
                                        kafkaProducer.close(Duration.ofSeconds(0));
                                    }
                                }
                            }
                        });
    }

    private static void initTransactionalProducerConfig(
            Properties producerConfig, String transactionalId) {
        producerConfig.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, transactionalId);
    }

    private void migrateNextTransactionalIdHindState(FunctionInitializationContext context)
            throws Exception {
        ListState<CusKafkaProducer.NextTransactionalIdHint> oldNextTransactionalIdHintState =
                context.getOperatorStateStore()
                        .getUnionListState(NEXT_TRANSACTIONAL_ID_HINT_DESCRIPTOR);
        nextTransactionalIdHintState =
                context.getOperatorStateStore()
                        .getUnionListState(NEXT_TRANSACTIONAL_ID_HINT_DESCRIPTOR_V2);

        ArrayList<CusKafkaProducer.NextTransactionalIdHint> oldTransactionalIdHints =
                Lists.newArrayList(oldNextTransactionalIdHintState.get());
        if (!oldTransactionalIdHints.isEmpty()) {
            nextTransactionalIdHintState.addAll(oldTransactionalIdHints);
            // clear old state
            oldNextTransactionalIdHintState.clear();
        }
    }


    @Override
    protected void invoke(CusKafkaProducer.KafkaTransactionState transaction, IN value, Context context) throws Exception {

    }

    @Override
    protected CusKafkaProducer.KafkaTransactionState beginTransaction() throws Exception {
        return null;
    }

    @Override
    protected void preCommit(CusKafkaProducer.KafkaTransactionState transaction) throws Exception {

    }

    @Override
    protected void commit(CusKafkaProducer.KafkaTransactionState transaction) {

    }

    @Override
    protected void abort(CusKafkaProducer.KafkaTransactionState transaction) {

    }

    public enum Semantic {

        EXACTLY_ONCE,

        AT_LEAST_ONCE,

        NONE

    }


    public static class KafkaTransactionState {

        private final transient FlinkKafkaInternalProducer<byte[], byte[]> producer;

        @Nullable final String transactionalId;

        final long producerId;

        final short epoch;

        @VisibleForTesting
        public KafkaTransactionState(
                String transactionalId, FlinkKafkaInternalProducer<byte[], byte[]> producer) {
            this(transactionalId, producer.getProducerId(), producer.getEpoch(), producer);
        }

        @VisibleForTesting
        public KafkaTransactionState(FlinkKafkaInternalProducer<byte[], byte[]> producer) {
            this(null, -1, (short) -1, producer);
        }

        @VisibleForTesting
        public KafkaTransactionState(
                @Nullable String transactionalId,
                long producerId,
                short epoch,
                FlinkKafkaInternalProducer<byte[], byte[]> producer) {
            this.transactionalId = transactionalId;
            this.producerId = producerId;
            this.epoch = epoch;
            this.producer = producer;
        }

        boolean isTransactional() {
            return transactionalId != null;
        }

        public FlinkKafkaInternalProducer<byte[], byte[]> getProducer() {
            return producer;
        }

        @Override
        public String toString() {
            return String.format(
                    "%s [transactionalId=%s, producerId=%s, epoch=%s]",
                    this.getClass().getSimpleName(), transactionalId, producerId, epoch);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            CusKafkaProducer.KafkaTransactionState that =
                    (CusKafkaProducer.KafkaTransactionState) o;

            if (producerId != that.producerId) {
                return false;
            }
            if (epoch != that.epoch) {
                return false;
            }
            return Objects.equals(transactionalId, that.transactionalId);
        }

        @Override
        public int hashCode() {
            int result = transactionalId != null ? transactionalId.hashCode() : 0;
            result = 31 * result + (int) (producerId ^ (producerId >>> 32));
            result = 31 * result + (int) epoch;
            return result;
        }
    }


    public static class KafkaTransactionContext {
        final Set<String> transactionalIds;

        @VisibleForTesting
        public KafkaTransactionContext(Set<String> transactionalIds) {
            checkNotNull(transactionalIds);
            this.transactionalIds = transactionalIds;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            CusKafkaProducer.KafkaTransactionContext that =
                    (CusKafkaProducer.KafkaTransactionContext) o;

            return transactionalIds.equals(that.transactionalIds);
        }

        @Override
        public int hashCode() {
            return transactionalIds.hashCode();
        }
    }



    public static class TransactionStateSerializer
            extends TypeSerializerSingleton<CusKafkaProducer.KafkaTransactionState> {

        private static final long serialVersionUID = 1L;

        @Override
        public boolean isImmutableType() {
            return true;
        }

        @Override
        public CusKafkaProducer.KafkaTransactionState createInstance() {
            return null;
        }

        @Override
        public CusKafkaProducer.KafkaTransactionState copy(
                CusKafkaProducer.KafkaTransactionState from) {
            return from;
        }

        @Override
        public CusKafkaProducer.KafkaTransactionState copy(
                CusKafkaProducer.KafkaTransactionState from,
                CusKafkaProducer.KafkaTransactionState reuse) {
            return from;
        }

        @Override
        public int getLength() {
            return -1;
        }

        @Override
        public void serialize(
                CusKafkaProducer.KafkaTransactionState record, DataOutputView target)
                throws IOException {
            if (record.transactionalId == null) {
                target.writeBoolean(false);
            } else {
                target.writeBoolean(true);
                target.writeUTF(record.transactionalId);
            }
            target.writeLong(record.producerId);
            target.writeShort(record.epoch);
        }

        @Override
        public CusKafkaProducer.KafkaTransactionState deserialize(DataInputView source)
                throws IOException {
            String transactionalId = null;
            if (source.readBoolean()) {
                transactionalId = source.readUTF();
            }
            long producerId = source.readLong();
            short epoch = source.readShort();
            return new CusKafkaProducer.KafkaTransactionState(
                    transactionalId, producerId, epoch, null);
        }

        @Override
        public CusKafkaProducer.KafkaTransactionState deserialize(
                CusKafkaProducer.KafkaTransactionState reuse, DataInputView source)
                throws IOException {
            return deserialize(source);
        }

        @Override
        public void copy(DataInputView source, DataOutputView target) throws IOException {
            boolean hasTransactionalId = source.readBoolean();
            target.writeBoolean(hasTransactionalId);
            if (hasTransactionalId) {
                target.writeUTF(source.readUTF());
            }
            target.writeLong(source.readLong());
            target.writeShort(source.readShort());
        }

        // -----------------------------------------------------------------------------------

        @Override
        public TypeSerializerSnapshot<CusKafkaProducer.KafkaTransactionState>
        snapshotConfiguration() {
            return new CusKafkaProducer.TransactionStateSerializer.TransactionStateSerializerSnapshot();
        }

        /** Serializer configuration snapshot for compatibility and format evolution. */
        @SuppressWarnings("WeakerAccess")
        public static final class TransactionStateSerializerSnapshot
                extends SimpleTypeSerializerSnapshot<CusKafkaProducer.KafkaTransactionState> {

            public TransactionStateSerializerSnapshot() {
                super(CusKafkaProducer.TransactionStateSerializer::new);
            }
        }
    }


    public static class ContextStateSerializer
            extends TypeSerializerSingleton<CusKafkaProducer.KafkaTransactionContext> {

        private static final long serialVersionUID = 1L;

        @Override
        public boolean isImmutableType() {
            return true;
        }

        @Override
        public CusKafkaProducer.KafkaTransactionContext createInstance() {
            return null;
        }

        @Override
        public CusKafkaProducer.KafkaTransactionContext copy(
                CusKafkaProducer.KafkaTransactionContext from) {
            return from;
        }

        @Override
        public CusKafkaProducer.KafkaTransactionContext copy(
                CusKafkaProducer.KafkaTransactionContext from,
                CusKafkaProducer.KafkaTransactionContext reuse) {
            return from;
        }

        @Override
        public int getLength() {
            return -1;
        }

        @Override
        public void serialize(
                CusKafkaProducer.KafkaTransactionContext record, DataOutputView target)
                throws IOException {
            int numIds = record.transactionalIds.size();
            target.writeInt(numIds);
            for (String id : record.transactionalIds) {
                target.writeUTF(id);
            }
        }

        @Override
        public CusKafkaProducer.KafkaTransactionContext deserialize(DataInputView source)
                throws IOException {
            int numIds = source.readInt();
            Set<String> ids = new HashSet<>(numIds);
            for (int i = 0; i < numIds; i++) {
                ids.add(source.readUTF());
            }
            return new CusKafkaProducer.KafkaTransactionContext(ids);
        }

        @Override
        public CusKafkaProducer.KafkaTransactionContext deserialize(
                CusKafkaProducer.KafkaTransactionContext reuse, DataInputView source)
                throws IOException {
            return deserialize(source);
        }

        @Override
        public void copy(DataInputView source, DataOutputView target) throws IOException {
            int numIds = source.readInt();
            target.writeInt(numIds);
            for (int i = 0; i < numIds; i++) {
                target.writeUTF(source.readUTF());
            }
        }

        // -----------------------------------------------------------------------------------

        @Override
        public TypeSerializerSnapshot<CusKafkaProducer.KafkaTransactionContext> snapshotConfiguration() {
            return new CusKafkaProducer.ContextStateSerializer.ContextStateSerializerSnapshot();
        }

        /** Serializer configuration snapshot for compatibility and format evolution. */
        @SuppressWarnings("WeakerAccess")
        public static final class ContextStateSerializerSnapshot
                extends SimpleTypeSerializerSnapshot<CusKafkaProducer.KafkaTransactionContext> {

            public ContextStateSerializerSnapshot() {
                super(CusKafkaProducer.ContextStateSerializer::new);
            }
        }
    }


    public static class NextTransactionalIdHint {
        public int lastParallelism = 0;
        public long nextFreeTransactionalId = 0;

        public NextTransactionalIdHint() {
            this(0, 0);
        }

        public NextTransactionalIdHint(int parallelism, long nextFreeTransactionalId) {
            this.lastParallelism = parallelism;
            this.nextFreeTransactionalId = nextFreeTransactionalId;
        }

        @Override
        public String toString() {
            return "NextTransactionalIdHint["
                    + "lastParallelism="
                    + lastParallelism
                    + ", nextFreeTransactionalId="
                    + nextFreeTransactionalId
                    + ']';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            CusKafkaProducer.NextTransactionalIdHint that = (CusKafkaProducer.NextTransactionalIdHint) o;

            if (lastParallelism != that.lastParallelism) {
                return false;
            }
            return nextFreeTransactionalId == that.nextFreeTransactionalId;
        }

        @Override
        public int hashCode() {
            int result = lastParallelism;
            result =
                    31 * result
                            + (int) (nextFreeTransactionalId ^ (nextFreeTransactionalId >>> 32));
            return result;
        }
    }


    public static class NextTransactionalIdHintSerializer
            extends TypeSerializerSingleton<CusKafkaProducer.NextTransactionalIdHint> {

        private static final long serialVersionUID = 1L;

        @Override
        public boolean isImmutableType() {
            return true;
        }

        @Override
        public CusKafkaProducer.NextTransactionalIdHint createInstance() {
            return new CusKafkaProducer.NextTransactionalIdHint();
        }

        @Override
        public CusKafkaProducer.NextTransactionalIdHint copy(CusKafkaProducer.NextTransactionalIdHint from) {
            return from;
        }

        @Override
        public CusKafkaProducer.NextTransactionalIdHint copy(
                CusKafkaProducer.NextTransactionalIdHint from, CusKafkaProducer.NextTransactionalIdHint reuse) {
            return from;
        }

        @Override
        public int getLength() {
            return Long.BYTES + Integer.BYTES;
        }

        @Override
        public void serialize(CusKafkaProducer.NextTransactionalIdHint record, DataOutputView target)
                throws IOException {
            target.writeLong(record.nextFreeTransactionalId);
            target.writeInt(record.lastParallelism);
        }

        @Override
        public CusKafkaProducer.NextTransactionalIdHint deserialize(DataInputView source) throws IOException {
            long nextFreeTransactionalId = source.readLong();
            int lastParallelism = source.readInt();
            return new CusKafkaProducer.NextTransactionalIdHint(lastParallelism, nextFreeTransactionalId);
        }

        @Override
        public CusKafkaProducer.NextTransactionalIdHint deserialize(
                CusKafkaProducer.NextTransactionalIdHint reuse, DataInputView source) throws IOException {
            return deserialize(source);
        }

        @Override
        public void copy(DataInputView source, DataOutputView target) throws IOException {
            target.writeLong(source.readLong());
            target.writeInt(source.readInt());
        }

        @Override
        public TypeSerializerSnapshot<CusKafkaProducer.NextTransactionalIdHint> snapshotConfiguration() {
            return new CusKafkaProducer.NextTransactionalIdHintSerializer.NextTransactionalIdHintSerializerSnapshot();
        }

        /** Serializer configuration snapshot for compatibility and format evolution. */
        @SuppressWarnings("WeakerAccess")
        public static final class NextTransactionalIdHintSerializerSnapshot
                extends SimpleTypeSerializerSnapshot<CusKafkaProducer.NextTransactionalIdHint> {

            public NextTransactionalIdHintSerializerSnapshot() {
                super(CusKafkaProducer.NextTransactionalIdHintSerializer::new);
            }
        }
    }

}
