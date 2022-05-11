package util


import util.jdk.KyroUtils
import util.jdk.KyroUtils.{readObject, writeObject}
import util.log4j2.LoggerUtil

import scala.reflect.ClassTag

object SparkTest {

  def main(args: Array[String]): Unit = {
    val logger = LoggerUtil.getLogger
    val sparkSession = SparkUtils.getSparkSession()
   // sparkSession.sparkContext.getConf.set("spark.kryo.registrator","spark.MyKryoRegistrator")
    logger.info("-----------注意")
    val b = new B()
    b.a=3
    val kyroUtils = new KyroUtils()
    var byte = kyroUtils.writeObjectBySpark(b,1024);

    val sc = sparkSession.sparkContext
    //sc.addSparkListener(new TestListener);
    val r = sc.parallelize(List(1, 4, 4, 4, 4, 4)).repartition(10)
    r.map((_,1)).groupByKey()
    r.cache()
    //r.checkpoint();
    println(r.map(x=>{
     val c =  kyroUtils.readObjectBySpark(byte,classOf[B]);
      logger.info("---------------------------b")
      c.a
    }).collect().mkString(","))
    while(true){

    }


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



