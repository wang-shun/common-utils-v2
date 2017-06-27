package com.youzan.sz.session;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.youzan.sz.DistributedCallTools.DistributedContextTools;
import com.youzan.sz.common.exceptions.BizException;
import com.youzan.sz.common.model.dto.DeleteTicketDTO;
import com.youzan.sz.common.model.oa.DeviceDTO;
import com.youzan.sz.common.response.BaseResponse;
import com.youzan.sz.common.response.enums.ResponseCode;
import com.youzan.sz.common.service.SessionService;
import com.youzan.sz.common.util.JsonUtils;
import com.youzan.sz.common.util.SpringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class SessionTools {

    public static final String SESSION = "session";

    public static final String ADMINID = "adminId";

    public static final String BID = "bid";

    public static final String SHOP_ID = "shopId";

    public static final String YZ_ACCOUNT = "yzAcc";

    public static final String ROLE = "role";

    public static final String STAFF_NAME = "staffName";

    public static final String STAFF_NO = "staffNo";

    public static final String STAFF_ID = "staffId";

    public static final String SHOP_ICON = "shopIcon";

    public static final String SHOP_NAME = "shopName";

    public static final String KDT_ID = "kdtId";//绑定网店id

    public static final String LOGINDEVICE = "loginDevice";

    public static final String AID = "aid";        //应用id

    public static final String CERT_STATUS = "certStatus"; //认证状态.0,未认证,1,个人人工,3,企业认证

    public static final String IDENTITY = "identity";//用户身份信息（上帝账号用）

    private static final Logger LOGGER = LoggerFactory.getLogger(com.youzan.sz.session.SessionTools.class);

    private static final Object INIT_MUTEX = new Object();

    private static com.youzan.sz.session.SessionTools instance = null;

    private static SessionService sessionService;


    private SessionTools() {
    }


    /**
     * 考虑到性能原因,这里不做可见性检查,因为只有刚开始启动时,可能出现可见性问题,稍微晚一会儿获取到sessionService也可以;
     */
    public static SessionTools getInstance() {
        if (sessionService == null) {
            init();
        }
        return instance;
    }


    public static void init() {
        synchronized (INIT_MUTEX) {
            if (sessionService != null) {
                return;
            }
            sessionService = SpringUtils.getBean(SessionService.class);
            if (sessionService == null) {
                LOGGER.warn("sessionService 还未初始化,无法使用");
                return;
            }
            instance = new com.youzan.sz.session.SessionTools();
        }
    }


    public void addDevice(String deviceId) {
        sessionService.addDevice(deviceId);
    }


    public void createSession() {
        sessionService.createSession();
    }


    /**
     * 移除session值(登出用)
     */
    public boolean deleteSession() {
        DeleteTicketDTO deleteTicketDTO = new DeleteTicketDTO();
        Long adminId = DistributedContextTools.getAdminId();
        deleteTicketDTO.setAdminId(adminId);
        BaseResponse baseResponse = sessionService.deleteSession(deleteTicketDTO);
        //        boolean result = sessionService.deleteDirectSession();
        if (baseResponse.isSucc()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("delete adminId:{} session succ,clear contexts", adminId);
            }
            DistributedContextTools.clear();
        }else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("delete adminId:{} session fail,skip clear contexts", adminId);
            }
        }
        return baseResponse.isSucc();
    }


    /**
     * 读取指定的session信息
     */
    public String get(String key) {
        return getLocalSession().get(key);
    }


    /**
     * 获取session，如果没有加载过就进行加载
     */
    public Map<String, String> getLocalSession() {
        Map<String, String> session = DistributedContextTools.get(SESSION);
        if (session == null) {
            session = sessionService.loadSession();
            DistributedContextTools.set(SESSION, session);
        }
        //从session中加载一次
        String bid = session.get(SessionTools.BID);
        String kdtId = session.get(SessionTools.KDT_ID);
        String shopId = session.get(SessionTools.SHOP_ID);
        String aid = session.get(SessionTools.AID);
        String identity = session.get(SessionTools.IDENTITY);
        if (bid != null) {
            DistributedContextTools.setAttr(DistributedContextTools.DistributedParamManager.Bid.class, Long.valueOf(bid));
            DistributedContextTools.setAttr(DistributedContextTools.DistributedParamManager.KdtId.class, Long.valueOf(bid));
        }
        if (kdtId != null) {
            DistributedContextTools.setAttr(DistributedContextTools.DistributedParamManager.Bid.class, Long.valueOf(kdtId));
            DistributedContextTools.setAttr(DistributedContextTools.DistributedParamManager.KdtId.class, Long.valueOf(kdtId));
        }

        if (shopId != null) {
            DistributedContextTools.setAttr(DistributedContextTools.DistributedParamManager.ShopId.class, Long.valueOf(shopId));
        }
        if (aid != null) {
            if (aid.equals("null")) {
                LOGGER.warn("数据异常,aid({}) is null", aid);
            }else {
                DistributedContextTools.setAttr(DistributedContextTools.DistributedParamManager.Aid.class, Integer.valueOf(aid));
            }
        }
        if (identity != null) {
            DistributedContextTools.setAttr(DistributedContextTools.DistributedParamManager.Identity.class, Integer.valueOf(identity));
        }
        return session;
    }


    /**
     * 获取登陆设备列表
     */
    public List<DeviceDTO> getLoginDevice() {
        Map<String, String> session = getLocalSession();
        if (StringUtils.isNotEmpty(session.get(LOGINDEVICE))) {
            return JsonUtils.json2ListBean(session.get(LOGINDEVICE), DeviceDTO.class);
        }
        return Collections.emptyList();
    }


    /**
     * 根据设备类型获取设备列表
     *
     * @param deviceType 设备类型
     */
    public List<DeviceDTO> getLoginDeviceByDeviceType(int deviceType) {
        Map<String, String> session = getLocalSession();
        if (StringUtils.isNotEmpty(session.get(LOGINDEVICE))) {
            return JsonUtils.json2ListBean(session.get(LOGINDEVICE), DeviceDTO.class).stream().filter(deviceDTO -> deviceDTO.getType() == deviceType).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }


    /**
     * 更新某一个key的值
     */
    public void set(String key, String value) {
        if (StringUtils.isEmpty(value)) {
            throw new BizException(ResponseCode.PARAMETER_ERROR);
        }
        sessionService.set(key, value);
        getLocalSession().put(key, value);
    }


    /**
     * 仅更新本地的session信息
     */
    public void setLocal(String key, String value) {
        getLocalSession().put(key, value);
    }


    public void updateShopBind() {
        final BaseResponse baseResponse = sessionService.updateShopBind();
        if (!baseResponse.isSucc()) {
            LOGGER.warn("update shop bind failed");
        }
    }

}
