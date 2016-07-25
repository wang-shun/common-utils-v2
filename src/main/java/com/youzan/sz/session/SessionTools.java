package com.youzan.sz.session;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.youzan.platform.bootstrap.exception.BusinessException;
import com.youzan.sz.DistributedCallTools.DistributedContextTools;
import com.youzan.sz.common.response.enums.ResponseCode;
import com.youzan.sz.common.util.JsonUtils;
import com.youzan.sz.common.util.SpringUtils;
import com.youzan.sz.oa.device.dto.DeviceDTO;
import com.youzan.sz.portal.session.service.SessionService;

public class SessionTools {


    public static final String    SESSION     = "session";
    public static final String    ADMINID     = "adminId";
    public static final String    BID         = "bid";
    public static final String    SHOP_ID     = "shopId";
    public static final String    YZ_ACCOUNT  = "yzAcc";
    public static final String    ROLE        = "role";
    public static final String    STAFF_NAME  = "staffName";
    public static final String    STAFF_NO    = "staffNo";
    public static final String    STAFF_ID    = "staffId";
    public static final String    SHOP_ICON   = "shopIcon";
    public static final String    KDT_ID      = "kdtId";
    public static final String    LOGINDEVICE = "loginDevice";

    private static com.youzan.sz.session.SessionTools instance  = null;
    private static Object                             initMutex = new Object();

    private static final Logger   LOGGER      = LoggerFactory.getLogger(com.youzan.sz.session.SessionTools.class);
    private static SessionService sessionService;


    public static void init() {
        synchronized (initMutex) {
            if(sessionService!=null){
                return;
            }
            sessionService = SpringUtils.getBean(SessionService.class);
            if(sessionService==null){
                LOGGER.warn("sessionService 还未初始化,无法使用");
                return;
            }
            instance=new com.youzan.sz.session.SessionTools();
        }
    }
    /**
     * 考虑到性能原因,这里不做可见性检查,因为只有刚开始启动时,可能出现可见性问题,稍微晚一会儿获取到sessionService也可以;
     * */
    public static com.youzan.sz.session.SessionTools getInstance() {
        if(sessionService==null){
            init();
        }
        return instance;
    }

    private SessionTools() {
    }

    public void createSession() {
        sessionService.createSession();
    }

    /**
     * 移除session值(登出用)
     *
     * @return
     */
    public boolean deleteSession() {
        boolean result = sessionService.deleteSession();
        if (result) {
            DistributedContextTools.clear();
        }
        return result;
    }

    /**
     * 读取指定的session信息
     *
     * @param key
     * @return
     */
    public String get(String key) {
        return getLocalSession().get(key);
    }

    /**
     * 获取session，如果没有加载过就进行加载
     *
     * @return
     */
    public Map<String, String> getLocalSession() {
        Map<String, String> session = DistributedContextTools.get(SESSION);
        if (session == null) {
            synchronized (com.youzan.sz.session.SessionTools.class) {
                session = DistributedContextTools.get(SESSION);
                if (session == null) {
                    session = sessionService.loadSession();
                    DistributedContextTools.set(SESSION, session);
                }
            }
        }
        return session;
    }

    /**
     * 更新某一个key的值
     *
     * @param key
     * @param value
     */
    public void set(String key, String value) {
        if(StringUtils.isEmpty(value)){
            throw new BusinessException((long) ResponseCode.PARAMETER_ERROR.getCode(), ResponseCode.PARAMETER_ERROR.getMessage());
        }
        sessionService.set(key, value);
        getLocalSession().put(key, value);
    }

    /**
     * 仅更新本地的session信息
     *
     * @param key
     * @param value
     */
    public void setLocal(String key, String value) {
        getLocalSession().put(key, value);
    }

    /**
     * 获取登陆设备列表
     * @return
     */
    public List<DeviceDTO> getLoginDevice() {
        Map<String, String> session = getLocalSession();
        if (StringUtils.isNotEmpty(session.get(LOGINDEVICE))) {
            return JsonUtils.json2ListBean(session.get(LOGINDEVICE), DeviceDTO.class);
        }
        return null;
    }

    /**
     * 根据设备类型获取设备列表
     * @param deviceType 设备类型
     * @return
     */
    public List<DeviceDTO> getLoginDeviceByDeviceType(int deviceType) {
        Map<String, String> session = getLocalSession();
        if (StringUtils.isNotEmpty(session.get(LOGINDEVICE))) {
            return JsonUtils.json2ListBean(session.get(LOGINDEVICE), DeviceDTO.class).stream()
                    .filter(deviceDTO -> deviceDTO.getType() == deviceType).collect(Collectors.toList());
        }
        return null;
    }
}
