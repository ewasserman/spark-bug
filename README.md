# SPARK-15070 Data corruption when using Dataset.groupBy[K : Encoder](func: T => K) when data loaded from JSON file.

Reported as: https://issues.apache.org/jira/browse/SPARK-15070

With:

    case class BugRecord(m: String, elapsed_time: java.lang.Double)

Running this code:

    val logs = sqlc.read.json("bug-data.json").as[BugRecord]
    logs.groupBy(r => "FOO").agg(avg($"elapsed_time").as[Double]).show(20, truncate = false)

On this data:

    {"m":"POST","elapsed_time":0.123456789012345678,"source_time":"abcdefghijk"}

Expected Output:
    +-----------+-------------------+
    |_1         |_2                 |
    +-----------+-------------------+
    |FOO        |0.12345678901234568|
    +-----------|-------------------+

Observed Output:
    +-----------+-------------------+
    |_1         |_2                 |
    +-----------+-------------------+
    |POSTabc    |0.12345726584950388|
    +-----------+-------------------+

The grouping key has been corrupted (it is not the product of the groupBy function) and is a combination of bytes from the actual key column and an extra attribute in the JSON not present in the case class. 

The aggregated value is also corrupted.


NOTE:
The problem does not manifest when using an alternate form of groupBy:

    logs.groupBy($"m").agg(avg($"elapsed_time").as[Double])

The corrupted key problem does not manifest when there is not an additional field in the JSON. Ie. if the data file is this:

    {"m":"POST","elapsed_time":0.123456789012345678}