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

```sql
help
```

#### 2.3 查看服务器状态

```sql
status
```

#### 2.4 显示HBase当前用户

```sql
whoami
```

#### 2.5 查看当前数据库有那些表

```sql
list
```

#### 2.6 检查表是否存在

```shell
exits
```

#### 2.7 检查表是否被禁用或启用

```sql
is_enabled --是否启用

is_disabled --是否启用
```

#### 2.8 改变表和列蔟的模式

```sql
--创建一个USER_INFO表，两个列蔟C1、C2
create 'USER_INFO', 'C1', 'C2'

--新增列蔟C3
alter 'USER_INFO', 'C3'

--删除列蔟C3
alter 'USER_INFO', 'delete' => 'C3'
```

#### 2.9 退出HBase客户端

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
-- create "表名","列蔟名"...

create "ORDER_INFO","C1"
```

**注:没带命名空间，默认为default命名空间**

#### 4.2 查看表结构

```sql
-- describe "表名"
describe 'ORDER_INFO'
```

#### 4.3 变更表信息

```sql
alter 'student',{NAME=>'info',VERSIONS=>5} --变更列族版本

get 'student','1001',{COLUMN=>'info:name',VERSIONS=>3} --查看当前列最新3个版本数据信息
```

#### 4.4 删除表

##### 4.4.1 禁用表

```sql
-- disable '表名'
disable 'ORDER_INFO'
```

**注:启用表为enable**

##### 4.4.2 删除表

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

## 8.过滤操作

HBase中的过滤器也是基于Java开发的，只不过在Shell中，我们是使用基于JRuby的语法来实现的交互式查询。以下是HBase 2.2的JAVA API文档

http://hbase.apache.org/2.2/devapidocs/index.html

#### 8.1 查看HBase Shell过滤器

```sql
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

```sql
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

```sql
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

```sql
scan 'ORDER_INFO', {FILTER => "SingleColumnValueFilter('C1', 'PAYWAY', = , 'binary:1') AND SingleColumnValueFilter('C1', 'PAY_MONEY', > , 'binary:3000')", FORMATTER => 'toString'}
```

注意:

- HBase shell中**默认比较是字符串比较**，所以如果是比较数值类型的，会出现不准确的情况

- 例如:在字符串比较中4000是比100000大的

<br>

# 二、HBase的JavaAPI操作