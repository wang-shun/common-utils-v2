package com.youzan.sz.common.aspect;

import com.youzan.sz.common.util.JsonUtils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;

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
    
    private int maxLength = 1024;
    
    
    public ParameterLogAspect() {
        LOGGER.info("init with max result length: " + maxLength);
    }
    
    
    public Object handle(ProceedingJoinPoint pjp) throws Throwable {
        Object result = null;
        MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
        Method method = methodSignature.getMethod();
        long beginTime = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        //记录日志--调用方法和参数入栈
        if (LOGGER.isInfoEnabled()) {
            StackLog stackLog = new StackLog();
            sb.append(method.getDeclaringClass().getCanonicalName()).append(".").append(method.getName());
            stackLog.setMethod(sb.toString());
            String params = JsonUtils.toJson(pjp.getArgs());
            stackLog.setParams(params);
            stackLog.setLevel(getStackLocal().size());
            //同时保存到list和stack
            addStackEnter(stackLog);
        }
        
        //执行方法
        result = pjp.proceed();
        
        //记录日志--返回结果入栈
        if (LOGGER.isInfoEnabled()) {
            //计时
            long times = System.currentTimeMillis() - beginTime;
            //出参
            Stack<StackLog> stacks = getStackLocal();
            StackLog stack = stacks.pop();
            stack.setTimes(times);
            if (AopUtils.isCglibProxy(result)) {
                stack.setResult(result.toString());
            }else {
                String json = JsonUtils.toJson(result);
                if (json != null && json.length() > maxLength) {
                    json = json.substring(0, maxLength) + "... ...(result is longer than " + maxLength + " want more? you can set the maxLength)";
                }
                stack.setResult(json);
            }
            
            if (getStackLocal().size() == 0 && getListLocal().size() > 0) {
                List<StackLog> stackLogList = getListLocal();
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
                    logSB.append(tab).append("method-->").append(stackLog.getMethod()).append(".").append("(").append(stackLog.getParams()).append(")").append(NEW_LINE);
                    logSB.append(tab).append("elapse-->").append("[").append(stackLog.getTimes()).append("ms ").append(timePercent).append("%]").append(NEW_LINE);
                    int pre = i - 1;
                    if (stackLog.getResult() != null && stackLogList.get(pre).getResult() != null) {
                        //优化如果返回结果与上一层的长度一样则认为是一样不重复打印
                        if (stackLog.getResult().length() > 64 && pre >= 0 && stackLogList.get(pre).getResult().length() == stackLog.getResult().length()) {
                            logSB.append(tab).append("result-->").append("same length as pre log").append(NEW_LINE).append(NEW_LINE);
                            continue;
                        }
                        logSB.append(tab).append("result-->").append(stackLog.getResult()).append(NEW_LINE).append(NEW_LINE);
                    }
                }
                logSB.append("------------end------------");
                Throwable t = throwLocal.get();
                if (t == null) {
                    LOGGER.info(logSB.toString());
                }else {
                    LOGGER.info(logSB.toString(), t);
                }
                getListLocal().clear();
                throwLocal.set(null);
            }
        }
        
        return result;
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
    
    
    public static void main(String[] args) {
        Stack<Integer> stack = new Stack<>();
        stack.push(1);
        stack.push(2);
        stack.push(3);
        System.out.println(stack.get(stack.size() - 1));
    }
}
