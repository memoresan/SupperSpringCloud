package cus.sparksql
import java.util

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.connector.catalog.{SupportsRead, SupportsWrite, Table}
import org.apache.spark.sql.connector.expressions.Transform
import org.apache.spark.sql.execution.datasources.FileFormat
import org.apache.spark.sql.types.{DataType, StructType}
import org.apache.spark.sql.util.CaseInsensitiveStringMap


abstract class CusTable ( sparkSession: SparkSession,
                            options: CaseInsensitiveStringMap,
                            paths: Seq[String],
                            userSpecifiedSchema: Option[StructType]) extends Table with SupportsRead with SupportsWrite {
  //import org.apache.spark.sql.connector.catalog.CatalogV2Implicits._

  override def partitioning(): Array[Transform] = super.partitioning()

  override def properties: util.Map[String, String] = options.asCaseSensitiveMap

  /**
   * The string that represents the format that this data source provider uses. This is
   * overridden by children to provide a nice alias for the data source. For example:
   *
   * {{{
   *   override def formatName(): String = "ORC"
   * }}}
   */
  def formatName: String

  /**
   * Returns a V1 [[FileFormat]] class of the same file data source.
   * This is a solution for the following cases:
   * 1. File datasource V2 implementations cause regression. Users can disable the problematic data
   *    source via SQL configuration and fall back to FileFormat.
   * 2. Catalog support is required, which is still under development for data source V2.
   */
  def fallbackFileFormat: Class[_ <: FileFormat]


  /**
   * Returns whether this format supports the given [[DataType]] in read/write path.
   * By default all data types are supported.
   */
  def supportsDataType(dataType: DataType): Boolean = true
}
