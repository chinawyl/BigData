# 一、MapReduce概述

### 1.MapReduce介绍

MapReduce是一种编程模型，用于大规模数据集的并行运算。概念"**Map（映射）**"和"**Reduce（归约）**"，是它们的主要思想。它极大地方便了编程人员在不会分布式并行编程的情况下，将自己的程序运行在**分布式系统**上。 当前的软件实现是指定一个Map（映射）函数，用来把**一组键值对映射成一组新的键值对**，指定并发的Reduce（归约）函数，用来保证所有映射的键值对中的每一个共享相同的键组。 

- Map负责“分”，即把复杂的任务分解为若干个“简单的任务”来并行处理,可以进行拆分的前提是这些小任务可以并行计算，彼此间几乎没有依赖关系。 

- Reduce负责“合”，即对map阶段的结果进行全局汇总。 

- MapReduce运行在yarn集群 

  - ResourceManager

  - NodeManager

![018-MapReduce介绍](./images/018-MapReduce介绍.png)

### 2.MapReduce设计构思 

MapReduce是一个分布式运算程序的编程框架，核心功能是将用户编写的业务逻辑代码和自带默认组件整合成一个完整的分布式运算程序，并发运行在Hadoop集群上。
MapReduce设计并提供了统一的计算框架，为程序员隐藏了绝大多数系统层面的处理细节。 为程序员提供一个抽象和高层的编程接口和框架。程序员仅需要关心其应用层的具体计算问 题，仅需编写少量的处理应用本身计算问题的程序代码。如何具体完成这个并行计算任务所 相关的诸多系统层细节被隐藏起来,交给计算框架去处理：
Map和Reduce为程序员提供了一个清晰的操作接口抽象描述。MapReduce中定义了如下的Map 和Reduce两个抽象的编程接口，由用户去编程实现.Map和Reduce,MapReduce处理的数据类型 是<key,value>键值对。

- Map: (k1; v1) → [(k2; v2)] 
- Reduce: (k2; [v2]) → [(k3; v3)]  

**一个完整的mapreduce程序在分布式运行时有三类实例进程**：

- MRAppMaster 负责整个程序的过程调度及状态协调

- MapTask 负责map阶段的整个数据处理流程 

- ReduceTask 负责reduce阶段的整个数据处理流程

![019-MapReduce设计构思](./images/019-MapReduce设计构思.png)

### 3.MapReduce思想

![020-MapReduce思想](./images/020-MapReduce思想.png)

### 4.MapReduce编程规范

##### 4.1 Map阶段2个步骤 

- 设置InputFormat类, 将数据切分为Key-Value(K1和V1) 对, 输入到第二步
- 自定义Map逻辑, 将第一步的结果转换成另外的Key-Value(K2和V2)对, 输出结果

##### 4.2 Shule阶段4个步骤 

- 对输出的Key-Value对进行分区
- 对不同分区的数据按照相同的Key排序
- (可选) 对分组过的数据初步规约, 降低数据的网络拷贝
- 对数据进行分组, 相同Key的Value放入一个集合中

##### 4.3 Reduce阶段2个步骤 

- 对多个Map任务的结果进行排序以及合并, 编写Reduce函数实现自己的逻辑, 对输入的Key-Value进行处理, 转为新的Key-Value(K3和V3)输出 
- 设置OutputFormat处理并保存Reduce输出的Key-Value数据

![021-MapReduce编程流程](./images/021-MapReduce编程流程.png)

### 5.WordCount案例

##### 5.1 数据准备

`创建文件`

```shell
cd /export/servers 
vim wordcount.txt
```

`往文件放入内容`

```shell
hello,world,hadoop
hive,sqoop,flume,hello
kitty,tom,jerry,world
hadoop
```

`在HDFS创建输入文件夹并上传文件`

```shell
hdfs dfs -mkdir -p /input/wordcount_input
hdfs dfs -put wordcount.txt /input/wordcount_input
```

##### 5.2 Mapper编写

