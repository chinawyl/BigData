package GradeRank.Question_Two;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class JobMain extends Configured implements Tool {
    @Override
    public int run(String[] strings) throws Exception {
        //1:创建job任务对象
        Job job = Job.getInstance(super.getConf(), "graderank_question_two");
        //如果打包运行出错,则需要加该配置
        job.setJarByClass(JobMain.class);

        //2:对job任务进行配置(八个步骤)
        //第一步:设置输入类和输入的路径
        job.setInputFormatClass(TextInputFormat.class);
        //TextInputFormat.addInputPath(job, new Path("hdfs://node01:8020/input/graderank/question_two_input"));
        TextInputFormat.addInputPath(job, new Path("file:///D:\\input\\graderank\\question_two_input"));

        //第二步:设置Mapper类和数据类型（K2和V2）
        job.setMapperClass(ScoreMapper.class);
        job.setMapOutputKeyClass(ScoreBean.class);
        job.setMapOutputValueClass(IntWritable.class);

        //第三步:设置分区
        job.setPartitionerClass(ScorePartition.class);

        //第四步:设置排序
        //已经在ScoreBean设置

        //第五步:设置规约
        //默认

        //第六步
        //设置分组
        job.setGroupingComparatorClass(ScoreGroup.class);
        job.setNumReduceTasks(5);

        //第七步：设置Reducer类和类型
        job.setReducerClass(ScoreReducer.class);
        job.setOutputKeyClass(ScoreBean.class);
        job.setOutputValueClass(Text.class);

        //第八步: 设置输出类和输出的路径
        job.setOutputFormatClass(TextOutputFormat.class);
        //TextOutputFormat.setOutputPath(job, new Path("hdfs://node01:8020/output/graderank/question_two_output"));
        TextOutputFormat.setOutputPath(job, new Path("file:///D:\\output\\graderank\\question_two_output"));

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
