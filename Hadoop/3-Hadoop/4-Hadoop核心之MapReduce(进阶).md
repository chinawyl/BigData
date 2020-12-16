# 一、MapReduce综合案例-流量统计

## 需求一:统计求和 

`具体需求`

统计每个手机号的**上行数据包总和**，**下行数据包总和**，**上行总流量之和**，**下行总流量之和** 

`分 析`

以手机号码作为key值，上行流量，下行流量，上行总流量，下行总流量四个字段作为value值，然后以这个key，和value作为map阶段的输出，reduce阶段的输入

### 1.数据准备

`data_flow文件内容`



`data_flow文件字段说明`

![029-data_flow文件字段说明](D:\BigData\Hadoop\3-Hadoop\images\029-data_flow文件字段说明.png)

`在HDFS创建输入文件夹并上传文件`

```shell
hdfs dfs -mkdir /input/flowcount_statistics
hdfs dfs -put data_flow.dat /input/flowcount_statistics
```

### 2.自定义map的输出value对象FlowBean 

```java
package FlowCount_Statistics;

import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class FlowBean implements Writable {
    private Integer upFlow;  //上行数据包数
    private Integer downFlow;  //下行数据包数
    private Integer upCountFlow; //上行流量总和
    private Integer downCountFlow;//下行流量总和

    public Integer getUpFlow() {
        return upFlow;
    }

    public void setUpFlow(Integer upFlow) {
        this.upFlow = upFlow;
    }

    public Integer getDownFlow() {
        return downFlow;
    }

    public void setDownFlow(Integer downFlow) {
        this.downFlow = downFlow;
    }

    public Integer getUpCountFlow() {
        return upCountFlow;
    }

    public void setUpCountFlow(Integer upCountFlow) {
        this.upCountFlow = upCountFlow;
    }

    public Integer getDownCountFlow() {
        return downCountFlow;
    }

    public void setDownCountFlow(Integer downCountFlow) {
        this.downCountFlow = downCountFlow;
    }

    @Override
    public String toString() {
        return  upFlow +
                "\t" + downFlow +
                "\t" + upCountFlow +
                "\t" + downCountFlow;
    }

    //序列化方法
    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(upFlow);
        dataOutput.writeInt(downFlow);
        dataOutput.writeInt(upCountFlow);
        dataOutput.writeInt(downCountFlow);
    }

    //反序列化
    @Override
    public void readFields(DataInput dataInput) throws IOException {
        this.upFlow = dataInput.readInt();
        this.downFlow = dataInput.readInt();
        this.upCountFlow = dataInput.readInt();
        this.downCountFlow = dataInput.readInt();
    }
}
```

### 3.定义FlowMapper

```java
package FlowCount_Statistics;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class FlowCountMapper extends Mapper<LongWritable,Text,Text,FlowBean> {
    /*
      将K1和V1转为K2和V2:
      K1              V1
      0               1363157985059 	13600217502	00-1F-64-E2-E8-B1:CMCC	120.196.100.55	www.baidu.com	综合门户	19	128	1177	16852	200
     ------------------------------
      K2              V2
      13600217502     FlowBean(19	128	1177	16852)
     */
    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        //1:拆分行文本数据,得到手机号--->K2
        String[] split = value.toString().split("\t");
        String phoneNum = split[1];

        //2:创建FlowBean对象,并从行文本数据拆分出流量的四个四段,并将四个流量字段的值赋给FlowBean对象
        FlowBean flowBean = new FlowBean();

        flowBean.setUpFlow(Integer.parseInt(split[6]));
        flowBean.setDownFlow(Integer.parseInt(split[7]));
        flowBean.setUpCountFlow(Integer.parseInt(split[8]));
        flowBean.setDownCountFlow(Integer.parseInt(split[9]));

        //3:将K2和V2写入上下文中
        context.write(new Text(phoneNum), flowBean);

    }
}
```

### 4.定义FlowReducer