```java
package WordCount;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

/*
    四个泛型解释
        KEYIN:K1的类型
        VALUEIN:V1的类型

        KEYOUT:K2的类型
        VALUEOUT:V2的类型
 */
public class WordCountMapper extends Mapper<LongWritable, Text, Text, LongWritable> {
    //map方法将 K1 和 V1 转为 k2 和 V2
    /*
        参数:
            key:    K1 行偏移量
            value:  V1 每一行文本数据
            context:   表示上下文对象
     */
    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        Text text = new Text(); //转换数据类型,不写在循环里，否则每次都创建对象
        LongWritable longWritable = new LongWritable(); //转换数据类型,不写在循环里，否则每次都创建对象

        //1:将文本数据进行拆分
        String[] split = value.toString().split(",");

        //2:遍历数组,组装 K2 和 V2
        for (String word : split){
            //3:将 K2 和 V2 写入上下文
            text.set(word);
            longWritable.set(1);
            context.write(text, longWritable);
        }
    }
}
```

##### 5.3 Reducer编写

```java
package WordCount;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/*
    四个泛型解释
        KEYIN:K2的类型
        VALUEIN:V2的类型

        KEYOUT:K3的类型
        VALUEOUT:V3的类型
 */
public class WordCountReducer extends Reducer<Text, LongWritable, Text, LongWritable> {
    //reduce方法将 K2 和 V2 转为 k3 和 V3
    /*
        参数:
            key:    K2  分割出的文本
            value:  V2  集合
            context:    表示上下文对象
    */

    @Override
    protected void reduce(Text key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException {
        long count = 0;
        //1:遍历集合,将集合中的数字相加，得到 V3
        for (LongWritable value :values){
            count += value.get();
        }
        //2:将 K3 和 V3 写入上下文
        context.write(key, new LongWritable(count));
    }
}
```

##### 5.4 主类JobMain编写

```java
package WordCount;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.net.URI;

public class JobMain extends Configured implements Tool {
    @Override
    public int run(String[] strings) throws Exception {
        /*
        1:创建一个job任务对象
         */
        Job job = Job.getInstance(super.getConf(), "wordcount");
        //如果打包运行出错,则需要加该配置
        job.setJarByClass(JobMain.class);

        /*
        2:配置job任务对象(八个步骤)
         */
        //第一步:指定文件的读取方式和读取路径
        job.setInputFormatClass(TextInputFormat.class);
        TextInputFormat.addInputPath(job, new Path("hdfs://node01:8020/input/wordcount_input")); //集群运行模式读取路径
        //TextInputFormat.addInputPath(job, new Path("file:///D:\\input\\wordcount_input")); //本地运行模式读取路径



        //第二步:指定Map阶段的处理方式和数据类型
        job.setMapperClass(WordCountMapper.class);
        //设置Map阶段K2的类型
        job.setMapOutputKeyClass(Text.class);
        //设置Map阶段V2的类型
        job.setMapOutputValueClass(LongWritable.class);


        //第三、四、五、六 采用默认的方式

        //第七步:指定Reduce阶段的处理方式和数据类型
        job.setReducerClass(WordCountReducer.class);
        //设置K3的类型
        job.setOutputKeyClass(Text.class);
        //设置V3的类型
        job.setOutputValueClass(LongWritable.class);

        //第八步: 设置输出类型
        job.setOutputFormatClass(TextOutputFormat.class);
        //设置输出的路径
        Path path = new Path("hdfs://node01:8020/output/wordcount_output"); //集群运行模式输出路径
        TextOutputFormat.setOutputPath(job, path);
        //TextOutputFormat.setOutputPath(job, new Path("file:///D:\\mapreduce\\output\\wordcount_output")); //本地运行模式输出路径

        //获取FileSystem
        FileSystem fileSystem = FileSystem.get(new URI("hdfs://node01:8020"), new Configuration());

        //判断目录是否存在
        boolean bl2 = fileSystem.exists(path);
        if(bl2){
            //删除目标目录
            fileSystem.delete(path, true);
        }

        //等待任务结束
        boolean bl = job.waitForCompletion(true);

        return bl ? 0:1;
    }

    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();

        //启动job任务
        int run = ToolRunner.run(configuration, new JobMain(), args);
        System.exit(run);
    }
}
```

##### 5.5 运行

`idea打jar包`

点击idea右侧的Maven，选择要打包的项目，双击对应项目的Lifecycle下的package即可

`上传jar包`

在target目录找到**MapReduce-1.0-SNAPSHOT.jar**包

`运行jar包`

```shell
hadoop jar MapReduce-1.0-SNAPSHOT.jar WordCount.JobMain
```

**注:主类路径(WordCount.JobMain)获取方式为右键点击主类Copy里的Copy Reference**

