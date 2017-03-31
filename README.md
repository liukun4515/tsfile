# TsFile Document
version 0.1.0

[!toc]

## Abstract

TsFile is a columnar storage format designed for time-series data, which supports efficient compression and query. It is available for integrition with data processing frameworks.


## Motivation

Nowadays, the implementation of IoT is becoming increasingly popular in areas such as Industry 4.0, Smart Home, wearables and Connected Healthcare. Comparing with traditional IT infrastructure usage monitoring scenarios, applications like intelligent control and alarm reporting stimulate more advanced analytics requirements on time-series data generated by sensors. Especially when IoT dives into industrial Internet, intelligent equipments produce one to two orders of magnitudes of data more than consumer-oriented IoT, where analytics comes more complicated to get actionable insights. As an illustrative example, a single wind turbine can generate hundreds of data points every 20 ms for fault detection or prediction through a set of sophisticated operations against time-series by data scientists, such as signal decomposition and filtration, segmentation for varied working conditions, pattern matching, frequency domain analysis etc..

Recent advances in time-series data management system are developed for data center monitoring. Currently there is not a file format optimized specifically for time-series data in above scenarios. So TsFile was born. TsFile is a specially designed file format rather than a database. Users can open, write, close and read a  TsFile easily like doing operations on a normal file. Besides, more interfaces are available on a TsFile.

The target of TsFile project is to support: high ingestion rate up to tens of million data points per second and rare updates only for the correction of low quality data; compact data packaging and deep compression for long-live historical data; traditional sequential and conditional query, complex exploratory query, signal processing, data mining and machine learning.

The features of TsFile is as follow:

* **Write**
	* Fast data import
	* Efficiently compression
	* diverse data encoding types
* **Read**
	* Efficiently query 
	* Time-sorted query data set
* **Integration**
	* HDFS
	* Spark and Hive
	* etc.

## Installation

There are two ways to use TsFile in your own project.

* For non-maven users:
	* Compile the source codes and build to jars
	
		```
		git clone https://github.com/thulab/tsfile.git
		cd tsfile/
		sh package.sh
		```
		Then, all the jars can be get in folder named `lib/`. Import `lib/*.jar` to your project.
	
* For maven users: Comiple source codes and deploy to your local repository in three steps:
	* Get the source codes
	
		```
		git clone https://github.com/thulab/tsfile.git
		```
	* Compile the source codes and deploy 
		
		```
		cd tsfile/
		mvn clean install -Dmaven.test.skip=true
		```
	* add dependencies into your project:
	
	  ```
		<dependency>
		   <groupId>com.corp.tsfile</groupId>
		   <artifactId>tsfile-timeseries</artifactId>
		   <version>0.1.0</version>
		</dependency>
	  ```

## Get Started

Now, you’re ready to start doing some awesome things with TsFile. This section demonstrates the detailed usage of TsFile.

### Time-series Data
A time-series is considered as a set of quadruples. A quadruple is defined as (deltaObject, measurement, time, value).

* **deltaObject**: In many stituations, a device which contains many sensors can be considered as a deltaObject.
* **measurement**: A sensor can be considered as a measurement


Table 1 illustates a set of time-series data. The set showed in the following table contains one deltaObject named "device\_1" with three measurements named "sensor\_1", "sensor\_2" and "sensor\_3". 

<center>
<table style="text-align:center">
	<tr><th colspan="6">device_1</th></tr>
	<tr><th colspan="2">sensor_1</th><th colspan="2">sensor_2</th><th colspan="2">sensor_3</th></tr>
	<tr><th>time</th><th>value</td><th>time</th><th>value</td><th>time</th><th>value</td>
	<tr><td>1</td><td>1.2</td><td>1</td><td>20</td><td>2</td><td>50</td></tr>
	<tr><td>3</td><td>1.4</td><td>2</td><td>20</td><td>4</td><td>51</td></tr>
	<tr><td>5</td><td>1.1</td><td>3</td><td>21</td><td>6</td><td>52</td></tr>
	<tr><td>7</td><td>1.8</td><td>4</td><td>20</td><td>8</td><td>53</td></tr>
</table>
<span>A set of time-series data</span>
</center>

