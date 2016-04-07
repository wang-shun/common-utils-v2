package com.youzan.sz.common.mybatis;


import com.youzan.sz.common.model.EnumValue;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by YANG on 16/3/29.
 */

public class EnumTypeHandler extends BaseTypeHandler<EnumValue> {

    private final EnumValue[] enums;
    
    public EnumTypeHandler(Class<EnumValue> type) {
        assert type != null;
        this.enums = type.getEnumConstants();
    }

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, EnumValue enumValue, JdbcType jdbcType) throws SQLException {
        preparedStatement.setInt(i, enumValue.getValue());
    }

    @Override
    public EnumValue getNullableResult(ResultSet resultSet, String s) throws SQLException {
        return handleResult(resultSet.getInt(s));
    }

    @Override
    public EnumValue getNullableResult(ResultSet resultSet, int i) throws SQLException {
        return handleResult(resultSet.getInt(i));
    }

    @Override
    public EnumValue getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        return handleResult(callableStatement.getInt(i));
    }

    private EnumValue handleResult(int val) {
        for (EnumValue v : enums) {
            if (v.getValue() == val) {
                return v;
            }
        }
        return null;
    }
}
