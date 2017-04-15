package com.youzan.sz.common.aspect;

import com.youzan.sz.common.util.JsonUtils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Value;

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
    
    private ThreadLocal<Throwable> throwLocal = new ThreadLocal<>();
    
    private boolean isShowResult = true;
    
    private int maxLength = 1024;
    
    
    public ParameterLogAspect() {
        LOGGER.info("init isShowResult:" + isShowResult + " max result length: " + maxLength);
    }
    
    
    public Object handle(ProceedingJoinPoint pjp) throws Throwable {
        Object result = null;
        MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
        Method method = methodSignature.getMethod();
        long beginTime = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        try {
            //记录日志--入栈
            if (LOGGER.isInfoEnabled()) {
                StackLog stackLog = new StackLog();
                sb.append(method.getDeclaringClass().getCanonicalName()).append(".").append(method.getName());
                stackLog.setMethod(sb.toString());
                String params = JsonUtils.toJson(pjp.getArgs());
                stackLog.setParams(params);
                stackLog.setLevel(getStackLocal().size());
                addStackEnter(stackLog);
            }
            
            //执行方法
            result = pjp.proceed();
            return result;
        } catch (Throwable t) {
            if (LOGGER.isInfoEnabled()) {
                if (throwLocal.get() == null) {
                    throwLocal.set(t);
                }
            }
            throw t;
        } finally {
            if (LOGGER.isInfoEnabled()) {
                //计时
                long times = System.currentTimeMillis() - beginTime;
                //出参
                StackLog stack = getStackLocal().pop();
                stack.setTimes(times);
                // TODO: 2017/4/6 判断是否只需要把最后一个返回结果打印出来
                if (isShowResult) {
                    if (AopUtils.isCglibProxy(result)) {
                        stack.setResult(result.toString());
                    }else {
                        String json = JsonUtils.toJson(result);
                        if (json != null && json.length() > maxLength) {
                            json = json.substring(0, maxLength) + "... ...(result is too long, has been substring, want more? you can set the maxLength)";
                        }
                        stack.setResult(json);
                    }
                }
                
                if (getStackLocal().size() == 0 && getListLocal().size() > 0) {
                    List<StackLog> stackLogList = getListLocal();
                    StackLog first = stackLogList.get(0);
                    double total = (double) first.getTimes();
                    NumberFormat numberFormat = NumberFormat.getInstance();
                    numberFormat.setMaximumFractionDigits(0);
                    StringBuilder logSB = new StringBuilder(NEW_LINE).append("------------start------------").append(NEW_LINE);
                    for (StackLog stackLog : stackLogList) {
                        String timePercent = numberFormat.format(stackLog.getTimes() / total * 100);
                        StringBuffer tabSB = new StringBuffer();
                        for (int t = 0; t < stackLog.getLevel(); t++) {
                            tabSB.append("\t");
                        }
                        String tab = tabSB.toString();
                        logSB.append(tab).append("method-->").append(stackLog.getMethod()).append(".").append("(").append(stackLog.getParams()).append(")").append(NEW_LINE);
                        logSB.append(tab).append("elapse-->").append("[").append(stackLog.getTimes()).append("ms ").append(timePercent).append("%]").append(NEW_LINE);
                        if (isShowResult && stackLog.getResult() != null) {
                            logSB.append(tab).append("result-->").append(stackLog.getResult()).append(NEW_LINE).append(NEW_LINE);
                        }
                    }
                    logSB.append("------------end------------");
                    Throwable t = throwLocal.get();
                    if (t == null) {
                        LOGGER.info(logSB.toString());
                    }else {
                        LOGGER.error(logSB.toString(), t);
                    }
                    getListLocal().clear();
                    throwLocal.set(null);
                }
            }
        }
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
    
    
    public boolean isShowResult() {
        return isShowResult;
    }
    
    
    public void setShowResult(boolean showResult) {
        isShowResult = showResult;
    }
    
    
    public int getMaxLength() {
        return maxLength;
    }
    
    
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }
    
    
    private class StackLog {
        
        private String method;
        
        private String params;
        
        private int level;
        
        private long times;
        
        private String result;
        
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
        
        
        public String getParams() {
            return params;
        }
        
        
        public void setParams(String params) {
            this.params = params;
        }
        
        
        public String getResult() {
            return result;
        }
        
        
        public void setResult(String result) {
            this.result = result;
        }
        
        
        public long getTimes() {
            return times;
        }
        
        
        public void setTimes(long times) {
            this.times = times;
        }
        
    }
}
