package com.youzan.sz.test.bean;

import java.util.List;


/**
 * Created by jinxiaofei.
 * Time 17/3/7 下午7:02
 * Desc 文件描述
 */
public class Teacher1 {
    
    private String name;
    
    private List<Student1> list;
    
    
    public String getName() {
        
        return name;
    }
    
    
    public void setName(String name) {
        
        this.name = name;
    }
    
    
    public List<Student1> getList() {
        
        return list;
    }
    
    
    public void setList(List<Student1> list) {
        
        this.list = list;
    }
    
}
