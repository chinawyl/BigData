package CommonFriends_Step2;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class Step2Reducer extends Reducer<Text,Text,Text,Text> {
    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        //1:原来的K2就是K3
        //2:将集合进行遍历,将集合中的元素拼接,得到V3
		int count = 0;
        StringBuffer buffer = new StringBuffer();
        for (Text value : values) {
            buffer.append(value.toString()).append("-");
            count++;
        }

        //将最后一位-去除
        String str = buffer.toString();
        String strs = "";
        if(str.endsWith("-")){
            strs = str.substring(0,str.length() - 1);
        }
        //3:将K3和V3写入上下文中
        context.write(key, new Text(count + "\t" + strs));
    }
}
