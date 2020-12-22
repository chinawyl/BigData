package GradeRank.Question_Two;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Partitioner;

public class ScorePartition extends Partitioner<ScoreBean, IntWritable> {
    @Override
    public int getPartition(ScoreBean scoreBean, IntWritable intWritable, int i) {
        if(scoreBean.getClassname().equals("1303")){
            return 0;
        }
        if(scoreBean.getClassname().equals("1304")){
            return 1;
        }
        if(scoreBean.getClassname().equals("1305")){
            return 2;
        }
        if(scoreBean.getClassname().equals("1306")){
            return 3;
        }else{
            return 4;
        }

    }
}
