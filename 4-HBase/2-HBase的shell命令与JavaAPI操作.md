# 一、HBase的Shell命令

## 1.需求

**有以下订单数据，我们想要将这样的一些数据保存到HBase中**

| 订单ID | 订单状态 | 支付金额 | 支付方式ID | 用户ID | 操作时间          | 商品分类 |
| ------ | -------- | -------- | ---------- | ------ | ----------------- | -------- |
| 001    | 已付款   | 200.5    | 1          | 001    | 2020-5-2 18:08:53 | 手机     |

## 2.基本操作

#### 2.1 启动HBase客户端

```shell
hbase shell
```

#### 2.2 查看帮助

```shell
help
```

#### 2.3 查看服务器状态

```shell
status
```

#### 2.4 显示HBase当前用户

```shell
whoami
```

#### 2.5 查看当前数据库有那些表

```shell
list
```

#### 2.6 检查表是否存在

```shell
exits
```

#### 2.7 检查表是否被禁用或启用

```shell
is_enabled #是否启用

is_disabled #是否启用
```

#### 2.8 改变表和列蔟的模式

```shell
#创建一个USER_INFO表，两个列蔟C1、C2
create 'USER_INFO', 'C1', 'C2'

#新增列蔟C3
alter 'USER_INFO', 'C3'

#删除列蔟C3
alter 'USER_INFO', 'delete' => 'C3'
```

#### 2.9 退出HBase客户端

```shell
exit
```

## 3.命名空间操作

#### 3.1 查看数据库有那些命名空间

```shell
list_namespace
```

#### 3.2 创建命名空间

```shell
create_namespace 'bigdata'
```

#### 3.3 在命名空间创建表

```shell
create 'bigdata:ORDER_INFO','C1'
```

#### 3.4 删除命名空间

```shell
#禁用命名空间里的表
disable 'bigdata:ORDER_INFO'

#删除命名空间里的表
drop 'bigdata:ORDER_INFO'

#删除命名空间
drop_namespace 'bigdata'
```

**注:删除命名空间必须先删除里面的表**

## 4.表操作

#### 4.1 创建表

```shell
#create "表名","列蔟名"...

create "ORDER_INFO","C1"
```

**注:没带命名空间，默认为default命名空间**

#### 4.2 查看表结构

```shell
#describe "表名"
describe 'ORDER_INFO'
```

#### 4.3 变更表信息

```shell
alter 'student',{NAME=>'info',VERSIONS=>5} #变更列族版本

get 'student','1001',{COLUMN=>'info:name',VERSIONS=>3} #查看当前列最新3个版本数据信息
```

#### 4.4 删除表

##### 4.4.1 禁用表

```shell
#disable '表名'
disable 'ORDER_INFO'
```

**注:启用表为enable**

##### 4.4.2 删除表

```shell
#drop '表名'
drop 'ORDER_INFO' 
```

**注:删除表之前必须禁用表**

## 5.数据操作

#### 5.1 添加数据

```shell
#put '表名','ROWKEY','列蔟名:列名','值'
put 'ORDER_INFO','000001','C1:ID','000001'
put 'ORDER_INFO','000001','C1:STATUS','已提交'
put 'ORDER_INFO','000001','C1:PAY_MONEY',4070
put 'ORDER_INFO','000001','C1:PAYWAY',1
put 'ORDER_INFO','000001','C1:ID','000001'
put 'ORDER_INFO','000001','C1:STATUS','已提交'
put 'ORDER_INFO','000001','C1:PAY_MONEY',4070
put 'ORDER_INFO','000001','C1:PAYWAY',1
put 'ORDER_INFO','000001','C1:USER_ID',4944191
put 'ORDER_INFO','000001','C1:OPERATION_DATE','2020-04-25 12:09:16'
put 'ORDER_INFO','000001','C1:CATEGORY','手机'
```

#### 5.2 查看数据

##### 5.2.1 查看订单数据

```shell
#get '表名','rowkey'
get 'ORDER_INFO','000001'
```

##### 5.2.2 查看订单数据(显示中文)

```shell
get 'ORDER_INFO','000001',{FORMATTER => "toString"}
```

##### 5.2.3 查看订单支付金额

```shell
get 'ORDER_INFO','000001','C1:PAY_MONEY',{FORMATTER => "toString"}
```