**One Line of Data**: In many industrial applications, a device normally contains more than one sensor and these sensors may have values at a same timestamp, which is called one line of data. 

Formally, one line of data consists of a `deltaObject_id`, a timestamp which indicates the milliseconds since January 1, 1970, 00:00:00, and several data pairs composed of `measurement_id` and corresponding `value`. All data pairs in one line belong to this `deltaObject_id` and have the same timestamp. If one of the `measurements` doesn't have a `value` in the `timestamp`, use a space instead(Actually, TsFile does not store null values). Its format is shown as follow:

```java
deltaObject_id, timestamp, <measurement_id, value>,...
```

An example is illustrated as follow. In this example, the data type of three measurements are  `INT32`, `FLOAT` and  `ENUMS` respectively.

```java
device_1, 1490860659000, m1, 10, m2, 12.12, m3, MAN
```


### Writing TsFile

#### Generate a TsFile File.
A TsFile can be generated by following three steps and the complete code will be given in the section "Example for writing TsFile".

* First, use the interface to construct a TsFile instance

	```
	public TsFile(TSRandomAccessFileWriter tsFileOutputStream, JSONObject schemaJson)
            throws IOException, WriteProcessException
	```
	
	**Parameters:**
	* TSRandomAccessFileWriter : An outputStream for writing a TsFile
	
		> The details to construct a TSRandomAccessFileWriter could be refered in section "Example for writing TsFile".
	* JSONObject : The schema of the TsFile. The format of the schama could be refered in following section "Format of Schema JSON".
	
* Second, write data continually.
	
	Two interfaces are available to write data into the TsFile.
	
	```
	public void writeLine(String line) throws IOException, WriteProcessException
	```
	```
	public void writeLine(TSRecord tsRecord) throws IOException, WriteProcessException
	```
	The ```line``` in the first interface refers "One Line of Data" definited aforementioned and the details to construct a `TSRecord` could be refered in section "Example for writing TsFile".
	
	
* Finally, call `close` to finish this writing process. 
	
	```
	public void close() throws IOException
	```

#### Format of Schema JSON
`SchemaJSON` consists of two parts: user settings of the TsFile and a schema specifying a list of allowable time series. The schema describes each measurement's `measurement_id`, `data_type`, `encoding`, etc..

An example is shown as follow:

``` json
{
    "schema": [
        {
            "measurement_id": "m1",
            "data_type": "INT32",
            "encoding": "RLE"
        },
        {
            "measurement_id": "m2",
            "data_type": "FLOAT",
            "encoding": "TS_2DIFF",
            "max_point_number": 2
        },
        {
            "measurement_id": "m3",
            "data_type": "ENUMS",
            "encoding": "BITMAP",
            "compressor": "SNAPPY",
            "enum_values":["MAN","WOMAN"],
            "max_point_number":3
        },
        {
            "measurement_id": "m4",
            "data_type": "BIGDECIMAL",
            "encoding": "RLE",
        }
    ],
    "row_group_size": 8388608,
    "page_size": 1048576,
}
```
`SchemaJSON` consists of a required field `schema` in type of `JSONArray`  and two optional fields:  `row_group_size` and `page_size`. For each entry in `schema` which corresponds to a time series, its field description is shown as follow:

| key      | is required|     description | allowed values|
| :-------- | --------:| :------:| :------:|
| measurement_id    |required	|name of the time series |any combination of letters, numbers and other symbols like `_` `.`  |
| data_type    		|required	|data type|`BOOLEAN`, `INT32`, `INT64`, `FLOAT`, `DOUBLE`, `ENUM` and `BYTE_ARRAY`(namely `String`)|
| encoding    		|required	| encoding approach for time domain. |`PLAIN`(for all data types), {`TS_2DIFF`, `RLE`}(for `INT32`, `INT64`, `FLOAT`, `DOUBLE`, `ENUM`), `BITMAP`(for `INT32` and `ENUM`)|
| enum_values 		|required if `data_type` is `ENUM`	| the fields of `ENUM`  	|  in format of `["MAN","WOMAN"]`|
| max\_point\_number    		|optional	| the number of reserved decimal digits. It's useful if the data type is `FLOAT`, `DOUBLE` or `BigDecimal`| natural number, defaults to 2|
| compressor    		|optional	| the type of compression.| `SNAPPY` and `UNCOMPRESSED`, defaults to `UNCOMPRESSED`|
|max\_string\_length	|optional	| maximal length of string. It's useful if the data type is `BYTE_ARRAY`.  | positive integer, defaults to 128|



