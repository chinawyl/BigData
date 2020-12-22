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

#### 2.2 查看帮助命令

```sql
help
```

#### 2.3 查看当前数据库有那些表

```sql
list
```

#### 2.4 退出HBase客户端

```sql
exit
```

## 3.命名空间操作

#### 3.1 查看数据库有那些命名空间

```sql
list_namespace
```

#### 3.2 创建命名空间

```sql
create_namespace 'bigdata'
```

#### 3.3 在命名空间创建表

```sql
create 'bigdata:ORDER_INFO','C1'
```

#### 3.4 删除命名空间

```sql
-- 
disable 'bigdata:ORDER_INFO'

--
drop 'bigdata:ORDER_INFO'

--
drop_namespace 'bigdata'
```

## 4.表操作

#### 4.1 创建表

```sql
-- create '表名','列蔟名'...

create 'ORDER_INFO','C1'
```

**注:没带命名空间，默认为default命名空间**

#### 4.2 查看表结构

```sql
-- describe '表名'
describe 'ORDER_INFO'
```

#### 4.3 删除表

##### 4.3.1 禁用表

```sql
-- disable '表名'
disable 'ORDER_INFO'
```

##### 4.3.2 删除表

```sql
-- drop '表名'
drop 'ORDER_INFO' 
```

**注:删除表之前必须禁用表**

## 5.数据操作

#### 5.1 添加数据

```sql
-- put '表名','ROWKEY','列蔟名:列名','值'
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

```sql
-- get '表名','rowkey'
get 'ORDER_INFO','000001'
```

##### 5.2.2 查看订单数据(显示中文)

```sql
get 'ORDER_INFO','000001',{FORMATTER => "toString"}
```

##### 5.2.3 查看订单支付金额

```sql
get 'ORDER_INFO','000001','C1:PAY_MONEY',{FORMATTER => "toString"}
```

#### 5.3 更新数据

```sql
put 'ORDER_INFO', '000001', 'C1:STATUS', '已付款'
```

#### 5.4 删除数据

##### 5.4.1 删除指定的列  

```sql
delete 'ORDER_INFO','000001','C1:STATUS'
```

##### 5.4.2 删除整行数据  

```sql
deleteall 'ORDER_INFO','000001'
```

#### 5.5 清空表数据

```sql
-- truncate '表名'
truncate 'ORDER_INFO'
```

## 6.计数操作

查看HBase中的ORDER_INFO表，一共有多少条记录  

#### 6.1 导入订单数据集ORDER_INFO

`数据集地址`



```shell
hbase shell /usr/BigData/hadoop-2.7.5/testdata/ORDER_INFO.txt
```

**注:表必须存在**

#### 6.2 查看订单数据集ORDER_INFO的记录条数

##### 6.2.1 count命令计数(不推荐)

```sql
-- count ‘表名’
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

```sql
-- $HBASE_HOME/bin/hbase org.apache.hadoop.hbase.mapreduce.RowCounter '表名'
$HBASE_HOME/bin/hbase org.apache.hadoop.hbase.mapreduce.RowCounter 'ORDER_INFO'
```

##### 注: 通过观察YARN的WEB UI，HBase启动了一个名字为rowcounter_ORDER_INFO的作业  

## 7.扫描操作

#### 7.1 查询订单所有数据

```sql
-- scan '表名'
scan 'ORDER_INFO',{FORMATTER => 'toString'}
```

**注:要避免scan一张大表**  

#### 7.2 查询3条订单数据(按记录条数查询)  

```sql
scan 'ORDER_INFO', {LIMIT => 3, FORMATTER => 'toString'}
```

#### 7.3 查询3条订单状态、支付方式(查询几列)

```sql
scan 'ORDER_INFO', {LIMIT => 3, COLUMNS => ['C1:STATUS', 'C1:PAYWAY'], FORMATTER => 'toString'}
```

#### 7.4 查询指定订单ID的数据

```sql
-- scan '表名', {ROWPREFIXFILTER => 'rowkey'}
scan 'ORDER_INFO', {ROWPREFIXFILTER => '02602f66-adc7-40d4-8485-76b5632b5b53', COLUMNS => ['C1:STATUS', 'C1:PAYWAY'], FORMATTER => 'toString'}
```

#### 7.5 查询连续行数据

```sql
scan 'ORDER_INFO',{STARTROW => '02602f66-adc7-40d4-8485-76b5632b5b53', STOPROW  => '02602f66-adc7-40d4-8485-76b5632b5b59'} --左闭右开

scan 'ORDER_INFO',{STARTROW => '02602f66-adc7-40d4-8485-76b5632b5b53'} --从起始到正无穷
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

<br>

# 二、HBase的JavaAPI操作