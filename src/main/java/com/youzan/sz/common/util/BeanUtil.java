package com.youzan.sz.common.util;

import com.youzan.platform.bootstrap.exception.BusinessException;
import com.youzan.sz.common.response.enums.ResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: YANG
 * Date: 2015/12/17
 * Time: 20:03
 */
public class BeanUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(BeanUtil.class);

    private BeanUtil() {
        throw new IllegalAccessError("Utility class");
    }

    public static <T> T copyProperty(Object source, Class<T> destinationClazz) {
        if (source == null || destinationClazz == null) {
            return null;
        }

        T t = BeanUtils.instantiate(destinationClazz);
        BeanUtils.copyProperties(source, t);
        return t;
    }

    public static <T> T copyProperty(Object source, T t) {
        if (source == null || t == null) {
            return null;
        }

        BeanUtils.copyProperties(source, t);
        return t;
    }

    public static <T> List<T> copyPropertyList(List<?> sources, Class<T> destinationClazz) {
        if (sources == null || sources.isEmpty()) {
            return Collections.emptyList();
        }

        List<T> result = new ArrayList<>(sources.size());
        for (Object obj : sources) {
            result.add(copyProperty(obj, destinationClazz));
        }
        return result;
    }

    /**
     * 只拷贝不是null的属性
     *
     * @param source
     * @param destinationClazz
     * @param <T>
     * @return
     */
    public static <T> T copyNonNullProperty(Object source, Class<T> destinationClazz) {
        if (source == null || destinationClazz == null) {
            return null;
        }

        T t = MyBeanUtils.instantiate(destinationClazz);
        MyBeanUtils.copyProperties(source, t);
        return t;
    }


    /**
     * 只拷贝不是null的属性
     *
     * @param source
     * @return
     */
    public static <T> T copyNonNullProperty(Object source, T t) {
        if (source == null || t == null) {
            return null;
        }

        MyBeanUtils.copyProperties(source, t);
        return t;
    }


    /**
     * 只拷贝不是null的属性
     *
     * @param destinationClazz
     * @param <T>
     * @return
     */
    public static <T> List<T> copyNonNullPropertyList(List<?> sources, Class<T> destinationClazz) {
        if (sources == null || sources.isEmpty()) {
            return Collections.emptyList();
        }

        List<T> result = new ArrayList<>(sources.size());
        for (Object obj : sources) {
            result.add(copyNonNullProperty(obj, destinationClazz));
        }
        return result;
    }


    /**
     * 将一个 Map 对象转化为一个 JavaBean
     *
     * @param destinationClazz 要转化的类型
     * @param map              包含属性值的 map
     * @return 转化出来的 JavaBean 对象
     * @throws IntrospectionException    如果分析类属性失败
     * @throws IllegalAccessException    如果实例化 JavaBean 失败
     * @throws InstantiationException    如果实例化 JavaBean 失败
     * @throws InvocationTargetException 如果调用属性的 setter 方法失败
     */
    public static <T> T transMap2Bean(Class<T> destinationClazz, Map map) {
        if (map == null) {
            return null;
        }
        BeanInfo beanInfo = null; // 获取类属性
        T obj;
        try {
            beanInfo = Introspector.getBeanInfo(destinationClazz);
            obj = destinationClazz.newInstance(); // 创建 JavaBean 对象
        } catch (Exception e) {
            LOGGER.error("BeanUtil Error:{}", e);
            throw new BusinessException((long) ResponseCode.ERROR.getCode(), "转换异常", e);
        }

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("bean info :{}" + beanInfo);
        }
        // 给 JavaBean 对象的属性赋值
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (int i = 0; i < propertyDescriptors.length; i++) {
            PropertyDescriptor descriptor = propertyDescriptors[i];
            String propertyName = descriptor.getName();

            if (map.containsKey(propertyName)) {
                // 下面一句可以 try 起来，这样当一个属性赋值失败的时候就不会影响其他属性赋值。
                Object value = map.get(propertyName);
                Object[] args = new Object[1];
                args[0] = value;
                try {
                    descriptor.getWriteMethod().invoke(obj, args);
                } catch (IllegalArgumentException ex) {
                    LOGGER.warn("BeanUtil Error: 类型转换异常,请自行转换,proprtyName:{},value:{}", propertyName, value);
                } catch (Exception e) {
                    LOGGER.warn("source map:{},BeanUtil Error:{}", JsonUtils.toJson(map), e);
                }
            }
        }
        return obj;
    }

    /**
     * 将一个 List Map 对象转化为一个 List JavaBean
     *
     * @param destinationClazz 要转化的类型
     * @param listMap          包含属性值的 map
     * @return 转化出来的 JavaBean 对象
     * @throws IntrospectionException    如果分析类属性失败
     * @throws IllegalAccessException    如果实例化 JavaBean 失败
     * @throws InstantiationException    如果实例化 JavaBean 失败
     * @throws InvocationTargetException 如果调用属性的 setter 方法失败
     */
    public static <T> List<T> transListMap2ListBean(Class<T> destinationClazz, List<Map> listMap) {
        List<T> result = new ArrayList<>();
        for (Map map : listMap) {
            result.add(transMap2Bean(destinationClazz, map));
        }
        return result;
    }

    /**
     * 将一个 JavaBean 对象转化为一个  Map
     *
     * @param bean 要转化的JavaBean 对象
     * @return 转化出来的  Map 对象
     * @throws IntrospectionException    如果分析类属性失败
     * @throws IllegalAccessException    如果实例化 JavaBean 失败
     * @throws InvocationTargetException 如果调用属性的 setter 方法失败
     */
    public static Map<String, Object> transBean2Map(Object bean) {
        Class type = bean.getClass();
        Map<String, Object> returnMap = new HashMap<>();
        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(type);
        } catch (IntrospectionException e) {
            LOGGER.warn("BeanUtil Error:{}", e);
            throw new BusinessException((long) ResponseCode.ERROR.getCode(), "转换异常");
        }

        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (int i = 0; i < propertyDescriptors.length; i++) {
            PropertyDescriptor descriptor = propertyDescriptors[i];
            String propertyName = descriptor.getName();
            if (!propertyName.equals("class")) {
                Method readMethod = descriptor.getReadMethod();
                Object result = null;
                try {
                    result = readMethod.invoke(bean);
                } catch (IllegalAccessException e) {
                    LOGGER.warn("BeanUtil Error:{}", e);
                } catch (InvocationTargetException e) {
                    LOGGER.warn("BeanUtil Error:{}", e);
                    throw new BusinessException((long) ResponseCode.ERROR.getCode(), "转换异常");
                }
                if (result != null) {
                    returnMap.put(propertyName, result);
                } else {
                    returnMap.put(propertyName, "");
                }
            }
        }
        return returnMap;
    }

    /**
     * 将一个 List JavaBean 对象转化为一个  List Map
     *
     * @param listBean 要转化的JavaBean 对象
     * @return 转化出来的  Map 对象
     * @throws IntrospectionException    如果分析类属性失败
     * @throws IllegalAccessException    如果实例化 JavaBean 失败
     * @throws InvocationTargetException 如果调用属性的 setter 方法失败
     */
    public static <T> List<Map> transListBean2ListMap(List<T> listBean) {
        List<Map> result = new ArrayList<>();
        for (T bean : listBean) {
            result.add(transBean2Map(bean));
        }
        return result;
    }

    public static void main(String[] args) throws IllegalAccessException, IntrospectionException,
            InvocationTargetException, InstantiationException {
//        List<StockRunningVO> result = new ArrayList<>();
//        StockRunningVO sr0 = new StockRunningVO();
//        sr0.setBusinessType("盘盈");
//        sr0.setSourceOrderNo(NumberUtils.initNumber(NumberTypes.STOCKCHECK));
//        sr0.setProductName("有赞瓜子");
//        sr0.setCreateTimeDesc("2016-04-01");
//        sr0.setOperatorStaffName("大琼");
//        sr0.setUnit("坨");
//        sr0.setQuantity(10);
//        result.add(sr0);
//        StockRunningVO sr1 = new StockRunningVO();
//        sr1.setBusinessType("盘亏");
//        sr1.setSourceOrderNo(NumberUtils.initNumber(NumberTypes.STOCKCHECK));
//        sr1.setProductName("有赞瓜子");
//        sr1.setCreateTimeDesc("2016-04-02");
//        sr1.setOperatorStaffName("大琼");
//        sr1.setUnit("坨");
//        sr1.setQuantity(-10);
//        result.add(sr1);
//        List<Map> maps = BeanUtil.transListBean2ListMap(result);
//        System.out.println(maps.toString());
//        List<StockRunningVO> e = BeanUtil.transListMap2ListBean(StockRunningVO.class, maps);
//        System.out.println(e.toString());


        AA aa = new AA();
        A a = BeanUtil.copyNonNullProperty(aa, A.class);
        System.out.println(a.getA());

    }


    public static class A {
        private int a = 1;

        public int getA() {
            return a;
        }

        public void setA(int a) {
            this.a = a;
        }
    }

    public static class AA {
        private Integer A;

        public Integer getA() {
            return A;
        }

        public void setA(Integer a) {
            A = a;
        }
    }

    private static abstract class MyBeanUtils extends org.springframework.beans.BeanUtils {

        public static void copyProperties(Object source, Object target) throws BeansException {
            copyProperties(source, target, null, (String[]) null);
        }

        public static void copyProperties(Object source, Object target, Class<?> editable) throws BeansException {
            copyProperties(source, target, editable, (String[]) null);
        }

        public static void copyProperties(Object source, Object target, String... ignoreProperties) throws BeansException {
            copyProperties(source, target, null, ignoreProperties);
        }

        private static void copyProperties(Object source, Object target, Class<?> editable, String... ignoreProperties) throws BeansException {
            Assert.notNull(source, "Source must not be null");
            Assert.notNull(target, "Target must not be null");
            Class actualEditable = target.getClass();
            if (editable != null) {
                if (!editable.isInstance(target)) {
                    throw new IllegalArgumentException("Target class [" + target.getClass().getName() + "] not assignable to Editable class [" + editable.getName() + "]");
                }

                actualEditable = editable;
            }

            PropertyDescriptor[] targetPds = getPropertyDescriptors(actualEditable);
            List ignoreList = ignoreProperties != null ? Arrays.asList(ignoreProperties) : null;
            int var8 = targetPds.length;

            for (int var9 = 0; var9 < var8; ++var9) {
                PropertyDescriptor targetPd = targetPds[var9];
                Method writeMethod = targetPd.getWriteMethod();
                if (writeMethod != null && (ignoreList == null || !ignoreList.contains(targetPd.getName()))) {
                    PropertyDescriptor sourcePd = getPropertyDescriptor(source.getClass(), targetPd.getName());
                    if (sourcePd != null) {
                        Method readMethod = sourcePd.getReadMethod();
                        if (readMethod != null && ClassUtils.isAssignable(writeMethod.getParameterTypes()[0], readMethod.getReturnType())) {
                            try {
                                if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
                                    readMethod.setAccessible(true);
                                }
                                Object value = readMethod.invoke(source);
                                if (value != null) {
                                    if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
                                        writeMethod.setAccessible(true);
                                    }
                                    writeMethod.invoke(target, value);
                                }
                            } catch (Throwable var15) {
                                throw new FatalBeanException("Could not copy property \'" + targetPd.getName() + "\' from source to target", var15);
                            }
                        }
                    }
                }
            }

        }
    }
}
