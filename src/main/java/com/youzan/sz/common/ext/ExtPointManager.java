package com.youzan.sz.common.ext;

import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * Created by heshen on 2017/4/25.
 */
public class ExtPointManager {
    
    // List of registered
    private final static ConcurrentMap<Class,Extpoint> register = new ConcurrentHashMap<>();
    
    public static ExtExceptionFilter getExtExceptionFilter(){
        return (ExtExceptionFilter) register.get(ExtExceptionFilter.class);
    }
    /**
     *
     * @param extpoint
     * @throws SQLException
     */
    public static synchronized void register(Extpoint extpoint)
            throws SQLException {
        
        if(extpoint != null) {
            if(extpoint instanceof ExtExceptionFilter){
                register.put(ExtExceptionFilter.class, extpoint);
            }else {
                throw new UnsupportedOperationException("can't regist extpoint: "+extpoint.getClass());
            }
        } else {
            // This is for compatibility with the original ExtPointManager
            throw new NullPointerException();
        }
        
    }
//
//    class ExtpointInfo {
//
//        final Extpoint extpoint;
//        ExtpointInfo(Extpoint extpoint) {
//            this.extpoint = extpoint;
//        }
//
//        @Override
//        public boolean equals(Object other) {
//            return (other instanceof ExtpointInfo)
//                    && this.extpoint == ((ExtpointInfo) other).extpoint;
//        }
//
//        @Override
//        public int hashCode() {
//            return extpoint.hashCode();
//        }
//
//        @Override
//        public String toString() {
//            return ("extpoint[className="  + extpoint + "]");
//        }
//    }
}
