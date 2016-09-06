package com.youzan.sz.DistributedCallTools;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.extension.SPI;
import com.alibaba.dubbo.rpc.*;
import com.alibaba.dubbo.rpc.protocol.dubbo.DecodeableRpcInvocation;
import com.youzan.platform.bootstrap.exception.BusinessException;
import com.youzan.sz.DistributedCallTools.DistributedContextTools.DistributedParamManager;
import com.youzan.sz.DistributedCallTools.DistributedContextTools.DistributedParamManager.*;
import com.youzan.sz.common.exceptions.BizException;
import com.youzan.sz.common.response.BaseResponse;
import com.youzan.sz.common.response.enums.ResponseCode;
import com.youzan.sz.common.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private static final Logger LOGGER = LoggerFactory
            .getLogger(com.youzan.sz.DistributedCallTools.DistributedCoreFilter.class);

    /**
     * 处理通用调用类型的返回对象结果，需要将返回对象包装成baseresponse对象
     *
     * @param invoke
     * @return
     */
    Result dealResult(Result invoke, Invocation invocation) {
        // 统一处理返回值，一遍能够达到给卡门使用的要求，同时对于卡门接口就不在返回异常了，统一包装成错误消息
        RpcResult rpcResult = (RpcResult) invoke;
        BaseResponse br = null;
        // 对于异常信息，统一进行包装
        if (invoke.hasException()) {
            if (invoke.getException() instanceof BizException) {
                BizException be = (BizException) invoke.getException();
                br = new BaseResponse<>(be.getCode().intValue(), be.getMessage(), be.getData());
            } else if (invoke.getException() instanceof BusinessException) {
                BusinessException be = (BusinessException) invoke.getException();
                br = new BaseResponse<>(be.getCode().intValue(), be.getMessage() + "####" + getThrowableStr(invoke.getException()),
                        invoke.getValue());

            } else if (invoke.getException().getCause() instanceof BusinessException) {
                BusinessException be = (BusinessException) invoke.getException().getCause();
                br = new BaseResponse<>(be.getCode().intValue(), getThrowableStr(be), invoke.getValue());
            } else {
                br = new BaseResponse<>(ResponseCode.ERROR.getCode(), null, invoke.getValue());
            }
            LOGGER.error("rpc invoke exception:{}", invoke.getException());
            // 变更处理后需要清空原有的异常信息
            rpcResult.setException(null);
            rpcResult.setValue(br);
        } else if (invoke.getValue() instanceof Map) {
            // 这种通用调用返回结果也会被转换成map形式，所以这里要进行进一步判断
            String invokeClass;
            if ("true".equals(invocation.getAttachment(CarmenCodec.CARMEN_CODEC))) {
                invokeClass = (String) ((Map) invoke.getValue()).remove("class");
            } else {
                invokeClass = (String) ((Map) invoke.getValue()).get("class");
            }
            if (((Map) invoke.getValue()).remove("class") != null) {
                LOGGER.info("remove class succ");
            }
            if (!BaseResponse.class.getName().equals(invokeClass)) {
                br = new BaseResponse(ResponseCode.SUCCESS.getCode(), ResponseCode.SUCCESS.getMessage(),
                        invoke.getValue());
                rpcResult.setValue(br);
            } else {
                final Object data = ((Map) invoke.getValue()).get("data");
                if (data != null && data instanceof HashMap) {
                    ((HashMap) data).remove("class");
                }
                br = new BaseResponse((Integer) ((Map) invoke.getValue()).get("code"),

                        (String) ((Map) invoke.getValue()).get("message"), data);
                rpcResult.setValue(br);
            }

        } else if (!(invoke.getValue() instanceof BaseResponse)) {
            br = new BaseResponse(ResponseCode.SUCCESS.getCode(), ResponseCode.SUCCESS.getMessage(), invoke.getValue());
            rpcResult.setValue(br);
        }
        rpcResult.setAttachment(CarmenCodec.CARMEN_CODEC, invocation.getAttachment(CarmenCodec.CARMEN_CODEC));
        return invoke;
    }

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
        for (int i = 0; i < arr.length; i++) {
            strBuf.append(arr[i]).append("####");
        }
        return strBuf.toString();
    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {

        RpcInvocation inv = (RpcInvocation) invocation;
        String m = "";
        Result invoke = null;
        boolean isSucess = true;
        long t = System.currentTimeMillis();

        // provider侧的调用处理
        if (inv instanceof DecodeableRpcInvocation || "true".equals(inv.getAttachment(Constants.GENERIC_KEY))) {
            inv.setAttachment(Constants.ASYNC_KEY, "false");

            try {
                // 处理通用invoke方式调用，目前是卡门调用过来的方式
                if (inv.getMethodName().equals(Constants.$INVOKE) && inv.getArguments() != null
                        && inv.getArguments().length == 3 && !invoker.getUrl().getParameter(Constants.GENERIC_KEY, false)) {
                    try {
                        m = (String) inv.getArguments()[0];
                        String[] typesTmp = (String[]) inv.getArguments()[1];
                        Object[] argsTmp = (Object[]) inv.getArguments()[2];
                        List<String> types = new ArrayList<>();
                        List<Object> args = new ArrayList<>();
                        // 将系统级的分布式变量放到统一的分布式上下文里面，同时将他们从传入参数中去除
                        for (int i = 0; i < typesTmp.length; i++) {
                            if (DistributedParamManager.isDistributedParam(typesTmp[i])) {
                                Class<?> key = DistributedParamManager.get(typesTmp[i]);
                                DistributedContextTools.set(key, argsTmp[i]);
                                continue;
                            }
                            types.add(typesTmp[i]);
                            args.add(argsTmp[i]);
                        }
                        // 保存过滤掉系统参数后的结果
                        inv.getArguments()[1] = types.toArray(new String[0]);
                        inv.getArguments()[2] = args.toArray();

                        invoke = invoker.invoke(inv);
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.info("core filter,path:{}:methodName:{},inArgs:{}", inv.getAttachment("path"), m,
                                    inv.getMethodName(), argsTmp);
                        }

                        if (invoke.hasException()) {
                            isSucess = false;
                        }
                    } catch (Throwable e) {
                        isSucess = false;
                        invoke = new RpcResult(e);
                    }
                    return dealResult(invoke, inv);
                } else {// 处理普通的rpc调用，即使用dubbo客户端直接调用的场景
                    String adminId = inv.getAttachment(AdminId.class.getCanonicalName());
                    if (null != adminId) {
                        DistributedContextTools.set(AdminId.class, Long.valueOf(adminId));
                    }
                    String requestIp = inv.getAttachment(RequestIp.class.getCanonicalName());
                    if (null != requestIp) {
                        DistributedContextTools.set(RequestIp.class, requestIp);
                    }
                    String kdtId = inv.getAttachment(KdtId.class.getCanonicalName());
                    if (null != kdtId) {
                        DistributedContextTools.set(KdtId.class, Long.valueOf(kdtId));
                    }
                    String deviceId = inv.getAttachment(DeviceId.class.getCanonicalName());
                    if (null != deviceId) {
                        DistributedContextTools.set(DeviceId.class, deviceId);
                    }
                    String deviceType = inv.getAttachment(DeviceType.class.getCanonicalName());
                    if (null != deviceType) {
                        DistributedContextTools.set(DeviceType.class, deviceType);
                    }
                    final String aid = inv.getAttachment(Aid.class.getCanonicalName());

                    if (aid != null) {
                        DistributedContextTools.set(Aid.class.getCanonicalName(), String.valueOf(aid));
                    }
                    final String bId = inv.getAttachment(Bid.class.getCanonicalName());
                    if (bId != null) {
                        DistributedContextTools.set(Bid.class.getCanonicalName(), String.valueOf(bId));
                    }
                    final String shopId = inv.getAttachment(ShopId.class.getCanonicalName());
                    if (shopId != null) {
                        DistributedContextTools.set(ShopId.class.getCanonicalName(), String.valueOf(shopId));
                    }
                }
                invoke = invoker.invoke(inv);
                if (invoke.hasException()) {
                    isSucess = false;
                }
                return invoke;
            } finally {
                // 调用结束后要清理掉分布式上下文，不然会有内存泄露和脏数据
                DistributedContextTools.clear();
                LOGGER.info("p:|" + m + "|" + (System.currentTimeMillis() - t) + "|" + isSucess);
            }
        } else {
            try {
                // TODO: 16/6/27 登陆接口访问票据不需要存放上下文
                // 获取需要传递的平台参数
                Long adminId = DistributedContextTools.getAdminId();
                String requestIp = DistributedContextTools.getRequestIp();
                Long KdtId = DistributedContextTools.getKdtId();
                String deviceId = DistributedContextTools.getDeviceId();
                final Long bId = DistributedContextTools.getBId();
                final Integer aid = DistributedContextTools.getAId();
                final Long shopId = DistributedContextTools.getShopId();
                String deviceType = DistributedContextTools.getDeviceType();
                m = inv.getMethodName();
                if (null != adminId) {
                    inv.setAttachment(AdminId.class.getCanonicalName(), adminId + "");
                }
                if (null != requestIp) {
                    inv.setAttachment(RequestIp.class.getCanonicalName(), requestIp);
                }
                if (null != KdtId) {
                    inv.setAttachment(DistributedParamManager.KdtId.class.getCanonicalName(), KdtId + "");
                }
                if (null != deviceId) {
                    inv.setAttachment(DeviceId.class.getCanonicalName(), deviceId + "");
                }
                if (null != deviceType) {
                    inv.setAttachment(DeviceType.class.getCanonicalName(), deviceType + "");
                }
                if (aid != null) {
                    inv.setAttachment(Aid.class.getCanonicalName(), String.valueOf(aid));
                }
                if (bId != null) {
                    inv.setAttachment(Bid.class.getCanonicalName(), String.valueOf(bId));
                }
                if (shopId != null) {
                    inv.setAttachment(ShopId.class.getCanonicalName(), String.valueOf(shopId));
                }

                invoke = invoker.invoke(inv);
                if (invoke.hasException()) {
                    isSucess = false;
                }
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.info("outArgs[{}]", JsonUtils.bean2Json(invoke.getValue()));
                }
                return invoke;
            } finally {
                LOGGER.info("c:|" + m + "|" + (System.currentTimeMillis() - t) + "|" + isSucess);
            }
        }

    }

}