```java
package FlowCount_Statistics;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class FlowCountReducer extends Reducer<Text,FlowBean,Text,FlowBean> {
    @Override
    protected void reduce(Text key, Iterable<FlowBean> values, Context context) throws IOException, InterruptedException {
        //1:遍历集合,并将集合中的对应的四个字段累计
         Integer upFlow = 0;  //上行数据包数
         Integer downFlow = 0;  //下行数据包数
         Integer upCountFlow = 0; //上行流量总和
         Integer downCountFlow = 0;//下行流量总和

        for (FlowBean value : values) {
            upFlow += value.getUpFlow();
            downFlow += value.getDownFlow();
            upCountFlow += value.getUpCountFlow();
            downCountFlow += value.getDownCountFlow();
        }

        //2:创建FlowBean对象,并给对象赋值  V3
        FlowBean flowBean = new FlowBean();
        flowBean.setUpFlow(upFlow);
        flowBean.setDownFlow(downFlow);
        flowBean.setUpCountFlow(upCountFlow);
        flowBean.setDownCountFlow(downCountFlow);

        //3:将K3和V3下入上下文中
        context.write(key, flowBean);
    }
}
```

### 5.程序main函数入口JobMain

```java
package FlowCount_Statistics;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.net.URI;

public class JobMain extends Configured implements Tool {
    @Override
    public int run(String[] args) throws Exception {
        //1:创建job任务对象
        Job job = Job.getInstance(super.getConf(), "flowcount_statistics");
        //如果打包运行出错,则需要加该配置
        job.setJarByClass(JobMain.class);

        //2:对job任务进行配置(八个步骤)
        //第一步:设置输入类和输入的路径
        job.setInputFormatClass(TextInputFormat.class);
        TextInputFormat.addInputPath(job, new Path("hdfs://node01:8020/input/flowcount_statistics"));
        //TextInputFormat.addInputPath(job, new Path("file:///D:\\input\\flowcount_statistics"));

        //第二步:设置Mapper类和数据类型（K2和V2）
        job.setMapperClass(FlowCountMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(FlowBean.class);

        //第三、四、五、六步

        //第七步:指定Reducer类和数据类型(K3和V3)
        job.setReducerClass(FlowCountReducer.class);
        job.setOutputValueClass(Text.class);
        job.setOutputValueClass(FlowBean.class);

        //第八步:指定输出类和输出路径
        job.setOutputFormatClass(TextOutputFormat.class);
        TextOutputFormat.setOutputPath(job, new Path("hdfs://node01:8020/output/flowcount_statistics"));
        //TextOutputFormat.setOutputPath(job, new Path("file:///D:\\output\\flowcount_statistics"));

        //获取FileSystem
        FileSystem fileSystem = FileSystem.get(new URI("hdfs://node01:8020"), new Configuration());

        //判断目录是否存在
        Path path = new Path("hdfs://node01:8020/output/flowcount_statistics");
        boolean bl2 = fileSystem.exists(path);
        if(bl2){
            //删除目标目录
            fileSystem.delete(path, true);
        }

        //3:等待任务结束
        boolean bl = job.waitForCompletion(true);

        return bl?0:1;
    }

    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();

        //启动job任务
        int run = ToolRunner.run(configuration, new JobMain(), args);
        System.exit(run);
    }
}
```

### 6.运行

```shell
hadoop jar MapReduce-1.0-SNAPSHOT.jar FlowCount_Statistics.JobMain
```

## 需求二: 上行流量倒序排序(递减排序)

`分析`

以需求一的输出数据作为排序的输入数据，**自定义FlowBean**,以FlowBean为map输出的 key，以手机号作为Map输出的value，因为MapReduce程序会对Map阶段输出的key进行排序

### 1.数据准备

`part-r-00000文件内容`

来源于需求一的**/output/flowcount_statistics/part-r-00000**文件

`在HDFS创建输入文件夹并上传文件`

```shell
hdfs dfs -mkdir /input/flowcount_sort
hdfs dfs -put part-r-00000 /input/flowcount_sort
```

### 2.定义FlowBean实现WritableComparable实现比较排序

