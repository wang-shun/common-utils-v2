package com.youzan.sz.DistributedCallTools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.dubbo.common.extension.SPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.Codec2;
import com.alibaba.dubbo.remoting.buffer.ChannelBuffer;
import com.alibaba.dubbo.remoting.exchange.Request;
import com.alibaba.dubbo.remoting.exchange.Response;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.rpc.protocol.dubbo.DubboCountCodec;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youzan.sz.DistributedCallTools.DistributedContextTools.DistributedParamManager;
import com.youzan.sz.common.response.BaseResponse;
import com.youzan.sz.common.response.enums.ResponseCode;
import com.youzan.sz.common.util.JsonUtils;
import com.youzan.sz.monitor.HeathCheck;

@SPI("dubbo")
public class CarmenCodec implements Codec2 {
    private static final Logger       LOGGER           = LoggerFactory
        .getLogger(com.youzan.sz.DistributedCallTools.CarmenCodec.class);

    /**
     *
     */
    private static final byte[]       CLRF             = new byte[] { 0x0D, 0x0A };
    public static final String        CARMEN_CODEC     = "CarmenCodec";
    private static final ObjectMapper om               = new ObjectMapper();
    private static byte[]             HEATH_CHECK_RESP = null;
    static {
        HEATH_CHECK_RESP = encodeRPC(new BaseResponse(ResponseCode.SUCCESS, "OK")).array();
    }