#### 5.3 更新数据

```shell
put 'ORDER_INFO', '000001', 'C1:STATUS', '已付款'
```

#### 5.4 删除数据

##### 5.4.1 删除指定的列  

```shell
delete 'ORDER_INFO','000001','C1:STATUS'
```

##### 5.4.2 删除整行数据  

```shell
deleteall 'ORDER_INFO','000001'
```

#### 5.5 清空表数据

```shell
#truncate '表名'
truncate 'ORDER_INFO'
```

## 6.计数操作

查看HBase中的ORDER_INFO表，一共有多少条记录  

#### 6.1 导入订单数据集ORDER_INFO

```shell
hbase shell /usr/BigData/hadoop-2.7.5/testdata/ORDER_INFO.txt
```

**注:表必须存在**

#### 6.2 查看订单数据集ORDER_INFO的记录条数

##### 6.2.1 count命令计数(不推荐)

```shell
#count ‘表名’
count 'ORDER_INFO'
```

**注:这个操作是比较耗时,在数据量大的这个命令可能会运行很久**

##### 6.2.2 MapReduce计数

`启动yarn(node01)`

```shell
cd /usr/BigData/hadoop-2.7.5/ 

sbin/start-yarn.sh
```

`mapreduce计数`

```shell
#$HBASE_HOME/bin/hbase org.apache.hadoop.hbase.mapreduce.RowCounter '表名'
$HBASE_HOME/bin/hbase org.apache.hadoop.hbase.mapreduce.RowCounter 'ORDER_INFO'
```

##### 注: 通过观察YARN的WEB UI，HBase启动了一个名字为rowcounter_ORDER_INFO的作业  

## 7.扫描操作

#### 7.1 查询订单所有数据

```shell
#scan '表名'
scan 'ORDER_INFO',{FORMATTER => 'toString'}
```

**注:要避免scan一张大表**  

#### 7.2 查询3条订单数据(按记录条数查询)  

```shell
scan 'ORDER_INFO', {LIMIT => 3, FORMATTER => 'toString'}
```

#### 7.3 查询3条订单状态、支付方式(查询几列)

```shell
scan 'ORDER_INFO', {LIMIT => 3, COLUMNS => ['C1:STATUS', 'C1:PAYWAY'], FORMATTER => 'toString'}
```

#### 7.4 查询指定订单ID的数据

```shell
#scan '表名', {ROWPREFIXFILTER => 'rowkey'}
scan 'ORDER_INFO', {ROWPREFIXFILTER => '02602f66-adc7-40d4-8485-76b5632b5b53', COLUMNS => ['C1:STATUS', 'C1:PAYWAY'], FORMATTER => 'toString'}
```

#### 7.5 查询连续行数据

```shell
scan 'ORDER_INFO',{STARTROW => '02602f66-adc7-40d4-8485-76b5632b5b53', STOPROW  => '02602f66-adc7-40d4-8485-76b5632b5b59'} #左闭右开

scan 'ORDER_INFO',{STARTROW => '02602f66-adc7-40d4-8485-76b5632b5b53'} #从起始到正无穷
```

**注:排序按照字典排序**

```shell
行键

1001
1001
10010
1002
1003
```

## 8.过滤操作

HBase中的过滤器也是基于Java开发的，只不过在Shell中，我们是使用基于JRuby的语法来实现的交互式查询。以下是HBase 2.2的JAVA API文档

http://hbase.apache.org/2.2/devapidocs/index.html

#### 8.1 查看HBase Shell过滤器

```shell
show_filters
```

#### 8.2 常见过滤器

##### 8.2.1 rowkey过滤器

| rowkey过滤器        |                                                    |
| ------------------- | -------------------------------------------------- |
| RowFilter           | 实现行键字符串的比较和过滤                         |
| PrefixFilter        | rowkey前缀过滤器                                   |
| KeyOnlyFilter       | 只对单元格的键进行过滤和显示，不显示值             |
| FirstKeyOnlyFilter  | 只扫描显示相同键的第一个单元格，其键值对会显示出来 |
| InclusiveStopFilter | 替代 ENDROW 返回终止条件行                         |

##### 8.2.2 列过滤器

