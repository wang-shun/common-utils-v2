package com.youzan.sz.common.aspect;

import com.alibaba.fastjson.JSON;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.Future;


/**
 * Created by mingle.
 * Time 2017/3/23 下午7:59
 * Desc 通过aop切面以层级形式记录spring中bean的调用关系出入参及时间
 */
public class LogAspect {

    private static final String NEW_LINE = "\r\n";

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private ThreadLocal<Stack<StackLog>> stackLocal = new ThreadLocal<>();

    private ThreadLocal<List<StackLog>> listLocal = new ThreadLocal<>();

    private ThreadLocal<Throwable> throwableThreadLocal = new ThreadLocal<>();

    private int maxLength = 1024;

    private boolean logAll = false;

    private boolean logFirst = true;

    /**
     * 存放不需要被toJson的类,如一些toJson会出错的类(可以通过本身实现toString方法来调整具体的输出)
     */
    private Set<Class> excludeClassSet = new HashSet<>();


    public void setLogAll(boolean logAll) {
        this.logAll = logAll;
    }


    public void setLogFirst(boolean logFirst) {
        this.logFirst = logFirst;
    }


    public void setExcludeClassSet(Set<Class> excludeClassSet) {
        this.excludeClassSet = excludeClassSet;
    }


    public LogAspect() {
        LOGGER.info("init with max result length: " + maxLength);
    }


    public Object handle(ProceedingJoinPoint pjp) throws Throwable {
        Object result = null;
        long beginTime = System.currentTimeMillis();
        try {
            MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
            Method method = methodSignature.getMethod();
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
        } catch (Throwable throwable) {
            LOGGER.warn("aop log error", throwable);
        }


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
        try {
            //计时
            long times = System.currentTimeMillis() - beginTime;
            //出参
            Stack<StackLog> stacks = getStackLocal();
            StackLog stack = stacks.pop();
            stack.setTimes(times);
            stack.setResult(result);

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
                    logSB.append(tab).append("method-->").append(stackLog.getMethod()).append("(").append(toJsonForParam(stackLog.getParams())).append(")").append(NEW_LINE);
                    logSB.append(tab).append("elapse-->").append("[").append(stackLog.getTimes()).append("ms ").append(timePercent).append("%]").append(NEW_LINE);
                    String stackResult = toJson(stackLog.getResult());
                    //优化如果返回结果与上一层的长度一样则认为是一样不重复打印
                    int pre = i - 1;
                    if (stackResult.length() > 64 && pre >= 0 && toJson(stackLogList.get(pre).getResult()).length() == stackResult.length()) {
                        logSB.append(tab).append("result-->").append("same length as pre log").append(NEW_LINE).append(NEW_LINE);
                        continue;
                    }
                    if (stackResult.length() > maxLength) {
                        stackResult = stackResult.substring(0, maxLength) + "!result is longer than " + maxLength + "!";
                    }
                    logSB.append(tab).append("result-->").append(stackResult).append(NEW_LINE);
                }
                logSB.append("------------end------------");

                if (throwable != null) {
                    LOGGER.warn(logSB.toString(), throwable);
                }else {
                    LOGGER.info(logSB.toString());
                }
                clearStack();
            }
        } catch (Throwable throwable) {
            LOGGER.warn("aop log error", throwable);
        }
    }


    private void clearStack() {
        getStackLocal().clear();
        getListLocal().clear();
        throwableThreadLocal.set(null);
    }


    private String toJsonForParam(Object[] objects) {
        StringBuffer stringBuffer = new StringBuffer("[");
        for (int i = 0; i < objects.length; i++) {
            stringBuffer.append(toJson(objects[i]));
            if (i < (objects.length - 1)) {
                stringBuffer.append(",");
            }
        }
        stringBuffer.append("]");
        return stringBuffer.toString();
    }


    private String toJson(Object o) {
        if (o == null) {
            return "null";
        }else if (AopUtils.isCglibProxy(o)) {
            return o.toString();
        }else if (o instanceof Runnable || o instanceof Future) {
            return "Runnable";
        }else if (excludeClassSet.contains(o.getClass().getCanonicalName())) {
            return o.toString();
        }else {
            return JSON.toJSONString(o);
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
}
