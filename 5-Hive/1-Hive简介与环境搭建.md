# 一、Hive简介



<br>

# 二、Hive环境搭建

### 1.Hive安装地址

##### 1.1 Hive官网地址

http://hive.apache.org

##### 1.2 文档查看地址

https://cwiki.apache.org/confluence/display/Hive/GettingStarted

##### 1.3 下载地址

http://archive.apache.org/dist/hive/

##### 1.4 github地址

https://github.com/apache/hive

### 2.上传Hive压缩包并解压(node01)

```shell
tar -zxvf apache-hive-1.2.1-bin.tar.gz -C /usr/BigData
```

### 3.修改Hive文件目录名

```shell
cd /usr/BigData

mv apache-hive-1.2.1-bin hive-1.2.1
```

### 4.安装配置Mysql

##### 4.1 检查Mysql安装情况

`删除rpm安装的mysql`

```shell
rpm -qa|grep mysql

rpm -e --nodeps 上条命令查询的名字
```

`删除mysql相关目录`

```shell
find / -name mysql

rm -rf 上条命令显示的路径
```

`删除mysql配置文件`

```shell
rm /etc/my.cnf
```

`删除系统自带的Mariadb`

```shell
rpm -qa|grep mariadb |xargs yum remove -y
```

##### 4.2 解压Mysql压缩包

`安装zip`

```shell
yum -y install zip unzip
```

`解压mysql-libs.zip`

```shell
unzip mysql-libs.zip
```

`查看解压文件内容`

```shell
MySQL-client-5.6.24-1.el6.x86_64.rpm
mysql-connector-java-5.1.27.tar.gz
MySQL-server-5.6.24-1.el6.x86_64.rpm
```

##### 4.3 Hive元数据配置到MySql

`解压驱动包`

```shell
tar -zxvf mysql-connector-java-5.1.27.tar.gz
```

`移动驱动文件`

```shell
cp mysql-connector-java-5.1.27-bin.jar /usr/BigData/hive-1.2.1/lib
```

##### 4.4 安装Mysql服务端

`安装mysql服务端`

```shell
rpm -ivh MySQL-server-5.6.24-1.el6.x86_64.rpm
```

`查看产生的随机密码`

```shell
cat /root/.mysql_secret

_wyRR_HS9QXpbkuU
```

`查看mysql状态`

```shell
service mysql status
```

`启动mysql`

```shell
service mysql start
```

##### 4.5 安装MySql客户端

```shell
rpm -ivh MySQL-client-5.6.24-1.el6.x86_64.rpm
```

`链接mysql`

```shell
mysql -uroot -p_wyRR_HS9QXpbkuU
```

`修改密码`

```mysql
SET PASSWORD=PASSWORD('123456');
```

`退出mysql`

```mysql
exit;
```

##### 4.6 MySql中user表中主机配置

**配置只要是root用户+密码，在任何主机上都能登录MySQL数据库**

`进入mysql`

```shell
mysql -uroot -p123456
```

`显示数据库`

```mysql
show databases;
```

`使用mysql数据库`

```mysql
use mysql;
```

`展示mysql数据库中的所有表`

```mysql
show tables;
```

`展示user表的结构`

```mysql
desc user;
```

`查询user表`

```mysql
select User, Host, Password from user;
```

`修改user表，把Host表内容修改为%`

```mysql
update user set host='%' where host='localhost';
```

`删除root用户的其他host`

```mysql
delete from user where Host='node01';
delete from user where Host='127.0.0.1';
delete from user where Host='::1';
```

`刷新mysql`

```mysql
flush privileges;
```

`退出mysql`

```mysql
quit;
```

**注:mysql有三种退出方式**

```mysql
exit;
quit;
\q;
```

### 5.修改Hive配置文件

##### 5.1 进入Hive配置文件目录

```shell
cd /usr/BigData/hive-1.2.1/conf
```

##### 5.2 修改hive-site.xml文件

`新建hive-site.xml`

```shell
vim hive-site.xml
```

`配置hive-site.xml`