```java
package FlowCount_Sort;

import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class FlowBean implements WritableComparable<FlowBean> {
    private Integer upFlow;  //上行数据包数
    private Integer downFlow;  //下行数据包数
    private Integer upCountFlow; //上行流量总和
    private Integer downCountFlow;//下行流量总和

    public Integer getUpFlow() {
        return upFlow;
    }

    public void setUpFlow(Integer upFlow) {
        this.upFlow = upFlow;
    }

    public Integer getDownFlow() {
        return downFlow;
    }

    public void setDownFlow(Integer downFlow) {
        this.downFlow = downFlow;
    }

    public Integer getUpCountFlow() {
        return upCountFlow;
    }

    public void setUpCountFlow(Integer upCountFlow) {
        this.upCountFlow = upCountFlow;
    }

    public Integer getDownCountFlow() {
        return downCountFlow;
    }

    public void setDownCountFlow(Integer downCountFlow) {
        this.downCountFlow = downCountFlow;
    }

    @Override
    public String toString() {
        return  upFlow +
                "\t" + downFlow +
                "\t" + upCountFlow +
                "\t" + downCountFlow;
    }

    //序列化方法
    @Override
    public void write(DataOutput out) throws IOException {
        out.writeInt(upFlow);
        out.writeInt(downFlow);
        out.writeInt(upCountFlow);
        out.writeInt(downCountFlow);
    }

    //反序列化
    @Override
    public void readFields(DataInput in) throws IOException {
        this.upFlow = in.readInt();
        this.downFlow = in.readInt();
        this.upCountFlow = in.readInt();
        this.downCountFlow = in.readInt();
    }

    //指定排序的规则
    @Override
    public int compareTo(FlowBean flowBean) {
       // return this.upFlow.compareTo(flowBean.getUpFlow()) * -1;
       return flowBean.upFlow - this.upFlow ;
    }
}
```

### 3.定义FlowMappe

```java
package FlowCount_Sort;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

/*
   K1: LongWritable 行偏移量
   V1: Text         行文本数据

   K2: FLowBean
   V2: Text       手机号
 */

public class FlowSortMapper extends Mapper<LongWritable,Text,FlowBean,Text> {
    //map方法:将K1和V1转为K2和V2
    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        //1:拆分行文本数据(V1),得到四个流量字段,并封装FlowBean对象---->K2
        String[] split = value.toString().split("\t");

        FlowBean flowBean = new FlowBean();

        flowBean.setUpFlow(Integer.parseInt(split[1]));
        flowBean.setDownFlow(Integer.parseInt(split[2]));
        flowBean.setUpCountFlow(Integer.parseInt(split[3]));
        flowBean.setDownCountFlow(Integer.parseInt(split[4]));

        //2:通过行文本数据,得到手机号--->V2
        String phoneNum = split[0];

        //3:将K2和V2下入上下文中
        context.write(flowBean, new Text(phoneNum));

    }
}
```

### 4.定义FlowReducer 

```java
package FlowCount_Sort;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;


import java.io.IOException;

/*
  K2: FlowBean
  V2: Text  手机号

  K3: Text  手机号
  V3: FlowBean
 */

public class FlowSortReducer extends Reducer<FlowBean,Text,Text,FlowBean> {
    @Override
    protected void reduce(FlowBean key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        //1:遍历集合,取出 K3,并将K3和V3写入上下文中
        for (Text value : values) {
            context.write(value, key);
        }

    }
}
```

### 5.程序main函数入口JobMain

```java
package FlowCount_Sort;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.net.URI;

public class JobMain extends Configured implements Tool {
    @Override
    public int run(String[] args) throws Exception {
        //1:创建job任务对象
        Job job = Job.getInstance(super.getConf(), "flowcount_sort");
        //如果打包运行出错,则需要加该配置
        job.setJarByClass(JobMain.class);

        //2:对job任务进行配置(八个步骤)
        //第一步:设置输入类和输入的路径
        job.setInputFormatClass(TextInputFormat.class);
        TextInputFormat.addInputPath(job, new Path("hdfs://node01:8020/input/flowcount_sort"));
        //TextInputFormat.addInputPath(job, new Path("file:///D:\\input\\flowcount_sort"));

        //第二步:设置Mapper类和数据类型（K2和V2）
        job.setMapperClass(FlowSortMapper.class);
        job.setMapOutputKeyClass(FlowBean.class);
        job.setMapOutputValueClass(Text.class);

        //第三、四、五、六步

        //第七步:指定Reducer类和数据类型(K3和V3)
        job.setReducerClass(FlowSortReducer.class);
        job.setOutputValueClass(Text.class);
        job.setOutputValueClass(FlowBean.class);

        //第八步:指定输出类和输出路径
        job.setOutputFormatClass(TextOutputFormat.class);
        TextOutputFormat.setOutputPath(job, new Path("hdfs://node01:8020/output/flowcount_sort"));
        //TextOutputFormat.setOutputPath(job, new Path("file:///D:\\output\\flowcount_sort"));

        //获取FileSystem
        FileSystem fileSystem = FileSystem.get(new URI("hdfs://node01:8020"), new Configuration());

        //判断目录是否存在
        Path path = new Path("hdfs://node01:8020/output/flowcount_sort");
        boolean bl2 = fileSystem.exists(path);
        if(bl2){
            //删除目标目录
            fileSystem.delete(path, true);
        }

        //3:等待任务结束
        boolean bl = job.waitForCompletion(true);

        return bl?0:1;
    }

    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();

        //启动job任务
        int run = ToolRunner.run(configuration, new JobMain(), args);
        System.exit(run);
    }
}
```

