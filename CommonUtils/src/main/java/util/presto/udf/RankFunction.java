package util.presto.udf;

import io.prestosql.spi.block.BlockBuilder;
import io.prestosql.spi.function.RankingWindowFunction;
import io.prestosql.spi.function.WindowFunctionSignature;
import static io.prestosql.spi.type.BigintType.BIGINT;

import java.util.List;

@WindowFunctionSignature(name = "rank", returnType = "bigint")
public class RankFunction
        extends RankingWindowFunction
{
    private long rank;
    private long count;

    @Override
    public void reset()
    {
        rank = 0;
        count = 1;
    }

    @Override
    public void processRow(BlockBuilder output, boolean newPeerGroup, int peerGroupCount, int currentPosition)
    {
        if (newPeerGroup) {
            rank += count;
            count = 1;
        }
        else {
            count++;
        }
        BIGINT.writeLong(output, rank);
    }
}