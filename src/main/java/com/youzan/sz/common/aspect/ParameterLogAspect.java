package com.youzan.sz.common.aspect;

import com.youzan.sz.common.util.JsonUtils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


/**
 * Created by mingle.
 * Time 2017/3/23 下午7:59
 * Desc 通过切面以层级记录spring中bean的调用关系及时间
 */
//@Component
public class ParameterLogAspect {
    
    private static final String NEW_LINE = "\r\n";
    
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    
    private ThreadLocal<Stack<StackLog>> stackLocal = new ThreadLocal<>();
    
    private ThreadLocal<List<StackLog>> listLocal = new ThreadLocal<>();
    
    private ThreadLocal<Throwable> throwableThreadLocal = new ThreadLocal<>();
    
    private int maxLength = 1024;
    
    private boolean logAll = true;
    
    private boolean logFirst = true;
    
    private String TRACEID = "TraceId";
    
    
    public void setLogAll(boolean logAll) {
        this.logAll = logAll;
    }
    
    
    public void setLogFirst(boolean logFirst) {
        this.logFirst = logFirst;
    }
    
    
    public ParameterLogAspect() {
        LOGGER.info("init with max result length: " + maxLength);
    }
    
    
    public Object handle(ProceedingJoinPoint pjp) throws Throwable {
        Object result = null;
        MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
        Method method = methodSignature.getMethod();
        long beginTime = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        Stack<StackLog> stacks = getStackLocal();
        //记录日志--调用方法和参数入栈
        StackLog stackLog = new StackLog();
        sb.append(method.getDeclaringClass().getCanonicalName()).append("#").append(method.getName());
        stackLog.setMethod(sb.toString());
        stackLog.setParams(pjp.getArgs());
        stackLog.setLevel(stacks.size());
        //同时保存到list和stack
        addStackEnter(stackLog);
        
        //执行方法
        try {
            result = pjp.proceed();
        } catch (Throwable t) {
            throwableThreadLocal.set(t);
            processResult(beginTime, result);
            throw t;
        }
        
        //记录日志--返回结果入栈
        processResult(beginTime, result);
        
        return result;
    }
    
    
    private void processResult(long beginTime, Object result) {
        //计时
        long times = System.currentTimeMillis() - beginTime;
        //出参
        Stack<StackLog> stacks = getStackLocal();
        StackLog stack = stacks.pop();
        stack.setTimes(times);
        stack.setResult(result);
        
        /*if (AopUtils.isCglibProxy(result)) {
            stack.setResult(result.toString());
        }else {
            String json = JsonUtils.toJson(result);
            if (json != null && json.length() > maxLength) {
                json = json.substring(0, maxLength) + "... ...(result is longer than " + maxLength + " want more? you can set the maxLength)";
            }
            stack.setResult(json);
        }*/
        
        if (getStackLocal().size() == 0 && getListLocal().size() > 0) {
            Throwable throwable = throwableThreadLocal.get();
            List<StackLog> stackLogList = getListLocal();
            //如果没有抛错且不需要全部打印且需要打印第一层函数
            if (throwable == null && !logAll) {
                if (logFirst) {
                    stackLogList = new ArrayList<>();
                    stackLogList.add(getListLocal().get(0));
                }else {
                    clearStack();
                    return;
                }
            }
            
            StackLog first = stackLogList.get(0);
            double total = (double) first.getTimes();
            NumberFormat numberFormat = NumberFormat.getInstance();
            numberFormat.setMaximumFractionDigits(0);
            StringBuilder logSB = new StringBuilder(NEW_LINE).append("------------start------------").append(NEW_LINE);
            for (int i = 0; i < stackLogList.size(); i++) {
                StackLog stackLog = stackLogList.get(i);
                String timePercent = numberFormat.format(stackLog.getTimes() / total * 100);
                StringBuffer tabSB = new StringBuffer();
                for (int t = 0; t < stackLog.getLevel(); t++) {
                    tabSB.append("\t");
                }
                String tab = tabSB.toString();
                logSB.append(tab).append("method-->").append(stackLog.getMethod()).append("(").append(JsonUtils.toJson(stackLog.getParams())).append(")")
                        .append("[").append(getTrackId()).append("]").append(NEW_LINE);
                logSB.append(tab).append("elapse-->").append("[").append(stackLog.getTimes()).append("ms ").append(timePercent).append("%]").append(NEW_LINE);
                String stackResult = JsonUtils.toJson(stackLog.getResult());
                //优化如果返回结果与上一层的长度一样则认为是一样不重复打印
                int pre = i - 1;
                if (stackResult.length() > 64 && pre >= 0 && JsonUtils.toJson(stackLogList.get(pre).getResult()).length() == stackResult.length()) {
                    logSB.append(tab).append("result-->").append("same length as pre log").append(NEW_LINE).append(NEW_LINE);
                    continue;
                }
                if (stackResult.length() > maxLength) {
                    stackResult = stackResult.substring(0, maxLength) + "!!!!!!(result is longer than " + maxLength + " want more? you can set the maxLength)";
                }
                logSB.append(tab).append("result-->").append(stackResult).append("[").append(getTrackId()).append("]").append(NEW_LINE);
            }
            logSB.append("------------end------------");
            
            if (throwable != null) {
                LOGGER.warn(logSB.toString(), throwable);
            }else {
                LOGGER.info(logSB.toString());
            }
            clearStack();
        }
    }
    
    
    private void clearStack() {
        getStackLocal().clear();
        getListLocal().clear();
        throwableThreadLocal.set(null);
    }
    
    
    private String getTrackId() {
        String traceId = MDC.get(TRACEID);
        if (traceId == null) {
            traceId = String.valueOf(System.currentTimeMillis());
            MDC.put(TRACEID, traceId);
        }
        return traceId;
    }
    
    
    private Stack<StackLog> getStackLocal() {
        
        Stack<StackLog> stack = stackLocal.get();
        if (stack == null) {
            stack = new Stack();
            stackLocal.set(stack);
        }
        return stack;
    }
    
    
    private void addStackEnter(StackLog stackLog) {
        List<StackLog> list = getListLocal();
        list.add(stackLog);
        Stack<StackLog> stack = getStackLocal();
        stack.push(stackLog);
    }
    
    
    private List<StackLog> getListLocal() {
        
        List<StackLog> list = listLocal.get();
        if (list == null) {
            list = new ArrayList<>();
            listLocal.set(list);
        }
        return list;
    }
    
    
    public int getMaxLength() {
        return maxLength;
    }
    
    
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }
    
    
    private class StackLog {
        
        private String method;
        
        private Object[] params;
        
        private int level;
        
        private long times;
        
        private Object result;
        
        private List<StackLog> list;
        
        
        public int getLevel() {
            return level;
        }
        
        
        public void setLevel(int level) {
            this.level = level;
        }
        
        
        public List<StackLog> getList() {
            return list;
        }
        
        
        public void setList(List<StackLog> list) {
            this.list = list;
        }
        
        
        public String getMethod() {
            return method;
        }
        
        
        public void setMethod(String method) {
            this.method = method;
        }
        
        
        public Object[] getParams() {
            return params;
        }
        
        
        public void setParams(Object[] params) {
            this.params = params;
        }
        
        
        public Object getResult() {
            return result;
        }
        
        
        public void setResult(Object result) {
            this.result = result;
        }
        
        
        public long getTimes() {
            return times;
        }
        
        
        public void setTimes(long times) {
            this.times = times;
        }
        
    }
    
    
    public static void main(String[] args) {
        Stack<Integer> stack = new Stack<>();
        stack.push(1);
        stack.push(2);
        stack.push(3);
        System.out.println(stack.get(stack.size() - 1));
    }
}