### 6.运行

```shell
hadoop jar MapReduce-1.0-SNAPSHOT.jar FlowCount_Sort.JobMain
```

## 需求三: 手机号码分区 (需求一改进)

`具体需求`

在需求一的基础上，继续完善，将不同的手机号分到不同的数据文件的当中去，需要**自定义分区**来实现，这里我们自定义来模拟分区，将以下数字开头的手机号进行分开

```shell
135 开头数据到一个分区文件 
136 开头数据到一个分区文件 
137 开头数据到一个分区文件 
其他分区
```

### 1.数据准备

`data_flow文件内容`

同需求一

`在HDFS创建输入文件夹并上传文件`

```shell
hdfs dfs -mkdir /input/flowcount_partition
hdfs dfs -put data_flow.dat /input/flowcount_partition
```

### 2.自定义分区

```java
package FlowCount_Partition;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;

public class FlowCountPartition extends Partitioner<Text,FlowBean> {

    /*
      该方法用来指定分区的规则:
        135 开头数据到一个分区文件
        136 开头数据到一个分区文件
        137 开头数据到一个分区文件
        其他分区

       参数:
         text : K2   手机号
         flowBean: V2
         i   : ReduceTask的个数
     */
    @Override
    public int getPartition(Text text, FlowBean flowBean, int i) {
        //1:获取手机号
        String phoneNum = text.toString();

        //2:判断手机号以什么开头,返回对应的分区编号(0-3)
        if(phoneNum.startsWith("135")){
            return  0;
        }else if(phoneNum.startsWith("136")){
            return  1;
        }else if(phoneNum.startsWith("137")){
            return  2;
        }else{
            return 3;
        }

    }
}
```

### 3.程序main函数入口JobMain

```java
package FlowCount_Partition;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.net.URI;

public class JobMain extends Configured implements Tool {
    @Override
    public int run(String[] args) throws Exception {
        //1:创建job任务对象
        Job job = Job.getInstance(super.getConf(), "flowcount_partition");
        //如果打包运行出错,则需要加该配置
        job.setJarByClass(JobMain.class);

        //2:对job任务进行配置(八个步骤)
        //第一步:设置输入类和输入的路径
        job.setInputFormatClass(TextInputFormat.class);
        TextInputFormat.addInputPath(job, new Path("hdfs://node01:8020/input/flowcount_partition"));
        //TextInputFormat.addInputPath(job, new Path("file:///D:\\input\\flowcount_partition"));

        //第二步:设置Mapper类和数据类型（K2和V2）
        job.setMapperClass(FlowCountMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(FlowBean.class);

        //第三、五、六步默认

        //第四步
        job.setPartitionerClass(FlowCountPartition.class);

        //第七步:指定Reducer类和数据类型(K3和V3)
        job.setReducerClass(FlowCountReducer.class);
        job.setOutputValueClass(Text.class);
        job.setOutputValueClass(FlowBean.class);

        //设置reduce个数
        job.setNumReduceTasks(4);

        //第八步:指定输出类和输出路径
        job.setOutputFormatClass(TextOutputFormat.class);
        TextOutputFormat.setOutputPath(job, new Path("hdfs://node01:8020/output/flowcount_partition"));
        //TextOutputFormat.setOutputPath(job, new Path("file:///D:\\output\\flowcount_partition"));

        //获取FileSystem
        FileSystem fileSystem = FileSystem.get(new URI("hdfs://node01:8020"), new Configuration());

        //判断目录是否存在
        Path path = new Path("hdfs://node01:8020/output/flowcount_partition");
        boolean bl2 = fileSystem.exists(path);
        if(bl2){
            //删除目标目录
            fileSystem.delete(path, true);
        }

        //3:等待任务结束
        boolean bl = job.waitForCompletion(true);

        return bl?0:1;
    }

    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();

        //启动job任务
        int run = ToolRunner.run(configuration, new JobMain(), args);
        System.exit(run);
    }
}
```

