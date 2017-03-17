package com.youzan.sz.common.mybatis;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.reflection.wrapper.ObjectWrapper;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.SimpleTypeRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;


/**
 * Created by YANG on 16/4/11.
 */

@Intercepts(@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}))
public class PaginationInterceptor implements Interceptor {
    
    private static int MAPPED_STATEMENT_INDEX = 0;
    
    private static int PARAMETER_INDEX = 1;
    
    private static int ROWBOUNDS_INDEX = 2;
    
    private static int RESULT_HANDLER_INDEX = 3;
    
    private Dialect dialect;
    
    
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        processInterceptor(invocation.getArgs());
        return invocation.proceed();
    }
    
    
    // 修改参数,加上LIMIT 参数
    private void processInterceptor(final Object[] queryArgs) {
        final MappedStatement mappedStatement = (MappedStatement) queryArgs[MAPPED_STATEMENT_INDEX];
        final Object parameter = queryArgs[PARAMETER_INDEX];
        final Object objBounds = queryArgs[ROWBOUNDS_INDEX];
        if (objBounds == null) {
            queryArgs[ROWBOUNDS_INDEX] = new RowBounds();
            return;
        }
        
        final RowBounds rowBounds = (RowBounds) objBounds;
        int offset = rowBounds.getOffset();
        int limit = rowBounds.getLimit();
        
        if (dialect.supportsLimit() && (offset != RowBounds.NO_ROW_OFFSET || limit != RowBounds.NO_ROW_LIMIT)) {
            BoundSql boundSql = mappedStatement.getBoundSql(parameter);
            StringBuilder bufferSql = new StringBuilder(boundSql.getSql().trim());
            
            if (bufferSql.lastIndexOf(";") == bufferSql.length() - 1) {
                bufferSql.deleteCharAt(bufferSql.length() - 1);
            }
            
            String sql = bufferSql.toString();
            if (dialect.supportsLimitOffset()) {
                sql = dialect.getLimitString(sql, offset, limit);
                offset = RowBounds.NO_ROW_OFFSET;
            }else {
                sql = dialect.getLimitString(sql, 0, limit);
            }
            
            limit = RowBounds.NO_ROW_LIMIT;
            
            queryArgs[ROWBOUNDS_INDEX] = new RowBounds(offset, limit);
            
            
            // 加上分页参数
            List<ParameterMapping> parameterMappings = new ArrayList(boundSql.getParameterMappings());
            Map<String, Object> pageParameters = new HashMap<String, Object>();
            Object parameterObject = boundSql.getParameterObject();
            
            
            if (parameterObject instanceof Map) {
                pageParameters.putAll((Map) parameterObject);
            }else if (parameterObject != null) {
                Class cls = parameterObject.getClass();
                if (cls.isPrimitive() || cls.isArray() || SimpleTypeRegistry.isSimpleType(cls) || Enum.class.isAssignableFrom(cls) || Collection.class.isAssignableFrom(cls)) {
                    for (ParameterMapping parameterMapping : parameterMappings) {
                        pageParameters.put(parameterMapping.getProperty(), parameterObject);
                    }
                }else {
                    MetaObject metaObject = mappedStatement.getConfiguration().newMetaObject(parameterObject);
                    ObjectWrapper wrapper = metaObject.getObjectWrapper();
                    for (ParameterMapping parameterMapping : parameterMappings) {
                        PropertyTokenizer prop = new PropertyTokenizer(parameterMapping.getProperty());
                        pageParameters.put(parameterMapping.getProperty(), wrapper.get(prop));
                    }
                }
                
            }
            
            BoundSql newBoundSql = new BoundSql(mappedStatement.getConfiguration(), sql, parameterMappings, pageParameters);
            
            for (ParameterMapping parameterMapping : boundSql.getParameterMappings()) {
                String prop = parameterMapping.getProperty();
                if (boundSql.hasAdditionalParameter(prop)) {
                    newBoundSql.setAdditionalParameter(prop, boundSql.getAdditionalParameter(prop));
                }
            }
            queryArgs[MAPPED_STATEMENT_INDEX] = copyFromMappedStatement(mappedStatement, new BoundSqlSqlSource(newBoundSql));
        }
    }
    
    
    // see: MapperBuilderAssistant
    private MappedStatement copyFromMappedStatement(MappedStatement statement, SqlSource newSqlSource) {
        MappedStatement.Builder builder = new MappedStatement.Builder(statement.getConfiguration(), statement.getId(), newSqlSource, statement.getSqlCommandType());
        
        builder.resource(statement.getResource());
        builder.fetchSize(statement.getFetchSize());
        builder.statementType(statement.getStatementType());
        builder.keyGenerator(statement.getKeyGenerator());
        if (statement.getKeyProperties() != null && statement.getKeyProperties().length != 0) {
            StringBuffer keyProperties = new StringBuffer();
            for (String keyProperty : statement.getKeyProperties()) {
                keyProperties.append(keyProperty).append(",");
            }
            keyProperties.delete(keyProperties.length() - 1, keyProperties.length());
            builder.keyProperty(keyProperties.toString());
        }
        
        //setStatementTimeout()
        builder.timeout(statement.getTimeout());
        
        //setStatementResultMap()
        builder.parameterMap(statement.getParameterMap());
        
        //setStatementResultMap()
        builder.resultMaps(statement.getResultMaps());
        builder.resultSetType(statement.getResultSetType());
        
        //setStatementCache()
        builder.cache(statement.getCache());
        builder.flushCacheRequired(statement.isFlushCacheRequired());
        builder.useCache(statement.isUseCache());
        
        return builder.build();
        
    }
    
    
    public static class BoundSqlSqlSource implements SqlSource {
        
        BoundSql boundSql;
        
        
        public BoundSqlSqlSource(BoundSql boundSql) {
            this.boundSql = boundSql;
        }
        
        
        public BoundSql getBoundSql(Object parameterObject) {
            return boundSql;
        }
    }
    
    
    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }
    
    
    @Override
    public void setProperties(Properties properties) {
        String dialectClass = properties.get("dialectClass").toString();
        try {
            dialect = (Dialect) Class.forName(dialectClass).newInstance();
        } catch (Exception e) {
            throw new RuntimeException("cannot create dialect instance by dialectClass:" + dialectClass, e);
        }
        
    }
}
