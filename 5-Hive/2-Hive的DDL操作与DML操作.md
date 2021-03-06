# 一、Hive的数据类型和交互方式

### 1.Hive数据类型

##### 1.1 数据类型列表

| 分类         | 类型      | 描述                                           | 字面量示例                                                   |
| ------------ | --------- | ---------------------------------------------- | ------------------------------------------------------------ |
| **原始类型** | BOOLEAN   | true/false                                     | TRUE                                                         |
|              | TINYINT   | 1字节的有符号整数, -128~127                    | 1Y                                                           |
|              | SMALLINT  | 2个字节的有符号整数，-32768~32767              | 1S                                                           |
|              | INT       | 4个字节的带符号整数                            | 1                                                            |
|              | BIGINT    | 8字节带符号整数                                | 1L                                                           |
|              | FLOAT     | 4字节单精度浮点数                              | 1.0                                                          |
|              | DOUBLE    | 8字节双精度浮点数                              | 1.0                                                          |
|              | DEICIMAL  | 任意精度的带符号小数                           | 1.0                                                          |
|              | STRING    | 字符串，变长                                   | “a”,’b’                                                      |
|              | VARCHAR   | 变长字符串                                     | “a”,’b’                                                      |
|              | CHAR      | 固定长度字符串                                 | “a”,’b’                                                      |
|              | BINARY    | 字节数组                                       | 无法表示                                                     |
|              | TIMESTAMP | 时间戳，毫秒值精度                             | 122327493795                                                 |
|              | DATE      | 日期                                           | ‘2016-03-29’                                                 |
|              | INTERVAL  | 时间频率间隔                                   |                                                              |
| **复杂类型** | ARRAY     | 有序的的同类型的集合                           | array(1,2)                                                   |
|              | MAP       | key-value,key必须为原始类型，value可以任意类型 | map(‘a’,1,’b’,2)                                             |
|              | STRUCT    | 字段集合,类型可以不同                          | struct(‘1’,1,1.0), named_stract(‘col1’,’1’,’col2’,1,’clo3’,1.0) |
|              | UNION     | 在有限取值范围内的一个值                       | create_union(1,’a’,63)                                       |

##### 1.2 案例实操

`创建文件`

```shell
vim /usr/BigData/hadoop-2.7.5/testdata/test.txt
```

`添加数据`

```shell
 {    
     "name":"songsong",
     "friends":["bingbing","lili"], //列表Array
     "children":{           		//键值Map
         "xiao song":18,
         "xiaoxiao song":19},
     "address":{           			//结构Struct,
         "street":"hui long guan",
         "city":"beijing"
     }
 }  
```

`Hive上创建测试表test`

```mysql
create table test(
name string,
friends array<string>,
children map<string, int>,
address struct<street:string, city:string>
)
row format delimited fields terminated by ','
collection items terminated by '_'
map keys terminated by ':'
lines terminated by '\n';
```

`字段解释`

- row format delimited fields terminated by ','    -- 列分隔符

- collection items terminated by '_'                        --MAP STRUCT 和 ARRAY 的分隔符(数据分割符号)

- map keys terminated by ':'                                    -- MAP中的key与value的分隔符

- lines terminated by '\n';                                         -- 行分隔符

`导入文本数据到测试表`

```mysql
load data local inpath '/usr/BigData/hadoop-2.7.5/testdata/test.txt' into table test;
```

`访问三种集合列里的数据`

- ARRAY的访问方式

  ```mysql
  select friends[1] from test;
  ```

- MAP的访问方式

  ```mysql
  select children['xiao song'] from test;
  ```

- STRUCT的访问方式

  ```mysql
  select address.city from test;
  ```

##### 1.3 类型转化

Hive的原子数据类型是可以进行隐式转换的，类似于Java的类型转换，例如某表达式使用INT类型，TINYINT会自动转换为INT类型，但是Hive不会进行反向转化，例如，某表达式使用TINYINT类型，INT不会自动转换为TINYINT类型，它会返回错误，除非使用CAST操作

