import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.sql.types._
import util.SparkUtils

object TF2Par {

  def main(args: Array[String]): Unit = {

   // val rfile = args(0)
    //val isLocal: Boolean = args(1).toBoolean

//
//    val schema = StructType(
//      Array(
//        StructField("key", StringType, true),
//        StructField("sex", StringType, true)
////        ,StructField("contact_num", StringType, true)
//      )
//    )


    val sparkSession = SparkUtils.getLocalSparkSession()
     // .setMaster("local")
    val conf = sparkSession.sparkContext.getConf
    conf.set("spark.sql.parquet.compression.codec","uncompressed")

    //    val hql = new HiveContext(sc)


    val schema = StructType(Array(StructField("name",StringType,false),StructField("z",StringType,false),StructField("sex",StringType,false)));


    val df = sparkSession.read.format("com.databricks.spark.csv")
      .option("timestampFormat", "yyyy/MM/dd HH:mm:ss ZZ")
      .option("delimiter", ",")
      .schema(schema)
      .load("file:///E://test.csv")

//
//      df.registerTempTable("table1")
//
//    hql.sql(
//        s"""
//           |create table tmp.flat_user_info_test1 as
//           |select * from table1
//           |
//           |""".stripMargin)








//    val session = SparkSession.builder()
//      .appName("parquet")
//     // .master("local")
//      .getOrCreate()

    

//    val df = session.read.format("com.databricks.spark.csv")
//      .option("timestampFormat", "yyyy/MM/dd HH:mm:ss ZZ")
//      //.schema(schema)
//      .option("header", true)
//      .option("delimiter", "\t")
//      .csv(rfile)


    df.write.parquet("/ztyTest1")


    sparkSession.stop()


  }





}
