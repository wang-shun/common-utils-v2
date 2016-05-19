package com.youzan.sz.common.util;

import com.youzan.platform.bootstrap.exception.BusinessException;
import com.youzan.sz.common.model.number.NumberTypes;
import com.youzan.sz.common.response.enums.ResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: YANG
 * Date: 2015/12/17
 * Time: 20:03
 */
public class BeanUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(BeanUtil.class);

    public static <T> T copyProperty(Object source, Class<T> destinationClazz) {

        if (source == null) {
            return null;
        }

        T t = BeanUtils.instantiate(destinationClazz);
        BeanUtils.copyProperties(source, t);
        return t;
    }


    public static <T> T copyProperty(Object source, T t) {

        if (source == null) {
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
        BeanInfo beanInfo = null; // 获取类属性
        T obj;
        try {
            beanInfo = Introspector.getBeanInfo(destinationClazz);
            obj = destinationClazz.newInstance(); // 创建 JavaBean 对象
        } catch (Exception e) {
            LOGGER.error("BeanUtil Error:{}", e);
            throw new BusinessException((long) ResponseCode.ERROR.getCode(), "转换异常");
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
                }catch (IllegalArgumentException ex){
                    LOGGER.error("BeanUtil Error:{}", "类型转换异常,请自行转换");
                }
                catch (Exception e) {
                    LOGGER.error("BeanUtil Error:{}", e);
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
    public static <T> Map transBean2Map(T bean) {
        Class type = bean.getClass();
        Map returnMap = new HashMap();
        BeanInfo beanInfo = null;
        try {
            beanInfo = Introspector.getBeanInfo(type);
        } catch (IntrospectionException e) {
            LOGGER.error("BeanUtil Error:{}", e);
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
                    result = readMethod.invoke(bean, new Object[0]);
                } catch (IllegalAccessException e) {
                    LOGGER.error("BeanUtil Error:{}", e);
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    LOGGER.error("BeanUtil Error:{}", e);
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

    public static void main(String[] args) throws IllegalAccessException, IntrospectionException, InvocationTargetException, InstantiationException {
        List<StockRunningVO> result = new ArrayList<>();
        StockRunningVO sr0 = new StockRunningVO();
        sr0.setBusinessType("盘盈");
        sr0.setSourceOrderNo(NumberUtils.initNumber(NumberTypes.STOCKCHECK));
        sr0.setProductName("有赞瓜子");
        sr0.setCreateTimeDesc("2016-04-01");
        sr0.setOperatorStaffName("大琼");
        sr0.setUnit("坨");
        sr0.setQuantity(10);
        result.add(sr0);
        StockRunningVO sr1 = new StockRunningVO();
        sr1.setBusinessType("盘亏");
        sr1.setSourceOrderNo(NumberUtils.initNumber(NumberTypes.STOCKCHECK));
        sr1.setProductName("有赞瓜子");
        sr1.setCreateTimeDesc("2016-04-02");
        sr1.setOperatorStaffName("大琼");
        sr1.setUnit("坨");
        sr1.setQuantity(-10);
        result.add(sr1);
        List<Map> maps = BeanUtil.transListBean2ListMap(result);
        System.out.println(maps.toString());
        List<StockRunningVO> e = BeanUtil.transListMap2ListBean(StockRunningVO.class, maps);
        System.out.println(e.toString());
    }
}
