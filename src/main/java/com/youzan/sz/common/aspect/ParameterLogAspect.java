package com.youzan.sz.common.aspect;

import com.youzan.sz.common.util.JsonUtils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by mingle.
 * Time 2/26/17 5:01 PM
 * Desc 文件描述
 */
@Aspect
public class ParameterLogAspect extends BaseAspect {
    
    private static final String NEW_LINE = "\r\n";
    
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    
    private ThreadLocal<Stack<StackLog>> stackLocal = new ThreadLocal<>();
    
    private ThreadLocal<List<StackLog>> listLocal = new ThreadLocal<>();
    
    private ThreadLocal<AtomicInteger> levelLocal = new ThreadLocal<>();
    
    private ThreadLocal<Throwable> throwLocal = new ThreadLocal<>();
    
    private boolean isShowResult = true;
    
    
    public ParameterLogAspect() {
    }
    
    
    public Object handle(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        long beginTime = System.currentTimeMillis();
        Object result = null;
        StringBuilder sb = new StringBuilder();
        try {
            //记录日志
            getLevelLocal().incrementAndGet();
            StackLog stackLog = new StackLog();
            sb.append(method.getDeclaringClass().getCanonicalName()).append(".").append(method.getName());
            stackLog.setMethod(sb.toString());
            String params = JsonUtils.toJson(pjp.getArgs());
            stackLog.setParams(params);
            stackLog.setLevel(getStackLocal().size());
            addStackEnter(stackLog);
            //执行方法
            result = pjp.proceed();
            return result;
        } catch (Throwable t) {
            if (throwLocal.get() == null) {
                throwLocal.set(t);
            }
            throw t;
        } finally {
            //计时
            long times = System.currentTimeMillis() - beginTime;
            //出参
            StackLog stack = getStackLocal().pop();
            stack.setTimes(times);
            if (isShowResult) {
                stack.setResult(JsonUtils.toJson(result));
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
    
    
    private AtomicInteger getLevelLocal() {
        
        AtomicInteger num = levelLocal.get();
        if (num == null) {
            num = new AtomicInteger(-1);
            levelLocal.set(num);
        }
        return num;
    }
    
    
    private List<StackLog> getListLocal() {
        
        List<StackLog> list = listLocal.get();
        if (list == null) {
            list = new ArrayList<>();
            listLocal.set(list);
        }
        return list;
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
    
    
    public boolean isShowResult() {
        return isShowResult;
    }
    
    
    public void setShowResult(boolean showResult) {
        isShowResult = showResult;
    }
    
    
    private class StackLog {
        
        private String method;
        
        private String params;
        
        private int level;
        
        private long times;
        
        private String result;
        
        private List<StackLog> list;
        
        
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
        
        
        public int getLevel() {
            return level;
        }
        
        
        public void setLevel(int level) {
            this.level = level;
        }
        
        
        public long getTimes() {
            return times;
        }
        
        
        public void setTimes(long times) {
            this.times = times;
        }
        
        
        public String getResult() {
            return result;
        }
        
        
        public void setResult(String result) {
            this.result = result;
        }
        
        
        public List<StackLog> getList() {
            return list;
        }
        
        
        public void setList(List<StackLog> list) {
            this.list = list;
        }
    }
}