`隐式类型转换规则如下`

- 任何整数类型都可以隐式地转换为一个范围更广的类型，如TINYINT可以转换成INT，INT可以转换成BIGINT

- 所有整数类型、FLOAT和STRING类型都可以隐式地转换成DOUBLE

- TINYINT、SMALLINT、INT都可以转换为FLOAT

- BOOLEAN类型不可以转换为任何其它的类型

`使用CAST操作显示进行数据类型转换`

**CAST('1' AS INT)**将把字符串'1' 转换成整数1

如果强制类型转换失败，如执行**CAST('X' AS INT)**，表达式返回空值NULL

### 2.Hive的交互方式

##### 2.1 Shell交互

```shell
cd /usr/BigData/hive-1.2.1

bin/hive
```

##### 2.2 JDBC交互(不做了解)

参考java jdbc连接mysql

##### 2.3 Beeline交互

`启动hiveserver2服务(第一台node01)`

```shell
cd /usr/BigData/hive-1.2.1

bin/hiveserver2
```

`启动beeline(第二台node01)`

```shell
cd /usr/BigData/hive-1.2.1

bin/beeline
```

`连接hiveserver2(第二台node01)`

```shell
beeline>!connect jdbc:hive2://node01:10000

Connecting to jdbc:hive2://node01:10000

Enter username for jdbc:hive2://node01:10000:root

Enter password for jdbc:hive2://node01:10000:123456

Connected to: Apache Hive (version 1.2.1)
Driver: Hive JDBC (version 1.2.1)
Transaction isolation: TRANSACTION_REPEATABLE_READ

0:jdbc:hive2://node01:10000> show databases;
```

**注:可在第一台node01里查看执行反馈和日志消息**

##### 2.4 Hive命令交互

`语法`

```shell
bin/hive -e 'hql语句' 
```

`案例`

```shell
cd /usr/BigData/hive-1.2.1

bin/hive -e 'select * from student'
```

<br>

# 二、Hive的DDL操作

### 1.创建数据库

##### 1.1 创建一个数据库

```mysql
create database hive2;
```