### 4.运行

```shell
hadoop jar MapReduce-1.0-SNAPSHOT.jar FlowCount_Partition.JobMain
```

<br>

# 二、MapReduce案例-Reduce端实现JOIN 

`具体需求`

查询每个商品对应订单号上的关联数据

`分 析`

通过将关联的条件作为map输出的key,将两表满足join条件的数据并携带数据所来源的文件信息,发往同一个reduce task,在reduce中进行数据的串联 

![030-Reduce端join操作](D:\BigData\Hadoop\3-Hadoop\images\030-Reduce端join操作.png)

![031-Reduce端join操作问题](D:\BigData\Hadoop\3-Hadoop\images\031-Reduce端join操作问题.png)

### 1.数据准备

`orders.txt文件内容`

```shell
#订单数据表
1001,20150710,p0001,2
1002,20150710,p0002,3
```

`product.txt文件内容`

```shell
#商品表
p0001,小米5,1000,2000
p0002,锤子T1,1000,3000
```

`在HDFS创建输入文件夹并上传文件`

```she
hdfs dfs -mkdir /input/join_reduce_input
hdfs dfs -put orders.txt /input/join_reduce_input
hdfs dfs -put product.txt /input/join_reduce_input
```

### 2.定义Mapper

```java
package Join_Reduce;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import java.io.IOException;

/*
  K1:  LongWritable
  V1:  Text

  K2: Text  商品的id
  V2: Text  行文本信息(商品的信息)
 */
public class ReduceJoinMapper extends Mapper<LongWritable,Text,Text,Text> {
    /*
   product.txt     K1                V1

                    0                 p0001,小米5,1000,2000

   orders.txt      K1                V1
                   0                1001,20150710,p0001,2

           -------------------------------------------
                  K2                 V2

                 p0001              p0001,小米5,1000,2000


                 p0001              1001,20150710,p0001,2
     */
    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        //1:判断数据来自哪个文件
        FileSplit fileSplit = (FileSplit) context.getInputSplit();
        String fileName = fileSplit.getPath().getName();
        if(fileName.equals("product.txt")){
            //数据来自商品表
            //2:将K1和V1转为K2和V2,写入上下文中
            String[] split = value.toString().split(",");
            String productId = split[0];

            context.write(new Text(productId), value);
        }else{
            //数据来自订单表
            //2:将K1和V1转为K2和V2,写入上下文中
            String[] split = value.toString().split(",");
            String productId = split[2];

            context.write(new Text(productId), value);
        }
    }
}
```

### 3.定义Reducer

```java
package Join_Reduce;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;


public class ReduceJoinReducer extends Reducer<Text,Text,Text,Text> {
    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
       //1:遍历集合,获取V3(first +second)
        String first = "";
        String second = "";
        for (Text value : values) {
            if(value.toString().startsWith("p")){
                first = value.toString();
            }else{
                second += value.toString();
            }
        }
        //2:将K3和V3写入上下文中
        context.write(key, new Text(first+"\t"+second));
    }
}
```

### 4.定义主类

```java
package Join_Reduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class JobMain extends Configured implements Tool {
    @Override
    public int run(String[] args) throws Exception {
        //1:获取Job对象
        Job job = Job.getInstance(super.getConf(), "join_reduce");
        //如果打包运行出错,则需要加该配置
        job.setJarByClass(JobMain.class);

        //2:设置job任务
            //第一步:设置输入类和输入路径
            job.setInputFormatClass(TextInputFormat.class);
            TextInputFormat.addInputPath(job, new Path("hdfs://node01:8020/input/join_reduce_input"));
            //TextInputFormat.addInputPath(job, new Path("file:///D:\\input\\join_reduce_input"));

            //第二步:设置Mapper类和数据类型
            job.setMapperClass(ReduceJoinMapper.class);
            job.setMapOutputKeyClass(Text.class);
            job.setMapOutputValueClass(Text.class);

            //第三、四、五、六

            //第七步:设置Reducer类和数据类型
            job.setReducerClass(ReduceJoinReducer.class);
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(Text.class);

            //第八步:设置输出类和输出的路径
            job.setOutputFormatClass(TextOutputFormat.class);
            TextOutputFormat.setOutputPath(job, new Path("hdfs://node01:8020/output/join_reduce_output"));
            //TextOutputFormat.setOutputPath(job, new Path("file:///D:\\output\\join_reduce_output"));

        //3:等待job任务结束
        boolean bl = job.waitForCompletion(true);

        return bl ? 0: 1;
    }

    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();

        //启动job任务
        int run = ToolRunner.run(configuration, new JobMain(), args);
        System.exit(run);
    }
}
```