#### Example for writing TsFile

```java
import com.corp.delta.tsfile.common.utils.RandomAccessOutputStream;
import com.corp.delta.tsfile.common.utils.TSRandomAccessFileWriter;
import com.corp.delta.tsfile.write.exception.WriteProcessException;
import com.corp.delta.tsfile.write.record.DataPoint;
import com.corp.delta.tsfile.write.record.TSRecord;
import com.corp.delta.tsfile.write.record.datapoint.FloatDataPoint;
import com.corp.delta.tsfile.write.record.datapoint.IntDataPoint;
import com.corp.delta.tsfile.file.TsFile;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class TsFileWriteTest {

    public static void main(String args[]) throws IOException, WriteProcessException {
        String path = "test.ts";
        String s = "{\n" +
                "    \"schema\": [\n" +
                "        {\n" +
                "            \"measurement_id\": \"sensor_1\",\n" +
                "            \"data_type\": \"FLOAT\",\n" +
                "            \"encoding\": \"RLE\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"measurement_id\": \"sensor_2\",\n" +
                "            \"data_type\": \"INT32\",\n" +
                "            \"encoding\": \"TS_2DIFF\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"measurement_id\": \"sensor_3\",\n" +
                "            \"data_type\": \"INT32\",\n" +
                "            \"encoding\": \"TS_2DIFF\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"row_group_size\": 134217728\n" +
                "}";
        JSONObject schemaObject = new JSONObject(s);

        TSRandomAccessFileWriter output = new RandomAccessOutputStream(new File(path));
        TsFile tsFile = new TsFile(output, schemaObject);

        tsFile.writeLine("device_1,1, sensor_1, 1.2, sensor_2, 20, sensor_3,");
        tsFile.writeLine("device_1,2, sensor_1, , sensor_2, 20, sensor_3, 50");
        tsFile.writeLine("device_1,3, sensor_1, 1.4, sensor_2, 21, sensor_3,");
        tsFile.writeLine("device_1,4, sensor_1, 1.2, sensor_2, 20, sensor_3, 51");

        TSRecord tsRecord1 = new TSRecord(6, "device_1");
        tsRecord1.dataPointList = new ArrayList<DataPoint>() {{
            add(new FloatDataPoint("sensor_1", 7.2f));
            add(new IntDataPoint("sensor_2", 10));
            add(new IntDataPoint("sensor_3", 11));
        }};
        TSRecord tsRecord2 = new TSRecord(7, "device_1");
        tsRecord2.dataPointList = new ArrayList<DataPoint>() {{
            add(new FloatDataPoint("sensor_1", 6.2f));
            add(new IntDataPoint("sensor_2", 20));
            add(new IntDataPoint("sensor_3", 21));
        }};
        TSRecord tsRecord3 = new TSRecord(8, "device_1");
        tsRecord3.dataPointList = new ArrayList<DataPoint>() {{
            add(new FloatDataPoint("sensor_1", 9.2f));
            add(new IntDataPoint("sensor_2", 30));
            add(new IntDataPoint("sensor_3", 31));
        }};
        tsFile.writeLine(tsRecord1);
        tsFile.writeLine(tsRecord2);
        tsFile.writeLine(tsRecord3);
        tsFile.close();
    }
}

```

### Interface for Reading TsFile

#### Before the Start

The set of time-series data in section "Time-series Data" is used here for a concrete introduction in this section. The set showed in the following table contains one deltaObject named "device\_1" with three measurements named "sensor\_1", "sensor\_2" and "sensor\_3". And the measurements has been simplified to do a simple illustration, which contains only 4 time-value pairs each.