**注:数据库在HDFS上的默认存储路径是/user/hive/warehouse/*.db**

##### 1.2 避免要创建的数据库已经存在

```mysql
create database if not exists hive2;
```

##### 1.3 创建一个数据库并指定在HDFS上存放的位置

```mysql
create database hive3 location '/hive3';
```

##### 1.4 创建一个类似于default没有库名的数据库

`创建目录`

```shell
hdfs dfs -mkdir -p /database/hive4
```

`创建数据库`

```mysql
create database hives location '/database/hive4';
```

`使用hives`

```mysql
use hives;
```

`创建表`

```mysql
create table tests(id int);
```

![004-默认数据库创建](./images/004-默认数据库创建.png)

**注:tests直接创建在hive4目录层级，没有hives目录层级**

### 2.查询数据库

##### 2.1 显示数据库

`显示数据库`

```mysql
show databases;
```

`过滤显示查询的数据库`

```mysql
show databases like 'hive*';
```

##### 2.2 查看数据库详情

`显示数据库信息`

```mysql
desc database hive3;
```

`显示数据库详细信息`

```mysql
desc database extended hive3;
```

##### 2.3 切换其他数据库

```mysql
use hive4;
```

### 3.修改数据库

用户可以使用**ALTER DATABASE**命令为某个数据库的**DBPROPERTIES**设置键-值对属性值，来描述这个数据库的属性信息。**数据库的其他元数据信息都是不可更改的**，包括数据库名和数据库所在的目录位置

##### 3.1 修改DBPROPERTIES

```mysql
alter database hive2 set dbproperties('createtime'='20170830');
```

##### 3.2 在hive中查看修改结果

```mysql
desc database extended hive2;
```

### 4.删除数据库4

##### 4.1 删除空数据库

```mysql
drop database hive2;
```

##### 4.2 删除非空数据库

**如果数据库不为空，可以采用cascade命令，强制删除**

```mysql
drop database hive4 cascade;
```

### 5.创建表

##### 5.1 建表语法

```mysql
CREATE [EXTERNAL] TABLE [IF NOT EXISTS] table_name 

[(col_name data_type [COMMENT col_comment], ...)] 

[COMMENT table_comment] 

[PARTITIONED BY (col_name data_type [COMMENT col_comment], ...)] 

[CLUSTERED BY (col_name, col_name, ...) 

[SORTED BY (col_name [ASC|DESC], ...)] INTO num_buckets BUCKETS] 

[ROW FORMAT row_format] 

[STORED AS file_format] 

[LOCATION hdfs_path]
```

##### 5.2 字段解释说明 

`CREATE TABLE`

创建一个指定名字的表，如果相同名字的表已经存在，则抛出异常，用户可以用**IF NOT EXISTS**忽略这个异常

`EXTERNAL`

可以让用户创建一个**外部表**，在建表的同时指定一个指向实际数据的路径(LOCATION)，Hive**创建内部表时，会将数据移动到数据仓库指向的路径**；若创建**外部表，仅记录数据所在的路径，不对数据的位置做任何改变**。在删除表的时候，内部表的元数据和数据会被一起删除，而外部表只删除元数据，不删除数据

`COMMENT`

为表和列添加注释

`PARTITIONED BY`

创建**分区**表

`CLUSTERED BY`

创建**分桶**表

`SORTED BY`

排序，不常用

`ROW FORMAT`

DELIMITED [FIELDS TERMINATED BY char] [COLLECTION ITEMS TERMINATED BY char]

​    [MAP KEYS TERMINATED BY char] [LINES TERMINATED BY char] 

  | SERDE serde_name [WITH SERDEPROPERTIES (property_name=property_value, property_name=property_value, ...)]

用户在建表的时候可以自定义SerDe或者使用自带的SerDe，如果用户没有指定表的**ROW FORMAT**或者指定表的**ROW FORMAT DELIMITED**，将会使用自带的SerDe。在建表的时候，用户还需要为表指定列，用户在指定表的列的同时也会指定自定义的SerDe，Hive通过SerDe确定表的具体的列的数据

**SerDe是Serialize/Deserilize的简称，目的是用于序列化和反序列化**

`STORED AS指定存储文件类型`

常用的存储文件类型:SEQUENCEFILE(**二进制序列文件**)、TEXTFILE(**文本**)、RCFILE(**列式存储格式文件**)

如果文件数据是纯文本，可以使用STORED AS TEXTFILE

如果数据需要压缩，可以使用 STORED AS SEQUENCEFILE

`LOCATION`

指定表在HDFS上的存储位置

`LIKE`

允许用户复制现有的表结构，但是不复制数据

### 6.内部表(管理表)

##### 6.1 理论

默认创建的表都是所谓的**管理表，有时也被称为内部表**。因为这种表，Hive会控制着数据的生命周期。Hive默认情况下会将这些表的数据存储在由配置项hive.metastore.warehouse.dir，例如(**/user/hive/warehouse**)所定义的目录的子目录下，当我们删除一个管理表时，Hive也会删除这个表中数据。管理表不适合和其他工具共享数据。

##### 6.2 案例实操

`普通创建表`

```mysql
create table if not exists student2(
id int, name string
)
row format delimited fields terminated by '\t'
stored as textfile
location '/user/hive/warehouse/student2';
```

`根据查询结果创建表(查询的结果会添加到新创建的表中)`

```mysql
create table if not exists student3 as select id, name from student;
```

`根据已经存在的表结构创建表`

```mysql
create table if not exists student4 like student;
```

`查询表的类型`

```mysql
desc formatted student2;
```

### 7.外部表

##### 7.1 理论

因为表是外部表，所以Hive并非认为其完全拥有这份数据。删除该表并不会删除掉这份数据，不过描述**表的元数据信息会被删除掉**