`读取生成的文件`

```shell
hdfs dfs -cat /output/wordcount_output/part-r-00000
```

<br>

# 二、MapReduce分区

### 1.分区概述

在 MapReduce 中, 通过我们指定分区, 会将同一个分区的数据发送到同一个 Reduce 当中进行 处理

例如: 为了数据的统计, 可以把一批类似的数据发送到同一个 Reduce 当中, 在同一个 Reduce 当 中统计相同类型的数据, 就可以实现类似的数据分区和统计等

##### 注:Reduce 当中默认的分区只有一个

### 2.Shuffle阶段分区概述

![022-Shuffle阶段分区](./images/022-Shuffle阶段分区.png)

编写分区代码(对彩票中大于等于15和小于15的进行分组)

### 3.分区案例

##### 3.1 数据准备

`partition.csv文件内容`



`在HDFS创建输入文件夹并上传文件`

```shell
hdfs dfs -mkdir /input/partition_input
hdfs dfs -put partition.csv /input/partition_input
```

##### 3.2 Mapper编写

```java
package Partition;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

/*
    K1: 行偏移量     LongWritable
    V1: 行文本数据   Text

    K2: 行文本数据   Text
    V2: LongWritable
 */
public class PartitionMapper extends Mapper<LongWritable, Text,Text, NullWritable> {
    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        context.write(value,NullWritable.get()); //NullWritable无法直接写入
    }
}
```

##### 3.3 Partitioner编写

```java
package Partition;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;

public class MyPartitioner extends Partitioner<Text,NullWritable> {
    @Override
    public int getPartition(Text text, NullWritable nullWritable, int i) {
        //1:拆分行文本数据(K2),获取中奖字段的值
        String[] split = text.toString().split("\t");
        String numStr = split[5];

        //2:判断中奖字段的值和15的关系,然后返回对应的分区编号
        if(Integer.parseInt(numStr) > 15){
            return  1;
        }else{
            return  0;
        }
    }
}
```

##### 3.4 Reducer编写

```java
package Partition;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/*
  K2:   Text
  V2:   NullWritable

  K3:   Text
  V3:   NullWritable
 */
public class PartitionerReducer extends Reducer<Text,NullWritable,Text,NullWritable> {
    @Override
    protected void reduce(Text key, Iterable<NullWritable> values, Context context) throws IOException, InterruptedException {
        context.write(key, NullWritable.get());
    }
}
```

##### 3.5主类JobMain编写

```java
package Partition;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
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
        Job job = Job.getInstance(super.getConf(), "partition");
        //如果打包运行出错,则需要加该配置
        job.setJarByClass(JobMain.class);

        //2:对job任务进行配置(八个步骤)
        //第一步:设置输入类和输入的路径
        job.setInputFormatClass(TextInputFormat.class);
        TextInputFormat.addInputPath(job, new Path("hdfs://node01:8020/input/partition_input"));
        //TextInputFormat.addInputPath(job, new Path("file:///D:\\input\\partition_input"));

        //第二步:设置Mapper类和数据类型（K2和V2）
        job.setMapperClass(PartitionMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(NullWritable.class);

        //第三步，指定分区类
        job.setPartitionerClass(MyPartitioner.class);

        //第四、五、六步

        //第七步:指定Reducer类和数据类型(K3和V3)
        job.setReducerClass(PartitionerReducer.class);
        job.setOutputValueClass(Text.class);
        job.setOutputValueClass(NullWritable.class);
        //设置ReduceTask的个数
        job.setNumReduceTasks(2);

        //第八步:指定输出类和输出路径
        job.setOutputFormatClass(TextOutputFormat.class);
        TextOutputFormat.setOutputPath(job, new Path("hdfs://node01:8020/output/partition_output"));
        //TextOutputFormat.setOutputPath(job, new Path("file:///D:\\output\\partition_output"));

        //获取FileSystem
        FileSystem fileSystem = FileSystem.get(new URI("hdfs://node01:8020"), new Configuration());

        //判断目录是否存在
        Path path = new Path("hdfs://node01:8020/output/partition_output");
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

##### 3.6运行

`idea打jar包`

点击idea右侧的Maven，选择要打包的项目，双击对应项目的Lifecycle下的**clean**后再点击**package**

`上传jar包`

在target目录找到**MapReduce-1.0-SNAPSHOT.jar**包

`运行jar包`

```shell
hadoop jar MapReduce-1.0-SNAPSHOT.jar Partition.JobMain
```

**注:主类路径(Partition.JobMain)获取方式为右键点击主类Copy里的Copy Reference**

`读取生成的文件`

```shell
hdfs dfs -cat /output/partition_output/part-r-00000 #小于15

