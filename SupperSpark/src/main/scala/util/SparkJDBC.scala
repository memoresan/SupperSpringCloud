package util

import java.util.Properties

import util.jdbc.JDBCUtils
import entity.{BaseEntity, DatabaseEntity}
import org.apache.spark.sql.SparkSession

object SparkJDBC{

  /**
   * read jdbc
   * @param sparkSession
   * @param baseEntity
   * @return
   */
  def readSingleByJDBC(sparkSession: SparkSession,baseEntity: DatabaseEntity)={
    val impalaPro = new Properties();
    impalaPro.setProperty("user",baseEntity.getUserName);
    impalaPro.setProperty("password",baseEntity.getPasswd)
    sparkSession.read.jdbc(baseEntity.getUrl,baseEntity.getTableName,impalaPro);
  }

  /**
   * 保证有一个字段数值型
   * @param sparkSession
   * @param baseEntity
   * @param columnName
   * @param partitionNum
   */
  def readConcurrentByJDBC(sparkSession: SparkSession,baseEntity: DatabaseEntity,columnName:String,partitionNum:Int): Unit ={
    val sql  = s"select min(${columnName}) ,max(${columnName}) from ${baseEntity.getTableName}"
    val resultSet = JDBCUtils.executeQuery(sql);
    var lowerBound:Long = 0;
    var upBound:Long = 0;
    while(resultSet.next()){
      lowerBound = resultSet.getObject(1).asInstanceOf[Long]
      upBound = resultSet.getObject(2).asInstanceOf[Long]
    }
    val impalaPro = new Properties();
    impalaPro.setProperty("user",baseEntity.getUserName);
    impalaPro.setProperty("password",baseEntity.getPasswd)
    sparkSession.read.jdbc(baseEntity.getUrl,baseEntity.getTableName,columnName,lowerBound,upBound,partitionNum,impalaPro);
  }

  def readConcurrentByJDBC(sparkSession: SparkSession,baseEntity: DatabaseEntity,predicates:Array[String]): Unit ={
    /*val predicates =
      Array(
        "2015-09-16" -> "2015-09-30",
        "2015-10-01" -> "2015-10-15",
        "2015-10-16" -> "2015-10-31",
        "2015-11-01" -> "2015-11-14",
        "2015-11-15" -> "2015-11-30",
        "2015-12-01" -> "2015-12-15"
      ).map {
        case (start, end) =>
          s"cast(modified_time as date) >= date '$start' " + s"AND cast(modified_time as date) <= date '$end'"
      }*/
    val impalaPro = new Properties();
    impalaPro.setProperty("user",baseEntity.getUserName);
    impalaPro.setProperty("password",baseEntity.getPasswd)
    sparkSession.read.jdbc(baseEntity.getUrl,baseEntity.getTableName,predicates,impalaPro);
  }




}