| 列过滤器                   |                                    |
| -------------------------- | ---------------------------------- |
| FamilyFilter               | 列簇过滤器                         |
| QualifierFilter            | 列标识过滤器，只显示对应列名的数据 |
| ColumnPrefixFilter         | 对列名称的前缀进行过滤             |
| MultipleColumnPrefixFilter | 可以指定多个前缀对列名称过滤       |
| ColumnRangeFilter          | 过滤列名称的范围                   |

##### 8.2.3 值过滤器

| 值过滤器                       |                                      |
| :----------------------------- | ------------------------------------ |
| ValueFilter                    | 值过滤器，找到符合值条件的键值对     |
| SingleColumnValueFilter        | 在指定的列蔟和列中进行比较的值过滤器 |
| SingleColumnValueExcludeFilter | 排除匹配成功的值                     |

##### 8.2.4 其他过滤器  

| 其他过滤器             |                                                             |
| ---------------------- | ----------------------------------------------------------- |
| ColumnPaginationFilter | 对一行的所有列分页，只返回 [offset,offset+limit] 范围内的列 |
| PageFilter             | 对显示结果按行进行分页显示                                  |
| TimestampsFilter       | 时间戳过滤，支持等值，可以设置多个时间戳                    |
| ColumnCountGetFilter   | 限制每个逻辑行返回键值对的个数，在 get 方法中使用           |
| DependentColumnFilter  | 允许用户指定一个参考列或引用列来过滤其他列的过滤器          |

#### 8.3 过滤器用法

