package GradeRank.Question_One;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class ScoreReducer extends Reducer<ScoreBean, Text, Text, ScoreBean> {
    @Override
    protected void reduce(ScoreBean key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        for(Text v: values){
            context.write(v,key);
        }
    }
}
