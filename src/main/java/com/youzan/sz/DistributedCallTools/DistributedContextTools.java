package com.youzan.sz.DistributedCallTools;

import com.youzan.platform.util.lang.StringUtil;
import com.youzan.sz.DistributedCallTools.DistributedContextTools.DistributedParamManager.AdminId;
import com.youzan.sz.DistributedCallTools.DistributedContextTools.DistributedParamManager.Aid;
import com.youzan.sz.DistributedCallTools.DistributedContextTools.DistributedParamManager.AppVersion;
import com.youzan.sz.DistributedCallTools.DistributedContextTools.DistributedParamManager.Bid;
import com.youzan.sz.DistributedCallTools.DistributedContextTools.DistributedParamManager.DeviceId;
import com.youzan.sz.DistributedCallTools.DistributedContextTools.DistributedParamManager.DeviceType;
import com.youzan.sz.DistributedCallTools.DistributedContextTools.DistributedParamManager.DistributedParam;
import com.youzan.sz.DistributedCallTools.DistributedContextTools.DistributedParamManager.KdtId;
import com.youzan.sz.DistributedCallTools.DistributedContextTools.DistributedParamManager.NoSession;
import com.youzan.sz.DistributedCallTools.DistributedContextTools.DistributedParamManager.OpAdminId;
import com.youzan.sz.DistributedCallTools.DistributedContextTools.DistributedParamManager.OpAdminName;
import com.youzan.sz.DistributedCallTools.DistributedContextTools.DistributedParamManager.RequestIp;
import com.youzan.sz.DistributedCallTools.DistributedContextTools.DistributedParamManager.ShopId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


public class DistributedContextTools {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(com.youzan.sz.DistributedCallTools.DistributedContextTools.class);
    
    
    public static class DistributedParamManager {
        
        public static class AdminId extends DistributedParam<Long> {
            
            public static String getName() {
                return "distributed.admin_id";
            }
        }
        
        
        public static class Aid extends DistributedParam<Integer> {
            
            public static String getName() {
                return "distributed.app_id";
            }
        }
        
        
        public static class Bid extends DistributedParam<Long> {
            
            public static String getName() {
                return "distributed.bid";
            }
        }
        
        
        public static class ShopId extends DistributedParam<Long> {
            
            public static String getName() {
                return "distributed.shop_id";
            }
        }
        
        
        public static abstract class DistributedParam<T> {
            
            public static String getName() {
                return "";
            }
        }
        
        
        public static class KdtId extends DistributedParam<Long> {
            
            public static String getName() {
                return "distributed.kdt_id";
            }
        }
        
        
        public static class RequestIp extends DistributedParam<String> {
            
            public static String getName() {
                return "distributed.request_ip";
            }
        }
        
        
        public static class DeviceId extends DistributedParam<String> {
            
            public static String getName() {
                return "distributed.device_id";
            }
        }
        
        
        public static class DeviceType extends DistributedParam<String> {
            
            public static String getName() {
                return "distributed.device_type";
            }
        }
        
        
        public static class OpAdminId extends DistributedParam<Long> {
            
            public static String getName() {
                return "distributed.op_admin_id";
            }
        }
        
        
        public static class OpAdminName extends DistributedParam<String> {
            
            public static String getName() {
                return "distributed.op_admin_name";
            }
        }
        
        
        public static class AppVersion extends DistributedParam<String> {
            
            public static String getName() {
                return "distributed.app_version";
            }
        }
        
        
        public static class NoSession extends DistributedParam<Integer> {
            
            public static String getName() {
                return "distributed.no_session";
            }
        }
        
        
        private static Map<String, Class<?>> cache = new HashMap<>();
        
        static {
            // 放入卡门调用时传入的参数类型映射
            cache.put(AdminId.getName(), AdminId.class);
            cache.put(RequestIp.getName(), RequestIp.class);
            cache.put(KdtId.getName(), KdtId.class);
            cache.put(DeviceId.getName(), DeviceId.class);
            cache.put(DeviceType.getName(), DeviceType.class);
            cache.put(Aid.getName(), Aid.class);
            cache.put(ShopId.getName(), ShopId.class);
            cache.put(Bid.getName(), Bid.class);
            cache.put(OpAdminId.getName(), OpAdminId.class);
            cache.put(OpAdminName.getName(), OpAdminName.class);
            cache.put(AppVersion.getName(), AppVersion.class);
            cache.put(NoSession.getName(), NoSession.class);
            // 放入使用客户端直接调用时放入的参数类型
            cache.put(AdminId.class.getCanonicalName(), AdminId.class);
            cache.put(KdtId.class.getCanonicalName(), KdtId.class);
            cache.put(RequestIp.class.getCanonicalName(), RequestIp.class);
            cache.put(DeviceId.class.getCanonicalName(), DeviceId.class);
            cache.put(DeviceType.class.getCanonicalName(), DeviceType.class);
            cache.put(Aid.class.getCanonicalName(), Aid.class);
            cache.put(ShopId.class.getCanonicalName(), ShopId.class);
            cache.put(Bid.class.getCanonicalName(), Bid.class);
            cache.put(OpAdminId.class.getCanonicalName(), OpAdminId.class);
            cache.put(OpAdminName.class.getCanonicalName(), OpAdminName.class);
            cache.put(AppVersion.class.getCanonicalName(), AppVersion.class);
            cache.put(NoSession.class.getCanonicalName(), NoSession.class);
            
        }
        
