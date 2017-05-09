package com.youzan.sz.common.ext;

import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * Created by heshen on 2017/4/25.
 */
public class ExtManager {
    
    // List of registered
    private final static ConcurrentMap<Class,Extpoint> register = new ConcurrentHashMap<>();
    
    public static ExtExceptionFilter getExtExceptionFilter(){
        return (ExtExceptionFilter) register.get(ExtExceptionFilter.class);
    }
    /**
     *
     * @param extpoint
     */
    public static synchronized void register(Extpoint extpoint){
        
        if(extpoint != null) {
            if(extpoint instanceof ExtExceptionFilter){
                register.put(ExtExceptionFilter.class, extpoint);
            }else {
                throw new UnsupportedOperationException("can't regist extpoint: "+extpoint.getClass());
            }
        } else {
            throw new NullPointerException();
        }
        
    }
}