hdfs dfs -cat /output/partition_output/part-r-00001 #大于15
```

<br>

# 三、MapReduce的计数器

### 1.概述

计数器是收集作业统计信息的有效手段之一，用于质量控制或应用级统计。计数器还可辅助 诊断系统故障。如果需要将日志信息传输到 map 或 reduce 任务， 更好的方法通常是看能否 用一个计数器值来记录某一特定事件的发生。对于大型分布式作业而言，使用计数器更为方 便。除了因为获取计数器值比输出日志更方便，还有根据计数器值统计特定事件的发生次数 要比分析一堆日志文件容易得多。

### 2.hadoop内置计数器列表

| MapReduce任务计数器    | org.apache.hadoop.mapreduce.TaskCounter                      |
| ---------------------- | ------------------------------------------------------------ |
| 文件系统计数器         | org.apache.hadoop.mapreduce.FileSystemCounter                |
| FileInputFormat计数器  | org.apache.hadoop.mapreduce.lib.input.FileInputFormatCounter |
| FileOutputFormat计数器 | org.apache.hadoop.mapreduce.lib.output.FileOutputFormatCounter |
| 作业计数器             | org.apache.hadoop.mapreduce.JobCounter                       |

### 3.自定义计数器

##### 3.1 方式一:通过context上下文对象获取计数器，通过context上下文对象，在map端使用计数器进行统计

```java
public class PartitionMapper  extends Mapper<LongWritable,Text,Text,NullWritable>{    
    @Override   
    protected void map(LongWritable key, Text value, Context context) throws Exception{   
        Counter counter = context.getCounter("MR_COUNT", "MyRecordCounter");   		              counter.increment(1L);
        context.write(value,NullWritable.get());   
    }
}
```

##### 3.2 方式二:通过enum枚举类型来定义计数器统计reduce端数据的输入的key有多少个

```java
public class PartitionerReducer extends Reducer<Text,NullWritable,Text,NullWritable> {   	public static enum Counter{       
    MY_REDUCE_INPUT_RECORDS,MY_REDUCE_INPUT_BYTES   
	}                                                                              			@Override                                                                                 protected void reduce(Text key, Iterable<NullWritable> values, Context context) throws IOException, InterruptedException {
       context.getCounter(Counter.MY_REDUCE_INPUT_RECORDS).increment(1L);                        context.write(key, NullWritable.get());        
   }                                                                                      }
```

<br>

# 四、MapReduce序列化和排序

### 1.序列化概述

- 序列化(Serialization)是指把结构化对象转化为字节流

- 反序列化 (Deserialization) 是序列化的逆过程,把字节流转为结构化对象. 当要在进程间传递对象或持久化对象的时候, 就需要序列化对象成字节流, 反之当要将接收到或从磁盘读取 的字节流转换为对象, 就要进行反序列化

- Java 的序列化 (Serializable) 是一个重量级序列化框架, 一个对象被序列化后, 会附带很多额 外的信息(各种校验信息, header, 继承体系等, 不便于在网络中高效传输. 所以, Hadoop 自己开发了一套序列化机制(Writable), 精简高效,不用像 Java 对象类一样传输多层的父子 关系, 需要哪个属性就传输哪个属性值, 大大的减少网络传输的开销 

- Writable是Hadoop的序列化格式, Hadoop定义了这样一个Writable接口,一个类要支持可序列化只需实现这个接口即可

- 另外Writable有一个子接口是WritableComparable,WritableComparable是既可实现序列化, 也可以对key进行比较, 我们这里可以通过自定义Key实现WritableComparable来实现 我们的排序功能

### 2.排序案例概述

##### 2.1数据格式

```shell
a   1 
a   9
b   3
a   7 
b   8
b   10
a   5
```

##### 2.2要求

- 第一列按照字典顺序进行排列
- 第一列相同的时候, 第二列按照升序进行排列

##### 2.3流程

![023-Shuffle阶段的排序和序列化](./images/023-Shuffle阶段的排序和序列化.png)

### 3.排序案例代码编写

##### 3.1 数据准备

`在HDFS创建输入文件夹并上传文件`

```shell
hdfs dfs -mkdir /input/sort_input
hdfs dfs -put sort.txt /input/sort_input
```

##### 3.2 自定义类型和比较器

```java
package SortData;

