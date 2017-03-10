package com.youzan.sz.common.mybatis;


import com.youzan.sz.common.model.EnumValue;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * Created by YANG on 16/3/29.
 */

public class EnumTypeHandler<E extends EnumValue> extends BaseTypeHandler<E> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(EnumTypeHandler.class);
    
    private Map<Integer, E> enumMap;
    
    
    public EnumTypeHandler(Class<E> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type argument cannot be null");
        }
        LOGGER.info("EnumType handled type:{}", type);
        if (type.getEnumConstants() != null) {
            enumMap = Arrays.stream(type.getEnumConstants()).collect(Collectors.toMap(EnumValue::getValue, Function.identity()));
        }else {
            enumMap = Collections.emptyMap();
        }
    }
    
    
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getValue());
    }
    
    
    @Override
    public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return enumMap.get(rs.getInt(columnName));
    }
    
    
    @Override
    public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return enumMap.get(rs.getInt(columnIndex));
    }
    
    
    @Override
    public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return enumMap.get(cs.getInt(columnIndex));
    }
    
}
