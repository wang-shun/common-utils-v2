package com.youzan.sz.common.util;

import com.youzan.sz.common.model.Page;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zefa on 16/4/25.
 */
public class PageUtils {

    public static void paging(Page page){
        int pageNo = page.getPage();
        int size = page.getSize();
        List list = page.getList();
        page.setTotal(list.size());
        int start = (pageNo - 1) * size;
        int end = start + size;
        try {
            page.setList(list.subList(start, end));
        }catch (IndexOutOfBoundsException e){
            if(start > page.getTotal()) {
                page.setList(new ArrayList<>());
            }else {
                page.setList(list.subList(start, page.getTotal()));
            }
        }
    }

    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>();
        for(int i = 1; i <= 105; i++){
            list.add(i);
        }
        Page page = new Page(11,10);
        page.setList(list);
        PageUtils.paging(page);
        System.out.println(page.getList().toString());
    }
}
