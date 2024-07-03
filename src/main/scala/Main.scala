import java.text.SimpleDateFormat
import java.util.Locale
import java.sql.Timestamp
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{date_format, from_utc_timestamp, to_timestamp}
import org.apache.spark.sql.types._
import org.apache.spark.sql.SaveMode
import org.apache.spark.sql.DataFrameWriter

object Main {
  def main(args: Array[String]): Unit = {
    // set args
    val ymdDate = new SimpleDateFormat("yyyy-MM-dd").parse(args(0))
    val ymdString = args(0)
    val fileName = new SimpleDateFormat("yyyy-MMM", Locale.ENGLISH).format(ymdDate)
    val filePath = fileName + ".csv"
    val s3aSavePath = "s3a://*/kaggle/"
    
    // init spark
    val spark = SparkSession.builder()
        .appName("idus")
        .master("local[*]")
        .config("spark.hadoop.fs.s3a.access.key", "access_key")
        .config("spark.hadoop.fs.s3a.secret.key", "secret_key")
        .config("spark.hadoop.fs.s3a.endpoint", "s3.amazonaws.com")
        .config("spark.hadoop.fs.s3a.aws.credentials.provider", "com.amazonaws.auth.InstanceProfileCredentialsProvider,com.amazonaws.auth.DefaultAWSCredentialsProviderChain")
        .config("spark.sql.warehouse.dir", "file:///C:/Hadoop/warehouse")
        .config("spark.sql.catalogImplementation", "hive")
        .getOrCreate()
    import spark.implicits._

    // read csv
    val csvSchema = StructType(
      Array(
        StructField("event_time", StringType),
        StructField("event_type", StringType),
        StructField("product_id", IntegerType),
        StructField("category_id", LongType),
        StructField("category_code", StringType),
        StructField("brand", StringType),
        StructField("price", DoubleType),
        StructField("user_id", LongType),
        StructField("user_session", StringType)
      )
    )
    val csvDS = spark
      .read
      .option("header", "true")
      .schema(csvSchema)
      .csv(filePath)
      .as[ActivityLog]

    // convert timestamp and add stamp_date for partition
    val convertTimestampDS = csvDS
      .withColumn("event_time", to_timestamp($"event_time", "yyyy-MM-dd HH:mm:ss 'UTC'"))
      .withColumn("stamp_date", date_format(from_utc_timestamp($"event_time", "Asia/Seoul"), "yyyy-MM-dd"))

    // save in s3 with parquet, snappy, partitioned by stamp_date
    convertTimestampDS
      .write
      .mode(SaveMode.Overwrite)
      .partitionBy("stamp_date")
      .option("compression", "snappy")
      .parquet(s3aSavePath)

    // Create External Table
    spark.sql(s"""
        CREATE EXTERNAL TABLE IF NOT EXISTS idus_activity_log (
            event_time TIMESTAMP,
            event_type STRING,
            product_id INT,
            category_id BIGINT,
            category_code STRING,
            brand STRING,
            price DOUBLE,
            user_id BIGINT,
            user_session STRING
        )
        PARTITIONED BY (stamp_date STRING)
        STORED AS PARQUET
        LOCATION '$s3aSavePath'
    """)
    spark.sql("MSCK REPAIR TABLE idus_activity_log")
    
    // Read External Table For Check
    val idusDS = spark.sql(s"""
        SELECT stamp_date, count(*)
        FROM idus_activity_log
        WHERE stamp_date = '$ymdString'
        GROUP BY stamp_date
    """)
    idusDS.show()
  }
}

case class ActivityLog(event_time: String, event_type: String, product_id: Int, category_id: Long, category_code: String, brand: String, price: Double, user_id: Long, user_session: String)