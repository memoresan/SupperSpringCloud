package hbase;

import entity.BaseEntity;
import entity.HbaseEntity;

public class HbaseReadOperator implements BaseOperator<HbaseEntity>{
    @Override
    public void before() {

    }

    @Override
    public void execute(HbaseEntity hbaseEntity) {
        new HbaseRead(hbaseEntity).execute();
    }

    @Override
    public void finish() {

    }
}
