# 一、Phoneix简介

### 1.概述

Apache Phoenix让Hadoop中支持低延迟OLTP和业务操作分析

- 提供标准的SQL以及完备的ACID事务支持
- 通过利用HBase作为存储，让NoSQL数据库具备通过有模式的方式读取数据
- Phoenix通过**协处理器在服务器端执行操作**，最小化客户机/服务器数据传输

### 2.官网

http://phoenix.apache.org/

### 3.Phoenix是否会影响HBase性能  

- Phoenix不会影响HBase性能，反而会提升HBase性能
- Phoenix将SQL查询编译为本机HBase扫描
- 确定scan的key的最佳startKey和endKey
- 编排scan的并行执行
- 将WHERE子句中的谓词推送到服务器端
- 通过协处理器执行聚合查询
- 用于提高非行键列查询性能的二级索引
- 统计数据收集，以改进并行化，并指导优化之间的选择
- 跳过扫描筛选器以优化IN、LIKE和OR查询
- 行键加盐保证分配均匀，负载均衡

<br>

# 二、Phoneix安装

### 1.下载安装包

从官网上下载与HBase版本对应的Phoenix版本，其中HBase 2.1应该使用版本[5.0.0-HBase-2.0]

### 2.上传安装包并解压(node01)

```shell
tar -zxvf apache-phoenix-5.0.0-HBase-2.0-bin.tar.gz -C /usr/BigData
```

### 3.复制phoenix的所有jar包到所有HBase节点的lib目录(node01)

```shell
cp /usr/BigData/apache-phoenix-5.0.0-HBase-2.0-bin/phoenix-*.jar /usr/BigData/hbase-2.1.0/lib

cd /usr/BigData/hbase-2.1.0/lib

scp -r phoenix-*.jar node02:/usr/BigData/hbase-2.1.0/lib
scp -r phoenix-*.jar node03:/usr/BigData/hbase-2.1.0/lib
```

### 4.修改配置文件

##### 4.1 进入目录并打开文件

```shell
cd /usr/BigData/hbase-2.1.0/conf

vim hbase-site.xml
```

##### 4.2 添加以下内容到配置文件

```xml
<configuration>
    <!-- 支持HBase命名空间映射 -->
    <property>
	<name>phoenix.schema.isNamespaceMappingEnabled</name>
	<value>true</value>
    </property>
    <!-- 支持索引预写日志编码 -->
    <property>
	<name>hbase.regionserver.wal.codec</name>
	<value>org.apache.hadoop.hbase.regionserver.wal.IndexedWALEditCodec</value>
    </property>
    <property> 
	<name>hbase.region.server.rpc.scheduler.factory.class</name> 
	<value>org.apache.hadoop.hbase.ipc.PhoenixRpcSchedulerFactory</value> 
	<description>Factory to create the Phoenix RPC Scheduler that uses separate queues for index and metadata updates</description>
    </property>
    <property>
	<name>hbase.rpc.controllerfactory.class</name> 
	<value>org.apache.hadoop.hbase.ipc.controller.ServerRpcControllerFactory</value> 
	<description>Factory to create the Phoenix RPC Scheduler that uses separate queues for index and metadata updates</description>
    </property>
    <property> 
	<name>hbase.master.loadbalancer.class</name> 
	<value>org.apache.phoenix.hbase.index.balancer.IndexLoadBalancer</value>
    </property>
    <property> 
	<name>hbase.coprocessor.master.classes</name> 
	<value>org.apache.phoenix.hbase.index.master.IndexMasterObserver</value>
    </property>
</configuration>
```

### 5.分发配置文件到每个节点

```shell
scp -r hbase-site.xml node02:/usr/BigData/hbase-2.1.0/conf
scp -r hbase-site.xml node03:/usr/BigData/hbase-2.1.0/conf
```

### 6.拷贝hbase-site.xml文件到phoenix的bin目录(node01)  

```shell
cp /usr/BigData/hbase-2.1.0/conf/hbase-site.xml /usr/BigData/apache-phoenix-5.0.0-HBase-2.0-bin/bin
```

### 7.重启HBase

```shell
cd /usr/BigData/hbase-2.1.0/bin

./stop-hbase.sh
./start-hbase.sh
```

### 8.启动Phoenix客户端  

```shell
cd /usr/BigData/apache-phoenix-5.0.0-HBase-2.0-bin

bin/sqlline.py node01:2181
```

### 9.查看安装情况

```shell
#查看Phoenix中的表
!table
```

### 10.查看WebUI

发现生成了许多表

<br>

# 三、Phoenix的使用

### 1.创建表(大写)

##### 1.1 语法

```shell
CREATE TABLE IF NOT EXISTS 表名 (
   ROWKEY名称 数据类型 PRIMARY KEY
	列蔟名.列名1 数据类型 NOT NULL,
	列蔟名.列名2 数据类型 NOT NULL,
	列蔟名.列名3 数据类型);
```

##### 1.2 语句

