package util

import org.apache.commons.lang3.StringUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

import scala.collection.JavaConverters.asScalaIteratorConverter

object SparkUtils {

    def getSparkSession(): SparkSession ={
      val sparkConf = new SparkConf()
      sparkConf.set("spark.serializer","org.apache.spark.serializer.KryoSerializer")
      sparkConf.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem")
      SparkSession.builder().config(sparkConf).getOrCreate();
    }

  def getLocalSparkSession(): SparkSession ={
    val sparkConf = new SparkConf()
    sparkConf.setAppName("a")
    sparkConf.setMaster("local[*]").set("spark.serializer","org.apache.spark.serializer.KryoSerializer")
    System.setProperty("hadoop.home.dir", "E:\\soft\\hadoop\\hadoop-2.7.2")
    val sparkSession = SparkSession.builder().config(sparkConf).getOrCreate();
    loadConfig(sparkSession);
    sparkSession
  }

  def setCheckpoint(sc :SparkSession): SparkSession ={
    sc.sparkContext.setCheckpointDir("/test/checkpoint")
    sc
  }

  def getLocalSparkSessionOnHive(hiveWarehouse:String): SparkSession ={
    val sparkConf = new SparkConf()
    sparkConf.set("spark.serializer","org.apache.spark.serializer.KryoSerializer")
    //注意当为spark://ip:port模式的时候必须要将jar上传到worker上，setjar(new String[]{"jar的地址"})
    sparkConf.setMaster("local[*]")
    var hivePath = hiveWarehouse
    if(StringUtils.isNotBlank(hiveWarehouse)){
      hivePath = "hdfs://192.168.54.90:8020/user/hive/warehouse"
    }
    sparkConf.set("spark.sql.warehouse.dir",hivePath)
    val sparkSession = SparkSession.builder().config(sparkConf).enableHiveSupport().getOrCreate();
    loadConfig(sparkSession);
    sparkSession
  }


  def loadConfig(sparkSession: SparkSession) = {
    val conf = new Configuration()
    // 这里的文件地址可以换成从数据库里查询
    val core = new Path("core-site.xml")
    val hdfs = new Path("hdfs-site.xml")
    val hive = new Path("hive-site.xml")
    conf.addResource(core)
    conf.addResource(hdfs)
    conf.addResource(hive)
    for (c <- conf.iterator().asScala){
      sparkSession.conf.set(c.getKey,c.getValue)
    }
  }

  def main(args: Array[String]): Unit = {
   /* val sql = "select a,b,v,d from table"
    val fristIndex = sql.indexOf("select")+6
    val lastIndex = sql.indexOf("from")
    val table = sql.substring(fristIndex,lastIndex).trim
    println(table.split(",").map(x=>x+" string").mkString(","))*/
  }

}
