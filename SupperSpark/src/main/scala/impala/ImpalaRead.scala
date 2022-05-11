package impala

import java.util.Properties

import entity.DatabaseEntity
import org.apache.spark.sql.catalog.Database
import util.yml.YmlUtils
import util.{SparkJDBC, SparkUtils}

class ImpalaRead {
  def readData(): Unit ={
    val sparkSession = SparkUtils.getLocalSparkSession();
    val properties:Properties = YmlUtils.getProperties
    val url = properties.getProperty("database.impala.url");
    val userName = properties.getProperty("database.impala.userName");
    val passwd = properties.getProperty("database.impala.passwd")
    val databaseEntity =  new DatabaseEntity(url,"",userName,passwd)
    //val df = SparkJDBC.readByJDBC(sparkSession,databaseEntity)
  }
}
