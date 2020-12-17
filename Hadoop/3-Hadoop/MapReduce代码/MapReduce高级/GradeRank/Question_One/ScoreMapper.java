package GradeRank.Question_One;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.text.DecimalFormat;

public class ScoreMapper extends Mapper<LongWritable, Text, ScoreBean, Text> {
    Text text = new Text();
    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String[] split = value.toString().split("\t");

        String name = split[2];
        int chinese = Integer.parseInt(split[3]);
        int math = Integer.parseInt(split[4]);
        int english = Integer.parseInt(split[5]);
        int sum = chinese+math+english;
        Double avg=(1.0)*sum/3;

        ScoreBean scoreBean = new ScoreBean(name,sum,avg);
        text.set(split[0]+"\t"+split[1]);

        context.write(scoreBean, text);
    }
}
