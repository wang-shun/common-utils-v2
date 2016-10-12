package com.youzan.sz.test.dq;

/**
 * Created by wangpan on 2016/9/30.
 */
public class DemoPoeple {
    private String name ="this is test demo";
    private int age = 1;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "DemoPoeple{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