```xml
<configuration>
	<property>
	  <name>javax.jdo.option.ConnectionURL</name>
	  <value>jdbc:mysql://node01:3306/hive?createDatabaseIfNotExist=true</value>
	  <description>JDBC connect string for a JDBC metastore</description>
	</property>

	<property>
	  <name>javax.jdo.option.ConnectionDriverName</name>
	  <value>com.mysql.jdbc.Driver</value>
	  <description>Driver class name for a JDBC metastore</description>
	</property>

	<property>
	  <name>javax.jdo.option.ConnectionUserName</name>
	  <value>root</value>
	  <description>username to use against metastore database</description>
	</property>

	<property>
	  <name>javax.jdo.option.ConnectionPassword</name>
	  <value>123456</value>
	  <description>password to use against metastore database</description>
	</property>
    
	<property>
		<name>hive.cli.print.header</name>
		<value>true</value>
	</property>

	<property>
		<name>hive.cli.print.current.db</name>
		<value>true</value>
	</property>
</configuration>
```

##### 5.3 修改hive-env.sh文件

`重命名hive-env.sh.template`

```shell
mv hive-env.sh.template hive-env.sh
```

`配置hive-env.sh`

```sh
export HADOOP_HOME=/usr/BigData/hadoop-2.7.5 #配置HADOOP_HOME路径

export HIVE_CONF_DIR=/usr/BigData/hive-1.2.1/conf #配置HIVE_CONF_DIR路径
```

##### 5.4 修改hive-log4j.properties文件

`重命名hive-log4j.properties.template`

```shell
mv hive-log4j.properties.template hive-log4j.properties
```

`配置hive-log4j.properties`

```properties
hive.log.dir=/usr/BigData/hive-1.2.1/logs
```

**注:Hive的log默认存放在/tmp/root/hive.log目录下(当前用户名下)**

### 6.Hive启动

##### 6.1 启动zookeeper(三台主机)

```shell
#进入目录
cd /usr/BigData/zookeeper-3.4.9/bin

#启动服务
./zkServer.sh start

#查看进程(只有Jps、QuorumPeerMain)
jps
```

##### 6.2 进入hadoop目录(node01)

```shell
cd /usr/BigData/hadoop-2.7.5
```

##### 6.3 启动hdfs(node01)

```shell
sbin/start-dfs.sh
```

**注:使用jps查看进程**

- node01

  **Jps**、**QuorumPeerMain** 、**NameNode**、**DataNode**、**SecondaryNameNode**

- node02

  **Jps**、**QuorumPeerMain** 、**DataNode**

- node03

  **Jps**、**QuorumPeerMain** 、**DataNode**

##### 6.4 启动yarn(node01)

```shell
sbin/start-yarn.sh
```

**注:使用jps查看进程**

- node01

  **Jps**、**QuorumPeerMain** 、**NameNode**、**DataNode**、**SecondaryNameNode**、**NodeManager** 、**ResourceManager** 

- node02

  **Jps**、**QuorumPeerMain** 、**DataNode**、 **NodeManager** 

- node03

  **Jps**、**QuorumPeerMain** 、**DataNode**、 **NodeManager** 

##### 6.5 启动历史任务记录(node01)

```shell
sbin/mr-jobhistory-daemon.sh start historyserver
```

**注:使用jps查看进程**

- node01

  **Jps**、**QuorumPeerMain** 、**NameNode**、**DataNode**、**SecondaryNameNode**、**NodeManager** 、**ResourceManager** 、 **JobHistoryServer** 

- node02

  **Jps**、**QuorumPeerMain** 、**DataNode**、 **NodeManager** 

- node03

  **Jps**、**QuorumPeerMain** 、**DataNode**、 **NodeManager** 

##### 6.6 关闭HDFS安全模式(node01)

```shell
bin/hadoop dfsadmin -safemode leave
```

##### 6.7 启动Hive

```shell
cd /usr/BigData/hive-1.2.1

bin/hive
```

##### 6.8 常见问题及解决方法

`无法启动多个客户端`

Metastore默认存储在自带的**derby**数据库中，需要使用MySQL存储Metastore，**按照4，5步骤执行**

`修改配置信息后没有生效`

**重新启动hive**