<center>
<table style="text-align:center">
	<tr><th colspan="6">device_1</th></tr>
	<tr><th colspan="2">sensor_1</th><th colspan="2">sensor_2</th><th colspan="2">sensor_3</th></tr>
	<tr><th>time</th><th>value</td><th>time</th><th>value</td><th>time</th><th>value</td>
	<tr><td>1</td><td>1.2</td><td>1</td><td>20</td><td>2</td><td>50</td></tr>
	<tr><td>3</td><td>1.4</td><td>2</td><td>20</td><td>4</td><td>51</td></tr>
	<tr><td>5</td><td>1.1</td><td>3</td><td>21</td><td>6</td><td>52</td></tr>
	<tr><td>7</td><td>1.8</td><td>4</td><td>20</td><td>8</td><td>53</td></tr>
</table>
<span>A set of time-series data</span>
</center>

#### Definition of Path

A path reprensents a series instance in TsFile. In the example given above, "device\_1.sensor\_1" is a path.

In read interfaces, The parameter ```paths``` indicates the measurements that will be selected.

Path instance can be easily constructed through the class ```Path```. For example:

```
Path p = new Path("device_1.sensor_1");
```

If "sensor\_1" and "sensor\_3" need to be selected in a query, just use following codes.

```
List<Path> paths = n ew ArrayList<Path>();
paths.add(new Path("device_1.sensor_1"));
paths.add(new Path("device_1.sensor_3"));
```

> **Notice:** When constructing a Path, the format of the parameter should be "\<deltaObjectId\>.\<measurementId\>"


#### Definition of Filter

##### Usage Scenario
Filter is used in TsFile reading process. 

##### FilterExpression
A filter expression consists of FilterSeries and FilterOperators.

* **FilterSeries**
	
	There are two kinds of FilterSeries.
	
	 * FilterSeriesType.TIME_FILTER: used to construct a filter for `time` in time-series data.
	 	
	 	```
		FilterSeries timeSeries = FilterFactory.timeFilterSeries();
	 	```
	 	
	 * FilterSeriesType.VALUE_FILTER: used to construct a filter for `value` in time-series data.
	 	
	 	```
	 	FilterSeries valueSeries = FilterFactory.intFilterSeries(device_1, sensor_1, VALUE_FILTER);
	 	```
		The FilterSeries above defines a series 'device\_1.sensor\_1' whose data type is INT32 and FilterSeriesType is VALUE_FILTER.

* **FilterOperator**

	FilterOperator can be used to construct diverse filters.

	**Basic filter operation:**

	 * Lt: Less than
	 * Gt: Greater than
	 * Eq: Equals
	 * NotEq: Not equals
	 * Not: Flip a filter
	 * And(left, right): Conjunction of two filters
	 * Or(left, right): Disjunction of two filters

##### How to build a FilterExpression

* **TimeFilterExpression Usage**

	First, define a FilterSeries with TIME_FILTER type.

	```
	FilterSeries timeSeries = FilterFactory.timeFilterSeries();
	```
	
	Then, construct FilterExpression. Some typical FilterExpression definitions are shown as below with the ```timeSeries``` defined above.

	```
	FilterExpression expression = FilterFactory.eq(timeSeries, 15); // series time = 15

	```
	```
	FilterExpression expression = FilterFactory.LtEq(timeSeries, 15, true); // series time <= 15

	```
	```
	FilterExpression expression = FilterFactory.LtEq(timeSeries, 15, false); // series time < 15

	```
	```
	FilterExpression expression = FilterFactory.GtEq(timeSeries, 15, true); // series time >= 15

	```
	```
	FilterExpression expression = FilterFactory.NotEq(timeSeries, 15); // series time != 15

	```
	```
	FilterExpression expression = FilterFactory.And( FilterFactory.GtEq(timeSeries, 15, true), FilterFactory.LtEq(timeSeries, 25, false)); // 15 <= series time < 25

	```
	```
	FilterExpression expression = FilterFactory.Or( FilterFactory.GtEq(timeSeries, 15, true), FilterFactory.LtEq(timeSeries, 25, false)); // series time >= 15 or series time < 25

	```
* **ValueFilterExpression Usage**

	First, define a FilterSeries with VALUE_FILTER type.

	```
	FilterSeries valueSeries = FilterFactory.intFilterSeries(root.beijing.vehicle, car, VALUE_FILTER);
	```

	Then, construct FilterExpression. Some typical FilterExpression definitions are shown as below with the ```valueSeries``` defined above

	```
	FilterExpression expression = FilterFactory.eq(valueSeries, 15); // series value = 15

	```
	```
	FilterExpression expression = FilterFactory.LtEq(valueSeries, 15, true); // series value <= 15

	```