##### 7.2 管理表和外部表的使用场景

每天将收集到的网站日志定期流入HDFS文本文件。在外部表(原始日志表)的基础上做大量的统计分析，用到的中间表、结果表使用内部表存储，数据通过SELECT+INSERT进入内部表

##### 7.3 案例实操

`进入数据库`

```mysql
use default;
```

`原始数据`

- dept.txt

```shell
10	ACCOUNTING	1700
20	RESEARCH	1800
30	SALES	1900
40	OPERATIONS	1700
```

- emp.txt

```shell
7369	SMITH	CLERK	7902	1980-12-17	800.00		20
7499	ALLEN	SALESMAN	7698	1981-2-20	1600.00	300.00	30
7521	WARD	SALESMAN	7698	1981-2-22	1250.00	500.00	30
7566	JONES	MANAGER	7839	1981-4-2	2975.00		20
7654	MARTIN	SALESMAN	7698	1981-9-28	1250.00	1400.00	30
7698	BLAKE	MANAGER	7839	1981-5-1	2850.00		30
7782	CLARK	MANAGER	7839	1981-6-9	2450.00		10
7788	SCOTT	ANALYST	7566	1987-4-19	3000.00		20
7839	KING	PRESIDENT		1981-11-17	5000.00		10
7844	TURNER	SALESMAN	7698	1981-9-8	1500.00	0.00	30
7876	ADAMS	CLERK	7788	1987-5-23	1100.00		20
7900	JAMES	CLERK	7698	1981-12-3	950.00		30
7902	FORD	ANALYST	7566	1981-12-3	3000.00		20
7934	MILLER	CLERK	7782	1982-1-23	1300.00		10
```

`分别创建部门和员工外部表，并向表中导入数据`

- 创建部门表

```mysql
create external table if not exists default.dept(
deptno int,
dname string,
loc int
)
row format delimited fields terminated by '\t';
```

- 创建员工表

```mysql
create external table if not exists default.emp(
empno int,
ename string,
job string,
mgr int,
hiredate string, 
sal double, 
comm double,
deptno int)
row format delimited fields terminated by '\t';
```

`向外部表中导入数据`

- 部门表导入数据

```mysql
load data local inpath '/usr/BigData/hadoop-2.7.5/testdata/dept.txt' into table default.dept;
```

- 员工表导入数据

```mysql
load data local inpath '/usr/BigData/hadoop-2.7.5/testdata/emp.txt' into table default.emp;
```

`查询结果`

```mysql
select * from emp;
select * from dept;
```

### 8.内部表与外部表的互相转换

##### 8.1 修改内部表student2为外部表

```mysql
alter table student2 set tblproperties('EXTERNAL'='TRUE');
```

##### 8.2 修改外部表student2为内部表

```mysql
alter table student2 set tblproperties('EXTERNAL'='FALSE');
```

**注:('EXTERNAL'='TRUE')和('EXTERNAL'='FALSE')为固定写法，区分大小写！**

### 9.分区表

分区表实际上就是对应一个HDFS文件系统上的独立的文件夹，该文件夹下是该分区所有的数据文件。**Hive中的分区就是分目录，把一个大的数据集根据业务需要分割成小的数据集**。在查询时通过WHERE子句中的表达式选择查询所需要的指定的分区，这样的查询效率会提高很多

##### 9.1 分区表基本操作

`引入分区表(需要根据日期对日志进行管理)`

```shell
/user/hive/warehouse/log_partition/20170702/20170702.log

/user/hive/warehouse/log_partition/20170703/20170703.log

/user/hive/warehouse/log_partition/20170704/20170704.log
```

`创建分区表(根据month分区)`

```mysql
create table dept_partition(
deptno int, dname string, loc string
)
partitioned by (month string)
row format delimited fields terminated by '\t';
```

`加载数据到分区表中`

