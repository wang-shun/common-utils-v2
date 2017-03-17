package com.youzan.sz.common.mybatis;

/**
 * Created by YANG on 16/4/11.
 */
public class MySQLDialect extends Dialect {
    
    private static final String LIMIT = "LIMIT";
    
    private static final String BLANK = " ";
    
    private static final String COMMA = ",";
    
    
    @Override
    public boolean supportsLimit() {
        return true;
    }
    
    
    @Override
    public boolean supportsLimitOffset() {
        return true;
    }
    
    
    @Override
    public String getLimitString(String sql, int offset, String offsetPlaceholder, int limit, String limitPlaceholder) {
        StringBuilder sb = new StringBuilder();
        sb.append(sql).append(BLANK).append(LIMIT).append(BLANK);
        
        if (offset > 0) {
            sb.append(offsetPlaceholder).append(COMMA).append(limitPlaceholder);
        }else {
            sb.append(limitPlaceholder);
        }
        return sb.toString();
    }
}