### 5.运行

```shell
hadoop jar MapReduce-1.0-SNAPSHOT.jar Join_Reduce.JobMain
```

<br>

# 三、MapReduce案例-Map端实现JOIN 

`具体需求`

查询每个商品对应订单号上的关联数据

`概述`

**适用于关联表中有小表的情形**
使用分布式缓存,可以将小表分发到所有的map节点,这样,map节点就可以在本地对自己所读到的大表数据进行join并输出最终结果,可以大大提高join操作的并发度,加快处理速度 

`分析`

先在mapper类中预先定义好小表,进行join
引入实际场景中的解决方案:一次加载数据库或者用

![032-Map端join操作](D:\BigData\Hadoop\3-Hadoop\images\032-Map端join操作.png)

### 1.数据准备

`orders.txt文件内容`

```shell
#订单数据表
1001,20150710,p0001,2
1002,20150710,p0002,3
```

`product.txt文件内容`

```shell
#商品表
p0001,小米5,1000,2000
p0002,锤子T1,1000,3000
```

`在HDFS创建输入文件夹并上传文件`

```she
hdfs dfs -mkdir /cache_file
hdfs dfs -mkdir /input/join_map_input
hdfs dfs -put product.txt /cache_file
hdfs dfs -put orders.txt /input/join_map_input
```

### 2.定义Mapper

```java
package Join_Map;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;

public class MapJoinMapper extends Mapper<LongWritable,Text,Text,Text>{
    private HashMap<String, String> map = new HashMap<>();

    //第一件事情:将分布式缓存的小表数据读取到本地Map集合(只需要做一次)
    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        //1:获取分布式缓存文件列表
        URI[] cacheFiles =  context.getCacheFiles();

        //2:获取指定的分布式缓存文件的文件系统(FileSystem)
        FileSystem fileSystem = FileSystem.get(cacheFiles[0], context.getConfiguration());

        //3:获取文件的输入流
        FSDataInputStream inputStream = fileSystem.open(new Path(cacheFiles[0]));

        //4:读取文件内容, 并将数据存入Map集合
           //4.1 将字节输入流转为字符缓冲流FSDataInputStream --->BufferedReader
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
           //4.2 读取小表文件内容,以行位单位,并将读取的数据存入map集合

        String line = null;
        while((line = bufferedReader.readLine()) != null){
            String[] split = line.split(",");
            map.put(split[0], line);
        }

        //5:关闭流
        bufferedReader.close();
        fileSystem.close();
    }

    //第二件事情:对大表的处理业务逻辑,而且要实现大表和小表的join操作
    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            //1:从行文本数据中获取商品的id: p0001 , p0002  得到了K2
            String[] split = value.toString().split(",");
            String productId = split[2];  //K2

            //2:在Map集合中,将商品的id作为键,获取值(商品的行文本数据) ,将value和值拼接,得到V2
            String productLine = map.get(productId);
            String valueLine = productLine+"\t"+value.toString(); //V2

            //3:将K2和V2写入上下文中
            context.write(new Text(productId), new Text(valueLine));
    }
}
```

### 3.定义主类

```java
package Join_Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.net.URI;

public class JobMain extends Configured implements Tool{
    @Override
    public int run(String[] args) throws Exception {
        //1:获取job对象
        Job job = Job.getInstance(super.getConf(), "join_map");
        //如果打包运行出错,则需要加该配置
        job.setJarByClass(JobMain.class);

        //2:设置job对象(将小表放在分布式缓存中)
            //将小表放在分布式缓存中
            // DistributedCache.addCacheFile(new URI("hdfs://node01:8020/input/join_map_input/product.txt"), super.getConf());
            job.addCacheFile(new URI("hdfs://node01:8020/cache_file/product.txt"));

            //第一步:设置输入类和输入的路径
            job.setInputFormatClass(TextInputFormat.class);
            TextInputFormat.addInputPath(job, new Path("hdfs://node01:8020/input/join_map_input"));
            //TextInputFormat.addInputPath(job, new Path("file:///D:\\input\\join_map_input"));

            //第二步:设置Mapper类和数据类型
            job.setMapperClass(MapJoinMapper.class);
            job.setMapOutputKeyClass(Text.class);
            job.setMapOutputValueClass(Text.class);

            //第八步:设置输出类和输出路径
            job.setOutputFormatClass(TextOutputFormat.class);
            TextOutputFormat.setOutputPath(job, new Path("hdfs://node01:8020/output/join_map_output"));
            //TextOutputFormat.setOutputPath(job, new Path("file:///D:\\output\\join_map_output"));

        //3:等待任务结束
        boolean bl = job.waitForCompletion(true);
        return bl ? 0 :1;
    }

    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();

        //启动job任务
        int run = ToolRunner.run(configuration, new JobMain(), args);
        System.exit(run);
    }
}
```