#### Read Interface

The method ```query()``` can be used to read from a TsFile. In class ```TsFile```, two override metheds named *query* are supported. Concrete description is as follow:

* **Method 1**

	```
	QueryDataSet query(	List<Path> paths,
						FilterExpression timeFilter,
						FilterExpression valueFilter
						) throws IOException
	```

	**Parameters:**

	* paths : selected `Series`
	* timeFilter : filter for timestamps. Input ```null``` if timeFilter is not required.
	* valueFitler : filter for specific series. Input ```null``` if valueFilter is not required.

	> **What does valueFilter mean in a query ?**

	> When executing a query in TsFile, all series involved will be viewed as a "Table". In this special Table, there are (1 + n) columns where "n" is the count of series and "1" indicates the column of timestamp.
	>
	> Fields in timestamp column is the union of timestamps from each series involved, which is in ascending order. Then each field of the series column is the value in corsponding timestamps or null instead.
	>
	> For example, the query parameters is :
	>
	> * paths : ["device\_1.sensor\_1","device\_1.sensor\_2"]
	> * timeFilter : timestamp <= 3
	> * valueFilter : device\_1.sensor\_3 <= 51 or device\_1.sensor\_1 < 1.4
	>
	> The virtual "Table" is:
	>	<table style="text-align:center">
	<tr><th>timestamp</th><th>device_1.sensor_1</th><th>device_1.sensor_2</th><th>device_1.sensor_3</th></tr>
	<tr><td>1</td><td>1.2</td><td>20</td><td>null</td></tr>
	<tr><td>2</td><td>null</td><td>20</td><td>50</td></tr>
	<tr><td>3</td><td>1.4</td><td>21</td><td>null</td></tr>
	<tr><td>4</td><td>null</td><td>20</td><td>51</td></tr>
	<tr><td>5</td><td>1.1</td><td>null</td><td>null</td></tr>
	<tr><td>6</td><td>null</td><td>null</td><td>52</td></tr>
	<tr><td>7</td><td>1.8</td><td>null</td><td>null</td></tr>
	<tr><td>8</td><td>null</td><td>null</td><td>53</td></tr>
	</table>
	>Then the result is:
	>	<table style="text-align:center">
	<tr><th>timestamp</th><th>device_1.sensor\_1</th><th>device_1.sensor\_3</th></tr>
	<tr><td>1</td><td>1.2</td><td>20</td></tr>
	<tr><td>2</td><td>null</td><td>20</tr>
	</table>

* **Method 2**

	```
	QueryDataSet query(	List<Path> paths,
						FilterExpression timeFilter,
						FilterExpression valueFilter,
						Map<String, Long> params
						) throws IOException
	```

	This method is designed for advanced applications such as the TsFile-Spark Connector. The differences from Method 1 is that this method has an additional parameter named "params".

	* **params** : This parameter is a Map instance which stores some additional options for a specific query. In current version, a partial query is supported by adding two options to this parameter.
		*  ```QueryConstant.PARTITION_START_OFFSET```: start offset for a TsFile
		*  ```QueryConstant.PARTITION_END_OFFSET```: end offset for a TsFile

		> **What is Partial Query ?**
		>
		> In some distributed file systems(e.g. HDFS), a file is split into severval parts which are called "Blocks" and stored in different nodes. Executing a query paralleled in each nodes involved makes better efficiency. Thus Partial Query is needed. Paritial Query only selects the results stored in the part split by ```QueryConstant.PARTITION_START_OFFSET``` and ```QueryConstant.PARTITION_END_OFFSET``` for a TsFile.

#### Example