    private static ByteBuffer encodeRPC(BaseResponse response) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);

        try {
            // 1. Write HTTP status line.
            StringBuilder statusLine = new StringBuilder("HTTP/1.1 ");
            statusLine.append(response.getCode() == 99999 ? 500 : 200);
            statusLine.append(" ");
            statusLine.append(response.getCode() == 99999 ? " Server Error" : "OK");
            baos.write(statusLine.toString().getBytes("UTF-8"));
            baos.write(CLRF);
            baos.write("Access-Control-Allow-Origin:*".getBytes("UTF-8"));
            baos.write(CLRF);
            baos.write("Content-Language:zh-CN".getBytes("UTF-8"));
            baos.write(CLRF);
            baos.write("Content-Type:application/json;charset=utf-8".getBytes("UTF-8"));
            baos.write(CLRF);
            // buffer.put(statusLine.toString().getBytes());
            // buffer.put(CLRF);

            StringBuilder header = new StringBuilder();
            // // 2. Write HTTP resoponse headers.
            // List<Pair<String, String>> headers = response.getHeaders();
            // // 增加允许跨域访问的协议头
            // Pair<String, String> accessControlAllowOriginHeader = new
            // Pair<String, String>("Access-Control-Allow-Origin",
            // "*");
            // headers.add(accessControlAllowOriginHeader);
            // for (Pair<String, String> pair : headers) {
            // header.setLength(0);
            // header.append(pair.first);
            // header.append(": ");
            // header.append(pair.second);
            // buffer.put(header.toString().getBytes());
            // buffer.put(CLRF);
            // }
            // 3. Write HTTP content length.
            byte[] body = null;
            try {
                body = ("{\"response\":" + om.writeValueAsString(response) + "}").getBytes("UTF-8");
            } catch (Throwable e) {
                e.printStackTrace();
            }

            header.setLength(0);
            header.append("Content-Length: ");
            header.append(null == body ? 0 : body.length);
            baos.write(header.toString().getBytes());
            baos.write(CLRF);
            // buffer.put(header.toString().getBytes());
            // buffer.put(CLRF);

            // // 4. Write HTTP response cookies.
            // List<Cookie> cookies = response.getCookies();
            // String value = null;
            // for (Cookie cookie : cookies)
            // {
            // value = generateCookieString(cookie);
            // if (value == null || value.length() == 0)
            // {
            // continue;
            // }
            //
            // header.setLength(0);
            // header.append("Set-Cookie: ");
            // header.append(value);
            // buffer.put(header.toString().getBytes());
            // buffer.put(CLRF);
            // }
            baos.write(CLRF);
            // buffer.put(CLRF);

            if (null != body) {
                baos.write(body);
                // buffer.put(body);
            }
            ByteBuffer buffer = ByteBuffer.wrap(baos.toByteArray());
            return buffer;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    private static String readHttpLine(ByteBuffer in) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        boolean isLineEnd = false;
        byte b;

        while (true && in.remaining() > 0) {
            if ((b = in.get()) == '\r') {
                in.get(); // skip '\n'
                isLineEnd = true;
                break;
            } else {
                baos.write(b);
            }
        }

        if (!isLineEnd) {
            return null;
        }

        return new String(baos.toByteArray());
    }

    private DubboCountCodec dubboCountCodec = new DubboCountCodec();

    @Override
    public Object decode(Channel channel, ChannelBuffer buffer) throws IOException {

        Request req = new Request(-1);
        try {

            ByteBuffer buf = buffer.toByteBuffer();
            buf.mark();

            // 判断是否是http协议,如果不是就用原生的dubbo协议解析
            byte magic = buf.get();
            if (magic != 'G' && magic != 'P') {
                buf.reset();
                return dubboCountCodec.decode(channel, buffer);
            }

            // 下面解析完成后要组装成dubbo的相关对象
            buf.reset();
            String requestLine = null, headerLine = null;
            String strArray[] = null;
            boolean isPostMethod = true;
            String interfaceName = null;
            String methodName = null;
            String param = null;
            int contentLengthHead = -1;
            String version = null;

            while (true) {
                headerLine = readHttpLine(buf);

                if (headerLine == null) {
                    return DecodeResult.NEED_MORE_INPUT;
                }

                // 只有对于post方法才需要读取body
                if (headerLine.length() == 0 && isPostMethod) // End of HTTP
                                                              // header
                {
                    // Read HTTP body content
                    int contentLength = 0;

                    if (contentLengthHead != -1) {
                        contentLength = contentLengthHead;
                    }

                    if (contentLength != buf.remaining()) {
                        return DecodeResult.NEED_MORE_INPUT;
                    }

                    if (contentLength > 0) {
                        byte[] content = new byte[contentLength];
                        buf.get(content);
                        param = new String(content, "UTF-8");
                    }
                    break;
                }

                if (requestLine == null) // first line
                {
                    // The first line is HTTP status line. [GET /img/gs.gif
                    // HTTP/1.1]
                    requestLine = headerLine;
                    strArray = requestLine.split(" ");
                    if ("GET".equals(strArray[0].trim())) {
                        isPostMethod = false;
                    }
                    String url = strArray[1].trim();

                    int pIndex = url.lastIndexOf("/?");
                    int mIndex = -1;
                    // 解析参数信息
                    if (pIndex != -1) {
                        param = url.substring(pIndex + 2);
                    } else if ((pIndex = url.lastIndexOf('?')) != -1) {
                        param = url.substring(pIndex + 1);
                    }

                    // 解析方法信息
                    if (pIndex != -1) {
                        mIndex = url.lastIndexOf("/", pIndex - 1);
                        methodName = url.substring(mIndex + 1, pIndex);
                    } else {
                        mIndex = url.lastIndexOf('/');
                        methodName = url.substring(mIndex + 1);
                    }
                    // 解析版本信息
                    int vIndex = -1;
                    vIndex = url.lastIndexOf('/', mIndex - 1);
                    version = url.substring(vIndex + 1, mIndex);
                    // 解析接口信息
                    int iIndex = -1;
                    iIndex = url.lastIndexOf('/', vIndex - 1);
                    interfaceName = "com.youzan." + url.substring(iIndex + 1, vIndex);
                } else
                // header line
                {
                    int index = headerLine.indexOf(":");
                    if (index < 0) {
                        break;
                    }
                    if (index >= headerLine.length() - 1) {
                        if ("Content-Length".equals(headerLine.substring(0, headerLine.length() - 1).trim())) {
                            contentLengthHead = 0;
                        }
                    } else {
                        if ("Content-Length".equals(headerLine.substring(0, index).trim())) {
                            contentLengthHead = Integer.valueOf(headerLine.substring(index + 1).trim());
                        }
                    }
                    continue;
                }
            }
            Map<String, String> parseQueryString = parseQueryString(param);

            req.setVersion("2.0.0");
            req.setTwoWay(true);
            RpcInvocation inv = new RpcInvocation();

            // 解析协议参数
            String jsonValue = parseQueryString.get("json");
            if (null == jsonValue || "".equals(jsonValue.trim())) {
                jsonValue = "[]";
            }

            // 解析公共参数
            String adminId = parseQueryString.get("admin_id");
            if (null == adminId || "".equals(adminId.trim())) {
                adminId = parseQueryString.get("access_token");
            }
            String requestIp = parseQueryString.get("request_ip");
            if (null == requestIp || "".equals(requestIp.trim())) {
                requestIp = channel.getRemoteAddress().getAddress().getHostAddress();
            }
            String kdtId = parseQueryString.get("kdt_id");
            String deviceId = parseQueryString.get("device_id");
            String deviceType = parseQueryString.get("device_type");
            String aid = parseQueryString.get("aid");
            String bid = parseQueryString.get("bid");
            String shopId = parseQueryString.get("shop_id");

            inv.setArguments(new Object[] { methodName,
                                            new String[] { DistributedParamManager.AdminId.getName(),
                                                           DistributedParamManager.RequestIp.getName(),
                                                           DistributedParamManager.KdtId.getName(),
                                                           DistributedParamManager.DeviceId.getName(),
                                                           DistributedParamManager.DeviceType.getName(),
                                                           DistributedParamManager.Aid.getName(),
                                                           DistributedParamManager.Bid.getName(),
                                                           DistributedParamManager.ShopId.getName(), "json" },
                                            new Object[] { adminId, requestIp, kdtId, deviceId, deviceType, aid, bid,
                                                           shopId, jsonValue } });
            inv.setMethodName(Constants.$INVOKE);
            inv.setParameterTypes(new Class[] { String.class, String[].class, Object[].class });
            Map<String, String> attachments = new HashMap<>();
            attachments.put(Constants.DUBBO_VERSION_KEY, "2.8.4");
            attachments.put(Constants.PATH_KEY, interfaceName);
            attachments.put(Constants.INTERFACE_KEY, interfaceName);
            // attachments.put("version", version);
            attachments.put(Constants.GENERIC_KEY, "true");
            attachments.put(Constants.ASYNC_KEY, "true");
            attachments.put(CARMEN_CODEC, "true");
            inv.setAttachments(attachments);
            buffer.skipBytes(buf.position());
            req.setData(inv);
            if (interfaceName.equals(HeathCheck.class.getCanonicalName())) {
                //                                Response response = new Response();
                //                RpcResult rpcResult = new RpcResult("ok");
                //                attachments.put(HeathCheck.HEATH_CHECK_TAG,"1");
                //                rpcResult.setAttachments(attachments);
                //                response.setResult(rpcResult);
                req.setData(HeathCheck.class.getCanonicalName());
                req.setBroken(true);
            }
        } catch (Throwable t) {
            // bad request
            req.setBroken(true);
            req.setData(t);
        }
        return req;

    }

    @Override
    public void encode(Channel channel, ChannelBuffer buffer, Object msg) throws IOException {
        //处理心跳
        if (msg instanceof Response && ((Response) msg).getStatus() == Response.BAD_REQUEST
            && ((Response) msg).getErrorMessage().contains(HeathCheck.class.getCanonicalName())) {
            buffer.writeBytes(HEATH_CHECK_RESP);
            return;
        }

        if (msg instanceof Response && ((Response) msg).getResult() instanceof RpcResult) {
            RpcResult res = (RpcResult) ((Response) msg).getResult();
            //处理心跳检查编码
            String heathCheck = res.getAttachment(HeathCheck.HEATH_CHECK_TAG);

            // 只处理卡门编解码
            String CarmenCodec = res.getAttachment(CARMEN_CODEC);
            if (res.getValue() instanceof BaseResponse && "true".equals(CarmenCodec)) {
                ByteBuffer encoder = encodeRPC((BaseResponse) res.getValue());
                buffer.writeBytes(encoder.array());
                return;
            }
        }
        dubboCountCodec.encode(channel, buffer, msg);
    }

    private Map<String, String> parseQueryString(String src) {
        String key = null, value = null;
        String strArray[] = null;
        Map<String, String> parameters = new HashMap<>();
        if (src != null && src.length() != 0) {
            strArray = src.split("&");
            for (String pair : strArray) {
                int eqIndex = pair.indexOf("=");
                if (eqIndex > 0) {
                    try {
                        key = URLDecoder.decode(pair.substring(0, eqIndex), "UTF-8");
                        value = URLDecoder.decode(pair.substring(eqIndex + 1), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        System.out.println("UnsupportedEncodingException:" + "UTF-8" + ", param=" + src);
                    }
                    parameters.put(key, value);
                }
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("parse parameter results:", JsonUtils.bean2Json(parameters));
        }
        return parameters;
    }

}
