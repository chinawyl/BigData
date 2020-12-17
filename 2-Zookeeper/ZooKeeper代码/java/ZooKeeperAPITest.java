import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.junit.Test;

public class ZooKeeperAPITest {
    //一、创造永久性节点
    @Test
    public void createzonde_PERSISTENT() throws Exception {
        //1.定制一个重试策略
        /*
            param1: 重试的时间间隔
            param2: 重试的最大次数
         */
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,1);

        //2.获取一个客户端对象
        /*
            param1:要连接的ZooKeeper服务器列表
            param2:会话的超时时间
            param3:链接超时时间
            param4:重试策略
         */
        String connectionStr = "192.168.146.121:2181,192.168.146.122:2181,192.168.146.123:2181";
        CuratorFramework client = CuratorFrameworkFactory.newClient(connectionStr, 8000, 8000, retryPolicy);

        //3.开启客户端
        client.start();

        //4.创建节点
        client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/Java_Hello", "world".getBytes());

        //5.关闭客户端
        client.close();
    }

    //二、创造临时性节点
    @Test
    public void createzonde_EPHEMERAL() throws Exception {
        //1.定制一个重试策略
        /*
            param1: 重试的时间间隔
            param2: 重试的最大次数
         */
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,1);

        //2.获取一个客户端对象
        /*
            param1:要连接的ZooKeeper服务器列表
            param2:会话的超时时间
            param3:链接超时时间
            param4:重试策略
         */
        String connectionStr = "192.168.146.121:2181,192.168.146.122:2181,192.168.146.123:2181";
        CuratorFramework client = CuratorFrameworkFactory.newClient(connectionStr, 8000, 8000, retryPolicy);

        //3.开启客户端
        client.start();

        //4.创建节点
        client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/Java_Hello2", "world2".getBytes());

        Thread.sleep(10000); //延时

        //5.关闭客户端
        client.close();
    }

    //三、设置节点数据
    @Test
    public void setZondeData() throws Exception {
        //1.定制一个重试策略
        /*
            param1: 重试的时间间隔
            param2: 重试的最大次数
         */
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,1);

        //2.获取一个客户端对象
        /*
            param1:要连接的ZooKeeper服务器列表
            param2:会话的超时时间
            param3:链接超时时间
            param4:重试策略
         */
        String connectionStr = "192.168.146.121:2181,192.168.146.122:2181,192.168.146.123:2181";
        CuratorFramework client = CuratorFrameworkFactory.newClient(connectionStr, 8000, 8000, retryPolicy);

        //3.开启客户端
        client.start();

        //4.创建节点
        client.setData().forPath("/Java_Hello", "zookeeper".getBytes());

        //5.关闭客户端
        client.close();
    }

    //四、获取节点数据
    @Test
    public void getZondeData() throws Exception {
        //1.定制一个重试策略
        /*
            param1: 重试的时间间隔
            param2: 重试的最大次数
         */
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,1);

        //2.获取一个客户端对象
        /*
            param1:要连接的ZooKeeper服务器列表
            param2:会话的超时时间
            param3:链接超时时间
            param4:重试策略
         */
        String connectionStr = "192.168.146.121:2181,192.168.146.122:2181,192.168.146.123:2181";
        CuratorFramework client = CuratorFrameworkFactory.newClient(connectionStr, 8000, 8000, retryPolicy);

        //3.开启客户端
        client.start();

        //4.创建节点
        byte [] bytes = client.getData().forPath("/Java_Hello");
        System.out.println(new String(bytes));

        //5.关闭客户端
        client.close();
    }
}
