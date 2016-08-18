package com.youzan.sz.DistributedCallTools;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.youzan.sz.DistributedCallTools.DistributedContextTools.DistributedParamManager.*;

public class DistributedContextTools {
    private final static Logger LOGGER = LoggerFactory
            .getLogger(com.youzan.sz.DistributedCallTools.DistributedContextTools.class);

    public static class DistributedParamManager {

        public static class AdminId extends DistributedParam {
            public static String getName() {
                return "distributed.admin_id";
            }
        }

        public static class AId extends DistributedParam {
            public static String getName() {
                return "distributed.app_id";
            }
        }

        public static abstract class DistributedParam {
            public static String getName() {
                return "";
            }
        }

        public static class KdtId extends DistributedParam {
            public static String getName() {
                return "distributed.kdt_id";
            }
        }

        public static class RequestIp extends DistributedParam {
            public static String getName() {
                return "distributed.request_ip";
            }
        }

        public static class DeviceId extends DistributedParam {
            public static String getName() {
                return "distributed.device_id";
            }
        }

        public static class DeviceType extends DistributedParam {
            public static String getName() {
                return "distributed.device_type";
            }
        }

        private static Map<String, Class<?>> cache = new HashMap<>();

        static {
            // 放入卡门调用时传入的参数类型映射
            cache.put(AdminId.getName(), AdminId.class);
            cache.put(RequestIp.getName(), RequestIp.class);
            cache.put(KdtId.getName(), RequestIp.class);
            cache.put(DeviceId.getName(), DeviceId.class);
            cache.put(DeviceType.getName(), DeviceType.class);
            cache.put(AId.getName(), AId.class);

            // 放入使用客户端直接调用时放入的参数类型
            cache.put(AdminId.class.getCanonicalName(), AdminId.class);
            cache.put(KdtId.class.getCanonicalName(), KdtId.class);
            cache.put(RequestIp.class.getCanonicalName(), RequestIp.class);
            cache.put(DeviceId.class.getCanonicalName(), DeviceId.class);
            cache.put(DeviceType.class.getCanonicalName(), DeviceType.class);
            cache.put(AId.class.getCanonicalName(), AId.class);
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
     * */
    public static void clear() {
        Long adminId = getAdminId();
        LOGGER.debug("开始清除{}请求参数", adminId);
        context.clear();
        LOGGER.debug("结束清除{}请求参数", adminId);
    }

    @SuppressWarnings("unchecked") public static <T> T get(String key) {
        return (T) context.get(key);
    }

    public static Long getAdminId() {
        String s = get(AdminId.class.getCanonicalName());
        if (null == s) {
            LOGGER.warn("not get adminId");
            return 0L;
        }
        return Long.valueOf(s);
    }

    public static Long getKdtId() {
        String kdtStr = get(KdtId.class.getCanonicalName());
        if (kdtStr == null || kdtStr.length() == 0) {
            LOGGER.warn("not get ktdId");
            return null;
        }
        return Long.valueOf(kdtStr);
    }

    //获取应用id
    public static String getAId() {
        final String aid = get(AId.class.getCanonicalName());
        if (aid == null || aid.length() == 0) {
            LOGGER.warn("not get aid");
        }
        return aid;

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

    public static <T> void set(Class<?> key, T value) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("set distribution key:{},value:{}", key.getSimpleName(), value);
        }
        context.put(key.getCanonicalName(), value);
    }

    public static <T> void set(String key, T value) {
        context.put(key, value);
    }

}
