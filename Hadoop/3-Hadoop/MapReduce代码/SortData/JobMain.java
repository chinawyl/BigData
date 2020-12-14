package SortData;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.net.URI;

public class JobMain extends Configured implements Tool {
    @Override
    public int run(String[] args) throws Exception {
        /*
        1:创建job对象
         */
        Job job = Job.getInstance(super.getConf(), "sortdata");
        //如果打包运行出错,则需要加该配置
        job.setJarByClass(JobMain.class);

        /*
        2:配置job任务(八个步骤)
         */
        //第一步:设置输入类和输入的路径
        job.setInputFormatClass(TextInputFormat.class);
        TextInputFormat.addInputPath(job, new Path("hdfs://node01:8020/input/sort_input"));
        //TextInputFormat.addInputPath(job, new Path("file:///D:\\input\\input\\sort_input"));

        //第二步: 设置Mapper类和数据类型
        job.setMapperClass(SortMapper.class);
        job.setMapOutputKeyClass(SortBean.class);
        job.setMapOutputValueClass(NullWritable.class);

        //第三、四、五、六

        //第七步：设置Reducer类和类型
        job.setReducerClass(SortReducer.class);
        job.setOutputKeyClass(SortBean.class);
        job.setOutputValueClass(NullWritable.class);


        //第八步: 设置输出类和输出的路径
        job.setOutputFormatClass(TextOutputFormat.class);
        TextOutputFormat.setOutputPath(job, new Path("hdfs://node01:8020/output/sort_output"));
        //TextOutputFormat.setOutputPath(job, new Path("file:///D:\\output\\sort_output"));

        //获取FileSystem
        FileSystem fileSystem = FileSystem.get(new URI("hdfs://node01:8020"), new Configuration());

        //判断目录是否存在
        Path path = new Path("hdfs://node01:8020/output/sort_output");
        boolean bl2 = fileSystem.exists(path);
        if(bl2){
            //删除目标目录
            fileSystem.delete(path, true);
        }

        /*
        3:等待任务结束
         */
        boolean bl = job.waitForCompletion(true);

        return bl?0:1;
    }

    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();

        //启动job任务
        int run = ToolRunner.run(configuration, new JobMain(), args);
        System.exit(run);
    }
}
