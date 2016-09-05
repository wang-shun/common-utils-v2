package com.youzan.sz.DistributedCallTools;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.extension.SPI;
import com.alibaba.dubbo.rpc.*;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youzan.platform.bootstrap.exception.BusinessException;
import com.youzan.sz.common.annotation.WithoutLogging;
import com.youzan.sz.common.response.enums.ResponseCode;
import com.youzan.sz.common.util.JsonUtils;
import com.youzan.sz.session.SessionTools;

/**
 * 1、处理json格式的参数，以方便调用的时候将参数设置成json格式，以方便配置和使用 <br>
 * 2、处理session信息
 *
 * @author dft
 */
@Activate(group = { Constants.PROVIDER }, order = -90000)
@SPI("web_kernel")
public class DistributedCoreWebFilter implements Filter {

    private static final Logger              LOGGER      = LoggerFactory
        .getLogger(com.youzan.sz.DistributedCallTools.DistributedCoreWebFilter.class);
    private static final ObjectMapper        om          = new ObjectMapper();
    private static final Map<String, Method> methodCache = new HashMap<>();

    static {
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

//    StaffService staffService = null;

    public DistributedCoreWebFilter() {
//        try {
            // 尝试获取账户服务
//            staffService = SpringUtils.getBean(StaffService.class);
//        } catch (Throwable e) {
//            LOGGER.warn("get the StaffService fail,if donn't need this,just ignore!");
//        }
    }

    /**
     * 获取接口的方法，如果是一个新的就先缓存一下
     *
     * @param methodName 方法名称
     * @param paramCount 参数个数
     * @param interf     接口类
     * @return 如果存在则返回方法，否则返回空
     */
    private Method getMethod(String methodName, int paramCount, Class<?> interf) {
        String key = interf.getCanonicalName() + methodName + paramCount;
        if (methodCache.containsKey(key)) {
            return methodCache.get(key);
        }
        Method[] methods = interf.getMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName) && method.getParameterTypes().length == paramCount) {
                methodCache.put(key, method);
                return method;
            }
        }
        return null;
    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {

        RpcInvocation inv = (RpcInvocation) invocation;
        // 处理通用invoke方式调用，目前是卡门调用过来的方式
        try {
            do {
                if (!inv.getMethodName().equals(Constants.$INVOKE) || inv.getArguments() == null
                    || inv.getArguments().length != 3 || invoker.getUrl().getParameter(Constants.GENERIC_KEY, false)) {
                    break;
                }
                String m = (String) inv.getArguments()[0];
                String[] typesTmp = (String[]) inv.getArguments()[1];
                Object[] argsTmp = (Object[]) inv.getArguments()[2];
                // 只处理json类型的接口，其他类型的即可忽略
                if (argsTmp.length != 1 || !"json".equals(typesTmp[0])) {
                    break;
                }
                JsonNode readValue = om.readValue((String) argsTmp[0], JsonNode.class);
                Class<?> interface1 = invoker.getInterface();
                int inputParamCount = readValue.isArray() ? readValue.size() : 1;
                Method method = getMethod(m, inputParamCount, interface1);
                if (null == method) {
                    throw new BusinessException((long) ResponseCode.METHOD_NOT_FOUND.getCode(),
                        "the method [" + m + "] not found in interface [" + interface1.getName() + "] with paramCount:"
                                                                                                + inputParamCount);
                }
                LOGGER.debug("web core filter:methodName {},inArgs:{}", method.getName(), argsTmp);
                if (method.getAnnotation(WithoutLogging.class) == null) {
                    Map<String, String> map = loadSession();
                    //除了不需要session的方法,其他方法都要做登录状态检测.
                    if (map == null) {
                        LOGGER.error("ERROR:" + ResponseCode.LOGIN_TIMEOUT.getMessage() + "接口名:" + m);
                        throw new BusinessException((long) ResponseCode.LOGIN_TIMEOUT.getCode(),
                                ResponseCode.LOGIN_TIMEOUT.getMessage());
                    }
                }
                String[] types = null;
                Object[] args = null;
                // 解析json中的参数，并进行对应映射
                if (readValue.isArray()) {
                    args = new Object[inputParamCount];
                    types = new String[inputParamCount];
                    for (int i = 0; i < args.length; i++) {
                        args[i] = om.readValue(readValue.get(i).toString(), method.getParameterTypes()[i]);
                        types[i] = method.getParameterTypes()[i].getName();
                    }

                } else {
                    args = new Object[] { om.readValue(readValue.toString(), method.getParameterTypes()[0]) };
                    types = new String[] { method.getParameterTypes()[0].getName() };
                }

                // 保存过滤掉系统参数后的结果
                inv.getArguments()[1] = types;
                inv.getArguments()[2] = args;

            } while (false);
        } catch (Throwable e) {
            LOGGER.error("distributed  error", e);
            throw new RuntimeException(e);
        }
        Result result = invoker.invoke(inv);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.info("result {}", JsonUtils.bean2Json(result.getValue()));
        }
        return result;
    }

    private Map<String, String> loadSession() {
        SessionTools session = SessionTools.getInstance();
        if (null != session) {
            return session.getLocalSession();
        } else {
            LOGGER.warn("the session not found for {}", DistributedContextTools.getAdminId());
        }
        return null;
    }

}
