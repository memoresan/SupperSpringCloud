package sparksql

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{IntegerType, StructField, StructType}
import util.SparkUtils
import util.log4j2.LoggerUtil

object SparkTest {

  def main(args: Array[String]): Unit = {
    val LOGGER = LoggerUtil.getLogger
    val sparkSession = SparkUtils.getLocalSparkSession()
    val sc = sparkSession.sparkContext
    val r = sc.parallelize(List(1, 2, 4, 4, 8, 9), 3)
    val r1 = sc.parallelize(List(1, 2, 2, 4, 8, 9), 3)
    val r2 = sc.parallelize(List(1, 2, 4, 4, 8, 9), 3)
    val rowRdd = r.map((_, 1)).reduceByKey((x, y) => x + y).map(x => {
      Row(x._1, x._2)
    })
    val structType = new StructType(Array(StructField("a", IntegerType), StructField("b", IntegerType)));
    val df = sparkSession.createDataFrame(rowRdd, structType)
    df.createOrReplaceTempView("test1")
    val rowRdd1 = r1.map((_, 1)).reduceByKey((x, y) => x + y).map(x => {
      Row(x._1, x._2)
    })
    val structType1 = new StructType(Array(StructField("a", IntegerType), StructField("b", IntegerType)));
    val df1 = sparkSession.createDataFrame(rowRdd1, structType1)
    df1.createOrReplaceTempView("test2")

    val rowRdd2 = r2.map((_, 1)).reduceByKey((x, y) => x + y).map(x => {
      Row(x._1, x._2)
    })
    val structType2 = new StructType(Array(StructField("a", IntegerType), StructField("b", IntegerType)));
    val df2 = sparkSession.createDataFrame(rowRdd1, structType1)
    df2.createOrReplaceTempView("test3")
    sparkSession.sql("select /*+ MAPJOIN (t2) */ /*+ MAPJOIN (t3) */  t.a from test1 t join test2 t2 on t.a = t2.a join test3 t3 on t3.a = t.a ").show()
    while (true) {}
    //LOGGER.info(r.mkString(","))


  }
}
