package GradeRank.Question_Two;

import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ScoreBean implements WritableComparable<ScoreBean> {

    private String classname;
    private String course;

    public ScoreBean(){
        super();
    }

    public ScoreBean(String classname, String course){
        this.classname = classname;
        this.course = course;
    }

    public String getClassname() {
        return classname;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }

    public String getCourse() {
        return course;
    }

    @Override
    public String toString() {
        return classname + "\t" + course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    @Override
    public int compareTo(ScoreBean scoreBean) {
        int temp = this.getClassname().compareTo(scoreBean.getClassname());
        if(temp==0){
            temp=this.getCourse().compareTo(scoreBean.getCourse());
        }
        return temp;
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeUTF(classname);
        dataOutput.writeUTF(course);
    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {
        this.classname = dataInput.readUTF();
        this.course = dataInput.readUTF();
    }
}