import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class SortBean implements WritableComparable<SortBean> {

    private String word;
    private int num;

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    @Override
    public String toString() {
        return word + "\t" + num;
    }

    //实现比较器，指定排序的规则
    /*
      规则:快速排序  归并排序
        第一列(word)按照字典顺序进行排列  aac   aad
        第一列相同的时候, 第二列(num)按照升序进行排列
     */
    @Override
    public int compareTo(SortBean sortBean) {
        int result = this.word.compareTo(sortBean.word); //对第一列:word排序
        if(result == 0){ //如果第一列相同，对第二列:num排序
            return this.num - sortBean.num;
        }
        return result;
    }

    //实现序列化
    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeUTF(word);
        dataOutput.writeInt(num);
    }

    //实现反序列化
    @Override
    public void readFields(DataInput dataInput) throws IOException {
        this.word = dataInput.readUTF();
        this.num = dataInput.readInt();
    }
}
```

##### 3.3 Mapper编写

```java
package SortData;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class SortMapper extends Mapper<LongWritable, Text, SortBean, NullWritable> {
    /*
      map方法将K1和V1转为K2和V2:

      K1            V1
      0            a  3
      5            b  7
      ----------------------
      K2                         V2
      SortBean(a  3)         NullWritable
      SortBean(b  7)         NullWritable
     */
    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        //1:将行文本数据(V1)拆分,并将数据封装到SortBean对象,就可以得到K2
        String[] split = value.toString().split("\t");

        SortBean sortBean = new SortBean();
        sortBean.setWord(split[0]);
        sortBean.setNum(Integer.parseInt(split[1])); //string类型转int类型

        //2:将K2和V2写入上下文中
        context.write(sortBean, NullWritable.get());
    }
}
```

##### 3.4 Reducer编写

```java
package SortData;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class SortReducer extends Reducer<SortBean,NullWritable,SortBean,NullWritable> {
    //reduce方法将新的K2和V2转为K3和V3
    @Override
    protected void reduce(SortBean key, Iterable<NullWritable> values, Context context) throws IOException, InterruptedException {
       context.write(key, NullWritable.get());
    }
}
```

##### 3.5 主类JobMain编写

```java
package SortData;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.net.URI;

