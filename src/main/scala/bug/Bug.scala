package c.a

import org.apache.spark.sql.functions._
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkContext, SparkConf}

case class BugRecord(m: String, elapsed_time: java.lang.Double)

object Bug {
  def main(args: Array[String]): Unit = {
    val c = new SparkConf().setMaster("local[2]").setAppName("BugTest")
    val sc = new SparkContext(c)
    val sqlc = new SQLContext(sc)

    import sqlc.implicits._
    val logs = sqlc.read.json("bug-data.json").as[BugRecord]
    logs.groupBy(r => "FOO").agg(avg($"elapsed_time").as[Double]).show(20, truncate = false)
    
    sc.stop()
  }
}