package com.youzan.sz.common.base;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.youzan.sz.common.util.JsonUtils;
import com.youzan.sz.session.SessionTools;

/**
 *
 * Created by zhanguo on 16/8/29.
 */
public abstract class BasePhpDelegate {
    protected final org.slf4j.Logger logger       = LoggerFactory.getLogger(getClass());

    private final static String      API_BASE_URL = "http://api.koudaitong.com";
    private final static String      URI_SPITOR   = "/";

    public abstract String getModel();

    public abstract String getBiz();

    protected StringBuilder getApiUrl() {
        //        PropertiesUtils.getProperty()
        String apiBaseUrl = getApiBaseUrl();
        if (apiBaseUrl != null && !apiBaseUrl.startsWith("http://")) {//如果没有指定协议头,则使用http协议
            apiBaseUrl += "http://" + apiBaseUrl;
        }
        return new StringBuilder(apiBaseUrl).append(URI_SPITOR).append(getModel()).append(URI_SPITOR).append(getBiz());
    }

    protected String getApiBaseUrl() {
        return API_BASE_URL;
    }

    //    api.koudaitong.com/account/teamCertification/getCertification?debug=web&kdt_id=1
    public String getInvokeUrl(String methodName, Map<String, String> params) {
        final String baseUrl = getApiUrl().append(URI_SPITOR).append(methodName).append("?").toString().intern();
        StringBuilder sb = new StringBuilder(baseUrl).append(getDefaultDebug());
        if (params != null) {
            for (String key : params.keySet()) {
                sb.append("&").append(key).append("=").append(params.get(key));

            }
        }
        return sb.toString();
    }

    protected static String wrapDebugModel(String debugMode) {
        if (debugMode == null) {
            return "debug=json";
        } else
            return "debug=" + debugMode;
    }

    protected String getDefaultDebug() {
        return wrapDebugModel(null);
    }

    protected Map<String, String> decode(String resp) {
        try {
            JsonUtils.json2Bean(resp, HashMap.class);
        } catch (Exception e) {
            logger.warn("decode php resp({}) encode error", resp, e);
        }
        return Collections.emptyMap();
    }

    //    public List<Map<String, String>> decodeArray(String resp) {
    //        return JsonUtils.json2ListBean(resp, HashMap.class);
    //    }
    protected static String getUserPhone() {
        return SessionTools.getInstance().get(SessionTools.YZ_ACCOUNT);
    }
}