```mysql
load data local inpath '/usr/BigData/hadoop-2.7.5/testdata/dept.txt' into table default.dept_partition partition(month='201707');

load data local inpath '/usr/BigData/hadoop-2.7.5/testdata/dept.txt' into table default.dept_partition partition(month='201708');

load data local inpath '/usr/BigData/hadoop-2.7.5/testdata/dept.txt' into table default.dept_partition partition(month='201709');
```

- 分区表目录

![005-创建分区表的目录](./images/005-创建分区表的目录.png)

- 分区表数据

![006-创建分区表的数据](./images/006-创建分区表的数据.png)

`查询分区表中数据`

- 单分区查询

```mysql
select * from dept_partition where month='201709';
```

- 多分区联合查询

```mysql
select * from dept_partition where month='201709'
union
select * from dept_partition where month='201708'
union
select * from dept_partition where month='201707';
```

`增加分区`

- 创建单个分区

```mysql
alter table dept_partition add partition(month='201706') ;
```

- 同时创建多个分区

```mysql
alter table dept_partition add partition(month='201705') partition(month='201704');
```

**注:增加多个分区的分区条件用空格隔开**

`删除分区`

- 删除单个分区

```mysql
alter table dept_partition drop partition (month='201704');
```

- 同时删除多个分区

```mysql
alter table dept_partition drop partition (month='201705'), partition (month='201706');
```

**注:删除多个分区的分区条件用逗号隔开**

`查看分区表有多少分区`

```mysql
show partitions dept_partition;
```

`查看分区表结构`

```mysql
desc formatted dept_partition;
```

##### 9.2 分区表注意事项

`创建二级分区表`

```mysql
create table dept_partition2(
    deptno int, dname string, loc string
)
partitioned by (month string, day string)
row format delimited fields terminated by '\t';
```

`加载数据到二级分区表中`

```mysql
load data local inpath '/usr/BigData/hadoop-2.7.5/testdata/dept.txt' into table default.dept_partition2 partition(month='201709', day='13');
```

`查询二级分区数据`

```mysql
select * from dept_partition2 where month='201709' and day='13';
```

`把数据直接上传到分区目录上，让分区表和数据产生关联的三种方式`

- **方式一:上传数据后修复**

  - 上传数据

    ```shell
    hdfs dfs -mkdir -p /user/hive/warehouse/dept_partition2/month=201709/day=12
    
    hdfs dfs -put /usr/BigData/hadoop-2.7.5/testdata/dept.txt /user/hive/warehouse/dept_partition2/month=201709/day=12
    ```

  - 查询数据(查询不到刚上传的数据)

    ```mysql
    select * from dept_partition2 where month='201709' and day='12';
    ```

  - 执行修复命令

    ```mysql
    msck repair table dept_partition2;
    ```

  - 再次查询数据

    ```mysql
    select * from dept_partition2 where month='201709' and day='12';
    ```

- **方式二:上传数据后添加分区**

  - 上传数据

    ```shell
    hdfs dfs -mkdir -p /user/hive/warehouse/dept_partition2/month=201709/day=11;
    
    hdfs dfs -put /usr/BigData/hadoop-2.7.5/testdata/dept.txt /user/hive/warehouse/dept_partition2/month=201709/day=11;
    ```

  - 执行添加分区

    ```mysql
    alter table dept_partition2 add partition(month='201709',day='11');
    ```

  - 查询数据

    ```mysql
    select * from dept_partition2 where month='201709' and day='11';
    ```

- **方式三:创建文件夹后load数据到分区**

  - 创建目录

    ```shell
    hdfs dfs -mkdir -p /user/hive/warehouse/dept_partition2/month=201709/day=10;
    ```

  - 上传数据

    ```mysql
    load data local inpath '/usr/BigData/hadoop-2.7.5/testdata/dept.txt' into table dept_partition2 partition(month='201709',day='10');
    ```

  - 查询数据

    ```mysql
    select * from dept_partition2 where month='201709' and day='10';
    ```