### 4.运行

```shell
hadoop jar MapReduce-1.0-SNAPSHOT.jar Join_Map.JobMain
```

<br>

# 四、MapReduce案例-求共同好友 

`具体需求`

求出哪些人两两之间有共同好友,及他俩的共同好友都有谁

![033-求共同好友](D:\BigData\Hadoop\3-Hadoop\images\033-求共同好友.png)

## 第一个MapReduce

### 1.数据准备

`friends.txt文件内容`

以下是qq的好友列表数据,冒号前是一个用户,冒号后是该用户的所有好友(**数据中的好友关系是单向的**)

```shell
A:B,C,D,F,E,O
B:A,C,E,K
C:A,B,D,E,I 
D:A,E,F,L
E:B,C,D,M,L
F:A,B,C,D,E,O,M
G:A,C,D,E,F
H:A,C,D,E,O
I:A,O
J:B,O
K:A,C,D
L:D,E,F
M:E,F,G
O:A,H,I,J
```

`在HDFS创建输入文件夹并上传文件`

```shell
hdfs dfs -mkdir /input/commonfriends_step1_input
hdfs dfs -put friends.txt /input/commonfriends_step1_input
```

### 2.定义Mapper

```java
package CommonFriends_Step1;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class Step1Mapper extends Mapper<LongWritable,Text,Text,Text> {
    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
         //1:以冒号拆分行文本数据: 冒号左边就是V2
        String[] split = value.toString().split(":");
        String userStr = split[0];

        //2:将冒号右边的字符串以逗号拆分,每个成员就是K2
        String[] split1 = split[1].split(",");
        for (String s : split1) {
            //3:将K2和v2写入上下文中
            context.write(new Text(s), new Text(userStr));
        }
    }
}
```

### 3.定义Reducer

```java
package CommonFriends_Step1;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class Step1Reducer extends Reducer<Text,Text,Text,Text> {
    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        //1:遍历集合,并将每一个元素拼接,得到K3
        StringBuffer buffer = new StringBuffer();

        for (Text value : values) {
            buffer.append(value.toString()).append("-");
        }

        //2:K2就是V3

        //3:将K3和V3写入上下文中
        context.write(new Text(buffer.toString()), key);
    }
}
```

### 4.定义主类

```java
package CommonFriends_Step1;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class JobMain extends Configured implements Tool {
    @Override
    public int run(String[] args) throws Exception {
        //1:获取Job对象
        Job job = Job.getInstance(super.getConf(), "commonfriends_step1");
        //如果打包运行出错,则需要加该配置
        job.setJarByClass(JobMain.class);

        //2:设置job任务
            //第一步:设置输入类和输入路径
            job.setInputFormatClass(TextInputFormat.class);
            TextInputFormat.addInputPath(job, new Path("hdfs://node01:8020/input/commonfriends_step1_input"));
            //TextInputFormat.addInputPath(job, new Path("file:///D:\\input\\commonfriends_step1_input"));

            //第二步:设置Mapper类和数据类型
            job.setMapperClass(Step1Mapper.class);
            job.setMapOutputKeyClass(Text.class);
            job.setMapOutputValueClass(Text.class);

            //第三、四、五、六

            //第七步:设置Reducer类和数据类型
            job.setReducerClass(Step1Reducer.class);
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(Text.class);

            //第八步:设置输出类和输出的路径
            job.setOutputFormatClass(TextOutputFormat.class);
            TextOutputFormat.setOutputPath(job, new Path("hdfs://node01:8020/output/commonfriends_step1_output"));
            //TextOutputFormat.setOutputPath(job, new Path("file:///D:\\output\\commonfriends_step1_output"));

        //3:等待job任务结束
        boolean bl = job.waitForCompletion(true);

        return bl ? 0: 1;
    }

    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();

        //启动job任务
        int run = ToolRunner.run(configuration, new JobMain(), args);
        System.exit(run);
    }
}
```

