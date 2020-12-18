# 一、HBase简介

### 1.概述

 HBase是一个**分布式**的、**面向列**的开源数据库。 HBase是Apache的Hadoop项目的子项目。HBase不同于一般的关系数据库，它是一个适合于**非结构化数据存储**的数据库。另一个不同的是HBase**基于列的而不是基于行**的模式。 

### 2.特点

##### 2.1 海量存储 

Hbase能存储PB级别的海量数据，在PB级别的数据以及采用廉价PC存储的情况下，能在几十到百毫秒内返回数据 

##### 2.2 列式存储 

列式存储其实说的是列族存储，Hbase是根据列族来存储数据的。列族下面可以有非常多的列，列族在创建表的时候就必须指定 

##### 2.3 极易扩展 

通过横向添加RegionSever的机器，进行水平扩展，提升Hbase上层的处理能力 

##### 2.4 高并发 

目前大部分使用Hbase的架构，都是采用的廉价PC，因此单个IO的延迟其实并不小，一般在几十到上百ms之间。这里说的高并发，主要是在并发的情况下，Hbase的单个IO延迟下降并不多。能获得高并发、低延迟的服务

##### 2.5 稀疏 

稀疏主要是针对Hbase列的灵活性，在列族中，你可以指定任意多的列，在列数据为空的情况下，是不会占用存储空间的

### 3.Hbase应用

##### 3.1 对象存储

不少的头条类、新闻类的的新闻、网页、图片存储在HBase之中，一些病毒公司的病毒库也是存储在HBase之中

##### 3.2 时序数据

HBase之上有OpenTSDB模块，可以满足时序类场景的需求

##### 3.3 推荐画像

用户画像，是一个比较大的稀疏矩阵，蚂蚁金服的风控就是构建在HBase之上

##### 3.4 时空数据

主要是轨迹、气象网格之类，滴滴打车的轨迹数据主要存在HBase之中，另外在技术所有大一点的数据量的车联网企业，数据都是存在HBase之中

##### 3.5 CubeDB OLAP

Kylin一个cube分析工具，底层的数据就是存储在HBase之中，不少客户自己基于离线计算构建cube存储在hbase之中，满足在线报表查询的需求

##### 3.6 消息/订单

在电信领域、银行领域，不少的订单查询底层的存储，另外不少通信、消息同步的应用构建在HBase之上

##### 3.3 Feeds流

典型的应用就是xx朋友圈类似的应用，用户可以随时发布新内容，评论、点赞。

##### 3.8 NewSQL

有Phoenix的插件，可以满足二级索引、SQL的需求，对接传统数据需要SQL非事务的需求

##### 3.9 其他

存储爬虫数据

海量数据备份

短网址

### 4.HBase和Hadoop

##### 4.1 HBase是基于Hadoop集群之上来搭建的

##### 4.2 Hadoop的局限性

* Hadoop主要是实现**批量数据**的处理，并且通过顺序方式访问数据
* Hadoop吞吐量比较高，但是**随机查询、实时操作性能较低**

### 5.HBase和NoSQL

##### 5.1 HBase是NoSQL数据库的一种

- 是一种建立在HDFS之上，提供高可靠性、高性能、列存储、可伸缩、实时读写**NoSQL**的数据库系统

##### 5.2 Hbase和传统RDBMS的差别

- 仅能通过主键(row key)和主键的range来检索数据，**仅支持单行事务**  

* **不支持JOIN**，摒弃了关系型模型，而且在HBase中**只有一种数据类型:byte[]**
* HBase可以用来存储非常大的表，上亿行的数据、有超过百万列，而且它**常用在实时数据处理**中

### 6.HDFS对比HBase

##### 6.1 HDFS

- HDFS是一个非常适合**存储大型文件**的分布式文件系统

- HDFS它不是一个通用的文件系统，也**无法在文件中快速查询**某个数据

##### 6.2 HBase

- HBase构建在HDFS之上，并为大型表提供快速记录查找(和更新)

- HBase内部将大量数据放在HDFS中名为**[StoreFiles]的索引**中，以便进行高速查找

- Hbase比较**适合做快速查询**等需求，而**不适合做大规模的OLAP**应用

### 7.Hive对比HBase

##### 7.1 Hive

- **数据仓库工具**

  本质就是将HDFS中已经存储的文件在Mysql中做了一个双射关系，以方便使用HQL去管理查询

- **用于数据分析、清洗**

  适用于离线的数据分析和清洗，延迟较高

- **基于HDFS、MapReduce**

  存储的数据依旧在DataNode上，编写的HQL语句终将是转换为MapReduce代码执行

##### 7.2 HBase

- **NoSQL数据库**

  是一种面向列存储的非关系型数据库。

- **用于存储结构化和非结构化的数据**

  适用于单表非关系型数据的存储，不适合做关联查询，类似JOIN等操作。