### 10.分桶表

##### 10.1 分区和分桶区别

`分区`

针对的是**数据的存储路径**，提供一个隔离数据和优化查询的便利方式，但并非所有的数据集都可形成合理的分区

`分桶`

针对的是**数据文件**，是将数据集分解成更容易管理的若干部分的另一个技术

##### 10.2 分桶表基本操作

`基本属性设置`

```mysql
#开启分桶功能
set hive.enforce.bucketing=true;

#设置Reduce个数，-1为有多少个桶就有多少Reduce
set mapreduce.job.reduces=-1;
```

`创建分桶表`

```mysql
create table stu_buck(id int, name string)
clustered by(id) 
into 4 buckets
row format delimited fields terminated by '\t';
```

`创建普通中间表`

```mysql
create table stu(id int, name string)
row format delimited fields terminated by '\t';
```

`插入数据到普通中间表`

```mysql
load data local inpath '/usr/BigData/hadoop-2.7.5/testdata/student.txt' into table stu;
```

`插入数据到分桶表(子查询)`

```mysql
insert into table stu_buck
select id, name from stu;
```

**注:直接Load不行，必须创建中间表利用子查询调动MapReduce插入**

查询分桶表数据`

```mysql
select * from stu_buck;
```

**注:发现数据根据id分成四大块**

![007-创建分桶表的数据](D:\BigData-master\5-Hive\images\007-创建分桶表的数据.png)

##### 10.3 分桶抽样查询

- 对于非常大的数据集，有时用户需要使用的是一个具有代表性的查询结果而不是全部结果。Hive可以通过**对表抽样**来满足这个需求。

- 查询表stu_buck中的数据

  ```mysql
  select * from stu_buck tablesample(bucket 1 out of 4 on id);
  ```

- 注:tablesample是抽样语句，语法:TABLESAMPLE(BUCKET x OUT OF y) 

  - y必须是table总bucket数的倍数或者因子。hive根据y的大小，决定抽样的比例。例如，table总共分了4份，当y=2时，抽取(4/2=)2个bucket的数据，当y=8时，抽取(4/8=)1/2个bucket的数据。

  - x表示从哪个bucket开始抽取，如果需要取多个分区，以后的分区号为当前分区号加上y。例如，table总bucket数为4，tablesample(bucket 1 out of 2)，表示总共抽取（4/2=）2个bucket的数据，抽取第1(x)个和第3(x+y)个bucket的数据。
  - **x的值必须小于等于y的值**

### 11.修改表

##### 11.1 重命名表

`语法`

```mysql
ALTER TABLE table_name RENAME TO new_table_name
```

`实操案例`

```mysql
alter table dept_partition2 rename to dept_partition3;
```

##### 11.2 增加/修改/替换列信息

`语法`

```mysql
#增加列
ALTER TABLE table_name CHANGE [COLUMN] col_old_name col_new_name column_type [COMMENT col_comment] [FIRST|AFTER column_name]

