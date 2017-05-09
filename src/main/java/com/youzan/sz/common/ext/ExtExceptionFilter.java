package com.youzan.sz.common.ext;

import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;


/**
 * Created by heshen on 2017/4/25.
 */
public interface ExtExceptionFilter extends Extpoint {
    
    Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException;
}
