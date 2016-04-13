package com.youzan.sz.common.util;

import com.youzan.sz.common.model.Page;

import java.util.List;

/**
 * Created by zefa on 16/4/13.
 */
public class PageUtils {
    public static List paging(Page page,List list){
        int pageNO = page.getPageNO();
        int pageSize = page.getPageSize();
        page.setTotal(list.size());
        return list.subList(pageNO*pageSize + 1,pageSize);
    }
}