#增加和替换列
ALTER TABLE table_name ADD|REPLACE COLUMNS (col_name data_type [COMMENT col_comment], ...) 
```

**注:ADD是代表新增一字段，字段位置在所有列后面(partition列前)，REPLACE则是表示替换表中所有字段**

`实操案例`

- 增加列

  ```mysql
  alter table dept_partition add columns(deptdesc string);
  ```

- 更新列

  ```mysql
  alter table dept_partition change column deptdesc desc int;
  ```

- 替换列

  ```mysql
  alter table dept_partition replace columns(deptno string, dname string, loc string);
  ```

**注:**

- 替换列如果是多列替换成一列，查询时只显示第一列
- 替换列如果是一列替换成多列，查询时不存在的列就为空

### 12.删除表

```mysql
drop table dept_partition;
```

<br>

# 三、Hive的DML操作

### 1.数据导入

##### 1.1 向表中装载数据(Load)

`语法`

```mysql
load data [local] inpath '/opt/module/datas/student.txt' overwrite | into table student [partition (partcol1=val1,…)];
```

`参数`

- load data:表示加载数据
- local:表示从本地加载数据到hive表；否则从HDFS加载数据到hive表
- inpath:表示加载数据的路径
- overwrite:表示覆盖表中已有数据，否则表示追加
- into table:表示加载到哪张表
- student:表示具体的表
- partition:表示上传到指定分区

`创建一张表`

```mysql
create table student(id string, name string) row format delimited fields terminated by '\t';
```

`1.1.1 加载本地文件到hive`

```mysql
load data local inpath '/usr/BigData/hadoop-2.7.5/testdata/student.txt' into table default.student;
```

`1.1.2 加载HDFS文件到hive中`

- 上传文件到HDFS

  ```mysql
  dfs -put /usr/BigData/hadoop-2.7.5/testdata/student.txt /user;
  ```

- 加载HDFS上数据

  ```mysql
  load data inpath '/user/student.txt' into table default.student;
  ```

`1.1.3 加载数据覆盖表中已有的数据`

```mysql
load data inpath '/user/student.txt' overwrite into table default.student;
```

##### 1.2 通过查询语句向表中插入数据(Insert)

`创建一张分区表`

```mysql
create table student(id int, name string) partitioned by (month string) row format delimited fields terminated by '\t';
```

`1.2.1 基本插入数据`

```mysql
insert into table student partition(month='201709') values(1,'wangwu');
```

`1.2.2 基本模式插入(根据单张表查询结果)`

```mysql
insert overwrite table student partition(month='201708')
select id, name from student where month='201709';
```

`1.2.3 多插入模式(根据多张表查询结果)`

```mysql
from student
insert overwrite table student partition(month='201707')
select id, name where month='201709'
insert overwrite table student partition(month='201706')
select id, name where month='201709';
```

##### 1.3 查询语句中创建表并加载数据(As Select)

```mysql
create table if not exists student3
as select id, name from student;
```

##### 1.4 创建表时通过Location指定加载数据路径

`1.4.1 创建表并指定在hdfs上的位置`

```mysql
create table if not exists student5(
id int, name string
)
row format delimited fields terminated by '\t'
location '/user/hive/warehouse/student5';
```

`1.4.2 上传数据到hdfs上`

```mysql
dfs -put /opt/module/datas/student.txt /user/hive/warehouse/student5;
```

##### 1.5 Import数据到指定Hive表中

```mysql
import table student2 partition(month='201709') from
'/user/hive/warehouse/export/student';
```

**注:先用export导出后，再将数据导入**

### 2.数据导出

##### 2.1 Insert导出

`将查询的结果导出到本地`

```mysql
insert overwrite local directory '/usr/BigData/hadoop-2.7.5/testdata/student'
select * from student;
```

`将查询的结果格式化导出到本地`

```mysql
insert overwrite local directory '/usr/BigData/hadoop-2.7.5/testdata/student1'
ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t'
select * from student;
```

`将查询的结果格式化导出到HDFS上(没有local)`

```mysql
insert overwrite directory '/user'
ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t'
select * from student;
```

##### 2.2 Hadoop命令导出到本地

```mysql
dfs -get /user/hive/warehouse/student/month=201709/000000_0
/usr/BigData/hadoop-2.7.5/testdata/student3.txt;
```

##### 2.3 Hive Shell 命令导出

`基本语法`

```shell
hive -f/-e 执行语句或者脚本 > file
```

`实操案例(hive目录下)`

```shell
bin/hive -e 'select * from default.student;' >
/usr/BigData/hadoop-2.7.5/testdata/student4.txt;
```

##### 2.4 Export导出到HDFS上

```mysql
export table default.student to
'/user/hive/warehouse/export/student';
```

##### 2.5 Sqoop导出

后面再说

### 3 清除表中数据(Truncate)

```mysql
truncate table student;
```

**注:Truncate只能删除管理表，不能删除外部表中数据**