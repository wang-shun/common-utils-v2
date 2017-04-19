package com.youzan.sz.test.nsq.json;

import com.youzan.sz.nsq.NSQMsg;

import java.util.List;

/**
 *
 * Created by zhanguo on 16/7/30.
 */
public class DemoStoreMsg extends NSQMsg {
    
    private int id;
    
    private String name;
    private List<Integer> shopId;
    
    
    public int getId() {
        return id;
    }
    
    
    public void setId(int id) {
        this.id = id;
    }
    
    
    public String getName() {
        return name;
    }
    
    
    public void setName(String name) {
        this.name = name;
    }
    
    
    public List<Integer> getShopId() {
        return shopId;
    }
    
    
    public void setShopId(List<Integer> shopId) {
        this.shopId = shopId;
    }
}