### 5.运行

```shell
hadoop jar MapReduce-1.0-SNAPSHOT.jar CommonFriends_Step1.JobMain
```

## 第二个MapReduce

### 1.数据准备

`part-r-00000文件内容`

来源于第一个MapReduce的**/outputcommonfriends_step2_output/part-r-00000**文件

`在HDFS创建输入文件夹并上传文件`

```shell
hdfs dfs -mkdir /input/commonfriends_step2_input
hdfs dfs -put part-r-00000 /input/commonfriends_step2_input
```

### 2.定义Mapper

```java
package CommonFriends_Step2;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.Arrays;

public class Step2Mapper extends Mapper<LongWritable,Text,Text,Text> {
    /*
     K1           V1

     0            A-F-C-J-E-	B
    ----------------------------------

     K2             V2
     A-C            B
     A-E            B
     A-F            B
     C-E            B
     */
    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        //1:拆分行文本数据,结果的第二部分可以得到V2
        String[] split = value.toString().split("\t");
        String   friendStr =split[1];

        //2:继续以'-'为分隔符拆分行文本数据第一部分,得到数组
        String[] userArray = split[0].split("-");

        //3:对数组做一个排序
        Arrays.sort(userArray);

        //4:对数组中的元素进行两两组合,得到K2
        /*
          A-E-C-J ----->  A  C  E

          A  C  E
            A  C  E
         */
        for (int i = 0; i <userArray.length -1 ; i++) {
            for (int j = i+1; j  < userArray.length ; j++) {
                //5:将K2和V2写入上下文中
                context.write(new Text(userArray[i] +"-"+userArray[j]), new Text(friendStr));
            }
        }
    }
}
```

### 3.定义Reducer

```java
package CommonFriends_Step2;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class Step2Reducer extends Reducer<Text,Text,Text,Text> {
    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        //1:原来的K2就是K3
        //2:将集合进行遍历,将集合中的元素拼接,得到V3
		int count = 0;
        StringBuffer buffer = new StringBuffer();
        for (Text value : values) {
            buffer.append(value.toString()).append("-");
            count++;
        }

        //将最后一位-去除
        String str = buffer.toString();
        String strs = "";
        if(str.endsWith("-")){
            strs = str.substring(0,str.length() - 1);
        }
        //3:将K3和V3写入上下文中
        context.write(key, new Text(count + "\t" + strs));
    }
}
```

### 4.定义主类

```java
package CommonFriends_Step2;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;


public class JobMain extends Configured implements Tool {
    @Override
    public int run(String[] args) throws Exception {
        //1:获取Job对象
        Job job = Job.getInstance(super.getConf(), "commonfriends_step2_job");
        job.setJarByClass(JobMain.class);

        //2:设置job任务
            //第一步:设置输入类和输入路径
            job.setInputFormatClass(TextInputFormat.class);
            TextInputFormat.addInputPath(job, new Path("hdfs://node01:8020/input/commonfriends_step2_input"));
            //TextInputFormat.addInputPath(job, new Path("file:///D:\\input\\commonfriends_step2_input"));

            //第二步:设置Mapper类和数据类型
            job.setMapperClass(Step2Mapper.class);
            job.setMapOutputKeyClass(Text.class);
            job.setMapOutputValueClass(Text.class);

            //第三、四、五、六

            //第七步:设置Reducer类和数据类型
            job.setReducerClass(Step2Reducer.class);
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(Text.class);

            //第八步:设置输出类和输出的路径
            job.setOutputFormatClass(TextOutputFormat.class);
            TextOutputFormat.setOutputPath(job, new Path("hdfs://node01:8020/output/commonfriends_step2_output"));
            //TextOutputFormat.setOutputPath(job, new Path("file:///D:\\output\\commonfriends_step2_output"));

        //3:等待job任务结束
        boolean bl = job.waitForCompletion(true);

        return bl ? 0: 1;
    }

    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();

        //启动job任务
        int run = ToolRunner.run(configuration, new JobMain(), args);
        System.exit(run);
    }
}
```

### 5.运行

```shell
hadoop jar MapReduce-1.0-SNAPSHOT.jar CommonFriends_Step2.JobMain
```