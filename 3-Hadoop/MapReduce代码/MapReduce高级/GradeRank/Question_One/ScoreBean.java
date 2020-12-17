package GradeRank.Question_One;

import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ScoreBean implements WritableComparable<ScoreBean> {
    public ScoreBean(){
        super();
    }
    public ScoreBean(String name,int sum,Double avg){
        this.name=name;
        this.sum=sum;
        this.avg=avg;
    }

    private String name;
    private int sum;
    private double avg;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSum() {
        return sum;
    }

    public void setSum(int sum) {
        this.sum = sum;
    }

    public double getAvg() {
        return avg;
    }

    public void setAvg(double avg) {
        this.avg = avg;
    }

    @Override
    public String toString() {
        return name +"\t"+ sum +"\t" + avg;
    }

    @Override
    public int compareTo(ScoreBean scoreBean) {
        return scoreBean.sum - this.sum;
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeUTF(name);
        dataOutput.writeInt(sum);
        dataOutput.writeDouble(avg);
    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {
        this.name = dataInput.readUTF();
        this.sum = dataInput.readInt();
        this.avg = dataInput.readDouble();
    }
}
