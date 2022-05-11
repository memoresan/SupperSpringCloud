package cus.rdd

import org.apache.spark.rdd.RDD

import scala.reflect.ClassTag

class ImplicitUtil {
  implicit def e[K:ClassTag,V:ClassTag](rdd: RDD[(K,V)]): CusPairRDDFunctions[K,V] = {
    new CusPairRDDFunctions[K, V](rdd)
  }
}
