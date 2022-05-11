package entity;

import java.sql.Timestamp;

public class Senior extends BaseEntity{
    private int age;
    private String name;
    private long score;

    public Senior() {
    }

    public Senior(int age, String name, long score) {
        this.age = age;
        this.name = name;
        this.score = score;
        this.setTimestamp(score);
    }

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Senior{" +
                "age=" + age +
                ", name='" + name + '\'' +
                ", timestamp="+getTimestamp()+"}";
    }
}
