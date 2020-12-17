package GradeRank.Question_Two;

import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

public class ScoreGroup extends WritableComparator {
    public ScoreGroup() {
        super(ScoreBean.class,true);
    }

    @Override
    public int compare(WritableComparable a, WritableComparable b) {
        ScoreBean first = (ScoreBean)a;
        ScoreBean second = (ScoreBean)b;

        int i= first.getClassname().compareTo(second.getClassname());
        if(i==0){
            return first.getCourse().compareTo(second.getCourse());
        }
        return i;
    }
}
