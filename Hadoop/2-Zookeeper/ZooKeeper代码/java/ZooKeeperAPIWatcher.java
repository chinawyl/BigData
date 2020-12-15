import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.Test;

public class ZooKeeperAPIWatcher {
    /*
    节点的watch机制
     */

    @Test
    public void watchZnode() throws Exception {
        //1.定制重试策略
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,1);

        //2.获取客户端
        String conectionStr = "192.168.146.121:2181,192.168.146.122:2181,192.168.146.123:2181";
        CuratorFramework client = CuratorFrameworkFactory.newClient(conectionStr,8000,8000,retryPolicy);

        //3.启动客户端
        client.start();

        //4.创建TreeCache对象，指定要监控的节点路径
        TreeCache treeCache = new TreeCache(client,"/Java_Hello");

        //5.自定义一个监听器
        treeCache.getListenable().addListener(new TreeCacheListener() {
            @Override
            public void childEvent(CuratorFramework curatorFramework, TreeCacheEvent treeCacheEvent) throws Exception {

                ChildData data = treeCacheEvent.getData();
                if (data != null) {
                    switch (treeCacheEvent.getType()) {
                        case NODE_ADDED:
                            System.out.println("监控到有新增节点");
                            break;
                        case NODE_REMOVED:
                            System.out.println("监控到有节点被移除");
                            break;
                        case NODE_UPDATED:
                            System.out.println("监控到有节点更新");
                            break;
                        default:
                            break;
                    }
                }
            }
        });

        //6.开始监听
        treeCache.start();
        Thread.sleep(200000);
    }
}
