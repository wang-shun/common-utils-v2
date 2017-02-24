package com.youzan.sz.DistributedCallTools;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.extension.SPI;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youzan.platform.bootstrap.exception.BusinessException;
import com.youzan.sz.common.SignOut;
import com.youzan.sz.common.annotation.WithoutLogging;
import com.youzan.sz.common.anotations.Admin;
import com.youzan.sz.common.response.enums.ResponseCode;
import com.youzan.sz.common.util.JsonUtils;
import com.youzan.sz.session.SessionTools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;


/**
 * 1、处理json格式的参数，以方便调用的时候将参数设置成json格式，以方便配置和使用 <br>
 * 2、处理session信息
 *
 * @author dft
 */
@Activate(group = {Constants.PROVIDER}, order = -90000)
@SPI("web_kernel")
public class DistributedCoreWebFilter implements Filter {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(com.youzan.sz.DistributedCallTools.DistributedCoreWebFilter.class);
    
    private static final ObjectMapper om = new ObjectMapper();
    
    private static final Map<String, Method> methodCache = new HashMap<>();
    
    static {
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    
    private void doAuth(String m, Method method, Class<?> interface1) {
        if (interface1.getAnnotation(Admin.class) != null) {//暂时不对管理进行鉴权
            return;
        }
        if (method.getAnnotation(Admin.class) != null) {
            return;
        }
        if (method.getAnnotation(WithoutLogging.class) == null && method.getAnnotation(SignOut.class) == null) {
            Map<String, String> map = loadSession();
            //除了不需要session的方法,其他方法都要做登录状态检测.
            if (map == null) {
                LOGGER.warn("ERROR:" + ResponseCode.LOGIN_TIMEOUT.getMessage() + "接口名:" + m);
                throw ResponseCode.LOGIN_TIMEOUT.getBusinessException();
            }
        }
    }
    
    
    /**
     * 获取接口的方法，如果是一个新的就先缓存一下
     *
     * @param methodName 方法名称
     * @param paramCount 参数个数
     * @param interf 接口类
     * @return 如果存在则返回方法，否则返回空
     */
    private Method getMethod(String methodName, int paramCount, Class<?> interf) {
        String key = interf.getCanonicalName() + methodName;
        if (paramCount > 0) {
            key = key + paramCount;
        }
        if (methodCache.containsKey(key)) {
            return methodCache.get(key);
        }
        boolean existByName = false; //是否存在该名称的method
        Method[] methods = interf.getMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                if (paramCount <= 0 || method.getParameterCount() == paramCount) {
                    methodCache.put(key, method);
                    return method;
                }
                existByName = true;
            }
        }
        
        if (!existByName) {
            return null;
        }
        return getMethod(methodName, -1, interf); //只按名称再查找一遍
    }
    
    
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        
        RpcInvocation inv = (RpcInvocation) invocation;
        // 处理通用invoke方式调用，目前是卡门调用过来的方式
        try {
            do {
                if (!inv.getMethodName().equals(Constants.$INVOKE) || inv.getArguments() == null || inv.getArguments().length != 3 || invoker.getUrl().getParameter(Constants.GENERIC_KEY, false)) {
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
                    throw new BusinessException((long) ResponseCode.METHOD_NOT_FOUND.getCode(), "the method [" + m + "] not found in interface [" + interface1.getName() + "] with paramCount:" +
                            inputParamCount);
                }
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("web core filter:methodName {},inArgs:{}", method.getName(), argsTmp);
                
                doAuth(m, method, interface1);
                String[] types;
                Object[] args;
                
                // 解析json中的参数，并进行对应映射
                if (readValue.isArray()) {
                    Parameter[] parameters = method.getParameters();
                    int parameterCount = parameters.length;
                    args = new Object[parameterCount];
                    types = new String[parameterCount];
                    
                    if (parameterCount > 0) {
                        JsonNode jsonNode = readValue.get(0); //取出第一个参数，判断是不是Json对象
                        Parameter parameter;
                        Class<?> parameterType;
                        
                        try {
                            if (jsonNode.isContainerNode()) { //参数是Json对象，新的方式 json=[{"bid":1,"shopId":2}]
                                for (int i = 0; i < parameterCount; i++) {
                                    parameter = parameters[i];
                                    parameterType = parameter.getType();
                                    if (ClassUtils.isPrimitiveOrWrapper(parameterType) || parameterType.equals(String.class)) {
                                        args[i] = om.readValue(jsonNode.get(parameter.getName()).toString(), parameterType);
                                    }else {
                                        args[i] = om.readValue(jsonNode.toString(), parameterType);
                                    }
                                    types[i] = parameterType.getName();
                                }
                            }else { //旧的接口传参方式 json=[1,2,3,4]
                                for (int i = 0; i < parameterCount; i++) {
                                    parameter = parameters[i];
                                    parameterType = parameter.getType();
                                    types[i] = parameterType.getName();
                                    args[i] = om.readValue(readValue.get(i).toString(), parameterType);
                                }
                            }
                        } catch (NullPointerException | ClassCastException e) {
                            LOGGER.error("请求失败，可能是参数不正确", e);
                            throw new BusinessException((long) ResponseCode.PARAMETER_ERROR.getCode(), "参数不正确", e);
                        }
                    }
                }else if (readValue.isObject()) {  // //参数是Json对象，新的方式 json={"bid":1,"shopId":2}
                    
                    Parameter[] parameters = method.getParameters();
                    int parameterCount = parameters.length;
                    args = new Object[parameterCount];
                    types = new String[parameterCount];
                    
                    if (parameterCount > 0) {
                        Parameter parameter;
                        Class<?> parameterType;
                        
                        try {
                            for (int i = 0; i < parameterCount; i++) {
                                parameter = parameters[i];
                                parameterType = parameter.getType();
                                if (ClassUtils.isPrimitiveOrWrapper(parameterType) || parameterType.equals(String.class) || parameterCount > 1) {
                                    args[i] = om.readValue(readValue.get(parameter.getName()).toString(), parameterType);
                                }else {
                                    args[i] = om.readValue(readValue.toString(), parameterType);
                                }
                                types[i] = parameterType.getName();
                            }
                        } catch (NullPointerException | ClassCastException e) {
                            LOGGER.error("请求失败，可能是参数不正确", e);
                            throw new BusinessException((long) ResponseCode.PARAMETER_ERROR.getCode(), "参数不正确", e);
                        }
                    }
                    
                }else {
                    args = new Object[]{om.readValue(readValue.toString(), method.getParameterTypes()[0])};
                    types = new String[]{method.getParameterTypes()[0].getName()};
                }
                
                // 保存过滤掉系统参数后的结果
                inv.getArguments()[1] = types;
                inv.getArguments()[2] = args;
                
            }
            while (false);
        } catch (Throwable e) {
            LOGGER.warn("distributed  error", e);
            throw new RuntimeException(e);
        }
        Result result = invoker.invoke(inv);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("result {}", JsonUtils.bean2Json(result.getValue()));
        }
        return result;
    }
    
    
    private Map<String, String> loadSession() {
        SessionTools session = SessionTools.getInstance();
        if (null != session) {
            return session.getLocalSession();
        }else {
            LOGGER.warn("the session not found for {}", DistributedContextTools.getAdminId());
        }
        return null;
    }
    
}
