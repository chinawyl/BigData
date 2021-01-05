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

### 4.安装Mysql

##### 4.1 检查Mysql安装情况

`删除rpm安装的Mysql`

```shell
rpm -qa|grep mysql

rpm -e --nodeps 上条命令查询的名字
```

`删除Mysql相关目录`

```shell
find / -name mysql

rm -rf 上条命令显示的路径
```

`删除Mysql配置文件`

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

##### 4.3 安装Mysql服务端

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

##### 4.4 安装MySql客户端

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

##### 4.5 MySql中user表中主机配置