package com.youzan.sz.common.base;

import com.youzan.sz.common.model.base.BasePO;
import com.youzan.sz.common.mybatis.MybatisDao;
import org.apache.ibatis.session.RowBounds;

import java.io.Serializable;
import java.util.List;

/**
 *
 * Created by zhanguo on 16/8/13.
 */
@MybatisDao
public interface BaseDao<T extends BasePO> {

    int save(BasePO basePO);

    int update(T t);

    T get(long kid);

    public long getMaxBid();

    T findOne(T t);

    int insert(T t);

    List<T> findList(T t);

    /**
     * 支持分页
     * */
    List<T> findList(T t, RowBounds rowBounds);

}
