package flinksql.cus.tableSouce;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.configuration.ConfigOptions;
import org.apache.flink.configuration.ReadableConfig;
import org.apache.flink.table.connector.format.DecodingFormat;
import org.apache.flink.table.connector.source.DynamicTableSource;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.factories.DeserializationFormatFactory;
import org.apache.flink.table.factories.DynamicTableFactory;
import org.apache.flink.table.factories.DynamicTableSourceFactory;
import org.apache.flink.table.factories.FactoryUtil;
import org.apache.flink.table.types.DataType;

import java.util.HashSet;
import java.util.Set;

/**
 * CREATE TABLE UserScores (name STRING, score INT)
 * WITH (
 *   'connector' = 'socket',
 *   'hostname' = 'localhost',
 *   'port' = '9999',
 *   'byte-delimiter' = '10',
 *   'format' = 'changelog-csv',
 *   'changelog-csv.column-delimiter' = '|'
 * );
 *
 */
public class SocketSqlFactory implements DynamicTableSourceFactory {
    //获取with里面的hostName
    public static final ConfigOption<String> HOSTNAME = ConfigOptions.key("hostname").stringType().noDefaultValue();
    public static final ConfigOption<Integer> PORT = ConfigOptions.key("port").intType().noDefaultValue();
    public static final ConfigOption<Integer> BYTE_DELIMITER = ConfigOptions.key("byte-delimiter")
            .intType()
            .defaultValue(10);


    @Override
    public String factoryIdentifier() {
        return "socket";
    }

    //必须字段
    @Override
    public Set<ConfigOption<?>> requiredOptions() {
        final Set<ConfigOption<?>> options = new HashSet<>();
        options.add(HOSTNAME);
        options.add(PORT);
        options.add(FactoryUtil.FORMAT);    // use pre-defined option for format
        return options;
    }

    //可选字段
    @Override
    public Set<ConfigOption<?>> optionalOptions() {
        final Set<ConfigOption<?>> options = new HashSet<>();
        options.add(BYTE_DELIMITER);
        return options;
    }
    @Override
    public DynamicTableSource createDynamicTableSource(Context context) {
        //寻找对应的format并且生成可序列化的format
        final FactoryUtil.TableFactoryHelper helper = FactoryUtil.createTableFactoryHelper(this, context);
        //从DeserializationFormatFactory的子类去查找含有format的类
        final DecodingFormat<DeserializationSchema<RowData>> decodingFormat = helper.discoverDecodingFormat(
                DeserializationFormatFactory.class, FactoryUtil.FORMAT);
        helper.validate();
        final ReadableConfig options = helper.getOptions();
        final String hostname = options.get(HOSTNAME);
        final int port = options.get(PORT);
        final byte byteDelimiter = (byte) (int)options.get(BYTE_DELIMITER);
        //将row转成dataType,tableSchema-->dataType 里面包含基础列+元数据列不包含计算列
        //DataType 里面包含了字段的名称类型等信息
        //final DataType producedDataType = context.getCatalogTable().getSchema().toPersistedRowDataType();

        final DataType producedDataType = context.getCatalogTable().getSchema().toPhysicalRowDataType();
        return new SocketDynamicTableSource(hostname, port, byteDelimiter, decodingFormat, producedDataType);
    }

}