```java
import com.corp.delta.tsfile.file.TsFile;
import com.corp.delta.tsfile.filter.definition.FilterExpression;
import com.corp.delta.tsfile.filter.definition.FilterFactory;
import com.corp.delta.tsfile.filter.definition.filterseries.FilterSeriesType;
import com.corp.delta.tsfile.read.LocalFileInput;
import com.corp.delta.tsfile.read.qp.Path;
import com.corp.delta.tsfile.read.query.QueryDataSet;
import com.corp.delta.tsfile.write.exception.WriteProcessException;

import java.io.IOException;
import java.util.ArrayList;

public class TsFileReadTest {

    public static void main(String args[]) throws IOException, WriteProcessException {
        String path = "test.ts";

        // read example : no filter
        LocalFileInput input = new LocalFileInput(path);
        TsFile readTsFile = new TsFile(input);
        ArrayList<Path> paths = new ArrayList<>();
        paths.add(new Path("device_1.sensor_1"));
        paths.add(new Path("device_1.sensor_2"));
        paths.add(new Path("device_1.sensor_3"));
        QueryDataSet queryDataSet = readTsFile.query(paths, null, null);
        while(queryDataSet.hasNextRecord()){
            System.out.println(queryDataSet.getNextRecord());
        }

        // time filter : 4 <= time < 10
        FilterExpression timeFilter = FilterFactory.and(FilterFactory.gtEq(FilterFactory.timeFilterSeries(), 4L, true)
                , FilterFactory.ltEq(FilterFactory.timeFilterSeries(), 10L, false));
        input = new LocalFileInput(path);
        readTsFile = new TsFile(input);
        paths = new ArrayList<>();
        paths.add(new Path("device_1.sensor_1"));
        paths.add(new Path("device_1.sensor_2"));
        paths.add(new Path("device_1.sensor_3"));
        queryDataSet = readTsFile.query(paths, timeFilter, null);
        while(queryDataSet.hasNextRecord()){
            System.out.println(queryDataSet.getNextRecord());
        }

        // value filter : device_1.sensor_2 > 20
        FilterExpression valueFilter = FilterFactory.ltEq(FilterFactory.intFilterSeries("device_1","sensor_2", FilterSeriesType.VALUE_FILTER), 20, false);
        input = new LocalFileInput(path);
        readTsFile = new TsFile(input);
        paths = new ArrayList<>();
        paths.add(new Path("device_1.sensor_1"));
        paths.add(new Path("device_1.sensor_2"));
        paths.add(new Path("device_1.sensor_3"));
        queryDataSet = readTsFile.query(paths, null, valueFilter);
        while(queryDataSet.hasNextRecord()){
            System.out.println(queryDataSet.getNextRecord());
        }

        // time filter : 4 <= time < 10, value filter : device_1.sensor_2 > 20
        timeFilter = FilterFactory.and(FilterFactory.gtEq(FilterFactory.timeFilterSeries(), 4L, true), FilterFactory.ltEq(FilterFactory.timeFilterSeries(), 10L, false));
        valueFilter = FilterFactory.gtEq(FilterFactory.intFilterSeries("device_1","sensor_3", FilterSeriesType.VALUE_FILTER), 21, true);
        input = new LocalFileInput(path);
        readTsFile = new TsFile(input);
        paths = new ArrayList<>();
        paths.add(new Path("device_1.sensor_1"));
        paths.add(new Path("device_1.sensor_2"));
        paths.add(new Path("device_1.sensor_3"));
        queryDataSet = readTsFile.query(paths, timeFilter, valueFilter);
        while(queryDataSet.hasNextRecord()){
            System.out.println(queryDataSet.getNextRecord());
        }

    }
}
```

### TsFile-Spark Connector

This library lets you expose a TsFile as Spark RDDs and execute arbitrary queries in your SparkSQL.

#### Requirements

The versions required for Spark and Java are as follow:

| Spark Version | Scala Version | Java Version |
| ------------- | ------------- | ------------ |
| `2.0+`        | `2.11`        | `1.8`        |

#### Building From Source

To build the TsFile-Spark connector, run the following commands at the root folder of TsFile:
```
mvn install -Dmaven.test.skip=true

cd integration-parent/tsfile-spark
mvn package -Dmaven.test.skip=true
```
The `tsfile-spark-0.1.0-jar-with-dependencies.jar` can be get in folder `target`.

#### Data Type Reflection from TsFile to SparkSQL

This library uses the following mapping the data type from TsFile to SparkSQL:

