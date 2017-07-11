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
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.rpc.protocol.dubbo.DecodeableRpcInvocation;
import com.youzan.api.common.response.ListResult;
import com.youzan.api.common.response.PlainResult;
import com.youzan.platform.bootstrap.exception.BusinessException;
import com.youzan.sz.DistributedCallTools.DistributedContextTools.DistributedParamManager;
import com.youzan.sz.DistributedCallTools.DistributedContextTools.DistributedParamManager.AdminId;
import com.youzan.sz.DistributedCallTools.DistributedContextTools.DistributedParamManager.Aid;
import com.youzan.sz.DistributedCallTools.DistributedContextTools.DistributedParamManager.AppVersion;
import com.youzan.sz.DistributedCallTools.DistributedContextTools.DistributedParamManager.Bid;
import com.youzan.sz.DistributedCallTools.DistributedContextTools.DistributedParamManager.ClientId;
import com.youzan.sz.DistributedCallTools.DistributedContextTools.DistributedParamManager.DeviceId;
import com.youzan.sz.DistributedCallTools.DistributedContextTools.DistributedParamManager.DeviceType;
import com.youzan.sz.DistributedCallTools.DistributedContextTools.DistributedParamManager.Identity;
import com.youzan.sz.DistributedCallTools.DistributedContextTools.DistributedParamManager.KdtId;
import com.youzan.sz.DistributedCallTools.DistributedContextTools.DistributedParamManager.NoSession;
import com.youzan.sz.DistributedCallTools.DistributedContextTools.DistributedParamManager.OpAdminId;
import com.youzan.sz.DistributedCallTools.DistributedContextTools.DistributedParamManager.OpAdminName;
import com.youzan.sz.DistributedCallTools.DistributedContextTools.DistributedParamManager.OpenApi;
import com.youzan.sz.DistributedCallTools.DistributedContextTools.DistributedParamManager.RequestIp;
import com.youzan.sz.DistributedCallTools.DistributedContextTools.DistributedParamManager.ShopId;
import com.youzan.sz.common.exceptions.BizException;
import com.youzan.sz.common.response.BaseResponse;
import com.youzan.sz.common.response.enums.ResponseCode;
import com.youzan.sz.common.util.JsonUtils;
import com.youzan.sz.common.util.MdcUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;


/**
 * 处理卡门中的通用参数信息，使用方式为，只要卡门传过来的参数类型是distributed.开头的参数都是系统处理范围，不过为了避免滥用，
 * 所以目前程序里面只处理admin_id、request_ip和kdt_id几个参数，后续需要增加什么参数只需要按照逻辑添加即可，如果在调用非体系内的服务时
 * ，需要在dubbo:reference标签里面增加filter="-kernel"的属性来去掉该过滤器，因为该过滤器在发起调用的时候会自动添加
 * 系统参数，这样会导致没有使用该过滤器的服务无法调通
 *
 * @author dft
 */
