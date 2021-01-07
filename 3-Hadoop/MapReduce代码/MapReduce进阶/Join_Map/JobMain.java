package Join_Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.net.URI;

public class JobMain extends Configured implements Tool{
    @Override
    public int run(String[] args) throws Exception {
        //1:获取job对象
        Job job = Job.getInstance(super.getConf(), "join_map");
        //如果打包运行出错,则需要加该配置
        job.setJarByClass(JobMain.class);

        //2:设置job对象(将小表放在分布式缓存中)
            //将小表放在分布式缓存中
            // DistributedCache.addCacheFile(new URI("hdfs://node01:8020/input/join_map_input/product.txt"), super.getConf());
            job.addCacheFile(new URI("hdfs://node01:8020/cache_file/product.txt"));

            //第一步:设置输入类和输入的路径
            job.setInputFormatClass(TextInputFormat.class);
            TextInputFormat.addInputPath(job, new Path("hdfs://node01:8020/input/join_map_input"));
            //TextInputFormat.addInputPath(job, new Path("file:///D:\\input\\join_map_input"));

            //第二步:设置Mapper类和数据类型
            job.setMapperClass(MapJoinMapper.class);
            job.setMapOutputKeyClass(Text.class);
            job.setMapOutputValueClass(Text.class);

            //第八步:设置输出类和输出路径
            job.setOutputFormatClass(TextOutputFormat.class);
            TextOutputFormat.setOutputPath(job, new Path("hdfs://node01:8020/output/join_map_output"));
            //TextOutputFormat.setOutputPath(job, new Path("file:///D:\\output\\join_map_output"));

        //3:等待任务结束
        boolean bl = job.waitForCompletion(true);
        return bl ? 0 :1;
    }

    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();

        //启动job任务
        int run = ToolRunner.run(configuration, new JobMain(), args);
        System.exit(run);
    }
}