**scan '表名', { Filter => "过滤器(比较运算符, '比较器表达式')” }**

##### 8.3.1 比较运算符

| 比较运算符 | 描述     |
| ---------- | -------- |
| =          | 等于     |
| >          | 大于     |
| >=         | 大于等于 |
| <          | 小于     |
| <=         | 小于等于 |
| !=         | 不等于   |

##### 8.3.2 比较器

| 比较器                 | 描述             |
| ---------------------- | ---------------- |
| BinaryComparator       | 匹配完整字节数组 |
| BinaryPrefixComparator | 匹配字节数组前缀 |
| BitComparator          | 匹配比特位       |
| NullComparator         | 匹配空值         |
| RegexStringComparator  | 匹配正则表达式   |
| SubstringComparator    | 匹配子字符串     |

##### 8.3.3 比较器表达式

基本语法：比较器类型:比较器的值  

| 比较器                 | 表达式语言缩写         |
| ---------------------- | ---------------------- |
| BinaryComparator       | binary:值              |
| BinaryPrefixComparator | binaryprefix:值        |
| BitComparator          | bit:值                 |
| NullComparator         | null                   |
| RegexStringComparator  | regexstring:正则表达式 |
| SubstringComparator    | substring:值           |

#### 8.4 过滤举例

##### 8.4.1 使用RowFilter查询指定订单ID的数据

`需求`

只查询订单的ID为：02602f66-adc7-40d4-8485-76b5632b5b53的数据 

`查看Java API`

![010-过滤操作1](D:\BigData\4-HBase\images\010-过滤操作1.png)

通过上图，可以分析得到，RowFilter过滤器接受两个参数

- op——比较运算符

- rowComparator——比较器

`命令`

```shell
scan 'ORDER_INFO', {FILTER => "RowFilter(=,'binary:02602f66-adc7-40d4-8485-76b5632b5b53')"}
```

##### 8.4.2 使用列过滤器查询指定订单ID的数据

`需求`

查询状态为[已付款]的订单

`查看Java API`

![011-过滤操作2](D:\BigData\4-HBase\images\011-过滤操作2.png)

需要传入四个参数

- 列簇

- 列标识（列名）

- 比较运算符

- 比较器

`命令`

```shell
scan 'ORDER_INFO', {FILTER => "SingleColumnValueFilter('C1', 'STATUS', =, 'binary:已付款')", FORMATTER => 'toString'}
```

注意:

- 列名STATUS的大小写一定要对！此处使用的是大写！
- 列名写错了查不出来数据，但HBase不会报错，因为HBase是无模式的

##### 8.4.3 使用多个过滤器共同来实现查询

`需求`

查询支付方式为1，且金额大于3000的订单  

`查看Java API`

此处需要使用多个过滤器共同来实现查询，多个过滤器，可以使用AND或者OR来组合多个过滤器完成查询

`命令`

```shell
scan 'ORDER_INFO', {FILTER => "SingleColumnValueFilter('C1', 'PAYWAY', = , 'binary:1') AND SingleColumnValueFilter('C1', 'PAY_MONEY', > , 'binary:3000')", FORMATTER => 'toString'}
```

注意:

- HBase shell中**默认比较是字符串比较**，所以如果是比较数值类型的，会出现不准确的情况

- 例如:在字符串比较中4000是比100000大的

<br>

# 二、HBase的JavaAPI操作

## 1.JavaAPI的基本操作

#### 1.1 创建工程和导入jar包(Maven配置)

`pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>cn.wyl</groupId>
    <artifactId>HBase</artifactId>
    <version>1.0-SNAPSHOT</version>

    <dependencies>
        <dependency>
            <groupId>org.apache.hbase</groupId>
            <artifactId>hbase-client</artifactId>
            <version>2.1.0</version>
        </dependency>
        <dependency>
            <groupId>org.apache.hbase</groupId>
            <artifactId>hbase-server</artifactId>
            <version>2.1.0</version>
        </dependency>
        <dependency>
            <groupId>commons-io</groupId>
            <artifactId>commons-io</artifactId>
            <version>2.6</version>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.12</version>
        </dependency>
        <dependency>
            <groupId>org.testng</groupId>
            <artifactId>testng</artifactId>
            <version>6.14.3</version>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.1</version>
                <configuration>
                    <source>1.8</source>
                    <target>1.8</target>
                    <encoding>UTF-8</encoding>
                    <!--    <verbal>true</verbal>-->
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>2.4.3</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                        <configuration>
                            <minimizeJar>true</minimizeJar>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

#### 1.2 复制HBase和Hadoop配置文件至resources目录

##### 1.2.1 core-site.xml

`文件目录`

```shell
cd /usr/BigData/hadoop-2.7.5/etc/hadoop
```

`文件内容`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
<!--
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License. See accompanying LICENSE file.
-->

<!-- Put site-specific property overrides in this file. -->

<configuration>
	<!-- 指定集群的文件系统类型:分布式文件系统 -->
	<property>
		<name>fs.default.name</name>
		<value>hdfs://node01:8020</value>
	</property>
	<!-- hadoop文件存储临时目录 -->
	<property>
		<name>hadoop.tmp.dir</name>
		<value>/usr/BigData/hadoop-2.7.5/hadoopDatas/tempDatas</value>
	</property>
	<!-- 缓冲区大小 -->
	<property>
		<name>io.file.buffer.size</name>
		<value>4096</value>
	</property>

	<!-- 开启hdfs的垃圾桶机制 -->
	<property>
		<name>fs.trash.interval</name>
		<value>10080</value>
	</property>
</configuration>
```

##### 1.2.2 hdfs-site.xml

`文件目录`

```shell
cd /usr/BigData/hadoop-2.7.5/etc/hadoop
```

`文件内容`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
<!--
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License. See accompanying LICENSE file.
-->

<!-- Put site-specific property overrides in this file. -->

<configuration>
	<property>
		<name>dfs.namenode.secondary.http-address</name>
		<value>node01:50090</value>
	</property>

	<!-- 指定namenode的访问地址和端口 -->
	<property>
		<name>dfs.namenode.http-address</name>
		<value>node01:50070</value>
	</property>
	<!-- 指定namenode元数据的存放位置 -->
	<property>
		<name>dfs.namenode.name.dir</name>
		<value>file:///usr/BigData/hadoop-2.7.5/hadoopDatas/namenodeDatas,file:///usr/BigData/hadoop-2.7.5/hadoopDatas/namenodeDatas2</value>
	</property>
	<!--  定义dataNode数据存储的节点位置，实际工作中，一般先确定磁盘的挂载目录，然后多个目录用，进行分割  -->
	<property>
		<name>dfs.datanode.data.dir</name>
		<value>file:///usr/BigData/hadoop-2.7.5/hadoopDatas/datanodeDatas,file:///usr/BigData/hadoop-2.7.5/hadoopDatas/datanodeDatas2</value>
	</property>
	
	<!-- 指定namenode日志文件的存放目录 -->
	<property>
		<name>dfs.namenode.edits.dir</name>
		<value>file:///usr/BigData/hadoop-2.7.5/hadoopDatas/nn/edits</value>
	</property>

	<property>
		<name>dfs.namenode.checkpoint.dir</name>
		<value>file:///usr/BigData/hadoop-2.7.5/hadoopDatas/snn/name</value>
	</property>
	<property>
		<name>dfs.namenode.checkpoint.edits.dir</name>
		<value>file:///usr/BigData/hadoop-2.7.5/hadoopDatas/dfs/snn/edits</value>
	</property>
	<!-- 文件切片的副本个数-->
	<property>
		<name>dfs.replication</name>
		<value>3</value>
	</property>

	<!-- 设置HDFS的文件权限-->
	<property>
		<name>dfs.permissions</name>
		<value>false</value>
	</property>

	<!-- 设置一个文件切片的大小：128M-->
	<property>
		<name>dfs.blocksize</name>
		<value>134217728</value>
	</property>
</configuration>
```

##### 1.2.3 hbase-site.xml

`文件目录`

```shell
cd /usr/BigData/hbase-2.1.0/conf
```

`文件内容`

```xml
<?xml version="1.0"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
<!--
/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
-->
<configuration>
    <!-- HBase数据在HDFS中的存放的路径 -->
    <property>
	<name>hbase.rootdir</name>
	<value>hdfs://node01:8020/hbase</value>
    </property>
    <!-- Hbase的运行模式。false是单机模式，true是分布式模式。若为false,Hbase和Zookeeper会运行在同一个JVM里面 -->
    <property>
	<name>hbase.cluster.distributed</name>
	<value>rue</value>
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

##### 1.2.4 log4j.properties

`文件内容`

```xml
# Configure logging for testing: optionally with log file

#log4j.rootLogger=debug,appender
log4j.rootLogger=info,appender  
#log4j.rootLogger=error,appender

#\u8F93\u51FA\u5230\u63A7\u5236\u53F0
log4j.appender.appender=org.apache.log4j.ConsoleAppender  
#\u6837\u5F0F\u4E3ATTCCLayout
log4j.appender.appender.layout=org.apache.log4j.TTCCLayout
```

#### 1.3 代码编写

```java
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HBaseDemo {
    //获取配置信息
    public static Configuration configuration;

    static {
        configuration = HBaseConfiguration.create();
    }

    //1.判断表是否存在
    public static boolean isExist(String tableName){
        try {
            //对表操作需要使用HbaseAdmin
            Connection connection = ConnectionFactory.createConnection(configuration);

            //管理表
            HBaseAdmin admin = (HBaseAdmin)connection.getAdmin();

            return admin.tableExists(TableName.valueOf(tableName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    //2.在hbase创建表
    public static void createTable(String tableName,String... columnfamily){
        try {
            //对表操作需要使用HbaseAdmin
            Connection connection = ConnectionFactory.createConnection(configuration);
            //管理表
            HBaseAdmin admin = (HBaseAdmin) connection.getAdmin();

            //1.表如果存在，请输入其他表名
            if(isExist(tableName)){
                System.out.println("表存在,请输入其他表名");
            }else{
                //2.注意:创建表的话，需要创建一个描述器
                HTableDescriptor htd = new HTableDescriptor(TableName.valueOf(tableName));

                //3.创建列族
                for(String cf:columnfamily){
                    htd.addFamily(new HColumnDescriptor(cf));
                }

                //4.创建表
                admin.createTable(htd);
                System.out.println("创建表成功");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("表已经存在");
        }
    }

    //3.删除hbase的表
    public static void deleteTable(String tableName) {
        try {
            //对表操作需要使用HbaseAdmin
            Connection connection = ConnectionFactory.createConnection(configuration);
            //管理表
            HBaseAdmin admin = (HBaseAdmin) connection.getAdmin();

            //1.表如果存在，请输入其他表名
            if (!isExist(tableName)) {
                System.out.println("表不存在");
            } else {
                //2.如果表存在，删除
                admin.disableTable(TableName.valueOf(tableName));
                admin.deleteTable(TableName.valueOf(tableName));
                System.out.println("表已经删除");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //4.添加数据put 'user','rowkey','info:name','tony'
    public static void addRow(String tableName,String rowkey,String cf,String column,String value){
        try {
            //对表操作需要使用HbaseAdmin
            Connection connection = ConnectionFactory.createConnection(configuration);
            Table t = connection.getTable(TableName.valueOf(tableName));
            //1.表如果存在，请输入其他表名
            if (!isExist(tableName)) {
                System.out.println("表不存在");
            } else {
                //2.用put方式加入数据
                Put p = new Put(Bytes.toBytes(rowkey));
                //3.加入数据
                p.addColumn(Bytes.toBytes(cf),Bytes.toBytes(column),Bytes.toBytes(value));
                t.put(p);

                System.out.println("添加数据成功");

            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("添加数据失败");
        }
    }

    //5.删除表中一行数据
    public static void deleteRow(String tableName,String rowkey,String cf ){
        try {
            //对表操作需要使用HbaseAdmin
            Connection connection = ConnectionFactory.createConnection(configuration);
            Table t = connection.getTable(TableName.valueOf(tableName));
            //1.表如果存在，请输入其他表名
            if (!isExist(tableName)) {
                System.out.println("表不存在");
            } else {
                //1.根据rowkey删除数据
                Delete delete = new Delete(Bytes.toBytes(rowkey));
                //2.删除
                t.delete(delete);
                System.out.println("删除数据成功");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("删除数据失败");
        }
    }

    //6.删除多行数据
    public static void deleteAll(String tableName,String... rowkeys){
        try {
            //对表操作需要使用HbaseAdmin
            Connection connection = ConnectionFactory.createConnection(configuration);
            Table t = connection.getTable(TableName.valueOf(tableName));
            //1.表如果存在，请输入其他表名
            if (!isExist(tableName)) {
                System.out.println("表不存在");
            } else {
                //1.把delete封装到集合
                List<Delete> list = new ArrayList<Delete>();
                //2.遍历
                for (String row:rowkeys){
                    Delete d = new Delete(Bytes.toBytes(row));
                    list.add(d);
                }
                t.delete(list);
                System.out.println("删除数据成功");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("删除数据失败");
        }
    }

    //7.扫描表数据 scan全表扫描
    public static void scanAll(String tableName){
        try {
            //对表操作需要使用HbaseAdmin
            Connection connection = ConnectionFactory.createConnection(configuration);
            Table t = connection.getTable(TableName.valueOf(tableName));

            //1.实例scan
            Scan s = new Scan();
            //2.拿到Scanner对象
            ResultScanner rs = t.getScanner(s);

            //3.遍历
            for (Result r:rs){
                Cell[] cells = r.rawCells();
                //遍历具体数据
                for (Cell c : cells){
                    System.out.print("行键为:"+Bytes.toString(CellUtil.cloneRow(c))+"  ");
                    System.out.print("列族为:"+Bytes.toString(CellUtil.cloneFamily(c))+"  ");
                    System.out.print("列名为:"+Bytes.toString(CellUtil.cloneQualifier(c))+"  ");
                    System.out.println("值为:"+Bytes.toString(CellUtil.cloneValue(c)));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //8.过滤数据
    public static void getRow(String tableName,String rowkey) throws IOException {
        Connection connection = ConnectionFactory.createConnection(configuration);
        //拿到表对象
        Table t = connection.getTable(TableName.valueOf(tableName));

        //1.扫描指定数据需要实例对象Get
        Get get = new Get(Bytes.toBytes(rowkey));
        //2.可加过滤条件
        get.addFamily(Bytes.toBytes("info"));
        Result rs = t.get(get);
        //3.遍历
        Cell[] cells = rs.rawCells();
        for (Cell c : cells){
            System.out.print("行键为:"+Bytes.toString(CellUtil.cloneRow(c))+"  ");
            System.out.print("列族为:"+Bytes.toString(CellUtil.cloneFamily(c))+"  ");
            System.out.print("列名:"+Bytes.toString(CellUtil.cloneQualifier(c))+"  ");
            System.out.println("值为:"+Bytes.toString(CellUtil.cloneRow(c))+"  ");
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println(isExist("ORDER_INFO"));
        createTable("Message","info");
        deleteTable("Tony");
        addRow("Message","105","info","age","12");
        deleteRow("Message","102","info");
        deleteAll("Message","101","103");
        scanAll("Message");
        getRow("Message","104");

    }
}
```

## 2.JavaAPI的scan+filter实际操作

#### 2.1 需求

查询2020年6月份所有用户的用水量  

#### 2.2 数据准备

##### 2.2.1 数据格式

| 用户id  | 姓名   | 用户地址                     | 性别 | 缴费时间   | 表示数（本次） | 表示数（上次） | 用量（立方） | 合计金额 | 查表日期   | 最迟缴费日期 |
| ------- | ------ | ---------------------------- | ---- | ---------- | -------------- | -------------- | ------------ | -------- | ---------- | ------------ |
| 4944191 | 登卫红 | 贵州省铜仁市德江县7单元267室 | 男   | 2020-05-10 | 308.1          | 283.1          | 25           | 150      | 2020-04-25 | 2020-06-09   |

##### 2.2.2 数据导入

`建表`

```shell
create table "WATER_BILL","C1"
```

`上传数据`

```shell
hadoop fs -mkdir -p /water_bill
hadoop fs -put part-m-00000_10w /water_bill
```

`MapReduce导入`

```shell
hbase org.apache.hadoop.hbase.mapreduce.Import WATER_BILL /water_bill
```

**注:要启动Yarn**

#### 2.3 代码编写

```java
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CompareOperator;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class HBasePractice {
    //获取配置信息
    public static Configuration configuration;

    static {
        configuration = HBaseConfiguration.create();
    }

    public static void scanFilterTest() throws IOException {
        Connection connection = ConnectionFactory.createConnection(configuration);

        // 1.获取表
        Table table = connection.getTable(TableName.valueOf("WATER_BILL"));

        // 2.构建scan请求对象
        Scan scan = new Scan();

        // 3.构建两个过滤器
        // a)构建两个日期范围过滤器(注意此处请使用RECORD_DATE——抄表日期比较)
        SingleColumnValueFilter startFilter = new SingleColumnValueFilter(Bytes.toBytes("C1")
                , Bytes.toBytes("RECORD_DATE")
                , CompareOperator.GREATER_OR_EQUAL
                , new BinaryComparator(Bytes.toBytes("2020-06-01")));

        SingleColumnValueFilter endFilter = new SingleColumnValueFilter(Bytes.toBytes("C1")
                , Bytes.toBytes("RECORD_DATE")
                , CompareOperator.LESS_OR_EQUAL
                , new BinaryComparator(Bytes.toBytes("2020-06-30")));

        // b)构建过滤器列表
        FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ALL, startFilter, endFilter);

        // 4.执行scan扫描请求
        scan.setFilter(filterList);
        ResultScanner resultScanner = table.getScanner(scan);
        Iterator<Result> iterator = resultScanner.iterator();

        // 5.迭代打印result
        while(iterator.hasNext()) {
            Result result = iterator.next();

            // 列出所有的单元格
            List<Cell> cellList = result.listCells();

            // 打印rowkey
            byte[] rowkey = result.getRow();
            System.out.println(Bytes.toString(rowkey));

            // 6.	迭代单元格列表
            for (Cell cell : cellList) {
                // 将字节数组转换为字符串
                // 获取列蔟的名称
                String cf = Bytes.toString(cell.getFamilyArray(), cell.getFamilyOffset(), cell.getFamilyLength());
                // 获取列的名称
                String columnName = Bytes.toString(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());

                String value = "";

                // 解决乱码问题：
                // 思路：
                // 如果某个列是以下列中的其中一个，调用toDouble将它认为是一个数值来转换
                //1.	NUM_CURRENT
                //2.	NUM_PREVIOUS
                //3.	NUM_USAGE
                //4.	TOTAL_MONEY
                if(columnName.equals("NUM_CURRENT")
                        || columnName.equals("NUM_PREVIOUS")
                        || columnName.equals("NUM_USAGE")
                        || columnName.equals("TOTAL_MONEY")) {
                    value = Bytes.toDouble(cell.getValueArray()) + "";
                }
                else {
                    // 获取值
                    value = Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
                }

                System.out.println(cf + ":" + columnName + " -> " + value);
            }
        }
        // 7.	关闭ResultScanner（这玩意把转换成一个个的类似get的操作，注意要关闭释放资源）
        resultScanner.close();
        // 8.	关闭表
        table.close();
    }

    public static void main(String[] args) throws IOException {
        scanFilterTest();
    }
}
```
