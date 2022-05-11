package cus.rdd

import org.apache.spark.{Partition, Partitioner, TaskContext}
import org.apache.spark.rdd.RDD

import scala.reflect.ClassTag

class CusGroupbyKeyRDD[K:ClassTag,V:ClassTag]( @transient var prev: RDD[_ <: Product2[K, V]],
                                               part: Partitioner) extends RDD[(K,V)](prev.context, Nil){
  override def compute(split: Partition, context: TaskContext): Iterator[(K, V)] = ???

  override protected def getPartitions: Array[Partition] = ???
}
