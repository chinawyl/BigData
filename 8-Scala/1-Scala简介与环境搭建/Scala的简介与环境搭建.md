# 一、简介

### 1.概念

Scala 是 Scalable Language 的简写，是一门多范式的编程语言 

### 2.特性

##### 2.1 面向对象特性

Scala是一种纯面向对象的语言，每个值都是对象。对象的数据类型以及行为由类和特质描述

类抽象机制的扩展有两种途径：一种途径是子类继承，另一种途径是灵活的混入机制。这两种途径能避免多重继承的种种问题

##### 2.2 函数式编程

Scala也是一种函数式语言，其函数也能当成值来使用。Scala提供了轻量级的语法用以定义匿名函数，支持高阶函数，允许嵌套多层函数，并支持柯里化。Scala的case class及其内置的模式匹配相当于函数式编程语言中常用的代数类型

更进一步，程序员可以利用Scala的模式匹配，编写类似正则表达式的代码处理XML数据

##### 2.3 静态类型

Scala具备类型系统，通过编译时检查，保证代码的安全性和一致性

##### 2.4 扩展性

Scala的设计秉承一项事实，即在实践中，某个领域特定的应用程序开发往往需要特定于该领域的语言扩展。Scala提供了许多独特的语言机制，可以以库的形式轻易无缝添加新的语言结构:

- 任何方法可用作前缀或后缀操作符
- 可以根据预期类型自动构造闭包

##### 2.5 并发性

Scala使用Actor作为其并发模型，Actor是类似线程的实体，通过邮箱发收消息。Actor可以复用线程，因此可以在程序中可以使用数百万个Actor,而线程只能创建数千个。在2.10之后的版本中，使用Akka作为其默认Actor实现

<br>

# 二、环境搭建(Windows10)

### 1.JDK安装(默认1.8比较好)

`下载网址`

https://www.oracle.com/java/technologies/javase-downloads.html

`安装教程`

https://www.runoob.com/java/java-environment-setup.html

**注:不需要配Classpath**

### 2.Scala安装(2.12)

##### 2.1下载对应版本

https://www.scala-lang.org/download/

##### 2.2 配置环境变量

https://www.bilibili.com/video/BV15t411H776?p=5

**注:类似于jdk**

### 3.搭建IDEA开发Scala环境(2.12)

##### 3.1下载Scala插件

https://plugins.jetbrains.com/plugin/1347-scala

**注:使用IDEA对应版本(2020.2.2)**

##### 3.2 移动压缩包

在**2**中scala安装主目录位置创建**plugins**文件夹并将插件压缩包移入其中

##### 3.3 IDEA配置插件

在idea的**File->Settings->Plugins->installed**里搜索scala(显示未找到)，然后点击设置图标里的**install plugin from disk**，最后找到**3.2**中插件安装位置点击ok即可

![001-IDEA配置插件](D:\BigData\8-Scala\1-Scala简介与环境搭建\images\001-IDEA配置插件.png)

##### 3.4 导入Scala框架

点击**Add Frameworks Support**，找到**scala**加入即可

**注:第一次需要配置sdk,路径为scala安装主目录**

##### 3.5 教程

https://www.bilibili.com/video/BV15t411H776?p=9

### 4.测试IDEA安装Scala环境

##### 1.新建Scala文件夹

在新建项目的**src下的main**文件夹下**新建scala文件夹**并右键点击**add framework support...**

##### 2.新建Test测试文件

右键新建scala文件，点击object类型

##### 3.测试代码

```scala
object Test {
  def main(args: Array[String]): Unit = {
    println("Hello Scala Idea")
  }
}
```

**注:输入main智能提示**