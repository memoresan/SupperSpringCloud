package spark

import java.io.{ObjectInputStream, ObjectOutputStream}

import cus.rdd.{BlanceEnum, CusPairRDDFunctions}
import org.apache.hadoop.conf.Configuration
import org.apache.spark.Partitioner
import org.apache.spark.rdd.{PairRDDFunctions, RDD}
import org.apache.spark.scheduler.SparkListener
import org.apache.spark.util.Utils
import util.SparkUtils
import util.jdk.KyroUtils
import util.jdk.KyroUtils.{readObject, writeObject}
import util.log4j2.LoggerUtil

import scala.reflect.ClassTag

object SparkTest {

  def main(args: Array[String]): Unit = {
    val logger = LoggerUtil.getLogger
    val sparkSession = SparkUtils.getLocalSparkSession()
   // sparkSession.sparkContext.getConf.set("spark.kryo.registrator","spark.MyKryoRegistrator")
    logger.info("-----------注意")
    val sc = sparkSession.sparkContext
    val r = sc.parallelize(List(1, 4, 4, 4, 4, 4)).repartition(10)
   /* println(r.mapPartitions(x=>{
      print(Thread.currentThread().getName+" ------执行1")
      val r = x.map(x=>{print(Thread.currentThread().getName+"?????"+x+1)})
      print(Thread.currentThread().getName+" ------执行2")
      r
    }).count())*/

    println(r.map(x=>{
      print(Thread.currentThread().getName+" ------执行1")
      print(Thread.currentThread().getName+"?????"+x+1)
      print(Thread.currentThread().getName+" ------执行2")
      x+1
    }).count())



   /* val b = new B()
    b.a=3
    var byte = writeObject(b,1024);
    val sc = sparkSession.sparkContext
    //sc.addSparkListener(new TestListener);
    val r = sc.parallelize(List(1, 4, 4, 4, 4, 4)).repartition(10)
    r.map((_,1)).groupByKey()
    r.cache()
    //r.checkpoint();
    println(r.map(x=>{
     val b =  readObject(byte,classOf[B]);
      logger.info("---------------------------b")
      b.a
    }).collect().mkString(","))*/


   // logger.info(r.partitioner.get.toString)
  /*  implicit def e[K:ClassTag,V:ClassTag](rdd: RDD[(K,V)]): CusPairRDDFunctions[K,V] = {
      new CusPairRDDFunctions[K, V](rdd)
    }*/
  /*  val path = "file:///usr/local/spark/spark-1.6.0-bin-hadoop2.6/README.md"  //local file
    val rdd1 = sc.textFile(path,2)*/

   // logger.info(r.map((_,1)).reduceByKey(_+_).collect().mkString(","))
    //logger.info(r.map((_,1)).cusGroupByKey(BlanceEnum.LOADBALANCING,10).collect().mkString(","))
    //logger.info(r.map((_,1)).groupByKey(10).collect().mkString(","))
  }
}

class B {
  var a:Int=1
  var b:Int=0
}

/*
class SerializableObject[T](@transient var value: T) extends Serializable {
  private def writeObject(out: ObjectOutputStream): Unit ={
    val bytes = KyroUtils.writeObject(value, 100)
    out.write(bytes);
  }

  private def readObject(in: ObjectInputStream): Unit = {
    val bytes = new Array[Byte](2014);
    KyroUtils.readObject(bytes,classOf[T])
    in.read(bytes)
  }
}
*/



