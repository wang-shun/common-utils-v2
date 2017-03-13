package com.youzan.sz.test.bean;

import com.youzan.sz.common.util.BeanUtil;
import com.youzan.sz.common.util.JsonUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by jinxiaofei.
 * Time 17/3/7 下午6:45
 * Desc 文件描述
 */
public class BeanUtilTest {
    
    public static void main(String[] args) {
        
        Teacher1 teacher1 = new Teacher1();
        teacher1.setName("张三");
        List<Student1> list = new ArrayList<>();
        Student1 student1 = new Student1();
        student1.setName("aaa");
        student1.setAddress("芒果网大厦");
        student1.setAge(10);
        list.add(student1);
        teacher1.setList(list);
        System.out.println(JsonUtils.bean2Json(teacher1));
        System.out.println(JsonUtils.bean2Json(BeanUtil.copyNonNullProperty(teacher1, Teacher2.class)));
        
    }
    
}