- **基于HDFS**

  数据持久化存储的体现形式是Hfile，存放于DataNode中，被ResionServer以region的形式进行管理

- **延迟较低，接入在线业务使用**

  面对大量的企业数据，HBase可以直线单表大量数据的存储，同时提供了高效的数据访问速度

### 8.总结Hive与HBase

##### 8.1 Hive和Hbase是两种基于Hadoop的不同技术

##### 8.2 Hive是一种类SQL的引擎，并且运行MapReduce任务

##### 8.3 Hbase是一种在Hadoop之上的NoSQL 的Key/value数据库

##### 8.4 这两种工具是可以同时使用的，Hive可以用来进行统计查询，HBase可以用来进行实时查询

##### 8.5 数据可以从Hive写到HBase，或者从HBase写回Hive

<br>

# 二、HBase环境搭建

### 1.上传HBase压缩包并解压(node01)

```shell
tar -zxvf hbase-2.1.0.tar.gz -C /usr/BigData/
```

### 2.进入HBase配置文件目录

```shell
cd /usr/BigData/cd /usr/BigData/conf/
```

### 3.修改HBase配置文件  

##### 3.1 修改hbase-env.sh文件

```shell
#添加jdk环境(默认是注释的)
export JAVA_HOME=/usr/java/jdk1.8.0_45 

#关掉HBase自带的ZooKeeper(默认是注释的)
export HBASE_MANAGES_ZK=false
```

##### 3.2 修改hbase-site.xml

```xml
<configuration>
    <!-- HBase数据在HDFS中的存放的路径 -->
    <property>
	<name>hbase.rootdir</name>
	<value>hdfs://node01:8020/hbase</value>
    </property>
    <!-- Hbase的运行模式。false是单机模式，true是分布式模式。若为false,Hbase和Zookeeper会运行在同一个JVM里面 -->
    <property>
	<name>hbase.cluster.distributed</name>
	<value>true</value>
    </property>
    <!-- ZooKeeper的地址 -->
    <property>
	<name>hbase.zookeeper.quorum</name>
	<value>node01,node02,node03</value>
    </property>
    <!-- ZooKeeper快照的存储位置 -->
    <property>
	<name>hbase.zookeeper.property.dataDir</name>
	<value>/usr/BigData/zookeeper-3.4.9/zkdatas</value>
    </property>
    <!--  V2.1版本，在分布式情况下, 设置为false -->
    <property>
	<name>hbase.unsafe.stream.capability.enforce</name>
	<value>false</value>
    </property>
</configuration>
```

##### 3.3 修改regionservers文件

```shell
node01
node02
node03
```

### 4.复制jar包到lib

```shell
cp $HBASE_HOME/lib/client-facing-thirdparty/htrace-core-3.1.0-incubating.jar $HBASE_HOME/lib/
```

### 5.分发安装包

```shell
scp -r /usr/BigData/hbase-2.1.0 node02:/usr/BigData/
scp -r /usr/BigData/hbase-2.1.0 node03:/usr/BigData/
```

### 6.配置环境变量(三台主机都要)

```shell
#打开环境变量配置文件
vim /etc/profile

#添加环境变量
export HBASE_HOME=/usr/BigData/hbase-2.1.0
export PATH=$HBASE_HOME/bin:$HBASE_HOME/sbin:$PATH

#刷新权限
source /etc/profile
```

### 7.启动HBase

**需要先启动ZooKeeper和HDFS,YARN可以不启动**

##### 7.1 启动ZooKeeper(三台主机)

```shell
#进入目录
cd /usr/BigData/zookeeper-3.4.9/bin/

#启动服务
./zkServer.sh start

#查看进程(只有Jps、QuorumPeerMain)
jps
```

##### 7.2 启动HDFS(node01)

```shell
cd /usr/BigData/hadoop-2.7.5/ 

sbin/start-yarn.sh
```

**注:使用jps查看进程**

- node01

  **Jps**、**QuorumPeerMain** 、**NameNode**、**DataNode**、**SecondaryNameNode**

- node02

  **Jps**、**QuorumPeerMain** 、**DataNode**

- node03

  **Jps**、**QuorumPeerMain** 、**DataNode**

##### 7.3 启动Hbase(node01)

```shell
cd /usr/BigData/hbase-2.1.0/bin/

./start-hbase.sh
```

**注:使用jps查看进程**

- node01

  **Jps**、**QuorumPeerMain** 、**NameNode**、**DataNode**、**SecondaryNameNode**、**HRegionServer** 、**HMaster**

- node02

  **Jps**、**QuorumPeerMain** 、**DataNode**、**HRegionServer**

- node03

  **Jps**、**QuorumPeerMain** 、**DataNode**、**HRegionServer**

### 8.web界面访问HBase

##### 查看WebUI

http://node01:16010