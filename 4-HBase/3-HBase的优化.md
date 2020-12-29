# 一、HBase高可用

### 1.HBase高可用简介

在当前的HBase集群中，只有一个Master，一旦Master出现故障，将会导致HBase不再可用。**HBase的高可用配置其实就是HMaster的高可用**。在HBase中HMaster负责监控HRegionServer的生命周期，均衡 RegionServer的负载， 如果HMaster 挂掉了，那么整个HBase集群将陷入不健康的状态，并且此时的工作状态并 不会维持太久

### 2.HBase高可用搭建

##### 2.1 关闭HBase集群(如果没有开启则跳过此步)

```shell
cd /usr/BigData/hbase-2.1.0/bin

./stop-hbase.sh 
```

##### 2.2 创建backup-masters文件  

```shell
cd /usr/BigData/hbase-2.1.0/conf

touch backup-masters
```

##### 2.3 配置高可用HMaster节点 

```shell
vim backup-masters

#添加节点
node02
node03
```

##### 2.4 分发backup-masters到所有的服务器节点中

```shell
scp -r backup-masters node02:/usr/BigData/hbase-2.1.0/conf
scp -r backup-masters node03:/usr/BigData/hbase-2.1.0/conf
```

##### 2.5 重启HBase集群

```shell
cd ../bin

./start-hbase.sh
```

##### 2.6 查看webui检查Backup Masters

http://node1:16010/master-status

**注:node01为Master而node02、node03为Backup Master**

##### 2.7 杀掉node01节点Master

```shell
jps #查看进程id

kill -9 HMaste进程id
```

##### 2.8 查看新Master

访问http://node2:16010和http://node3:16010观察是否选举了新的Master  

<br>

# 二、HBase预分区

### 1.HBase预分区简介

每一个region维护着StartRow与EndRow，如果加入的数据符合某个Region维护的RowKey范围，则该数据交给这个Region维护。那么依照这个原则，我们可以将数据所要投放的分区提前大致的规划好，以提高HBase性能

### 2.HBase预分区设定

##### 2.1 手动设定预分区

```sql
create 'staff1','info','partition1',SPLITS => ['1000','2000','3000','4000']
```

##### 2.2 生成16进制序列预分区

```sql
create 'staff2','info','partition2',{NUMREGIONS => 15, SPLITALGO => 'HexStringSplit'}
```

##### 2.3 按照文件中设置的规则预分区

`创建splits.txt文件`

```shell
cd /usr/BigData/hbase-2.1.0

vim splits.txt
```

`添加以下内容`

```shell
aaaa
bbbb
cccc
dddd 
```

`根据文件设置分区`

```sql
create 'staff3','info','partition3',SPLITS_FILE => 'splits.txt' --在家目录创建文件为相对路径
```

##### 2.4 使用JavaAPI创建预分区 

```java

```

<br>

# 三、HBase的RowKey设计 

### 1.HBase的RowKey设计简介

一条数据的唯一标识就是RowKey，那么这条数据存储于哪个分区，取决于RowKey处于哪个一个预分区的区间内，设计RowKey的主要目的 ，就是让数据均匀的分布于所有的region中，在一定程度上防止数据倾斜

### 2.HBase的RowKey设定规则

##### 2.1 生成随机数、hash、散列值 

```shell
比如:
原本rowKey为1001的，SHA1后变成:dd01903921ea24941c26a48f2cec24e0bb0e8cc7
原本rowKey为3001的，SHA1后变成:49042c54de64a1e9bf0b33e00245660ef92dc7bd
原本rowKey为5001的，SHA1后变成:7b61dec07e02c188790670af43e717f0f46e8913
在做此操作之前，一般我们会选择从数据集中抽取样本，来决定什么样的rowKey来Hash后作为每个分区的临界值
```

##### 2.2 字符串反转

```shell
20170524000001 转成 10000042507102
20170524000002 转成 20000042507102
```

##### 2.3 字符串拼接

```shell
20170524000001_a12e
20170524000001_93i7
```

<br>

# 四、其他优化

### 1.内存优化

