package GradeRank.Question_Two;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class ScoreReducer extends Reducer<ScoreBean, IntWritable, ScoreBean, Text> {
    @Override
    protected void reduce(ScoreBean key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
        int sum=0;
        int count=0;

        for(IntWritable v: values){
            sum+=v.get();
            count++;
        }
        context.write(key,new Text(""+1.0*sum/count));
    }
}