```shell
create table if not exists ORDER_DTL(
    ID varchar primary key,
    C1.STATUS varchar,
    C1.MONEY float,
    C1.PAY_WAY integer,
    C1.USER_ID varchar,
    C1.OPERATION_TIME varchar,
    C1.CATEGORY varchar
);
```

**注: 在HBase中，如果在列蔟、列名没有添加双引号，Phoenix会自动转换为大写**

### 2.查看表

##### 2.1 查看所有的表

```shell
!tables
```

##### 2.2 查看表结构

```shell
!desc ORDER_DTL
```

### 3.删除表

```shell
drop table if exists ORDER_DTL;
```

### 4.创建表(小写)

```shell
create table if not exists ORDER_DTL(
    "id" varchar primary key,
    "C1"."status" varchar,
    "C1"."money" double,
    "C1"."pay_way" integer,
    "C1"."user_id" varchar,
    "C1"."operation_time" varchar,
    "C1"."category" varchar
);
```

**注:一旦加了小写，后面都得任何应用该列的地方都得使用双引号**

### 5.插入数据

| 订单ID | 订单状态 | 支付金额  | 支付方式ID | 用户ID  | 操作时间            | 商品分类 |
| ------ | -------- | --------- | ---------- | ------- | ------------------- | -------- |
| ID     | STATUS   | PAY_MONEY | PAYWAY     | USER_ID | OPERATION_DATE      | CATEGORY |
| 000001 | 已提交   | 4070      | 1          | 4944191 | 2020-04-25 12:09:16 | 手机;    |

##### 5.1 语法

```shell
upsert into 表名(列蔟列名, xxxx, ) VALUES(XXX, XXX, XXX)
```

##### 5.2 语句

```shell
UPSERT INTO ORDER_DTL VALUES('000001', '已提交', 4070, 1, '4944191', '2020-04-25 12:09:16', '手机;');
```

### 6.查询数据

##### 6.1 查询所有数据

```shell
select * from ORDER_DTL;
```

##### 6.2 根据ID查询数据

```shell
select * from ORDER_DTL where "id" = '000001';
```

##### 6.3 查询订单状态

```shell
select "status" from ORDER_DTL
```

### 7.删除数据

```shell
delete from ORDER_DTL where "id" = '000001';
```

### 8.插入多条数据

```shell
UPSERT INTO "ORDER_DTL" VALUES('000002','已提交',4070,1,'4944191','2020-04-25 12:09:16','手机;');
UPSERT INTO "ORDER_DTL" VALUES('000003','已完成',4350,1,'1625615','2020-04-25 12:09:37','家用电器;;电脑;');
UPSERT INTO "ORDER_DTL" VALUES('000004','已提交',6370,3,'3919700','2020-04-25 12:09:39','男装;男鞋;');
UPSERT INTO "ORDER_DTL" VALUES('000005','已付款',6370,3,'3919700','2020-04-25 12:09:44','男装;男鞋;');
UPSERT INTO "ORDER_DTL" VALUES('000006','已提交',9380,1,'2993700','2020-04-25 12:09:41','维修;手机;');
UPSERT INTO "ORDER_DTL" VALUES('000007','已付款',9380,1,'2993700','2020-04-25 12:09:46','维修;手机;');
UPSERT INTO "ORDER_DTL" VALUES('000008','已完成',6400,2,'5037058','2020-04-25 12:10:13','数码;女装;');
UPSERT INTO "ORDER_DTL" VALUES('000009','已付款',280,1,'3018827','2020-04-25 12:09:53','男鞋;汽车;');
UPSERT INTO "ORDER_DTL" VALUES('000010','已完成',5600,1,'6489579','2020-04-25 12:08:55','食品;家用电器;');
UPSERT INTO "ORDER_DTL" VALUES('000011','已付款',5600,1,'6489579','2020-04-25 12:09:00','食品;家用电器;');
UPSERT INTO "ORDER_DTL" VALUES('000012','已提交',8340,2,'2948003','2020-04-25 12:09:26','男装;男鞋;');
UPSERT INTO "ORDER_DTL" VALUES('000013','已付款',8340,2,'2948003','2020-04-25 12:09:30','男装;男鞋;');
UPSERT INTO "ORDER_DTL" VALUES('000014','已提交',7060,2,'2092774','2020-04-25 12:09:38','酒店;旅游;');
UPSERT INTO "ORDER_DTL" VALUES('000015','已提交',640,3,'7152356','2020-04-25 12:09:49','维修;手机;');
UPSERT INTO "ORDER_DTL" VALUES('000016','已付款',9410,3,'7152356','2020-04-25 12:10:01','维修;手机;');
UPSERT INTO "ORDER_DTL" VALUES('000017','已提交',9390,3,'8237476','2020-04-25 12:10:08','男鞋;汽车;');
UPSERT INTO "ORDER_DTL" VALUES('000018','已提交',7490,2,'7813118','2020-04-25 12:09:05','机票;文娱;');
UPSERT INTO "ORDER_DTL" VALUES('000019','已付款',7490,2,'7813118','2020-04-25 12:09:06','机票;文娱;');
UPSERT INTO "ORDER_DTL" VALUES('000020','已付款',5360,2,'5301038','2020-04-25 12:08:50','维修;手机;');
UPSERT INTO "ORDER_DTL" VALUES('000021','已提交',5360,2,'5301038','2020-04-25 12:08:53','维修;手机;');
UPSERT INTO "ORDER_DTL" VALUES('000022','已取消',5360,2,'5301038','2020-04-25 12:08:58','维修;手机;');
UPSERT INTO "ORDER_DTL" VALUES('000023','已付款',6490,0,'3141181','2020-04-25 12:09:22','食品;家用电器;');
UPSERT INTO "ORDER_DTL" VALUES('000024','已付款',3820,1,'9054826','2020-04-25 12:10:04','家用电器;;电脑;');
UPSERT INTO "ORDER_DTL" VALUES('000025','已提交',4650,2,'5837271','2020-04-25 12:08:52','机票;文娱;');
UPSERT INTO "ORDER_DTL" VALUES('000026','已付款',4650,2,'5837271','2020-04-25 12:08:57','机票;文娱;');
```

