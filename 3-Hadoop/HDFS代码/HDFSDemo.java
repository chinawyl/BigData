import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class HDFSDemo {
    /*
    一、url访问
     */
    @Test
    public void urlHDFS() throws IOException {
        //1:注册url
        URL.setURLStreamHandlerFactory(new FsUrlStreamHandlerFactory());
        //2:获取hdfs文件的输入流
        InputStream inputStream = new URL("hdfs://node01:8020/a.txt").openStream();

        //3:获取本地文件的输出流
        FileOutputStream outputStream = new FileOutputStream(new File("D:\\urlHDFS.txt"));

        //4:实现文件的拷贝
        IOUtils.copy(inputStream, outputStream);

        //5:关闭流
        IOUtils.closeQuietly(inputStream);
        IOUtils.closeQuietly(outputStream);
    }

    /*
    二、FileSystem访问
     */
    //获取FileSystem:方式1
    @Test
    public void getFileSystem1() throws IOException {
        //创建Configuration对象
        Configuration configuration = new Configuration();

        //设置文件系统的类型
        configuration.set("fs.defaultFS", "hdfs://node01:8020");

        //获取指定的文件系统
        FileSystem fileSystem = FileSystem.get(configuration);

        //输出
        System.out.println(fileSystem);
    }

    //获取FileSystem:方式2
    @Test
    public void getFileSystem2() throws URISyntaxException, IOException {
        //获取FileSystem对象
        FileSystem fileSystem = FileSystem.get(new URI("hdfs://node01:8020"), new Configuration());

        //输出
        System.out.println(fileSystem);
    }

    //获取FileSystem:方式3
    @Test
    public void getFileSystem3() throws IOException {
        //创建Configuration对象
        Configuration configuration = new Configuration();

        //指定文件系统类型
        configuration.set("fs.defaultFS", "hdfs://node01:8020");

        //获取指定的文件系统
        FileSystem fileSystem = FileSystem.newInstance(configuration);

        //输出
        System.out.println(fileSystem);
    }

    //获取FileSystem:方式4
    @Test
    public void getFileSystem4() throws URISyntaxException, IOException {
        //获取FileSystem对象
        FileSystem fileSystem = FileSystem.newInstance(new URI("hdfs://node01:8020"), new Configuration());

        //输出
        System.out.println(fileSystem);
    }

    /*
    三、遍历所有文件
     */
    @Test
    public void listFiles() throws URISyntaxException, IOException {
        //1:获取FileSystem实例
        FileSystem fileSystem = FileSystem.get(new URI("hdfs://node01:8020"), new Configuration());

        //2:调用方法listFiles 获取 /目录下所有的文件信息
        RemoteIterator<LocatedFileStatus> iterator = fileSystem.listFiles(new Path("/"), true);

        //3:遍历迭代器
        while (iterator.hasNext()){
            LocatedFileStatus fileStatus = iterator.next();

            //获取文件的绝对路径 : hdfs://node01:8020/xxx
            System.out.println(fileStatus.getPath() + "----" +fileStatus.getPath().getName());

            //文件的block信息
            BlockLocation[] blockLocations = fileStatus.getBlockLocations();
            System.out.println("block数:"+blockLocations.length);
        }
    }

    /*
    四、创建文件夹与文件
     */
    @Test
    public void mkdirsTest() throws URISyntaxException, IOException {
        //1:获取FileSystem实例
        FileSystem fileSystem = FileSystem.get(new URI("hdfs://node01:8020"), new Configuration());

        //2:创建文件夹
        boolean bl = fileSystem.mkdirs(new Path("/aaa/bbb/ccc")); //递归创建文件夹
        fileSystem.create(new Path("/aaa2/bbb/ccc/test.txt")); //递归创建文件
        System.out.println(bl);

        //3:关闭流
        fileSystem.close();
    }

    /*
    五、文件上传
     */
    @Test
    public void uploadFile() throws URISyntaxException, IOException {
        //1:获取FileSystem
        FileSystem fileSystem = FileSystem.get(new URI("hdfs://node01:8020"), new Configuration());

        //2:调用方法,实现上传
        fileSystem.copyFromLocalFile(new Path("D:\\apache-maven-3.6.3\\README.txt"), new Path("/"));

        //3:关闭FileSystem
        fileSystem.close();
    }

    /*
    六、文件下载
     */
    //文件下载:方式1
    @Test
    public void downloadFile1() throws URISyntaxException, IOException {
        //1:获取FileSystem
        FileSystem fileSystem = FileSystem.get(new URI("hdfs://node01:8020"), new Configuration());

        //2:获取hdfs的输入流
        FSDataInputStream inputStream = fileSystem.open(new Path("/README.txt"));

        //3:获取本地路径的输出流
        FileOutputStream outputStream = new FileOutputStream("D://README1.txt");

        //4:文件的拷贝
        IOUtils.copy(inputStream, outputStream);

        //5:关闭流
        IOUtils.closeQuietly(inputStream);
        IOUtils.closeQuietly(outputStream);
        fileSystem.close();
    }

    //文件下载:方式2
    @Test
    public void downloadFile2() throws URISyntaxException, IOException {
        //1:获取FileSystem
        FileSystem fileSystem = FileSystem.get(new URI("hdfs://node01:8020"), new Configuration());

        //2:调用方法,实现文件的下载
        fileSystem.copyToLocalFile(new Path("/README.txt"), new Path("D://README2.txt"));

        //3:关闭流
        fileSystem.close();
    }

    /*
    七、HDFS的权限访问控制
     */
    @Test
    public void accessRight() throws URISyntaxException, IOException, InterruptedException {
        //1:获取FileSystem(伪造用户)
        FileSystem fileSystem = FileSystem.get(new URI("hdfs://node01:8020"), new Configuration(),"root");

        //2:调用方法,实现文件的下载
        fileSystem.copyToLocalFile(new Path("/README.txt"), new Path("D://accessRight.txt"));

        //3:关闭流
        fileSystem.close();
    }

    /*
    八、小文件合并
     */
    @Test
    public void mergeFile() throws URISyntaxException, IOException, InterruptedException {
        //1:获取FileSystem(分布式文件系统)
        FileSystem fileSystem = FileSystem.get(new URI("hdfs://node01:8020"), new Configuration(),"root");

        //2:获取hdfs大文件的输出流
        FSDataOutputStream outputStream = fileSystem.create(new Path("/Big.txt"));

        //3:获取一个本地文件系统
        LocalFileSystem localFileSystem = FileSystem.getLocal(new Configuration());

        //4:获取本地文件夹下所有文件的详情
        FileStatus[] fileStatuses = localFileSystem.listStatus(new Path("D:\\TXT"));

        //5:遍历每个文件，获取每个文件的输入流
        for (FileStatus fileStatus : fileStatuses) {
            FSDataInputStream inputStream = localFileSystem.open(fileStatus.getPath());

            //6:将小文件的数据复制到大文件
            IOUtils.copy(inputStream, outputStream);
            IOUtils.closeQuietly(inputStream);
        }

        //7:关闭流
        IOUtils.closeQuietly(outputStream);
        localFileSystem.close();
        fileSystem.close();
    }

}
