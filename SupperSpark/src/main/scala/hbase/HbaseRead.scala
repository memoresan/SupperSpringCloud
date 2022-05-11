package hbase

import entity.HbaseEntity
import util.SparkUtils

class HbaseRead(hbaseEntity: HbaseEntity) {
    def execute(): Unit ={
        val ss = SparkUtils.getLocalSparkSession()
        ss.read.json()
    }
}