### 9.分页查询

**limit表示每页多少条记录，offset表示从第几条记录开始查起**

```shell
#第一页
select * from ORDER_DTL limit 10 offset 0;

#第二页,offset从10开始
select * from ORDER_DTL limit 10 offset 10;

#第三页
select * from ORDER_DTL limit 10 offset 20;
```

### 10.预分区表

##### 10.1 ROWKEY预分区

按照用户ID来分区，一共**4个分区**，并指定数据的**压缩格式为GZ**

```shell
drop table if exists ORDER_DTL;

create table if not exists ORDER_DTL(
    "id" varchar primary key,
    "C1"."status" varchar,
    "C1"."money" float,
    "C1"."pay_way" integer,
    "C1"."user_id" varchar,
    "C1"."operation_time" varchar,
    "C1"."category" varchar
) 
CONPRESSION='GZ'
SPLIT ON ('3','5','7');
```

查看WebUI发现数据分布在每一个Region中

##### 10.2 加盐指定数量分区

```shell
drop table if exists ORDER_DTL;

create table if not exists ORDER_DTL(
    "id" varchar primary key,
    "C1"."status" varchar,
    "C1"."money" float,
    "C1"."pay_way" integer,
    "C1"."user_id" varchar,
    "C1"."operation_time" varchar,
    "C1"."category" varchar
) 
CONPRESSION='GZ'
SALT_BUCKETS=10;
```

查看WebUI发现生成了10个Region  

### 11.建立视图

##### 11.1 映射HBase中的表

```shell
CREATE VIEW "my_hbase_table"
(k VARCHAR primary key, "v" UNSIGNED_LONG)
default_column_family='a';
```

##### 11.2 映射Phoenix中的表

```shell
CREATE VIEW my_view(new_col SMALLINT)
AS SELECT * FROM my_table WHERE k = 100;
```

##### 11.3 建立MOMO_CHAT:MSG的视图

```shell
create view if not exists "MOMO_CHAT". "MSG" (
    "pk" varchar primary key, -- 指定ROWKEY映射到主键
    "C1"."msg_time" varchar,
    "C1"."sender_nickyname" varchar,
    "C1"."sender_account" varchar,
    "C1"."sender_sex" varchar,
    "C1"."sender_ip" varchar,
    "C1"."sender_os" varchar,
    "C1"."sender_phone_type" varchar,
    "C1"."sender_network" varchar,
    "C1"."sender_gps" varchar,
    "C1"."receiver_nickyname" varchar,
    "C1"."receiver_ip" varchar,
    "C1"."receiver_account" varchar,
    "C1"."receiver_os" varchar,
    "C1"."receiver_phone_type" varchar,
    "C1"."receiver_network" varchar,
    "C1"."receiver_gps" varchar,
    "C1"."receiver_sex" varchar,
    "C1"."msg_type" varchar,
    "C1"."distance" varchar,
    "C1"."message" varchar
);
```

##### 11.4 查询一条数据

```shell
SELECT * FROM "MOMO_CHAT"."MSG" LIMIT 1;
```

### 12.创建索引

##### 12.1 全局索引

```shell
CREATE INDEX 索引名称 ON 表名 (列名1, 列名2, 列名3...)
```

##### 12.2 本地索引

```shell
CREATE local INDEX 索引名称 ON 表名 (列名1, 列名2, 列名3...)
```

##### 12.3 覆盖索引

```shell
CREATE INDEX my_index ON my_table (v1,v2) INCLUDE(v3)
```

##### 12.4 函数索引

```shell
#创建索引
CREATE INDEX UPPER_NAME_IDX ON EMP (UPPER(FIRST_NAME||' '||LAST_NAME))

#以下查询会走索引
SELECT EMP_ID FROM EMP WHERE UPPER(FIRST_NAME||' '||LAST_NAME)='JOHN DOE'
```