public class JobMain extends Configured implements Tool {
    @Override
    public int run(String[] args) throws Exception {
        /*
        1:创建job对象
         */
        Job job = Job.getInstance(super.getConf(), "sortdata");
        //如果打包运行出错,则需要加该配置
        job.setJarByClass(JobMain.class);

        /*
        2:配置job任务(八个步骤)
         */
        //第一步:设置输入类和输入的路径
        job.setInputFormatClass(TextInputFormat.class);
        TextInputFormat.addInputPath(job, new Path("hdfs://node01:8020/input/sort_input"));
        //TextInputFormat.addInputPath(job, new Path("file:///D:\\input\\input\\sort_input"));

        //第二步: 设置Mapper类和数据类型
        job.setMapperClass(SortMapper.class);
        job.setMapOutputKeyClass(SortBean.class);
        job.setMapOutputValueClass(NullWritable.class);

        //第三、四(第四步排序不用在主类设置)、五、六

        //第七步：设置Reducer类和类型
        job.setReducerClass(SortReducer.class);
        job.setOutputKeyClass(SortBean.class);
        job.setOutputValueClass(NullWritable.class);


        //第八步: 设置输出类和输出的路径
        job.setOutputFormatClass(TextOutputFormat.class);
        TextOutputFormat.setOutputPath(job, new Path("hdfs://node01:8020/output/sort_output"));
        //TextOutputFormat.setOutputPath(job, new Path("file:///D:\\output\\sort_output"));

        //获取FileSystem
        FileSystem fileSystem = FileSystem.get(new URI("hdfs://node01:8020"), new Configuration());

        //判断目录是否存在
        Path path = new Path("hdfs://node01:8020/output/sort_output");
        boolean bl2 = fileSystem.exists(path);
        if(bl2){
            //删除目标目录
            fileSystem.delete(path, true);
        }

        /*
        3:等待任务结束
         */
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

##### 3.6 运行

`idea打jar包`

点击idea右侧的Maven，选择要打包的项目，双击对应项目的Lifecycle下的**clean**后再点击**package**

`上传jar包`

在target目录找到**MapReduce-1.0-SNAPSHOT.jar**包

`运行jar包`

```shell
hadoop jar MapReduce-1.0-SNAPSHOT.jar SortData.JobMain
```

**注:主类路径(Partition.JobMain)获取方式为右键点击主类Copy里的Copy Reference**

`读取生成的文件`

```shell
hdfs dfs -cat /output/sort_output/part-r-00000
```

<br>

# 五、MapReduce规约

### 1.概念

每一个map都可能会产生大量的本地输出，Combiner的作用就是**对map端的输出先做一次合并**，以减少在map 和reduce节点之间的数据传输量，以提高网络IO性能，是MapReduce的一种优化手段之一

- combiner是MR程序中Mapper和Reducer之外的一种组件

- combiner组件的父类就是Reducer

- combiner和reducer的区别在于运行的位置
  - Combiner是在每一个maptask所在的节点运行
  - Reducer是接收全局所有Mapper的输出结果

- combiner的意义就是对每一个maptask的输出进行局部汇总，以减小网络传输量

![024-规约概念](./images/024-规约概念.png)

### 2.实现步骤(WordCount案例改编)

##### 2.1 Combiner编写

```java
public class MyCombiner extends Reducer<Text, LongWritable, Text, LongWritable> {
    @Override
    protected void reduce(Text key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException {
        long count = 0;
        //1:遍历集合,将集合中的数字相加，得到 V3
        for (LongWritable value :values){
            count += value.get();
        }
        //2:将 K3 和 V3 写入上下文
        context.write(key, new LongWritable(count));
    }
}
```

##### 2.2 主类JobMain编写

```java
//第三、四步采用默认设置

//第五步:规约(Combiner)
job.setCombinerClass(MyCombiner.class);

//第六步采用默认设置
```

<br>

# 六、MapReduce工作机制

### 1.全过程

![025-MapReduce工作机制-全流程](./images/025-MapReduce工作机制全流程.png)

### 2.MapTask工作机制

整个Map阶段流程大体为inputFile通过split被逻辑切分为多个split文件,通过Record按行读取内容给map(用户自己实现的)进行处理,数据被map处理结束之后交给OutputCollector收集器,对其结果key进行分区(默认使用hash分区)然后写入buer,每个map task都有一个内存缓冲区,存储着map的输出结果,当缓冲区快满的时候需要将缓冲区的数据以一个临时文件的方 式存放到磁盘,当整个map task结束后再对磁盘中这个map task产生的所有临时文件做合并,生成最终的正式输出文件,然后等待reduce task来拉数据

![026-MapTask工作机制](./images/026-MapTask工作机制.png)
- 读取数据组件InputFormat(默认TextInputFormat)会通过getSplits方法对输入目录中文件进行逻辑切片规划得到block,**有多少个block就对应启动多少个MapTask**

- 将输入文件切分为block由RecordReader对象(默认是LineRecordReader)进行读取,以\n作为分隔符,读取一行数据,返回<key,value>,**Key表示每行首字符偏移值,Value表示这一行文本内容**

- 读取block返回<key,value>,进入用户自己继承的Mapper类中,执行用户重写的map函数,**RecordReader读取一行,这里调用一次**

- Mapper逻辑结束之后,将Mapper的每条结果通过context.write进行collect数据收集在collect中,会先对其进行**分区处理,默认使用HashPartitioner**

- 接下来,会将数据写入内存,内存中这片区域叫做**环形缓冲区**,缓冲区的作用是批量收集Mapper结果,减少磁盘IO的影响。我们的**Key/Value**对以及**Partition的结果**都会被写入缓冲区。写入之前,Key与Value值都会**被序列化成字节数组**
  - 环形缓冲区其实是一个数组, 数组中存放着**Key,Value的序列化数据**和**Key,Value的元数据信息**, 包括 Partition,Key的起始位置,Value的起始位置以及Value的长度
  - 缓冲区是有大小限制, **默认是100MB** 。当Mapper的输出结果很多时,就可能会撑爆内存,所以需要在一定条件下将缓冲区中的数据临时写入磁盘, 然后重新利用这块缓冲区.。这个从内存往磁盘写数据的过程被称为**Spill**,中文可译为**溢写**。这个溢写是由单独线程来完成,不影响往缓冲区写Mapper结果的线程。溢写线程启动时不应该阻止Mapper的结果输出,所以整个缓冲区有个**溢写的比例spill.percent**。这个比例**默认是0.8**

- 当溢写线程启动后,需要对这80MB空间内的Key做排序(Sort)。排序是MapReduce模型默认的行为,这里的排序也是对序列化的字节做的排序

- **合并溢写文件,** 每次溢写会在磁盘上生成一个**临时文件**(**写之前判断是否有Combine**r),如果Mapper的输出结果真的很大,有多次这样的溢写发生,磁盘上相应的就会有多个临时文件存在。当整个数据处理结束之后开始对磁盘中的临时文件进行**Merge合并**,因为最终的文件只有一个,写入磁盘,并且为这个文件提供了一个索引文件,以记录每个reduce对应数据的偏移量

### 3.ReduceTask工作机制

Reduce大致分为**copy、sort、reduce**三个阶段。copy阶段包含一个eventFetcher来获取已完成的map列表,由Fetcher线程去copy数据,在此过程中会启动两个merge线程,分别为inMemoryMerger和 onDiskMerger,分别将内存中的数据merge到磁盘和将磁盘中的数据进行merge,待数据copy完成之后,copy阶段就完成了。sort阶段开始,sort阶段执行finalMerge操作,完成之后就是reduce阶段,调用用户定义的reduce函数进行处理

![027-ReduceTask工作机制](./images/027-ReduceTask工作机制.png)

- **Copy阶段:**简单地拉取数据。Reduce进程启动一些数据copy线程(Fetcher),通过HTTP方式请求maptask获取属于自己的文件

- **Merge阶段:**这里的merge如map端的merge动作,只是数组中存放的是不同map端copy来的数值。Copy过来的数据会先放入内存缓冲区中,这里的缓冲区大小要比map端的更为灵活。merge有三种形式:内存到内存;内存到磁盘；磁盘到磁盘。默认情况下第一种形式不启用。当内存中的数据量到达一定阈值,就启动内存到磁盘的merge。与map端类似,这也是溢写的过程,这个过程中如果你设置有Combiner,也是会启用的,然后在磁盘中生成了众多的溢写文件。第二种merge方式一直在运行,直到没有map端的数据时才结束,然后启动第三种磁盘到磁盘的merge方式生成最终的文件

- **合并排序:**把分散的数据合并成一个大的数据后,还会再对合并后的数据排序

- **对排序后的键值对调用reduce方法:**键相等的键值对调用一次reduce方法,每次调用会产生零个或者多个键值对,最后把这些输出的键值对写入到HDFS文件中

### 4.Shuffle工作机制

map阶段处理的数据如何传递给reduce 阶段,是MapReduce框架中最关键的一个流程,这个流程就叫:洗牌、发牌 ——(核心机制:数据分区**排序、分组、规约、合并**等过程)

![028-Shule工作机制](./images/028-Shule工作机制.png)

- **Collect阶段:**将 MapTask的结果输出到默认大小为100M的环形缓冲区,保存的是key/value,Partition分区信息等

- **Spill阶段:**当内存中的数据量达到一定的阀值的时候,就会将数据写入本地磁盘,在将数据写入磁盘之前需要对数据进行一次排序的操作,如果配置了combiner,还会将有相同分区号和key的数据进行排序

- **Merge阶段:**把所有溢出的临时文件进行一次合并操作,以确保一个MapTask最终只 产生一个中间数据文件Copy阶段:ReduceTask启动Fetcher线程到已经完成MapTask的节点上复制一份属于 自己的数据,这些数据默认会保存在内存的缓冲区中,当内存的缓冲区达到一定的阀值的时候,就会将数据写到磁盘之上

- **Merge阶段:**在ReduceTask远程复制数据的同时,会在后台开启两个线程对内存到本地的数据文件进行合并操作

- **Sort阶段:**在对数据进行合并的同时进行排序操作,由于MapTask阶段已经对数据进行了局部的排序,ReduceTask 只需保证Copy的数据的最终整体有效性即可。Shule中的缓冲区大小会影响到mapreduce程序的执行效率,缓冲区越大,磁盘io的次数越少,执行速度就越快,缓冲区的大小参数可以调整,参数(mapreduce.task.io.sort.mb)默认为100M