@Activate(group = {Constants.PROVIDER, Constants.CONSUMER}, order = -100000)
@SPI("kernel")
public class DistributedCoreFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(com.youzan.sz.DistributedCallTools.DistributedCoreFilter.class);

    private static final String MDC_TRACE = "MDC_TRACE";
    
    private static final int CARMEN_SUCCESS_CODE = 200;

    private ThreadLocal<Stack<Integer>> stackLocal = ThreadLocal.withInitial(() -> new Stack<Integer>());


    public String getThrowableStr(Throwable e) {
        if (e == null) {
            return "";
        }

        ArrayWriter aw = new ArrayWriter();
        e.printStackTrace(aw);
        String[] arr = aw.toStringArray();
        if (arr == null) {
            return "";
        }

        StringBuilder strBuf = new StringBuilder();
        for (String anArr : arr) {
            strBuf.append(anArr).append("####");
        }
        return strBuf.toString();
    }


    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {

        RpcInvocation inv = (RpcInvocation) invocation;
        String method = "";
        Result invoke = null;
        boolean isSuccess = true;
        long t = System.currentTimeMillis();

        // provider侧的调用处理
        if (inv instanceof DecodeableRpcInvocation || "true".equals(inv.getAttachment(Constants.GENERIC_KEY))) {
            inv.setAttachment(Constants.ASYNC_KEY, "false");

            try {
                // 设置请求的唯一key，方便日志的grep
                initLogMdc();

                // 处理通用invoke方式调用，目前是卡门调用过来的方式
                if (inv.getMethodName().equals(Constants.$INVOKE) && inv.getArguments() != null && inv.getArguments().length == 3 && !invoker.getUrl().getParameter(Constants.GENERIC_KEY, false)) {
                    try {
                        method = (String) inv.getArguments()[0];
                        String[] typesTmp = (String[]) inv.getArguments()[1];
                        Object[] argsTmp = (Object[]) inv.getArguments()[2];
                        List<String> types = new ArrayList<>();
                        List<Object> args = new ArrayList<>();
                        // 将系统级的分布式变量放到统一的分布式上下文里面，同时将他们从传入参数中去除
                        for (int i = 0; i < typesTmp.length; i++) {
                            if (DistributedParamManager.isDistributedParam(typesTmp[i])) {
                                if (DistributedParamManager.CarmenParam.getName().equals(typesTmp[i])) {
                                    Map<String, Object> carmenParam = (Map<String, Object>) argsTmp[i];
                                    if (LOGGER.isInfoEnabled()) {
                                        LOGGER.info("openApi {}", JsonUtils.toJson(carmenParam));
                                    }
                                    //设置openApi参数
                                    Object kdtId = carmenParam.get(DistributedParamManager.KdtId.getCarmenName());
                                    if (kdtId != null) {
                                        DistributedContextTools.set(DistributedParamManager.KdtId.class, kdtId);
                                        DistributedContextTools.set(DistributedParamManager.Bid.class, kdtId);
                                    }

                                    Object adminId = carmenParam.get(DistributedParamManager.AdminId.getCarmenName());
                                    if (adminId != null) {
                                        DistributedContextTools.set(DistributedParamManager.AdminId.class, adminId);
                                    }

                                    Object requestIp = carmenParam.get(DistributedParamManager.RequestIp.getCarmenName());
                                    if (requestIp != null) {
                                        DistributedContextTools.set(DistributedParamManager.RequestIp.class, requestIp);
                                    }

                                    Object clientId = carmenParam.get(DistributedParamManager.ClientId.getCarmenName());
                                    if (clientId != null) {
                                        DistributedContextTools.set(DistributedParamManager.ClientId.class, clientId);
                                    }

                                    DistributedContextTools.set(DistributedParamManager.OpenApi.class, true);
                                    DistributedContextTools.set(DistributedParamManager.DeviceType.class, String.valueOf(com.youzan.sz.common.model.enums.DeviceType.CARMEN.getValue()));

                                }else {
                                    Class<?> key = DistributedParamManager.get(typesTmp[i]);
                                    DistributedContextTools.set(key, argsTmp[i]);
                                }
                                continue;
                            }
                            types.add(typesTmp[i]);
                            args.add(argsTmp[i]);
                        }
                        // 保存过滤掉系统参数后的结果
                        inv.getArguments()[1] = types.toArray(new String[0]);
                        inv.getArguments()[2] = args.toArray();

                        invoke = invoker.invoke(inv);
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("core filter,path:{}:methodName:{},inArgs:{}", inv.getAttachment("path"), method, inv.getMethodName(), JsonUtils.bean2Json(argsTmp));
                        }

                        if (invoke.hasException()) {
                            isSuccess = false;
                        }
                    } catch (Throwable e) {
                        isSuccess = false;
                        invoke = new RpcResult(e);
                    }
                    return dealResult(invoke, inv);
                }else {// 处理普通的rpc调用，即使用dubbo客户端直接调用的场景
                    String adminId = inv.getAttachment(AdminId.class.getCanonicalName());
                    if (null != adminId) {
                        DistributedContextTools.setAttr(AdminId.class, Long.valueOf(adminId));
                    }
                    String requestIp = inv.getAttachment(RequestIp.class.getCanonicalName());
                    if (null != requestIp) {
                        DistributedContextTools.setAttr(RequestIp.class, requestIp);
                    }
                    String kdtId = inv.getAttachment(KdtId.class.getCanonicalName());
                    if (null != kdtId) {
                        DistributedContextTools.setAttr(KdtId.class, Long.valueOf(kdtId));
                    }
                    String deviceId = inv.getAttachment(DeviceId.class.getCanonicalName());
                    if (null != deviceId) {
                        DistributedContextTools.setAttr(DeviceId.class, deviceId);
                    }
                    String deviceType = inv.getAttachment(DeviceType.class.getCanonicalName());
                    if (null != deviceType) {
                        DistributedContextTools.setAttr(DeviceType.class, deviceType);
                    }

                    String aid = inv.getAttachment(Aid.class.getCanonicalName());
                    if (aid != null) {
                        DistributedContextTools.set(Aid.class.getCanonicalName(), String.valueOf(aid));
                    }
                    String bid = inv.getAttachment(Bid.class.getCanonicalName());
                    if (bid != null) {
                        DistributedContextTools.set(Bid.class.getCanonicalName(), String.valueOf(bid));
                    }
                    String shopId = inv.getAttachment(ShopId.class.getCanonicalName());
                    if (shopId != null) {
                        DistributedContextTools.set(ShopId.class.getCanonicalName(), String.valueOf(shopId));
                    }
                    String opAdminId = inv.getAttachment(OpAdminId.class.getCanonicalName());
                    if (opAdminId != null) {
                        DistributedContextTools.set(OpAdminId.class.getCanonicalName(), String.valueOf(opAdminId));
                    }
                    String opAdminName = inv.getAttachment(OpAdminName.class.getCanonicalName());
                    if (opAdminName != null) {
                        DistributedContextTools.set(OpAdminName.class.getCanonicalName(), String.valueOf(opAdminName));
                    }
                    String appVersion = inv.getAttachment(AppVersion.class.getCanonicalName());
                    if (appVersion != null) {
                        DistributedContextTools.set(AppVersion.class.getCanonicalName(), String.valueOf(appVersion));
                    }
                    String noSession = inv.getAttachment(NoSession.class.getCanonicalName());
                    if (noSession != null) {
                        DistributedContextTools.set(NoSession.class.getCanonicalName(), String.valueOf(noSession));
                    }
                    String identity = inv.getAttachment(Identity.class.getCanonicalName());
                    if (identity != null) {
                        DistributedContextTools.set(Identity.class.getCanonicalName(), Integer.valueOf(identity));
                    }
                    String clientId = inv.getAttachment(ClientId.class.getCanonicalName());
                    if (clientId != null) {
                        DistributedContextTools.set(ClientId.class.getCanonicalName(), clientId);
                    }
                    String openApi = inv.getAttachment(OpenApi.class.getCanonicalName());
                    if (openApi != null) {
                        DistributedContextTools.set(OpenApi.class.getCanonicalName(), Boolean.valueOf(openApi));
                    }
                }
                invoke = invoker.invoke(inv);
                if (invoke.hasException()) {
                    isSuccess = false;
                }
                return invoke;
            } catch (Throwable e) {
                LOGGER.warn("normal rpc invoke fail", e);
                isSuccess = false;
                return new RpcResult(e);

            } finally {
                // 调用结束后要清理掉分布式上下文，不然会有内存泄露和脏数据
                DistributedContextTools.clear();
                LOGGER.info("p:|" + method + "|" + (System.currentTimeMillis() - t) + "|" + isSuccess);
                clearLogMdc();
            }
        }else {
            try {
                // 设置请求的唯一key，方便日志的grep
                initLogMdc();
                // TODO: 16/6/27 登陆接口访问票据不需要存放上下文
                // 获取需要传递的平台参数
                Long adminId = DistributedContextTools.getAdminId();
                String requestIp = DistributedContextTools.getRequestIp();
                Long KdtId = DistributedContextTools.getKdtId();
                String deviceId = DistributedContextTools.getDeviceId();
                Long bid = DistributedContextTools.getBid();
                Integer aid = DistributedContextTools.getAId();
                Long shopId = DistributedContextTools.getShopId();
                String deviceType = DistributedContextTools.getDeviceType();
                Long opAdminId = DistributedContextTools.getOpAdminId();
                String opAdminName = DistributedContextTools.getOpAdminName();
                String appVersion = DistributedContextTools.getAppVersion();
                Integer noSession = DistributedContextTools.getNoSession();
                String clientId = DistributedContextTools.getClientId();
                Boolean isOpenApi = DistributedContextTools.getOpenApi();
                method = inv.getMethodName();

                if (null != adminId) {
                    inv.setAttachment(AdminId.class.getCanonicalName(), adminId + "");
                }
                if (null != requestIp) {
                    inv.setAttachment(RequestIp.class.getCanonicalName(), requestIp);
                }
                if (null != KdtId) {
                    inv.setAttachment(KdtId.class.getCanonicalName(), KdtId + "");
                }
                if (null != deviceId) {
                    inv.setAttachment(DeviceId.class.getCanonicalName(), deviceId);
                }
                if (null != deviceType) {
                    inv.setAttachment(DeviceType.class.getCanonicalName(), deviceType + "");
                }
                if (aid != null) {
                    inv.setAttachment(Aid.class.getCanonicalName(), aid.toString());
                }
                if (bid != null) {
                    inv.setAttachment(Bid.class.getCanonicalName(), bid.toString());
                }
                if (shopId != null) {
                    inv.setAttachment(ShopId.class.getCanonicalName(), shopId.toString());
                }
                if (opAdminId != null)
                    inv.setAttachment(OpAdminId.class.getCanonicalName(), opAdminId.toString());
                if (opAdminName != null)
                    inv.setAttachment(OpAdminName.class.getCanonicalName(), opAdminName);
                // app版本信息
                if (appVersion != null) {
                    inv.setAttachment(DistributedParamManager.AppVersion.class.getCanonicalName(), appVersion);
                }

                if (noSession != null) {
                    inv.setAttachment(DistributedParamManager.NoSession.class.getCanonicalName(), noSession.toString());
                }

                if (clientId != null) {
                    inv.setAttachment(DistributedParamManager.ClientId.class.getCanonicalName(), clientId);
                }

                if (isOpenApi != null) {
                    inv.setAttachment(DistributedParamManager.OpenApi.class.getCanonicalName(), isOpenApi.toString());
                }

                invoke = invoker.invoke(inv);
                if (invoke.hasException()) {
                    isSuccess = false;
                }
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("outArgs[{}]", JsonUtils.bean2Json(invoke.getValue()));
                }
                return invoke;
            } catch (Throwable e) {
                LOGGER.warn("consumer invoke fail", e);
                isSuccess = false;
                return new RpcResult(e);
            } finally {
                LOGGER.info("c:|" + method + "|" + (System.currentTimeMillis() - t) + "|" + isSuccess);
                clearLogMdc();
            }
        }

    }


    /**
     * 设置请求的唯一key，方便日志的grep
     */
    private void initLogMdc() {
        if (isLogMdc()) {
            Stack<Integer> stack = stackLocal.get();
            if (stack.isEmpty()) {
                // 设置请求的唯一key，方便日志的grep
                MDC.put(MDC_TRACE, MdcUtil.createMDCTraceId());
            }

            stack.push(1);
        }
    }


    /**
     * 处理通用调用类型的返回对象结果，需要将返回对象包装成baseresponse对象
     */
    Result dealResult(Result invoke, Invocation invocation) {
        // 统一处理返回值，一遍能够达到给卡门使用的要求，同时对于卡门接口就不在返回异常了，统一包装成错误消息
        RpcResult rpcResult = (RpcResult) invoke;
        BaseResponse br = null;
        // 对于异常信息，统一进行包装
        if (invoke.hasException()) {

            if (invoke.getException().getCause() instanceof BizException) {
                BizException be = (BizException) invoke.getException().getCause();
                br = new BaseResponse<>(be.getCode().intValue(), be.getMessage(), be.getData());
            }else if (invoke.getException() instanceof BusinessException) {
                BusinessException be = (BusinessException) invoke.getException();
                br = new BaseResponse<>(be.getCode().intValue(), be.getMessage(), invoke.getValue());

            }else if (invoke.getException().getCause() instanceof BusinessException) {
                BusinessException be = (BusinessException) invoke.getException().getCause();
                br = new BaseResponse<>(be.getCode().intValue(), be.getMessage(), invoke.getValue());
            }else {
                br = new BaseResponse<>(ResponseCode.ERROR.getCode(), invoke.getException().getMessage(), invoke.getValue());
            }
            LOGGER.warn("rpc invoke exception:{}", invoke.getException());
            // 变更处理后需要清空原有的异常信息
            rpcResult.setException(null);
            rpcResult.setValue(br);
        }else if (invoke.getValue() instanceof Map) {
            // 这种通用调用返回结果也会被转换成map形式，所以这里要进行进一步判断
            String invokeClass;
            int resultCode = ResponseCode.SUCCESS.getCode();
            if ("true".equals(invocation.getAttachment(CarmenCodec.CARMEN_CODEC))) {
                invokeClass = (String) ((Map) invoke.getValue()).remove("class");
            }else if (DistributedContextTools.getOpenApi()) {
                invokeClass = (String) ((Map) invoke.getValue()).remove("class");
                resultCode = CARMEN_SUCCESS_CODE;
            }else {
                invokeClass = (String) ((Map) invoke.getValue()).get("class");
            }

            if (ListResult.class.getName().equals(invokeClass)) {//返回listResult
                final Object data = ((Map) invoke.getValue()).get("data");
                final ListResult listResult = new ListResult();
                listResult.setCount((Integer) ((Map) invoke.getValue()).get("count"));
                listResult.setData((List) data);
                listResult.setCode(resultCode);
                listResult.setMessage((String) ((Map) invoke.getValue()).get("message"));
                rpcResult.setValue(listResult);
            }else if (PlainResult.class.getName().equals(invokeClass)) {//返回listResult
                final Object data = ((Map) invoke.getValue()).get("data");
                final PlainResult plainResult = new PlainResult<>();
                Integer code = (Integer) ((Map) invoke.getValue()).get("code");
                if (ResponseCode.SUCCESS.getCode() == code && DistributedContextTools.getOpenApi()) {
                    code = CARMEN_SUCCESS_CODE;
                }
                plainResult.setCode(code);
                plainResult.setData(data);
                plainResult.setMessage((String) ((Map) invoke.getValue()).get("message"));
                rpcResult.setValue(plainResult);
            }else if (!BaseResponse.class.getName().equals(invokeClass)) {
                //todo 此场景openapi还没有测试到
                br = new BaseResponse<>(resultCode, ResponseCode.SUCCESS.getMessage(), invoke.getValue());
                rpcResult.setValue(br);
            }else {
                final Object data = ((Map) invoke.getValue()).get("data");
                Integer code = (Integer) ((Map) invoke.getValue()).get("code");
                if (ResponseCode.SUCCESS.getCode() == code && DistributedContextTools.getOpenApi()) {
                    code = CARMEN_SUCCESS_CODE;
                }
                br = new BaseResponse<>(code, (String) ((Map) invoke.getValue()).get("message"), data);
                rpcResult.setValue(br);
            }

        }else if (!(invoke.getValue() instanceof BaseResponse)) {
            br = new BaseResponse<>(ResponseCode.SUCCESS.getCode(), ResponseCode.SUCCESS.getMessage(), invoke.getValue());
            rpcResult.setValue(br);
        }
        rpcResult.setAttachment(CarmenCodec.CARMEN_CODEC, invocation.getAttachment(CarmenCodec.CARMEN_CODEC));
        return invoke;
    }


    private void clearLogMdc() {
        if (isLogMdc()) {
            Stack<Integer> stack = stackLocal.get();
            stack.pop();
            if (stack.isEmpty()) {
                // 设置请求的唯一key，方便日志的grep
                MDC.remove(MDC_TRACE);
                stackLocal.remove();
            }
        }
    }


    private boolean isLogMdc() {
        return LOGGER.isInfoEnabled();
    }

}
