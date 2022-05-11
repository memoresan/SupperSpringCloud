package util.kudu;

import org.apache.kudu.ColumnSchema;
import org.apache.kudu.Schema;
import org.apache.kudu.Type;
import org.apache.kudu.client.*;

import java.util.ArrayList;
import java.util.Arrays;

public class KuduUtils {
    private  static volatile KuduClient client ;
    public static KuduClient getClient(){
        if(client == null){
            synchronized (KuduUtils.class) {
                if (client == null) {
                    client = new KuduClient.KuduClientBuilder("192.168.16.176")
                            .defaultSocketReadTimeoutMs(6000).
                                    build();
                }
            }
        }
        return client;
    }

    public static void main(String[] agrs) throws KuduException {
        getClient();
       // insertTable("personInfo");
       //selectTable();
        //deleteTable("personinfo1");
        selectTable();
       // createTable("personinfo2");
    }


    public static void createTable(String tableName) throws KuduException {
        ArrayList<ColumnSchema> schemaList = new ArrayList<>();
        schemaList.add(new ColumnSchema.ColumnSchemaBuilder("id",Type.INT32).key(true).build());
        schemaList.add(new ColumnSchema.ColumnSchemaBuilder("name",Type.STRING).build());
        schemaList.add(new ColumnSchema.ColumnSchemaBuilder("age", Type.INT32).build());
        schemaList.add(new ColumnSchema.ColumnSchemaBuilder("score",Type.DOUBLE).build());
        Schema schema = new Schema(schemaList);
        CreateTableOptions options = new CreateTableOptions();
        //指定按照id进行hash分区到3个分区，默认id.hashcode % 3 ，决定数据进入哪个tablet
        options.addHashPartitions(Arrays.asList("id"), 3);
        //range分区  1.必须是主键才可以，设置最大和最小的时候必须是主键里面的字段
        options.setRangePartitionColumns(Arrays.asList("id"));
        //分区个数是 hash的分区*range的分区
        //指定id分区范围 0-100 ,100-200 ,200-300,300-400,400-500
        int count = 0;
        for(int i=0;i<5;i++){
            //指定range的下边界
            PartialRow lower = schema.newPartialRow();
            lower.addInt("id",count );

            count += 100;
            //指定range的上边界
            PartialRow upper = schema.newPartialRow();
            upper.addInt("id",count );
            options.addRangePartition(lower,upper );
        }

        client.createTable(tableName,schema , options);
        client.close();
    }


    public static void insertTable(String table) throws KuduException {
        KuduTable personInfo = client.openTable(table);
        Insert insert = personInfo.newInsert();
        PartialRow row = insert.getRow();
        row.addInt("id", 1);
        row.addString("name","zhangsan" );
        row.addInt("age",18 );
        row.addDouble("score",100.0 );
        KuduSession kuduSession = client.newSession();
        //设置kudu刷新插入数据策略
        kuduSession.setFlushMode(SessionConfiguration.FlushMode.AUTO_FLUSH_SYNC);
        //插入数据
        kuduSession.apply(insert);

        /**
         * 5.关闭KuduClient对象。
         */
        kuduSession.close();
        client.close();
    }


    public static void selectTable() throws KuduException {
        KuduTable personInfo = client.openTable("personInfo");
        KuduScanner scanner = client.newScannerBuilder(personInfo)
                //设置查询的列
                .setProjectedColumnNames(Arrays.asList("id", "name", "age"))
                .build();
        //scanner 中是多个tablet
        while(scanner.hasMoreRows()){
            //获取一个tablet 数据
            RowResultIterator rowResults = scanner.nextRows();
            while(rowResults.hasNext()){
                RowResult next = rowResults.next();
                int id = next.getInt("id");
                String name = next.getString("name");
                int age = next.getInt("age");
                System.out.println("id = "+id+",name = "+name+",age = "+age);
            }
        }
        client.close();
    }

    public static void deleteTable(String tableName) throws KuduException {
        if(client.tableExists(tableName)){
            client.deleteTable(tableName);
        }
    }



}
