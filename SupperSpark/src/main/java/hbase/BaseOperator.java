package hbase;

import entity.BaseEntity;

public interface BaseOperator<T extends BaseEntity> {

     void before();
     void execute(T entity);
     void finish();
}
