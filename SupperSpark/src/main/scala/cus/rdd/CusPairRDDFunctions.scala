package cus.rdd
import org.apache.spark.Partitioner
import org.apache.spark.rdd.{PairRDDFunctions, RDD}

import scala.collection.mutable
import scala.reflect.ClassTag

class CusPairRDDFunctions[K:ClassTag,V:ClassTag](rdd:RDD[(K,V)]) extends PairRDDFunctions(rdd){

    def cusGroupByKey(enum:BlanceEnum = null,partitionNumber:Int = rdd.partitions.size):RDD[(K, Iterable[V])]  ={
        if(enum != null){
         groupByFunction(new CusPartitioner(partitionNumber))
        }else{
          groupByKey();
        }
    }
    def groupByFunction(partitioner: Partitioner): RDD[(K,Iterable[V])] ={
      val createCombiner = (v: V) => mutable.Buffer(v)
      val mergeValue = (buf: mutable.Buffer[V], v: V) => buf += v
      val mergeCombiners = (c1: mutable.Buffer[V], c2: mutable.Buffer[V]) => c1 ++= c2
      val bufs = combineByKeyWithClassTag[mutable.Buffer[V]](
        createCombiner, mergeValue, mergeCombiners, partitioner,false)
      bufs.asInstanceOf[RDD[(K, Iterable[V])]]

    }


}

class CusPartitioner(number:Int) extends Partitioner{
  override def numPartitions: Int = {
      if(number != 0){
        number
      }else{
        1
      }
  }

  override def getPartition(key: Any): Int = {
   /* val k = key.asInstanceOf[String]
    k.split("_")(0).asInstanceOf[Int]*/
    1
  }
}

object CusPartitioner{
  private var i:Int = -1;
}
