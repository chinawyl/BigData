package GradeRank.Question_Two;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class ScoreMapper extends Mapper<LongWritable, Text, ScoreBean, IntWritable> {
    ScoreBean scoreBean = new ScoreBean();
    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String[] fields = value.toString().split("\t");
        int score=0;
        scoreBean.setClassname(fields[0]);
        for(int i=3;i<fields.length;i++){
            if(i==3){
                scoreBean.setCourse("语文");
                score=Integer.parseInt(fields[3]);
                context.write(scoreBean,new IntWritable(score));
            }
            if(i==4){
                scoreBean.setCourse("数学");
                score=Integer.parseInt(fields[4]);
                context.write(scoreBean,new IntWritable(score));
            }else{
                scoreBean.setCourse("英语");
                score=Integer.parseInt(fields[5]);
                context.write(scoreBean,new IntWritable(score));
            }
        }

    }
}