        public static Class<?> get(Class<?> param) {
            return cache.get(param.getCanonicalName());
        }
        
        
        public static Class<?> get(String param) {
            return cache.get(param);
        }
        
        
        public static boolean isDistributedParam(Class<?> param) {
            return cache.containsKey(param.getCanonicalName());
        }
        
        
        public static boolean isDistributedParam(String param) {
            return cache.containsKey(param);
        }
    }
    
    
    private static com.youzan.sz.DistributedCallTools.DistributedContext context = new DistributedContext();
    
    
    /**
     * 由于每次调用都会重新放置context等对象.所以这里可以清除本线程所有的
     */
    public static void clear() {
        Long adminId = getAdminId();
        context.clear();
        LOGGER.trace("结束清除{}请求参数", adminId);
    }
    
    
    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        return (T) context.get(key);
    }
    
    
    public static Long getAdminId() {
        Object obj = get(AdminId.class.getCanonicalName());
        return getLong(obj);
    }
    
    
    public static Long getKdtId() {
        Object obj = get(KdtId.class.getCanonicalName());
        return getLong(obj);
    }
    
    
    public static Integer getNoSession() {
        Object obj = get(NoSession.class.getCanonicalName());
        if (obj != null) {
            if (obj instanceof Integer)
                return (Integer) obj;
            if (obj instanceof String) {
                return Integer.valueOf(obj.toString());
            }
        }
        return 0;
    }
    
    
    private static Long getLong(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof String) {
            if (StringUtil.isEmpty(obj.toString())) {
                return 0L;
            }
            return Long.valueOf(obj.toString());
        }else if (obj instanceof Number) {
            return Long.valueOf(obj + "");
        }
        return (Long) obj;
    }
    
    
    //获取应用id
    public static Integer getAId() {
        
        final Object aid = get(Aid.class.getCanonicalName());
        if (aid instanceof String) {
            return Integer.valueOf((String) aid);
        }else if (aid instanceof Long) {
            return Integer.valueOf(aid + "");
        }
        return (Integer) aid;
    }
    
    
    //获取应用id
    public static Long getShopId() {
        final Object shopId = get(ShopId.class.getCanonicalName());
        if (shopId instanceof String) {
            return Long.valueOf((String) shopId);
        }else if (shopId instanceof Integer) {
            return Long.valueOf(shopId + "");
        }
        return (Long) shopId;
    }
    
    //获取应用id
    
    
    /**
     * @deprecated 改个名字
     */
    @Deprecated
    public static Long getBId() {
        return getBid();
    }
    
    
    //获取应用id
    public static Long getBid() {
        final Object bid = get(Bid.class.getCanonicalName());
        if (bid == null)//bid为空尝试获取一下kdtId
            return getKdtId();
        if (bid instanceof String) {
            return Long.valueOf((String) bid);
        }
        return (Long) bid;
        
    }
    
    
    //获取操作人id
    public static Long getOpAdminId() {
        final Object opAdminId = get(OpAdminId.class.getCanonicalName());
        if (opAdminId instanceof String) {
            return Long.valueOf((String) opAdminId);
        }
        return (Long) opAdminId;
    }
    
    
    //获取操作人名字
    public static String getOpAdminName() {
        return get(OpAdminName.class.getCanonicalName());
    }
    
    
    // APP版本信息
    public static String getAppVersion() {
        return get(AppVersion.class.getCanonicalName());
    }
    
    
    public static String getRequestIp() {
        return get(RequestIp.class.getCanonicalName());
    }
    
    
    public static String getDeviceId() {
        return get(DeviceId.class.getCanonicalName());
    }
    
    
    public static String getDeviceType() {
        return get(DeviceType.class.getCanonicalName());
    }
    
    
    /**
     * 设置属性,有泛型检查
     */
    public static <T extends DistributedParam<V>, V> void setAttr(Class<T> key, V value) {
        context.put(key.getCanonicalName(), value);
    }
    
    
    @Deprecated
    public static <T> void set(Class<?> key, T value) {
        //        if (LOGGER.isDebugEnabled()) {
        //            LOGGER.debug("set distribution key:{},value:{}", key.getSimpleName(), value);
        //        }
        //        setAttr(key,value);
        context.put(key.getCanonicalName(), value);
    }
    
    
    public static <T> void set(String key, T value) {
        context.put(key, value);
    }
    
    
    public static void main(String[] args) {
        System.out.println(Long.parseLong(""));
    }
    
}