| TsFile 		   | SparkSQL|
| --------------| -------------- |
| BOOLEAN     		   | BooleanType    |
| INT32       		   | IntegerType    |
| INT64       		   | LongType       |
| FLOAT       		   | FloatType      |
| DOUBLE      		   | DoubleType     |
| ENUMS                | StringType     |
| BYTE_ARRAY           | BinaryType     |


#### TsFile Schema -> SparkSQL Table Structure

The set of time-series data in section "Time-series Data" is used here to illustrate the mapping from TsFile Schema to SparkSQL Table Stucture.

<center>
<table style="text-align:center">
	<tr><th colspan="6">device_1</th></tr>
	<tr><th colspan="2">sensor_1</th><th colspan="2">sensor_2</th><th colspan="2">sensor_3</th></tr>
	<tr><th>time</th><th>value</td><th>time</th><th>value</td><th>time</th><th>value</td>
	<tr><td>1</td><td>1.2</td><td>1</td><td>20</td><td>2</td><td>50</td></tr>
	<tr><td>3</td><td>1.4</td><td>2</td><td>20</td><td>4</td><td>51</td></tr>
	<tr><td>5</td><td>1.1</td><td>3</td><td>21</td><td>6</td><td>52</td></tr>
	<tr><td>7</td><td>1.8</td><td>4</td><td>20</td><td>8</td><td>53</td></tr>
</table>
<span>A set of time-series data</span>
</center>

There are two reserved columns in Spark SQL Table:

- `time` : Timestamp, LongType
- `delta_object` : Delta_object ID, StringType

The SparkSQL Table Structure is as follow:

<center>
	<table style="text-align:center">
	<tr><th>time(LongType)</th><th>delta_object(StringType)</th><th>sensor_1(FloatType)</th><th>sensor_2(IntType)</th><th>sensor_3(IntType)</th></tr>
	<tr><td>1</td><td>device_1</td><td>1.2</td><td>20</td><td>null</td></tr>
	<tr><td>2</td><td>device_1</td><td>null</td><td>20</td><td>50</td></tr>
	<tr><td>3</td><td>device_1</td><td>1.4</td><td>21</td><td>null</td></tr>
	<tr><td>4</td><td>device_1</td><td>null</td><td>20</td><td>51</td></tr>
	<tr><td>5</td><td>device_1</td><td>1.1</td><td>null</td><td>null</td></tr>
	<tr><td>6</td><td>device_1</td><td>null</td><td>null</td><td>52</td></tr>
	<tr><td>7</td><td>device_1</td><td>1.8</td><td>null</td><td>null</td></tr>
	<tr><td>8</td><td>device_1</td><td>null</td><td>null</td><td>53</td></tr>
	</table>

</center>

#### Examples

##### Scala API

* **Example 1**

	```scala
	// import this library and Spark
	import com.corp.delta.TsFile.spark._
	import org.apache.spark.sql.SparkSession

	val spark = SparkSession.builder().master("local").getOrCreate()

	//read data in TsFile and create a table
	val df = spark.read.TsFile("test.ts")
	df.createOrReplaceTempView("TsFile_table")

	//query with filter
	val newDf = spark.sql("select * from TsFile_table where sensor_1 > 1.2").cache()

	newDf.show()

	```

* **Example 2**

	```scala
	val spark = SparkSession.builder().master("local").getOrCreate()
	val df = spark.read
	      .format("com.corp.delta.TsFile")
	      .load("test.ts")

	df.createOrReplaceTempView("TsFile_table")

	df.filter("sensor_1 > 1.2").show()

	```

* **Example 3**

	```scala
	val spark = SparkSession.builder().master("local").getOrCreate()

	//create a table in SparkSQL and build relation with a TsFile
	spark.sql("create temporary view TsFile using com.corp.delta.TsFile options(path = \"test.ts\")")

	spark.sql("select * from TsFile where sensor_1 > 1.2").show()

	```

##### spark-shell

This library can be used in `spark-shell`.

```
$ bin/spark-shell --jars TsFile-spark-connector.jar

scala> sql("CREATE TEMPORARY TABLE TsFile_table USING com.corp.delta.TsFile OPTIONS (path \"hdfs://localhost:9000/test.TsFile\")")

scala> sql("select * from TsFile_table where sensor_1 > 1.2").show()
```