HBase操作过程中需要大量的内存开销，因为Table是可以缓存在内存中的，一般会**分配整个可用内存的70%**给其HBase的Java堆。但是不建议分配非常大的堆内存，因为G**C过程持续太久会导致RegionServer处于长期不可用状态**，一般16~48G内存就可以了，如果因为框架占用内存过高导致系统内存不足，框架很容易被系统服务拖死 

### 2.基础优化

##### 2.1 允许在HDFS的文件中追加内容

`hdfs-site.xml、hbase-site.xml`

- 属性：dfs.support.append

- 解释：开启 HDFS 追加同步，可以优秀的配合HBase的数据同步和持久化，**默认值为true**

##### 2.2 优化DataNode允许的最大文件打开数

`hdfs-site.xml`

- 属性：dfs.datanode.max.transfer.threads

- 解释：HBase一般都会同一时间操作大量的文件，根据集群的数量和规模以及数据动作， 设置为4096或者更高，**默认值为4096**

##### 2.3 优化延迟高的数据操作的等待时间

`hdfs-site.xml`

- 属性：dfs.image.transfer.timeout

- 解释：如果对于某一次数据操作来讲，延迟非常高，socket需要等待更长的时间，建议把该值设置为更大的值(**默认为60000毫秒**)，以确保socket不会被timeout掉 

##### 2.4 优化数据的写入效率

`mapred-site.xml`

- 属性：mapreduce.map.output.compress mapreduce.map.output.compress.codec

- 解释：开启这两个数据可以大大提高文件的写入效率，减少写入时间。第一个属性值修改为**true**，第二个属性值修改为**org.apache.hadoop.io.compress.GzipCodec**或者其他压缩方式

##### 2.5 设置RPC监听数量

`hbase-site.xml`

- 属性：Hbase.regionserver.handler.count

- 解释：**默认值为30**，用于指定RPC监听的数量，可以根据客户端的请求数进行调整，读写请求较多时，增加它

##### 2.6 优化HStore文件大小

`hbase-site.xml`

- 属性：hbase.hregion.max.filesize

- 解释：**默认值为10737418240(10GB)**，如果需要运行HBase的MR任务，可以减小此值， 因为一个region对应一个map任务，**如果单个region过大，会导致map任务执行时间过长**。该值的意思就是，如果HFile的大小达到这个数值，则这个region会被切分为两个Hfile

##### 2.7 优化HBase客户端缓存

`hbase-site.xml`

- 属性：hbase.client.write.buffer

- 解释：用于指定Hbase客户端缓存，增大该值可以减少RPC调用次数，但是会消耗更多内存，反之则相反，一般我们需要**设定一定的缓存大小，以达到减少RPC次数的目的**

##### 2.8 指定scan.next扫描HBase所获取的行数

`hbase-site.xml`

- 属性：hbase.client.scanner.caching

- 解释：用于指定scan.next方法获取的默认行数，**值越大，消耗内存越大**

##### 2.9 flush、compact、split机制

当MemStore达到阈值，将Memstore中的数据Flush进Storefile；compact机制则是把flush出来的小文件合并成大的Storefile文件。split则是当Region达到阈值，会把过大的Region一分为二
`Memstore`

- 属性：hbase.hregion.memstore.flush.size: 134217728

- 解释：**默认阈值为128M**，这个参数的作用是当单个HRegion内所有的Memstore大小总和超过指定值时，flush该 HRegion 的所有 memstore。RegionServer 的 flush 是通过将请求添加一个队列，模拟生产消费模型来异步处理的。那这里就有一个问题，当队列来不及消费，产生大量积压请求时，可能会导致内存陡增，最坏的情况是触发OOM

  - **hbase.regionserver.global.memstore.upperLimit：0.4**

  - **hbase.regionserver.global.memstore.lowerLimit：0.38**

  即：当MemStore使用内存总量达到hbase.regionserver.global.memstore.upperLimit指定值时，将会有多个 MemStores flush到文件中，MemStore flush顺序是按照大小降序执行的，直到刷新到MemStore使用内存略小于lowerLimit