package com.youzan.sz.test.bean;

import java.util.List;


/**
 * Created by jinxiaofei.
 * Time 17/3/7 下午7:04
 * Desc 文件描述
 */
public class Teacher2 {
    
    
    private String name;
    
    private Integer age;
    
    private List<Student2> list;
    
    
    public String getName() {
        
        return name;
    }
    
    
    public void setName(String name) {
        
        this.name = name;
    }
    
    
    public Integer getAge() {
        
        return age;
    }
    
    
    public void setAge(Integer age) {
        
        this.age = age;
    }
    
    
    public List<Student2> getList() {
        
        return list;
    }
    
    
    public void setList(List<Student2> list) {
        
        this.list = list;
    }
}