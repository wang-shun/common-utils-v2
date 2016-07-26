package com.youzan.sz.common.util;

import com.youzan.sz.oa.operatelog.service.OperateLogService;
import com.youzan.sz.portal.session.service.SessionService;
import com.youzan.sz.session.SessionTools;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.*;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.ConversionService;
import org.springframework.util.StringValueResolver;

import java.beans.PropertyEditor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.security.AccessControlContext;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by zefa on 16/7/25.
 */
public class TestUtils {
    private TestUtils() {
        throw new IllegalAccessError("Utility class");
    }

    public static void mockStringUrils() throws Exception {
        Field declaredField = SpringUtils.class.getDeclaredField("beanFactory");
        declaredField.setAccessible(true);
        declaredField.set(SpringUtils.class, new ConfigurableListableBeanFactory() {
            @Override
            public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
                // TODO Auto-generated method stub

            }

            @Override
            public void addEmbeddedValueResolver(StringValueResolver valueResolver) {
                // TODO Auto-generated method stub

            }

            @Override
            public void addPropertyEditorRegistrar(PropertyEditorRegistrar registrar) {
                // TODO Auto-generated method stub

            }

            @Override
            public Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName)
                    throws BeansException {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName)
                    throws BeansException {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public void applyBeanPropertyValues(Object existingBean, String beanName) throws BeansException {
                // TODO Auto-generated method stub

            }

            @Override
            public Object autowire(Class<?> beanClass, int autowireMode, boolean dependencyCheck)
                    throws BeansException {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public void autowireBean(Object existingBean) throws BeansException {
                // TODO Auto-generated method stub

            }

            @Override
            public void autowireBeanProperties(Object existingBean, int autowireMode, boolean dependencyCheck)
                    throws BeansException {
                // TODO Auto-generated method stub

            }

            @Override
            public void clearMetadataCache() {
                // TODO Auto-generated method stub

            }

            @Override
            public Object configureBean(Object existingBean, String beanName) throws BeansException {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public boolean containsBean(String name) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean containsBeanDefinition(String beanName) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean containsLocalBean(String name) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean containsSingleton(String beanName) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void copyConfigurationFrom(ConfigurableBeanFactory otherFactory) {
                // TODO Auto-generated method stub

            }

            @Override
            public void copyRegisteredEditorsTo(PropertyEditorRegistry registry) {
                // TODO Auto-generated method stub

            }

            @Override
            public Object createBean(Class<?> beanClass, int autowireMode, boolean dependencyCheck)
                    throws BeansException {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public <T> T createBean(Class<T> beanClass) throws BeansException {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public void destroyBean(Object existingBean) {
                // TODO Auto-generated method stub

            }

            @Override
            public void destroyBean(String beanName, Object beanInstance) {
                // TODO Auto-generated method stub

            }

            @Override
            public void destroyScopedBean(String beanName) {
                // TODO Auto-generated method stub

            }

            @Override
            public void destroySingletons() {
                // TODO Auto-generated method stub

            }

            @Override
            public <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType)
                    throws NoSuchBeanDefinitionException {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public void freezeConfiguration() {
                // TODO Auto-generated method stub

            }

            @Override
            public AccessControlContext getAccessControlContext() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String[] getAliases(String name) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public <T> T getBean(Class<T> requiredType) throws BeansException {
                if (requiredType.getTypeName().equals(SessionService.class.getTypeName())){
                    return (T) mockSession();
                }
                if (requiredType.getTypeName().equals(OperateLogService.class.getTypeName())){
                    return (T) mockOpLog();
                }
                return null;
            }

            @Override
            public <T> T getBean(Class<T> requiredType, Object... args) throws BeansException {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Object getBean(String name) throws BeansException {

                return new Object();
            }

            @Override
            public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Object getBean(String name, Object... args) throws BeansException {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public ClassLoader getBeanClassLoader() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public int getBeanDefinitionCount() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public String[] getBeanDefinitionNames() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public BeanExpressionResolver getBeanExpressionResolver() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String[] getBeanNamesForType(Class<?> type) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String[] getBeanNamesForType(Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String[] getBeanNamesForType(ResolvableType type) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Iterator<String> getBeanNamesIterator() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public int getBeanPostProcessorCount() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public <T> Map<String, T> getBeansOfType(Class<T> type, boolean includeNonSingletons,
                                                     boolean allowEagerInit) throws BeansException {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType)
                    throws BeansException {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public ConversionService getConversionService() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String[] getDependenciesForBean(String beanName) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String[] getDependentBeans(String beanName) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public BeanDefinition getMergedBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public BeanFactory getParentBeanFactory() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Scope getRegisteredScope(String scopeName) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String[] getRegisteredScopeNames() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Object getSingleton(String beanName) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public int getSingletonCount() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public Object getSingletonMutex() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String[] getSingletonNames() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public ClassLoader getTempClassLoader() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Class<?> getType(String name) throws NoSuchBeanDefinitionException {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public TypeConverter getTypeConverter() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public void ignoreDependencyInterface(Class<?> ifc) {
                // TODO Auto-generated method stub

            }

            @Override
            public void ignoreDependencyType(Class<?> type) {
                // TODO Auto-generated method stub

            }

            @Override
            public Object initializeBean(Object existingBean, String beanName) throws BeansException {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public boolean isAutowireCandidate(String beanName, DependencyDescriptor descriptor)
                    throws NoSuchBeanDefinitionException {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean isCacheBeanMetadata() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean isConfigurationFrozen() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean isCurrentlyInCreation(String beanName) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean isFactoryBean(String name) throws NoSuchBeanDefinitionException {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean isPrototype(String name) throws NoSuchBeanDefinitionException {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean isTypeMatch(String name, Class<?> typeToMatch) throws NoSuchBeanDefinitionException {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean isTypeMatch(String name, ResolvableType typeToMatch) throws NoSuchBeanDefinitionException {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void preInstantiateSingletons() throws BeansException {
                // TODO Auto-generated method stub

            }

            @Override
            public void registerAlias(String beanName, String alias) throws BeanDefinitionStoreException {
                // TODO Auto-generated method stub

            }

            @Override
            public void registerCustomEditor(Class<?> requiredType,
                                             Class<? extends PropertyEditor> propertyEditorClass) {
                // TODO Auto-generated method stub

            }

            @Override
            public void registerDependentBean(String beanName, String dependentBeanName) {
                // TODO Auto-generated method stub

            }

            @Override
            public void registerResolvableDependency(Class<?> dependencyType, Object autowiredValue) {
                // TODO Auto-generated method stub

            }

            @Override
            public void registerScope(String scopeName, Scope scope) {
                // TODO Auto-generated method stub

            }

            @Override
            public void registerSingleton(String beanName, Object singletonObject) {
                // TODO Auto-generated method stub

            }

            @Override
            public void resolveAliases(StringValueResolver valueResolver) {
                // TODO Auto-generated method stub

            }

            @Override
            public Object resolveDependency(DependencyDescriptor descriptor, String beanName) throws BeansException {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Object resolveDependency(DependencyDescriptor descriptor, String beanName,
                                            Set<String> autowiredBeanNames, TypeConverter typeConverter) throws BeansException {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String resolveEmbeddedValue(String value) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public void setBeanClassLoader(ClassLoader beanClassLoader) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setBeanExpressionResolver(BeanExpressionResolver resolver) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setCacheBeanMetadata(boolean cacheBeanMetadata) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setConversionService(ConversionService conversionService) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setCurrentlyInCreation(String beanName, boolean inCreation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setParentBeanFactory(BeanFactory parentBeanFactory) throws IllegalStateException {
                // TODO Auto-generated method stub

            }

            @Override
            public void setTempClassLoader(ClassLoader tempClassLoader) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setTypeConverter(TypeConverter typeConverter) {
                // TODO Auto-generated method stub

            }
        });
        System.out.println(SessionTools.getInstance());
    }

    public static OperateLogService mockOpLog() {
        return (OperateLogService) Proxy.newProxyInstance(OperateLogService.class.getClassLoader(),
                new Class[]{OperateLogService.class}, (proxy, method, args) -> {
                    return null;
                });

    }

    public static SessionService mockSession() {
        return (SessionService) Proxy.newProxyInstance(SessionService.class.getClassLoader(),
                new Class[]{SessionService.class}, (proxy, method, args) -> {
                    Map<String, String> result = new HashMap<String, String>();
                    result.put(SessionTools.BID, "1");
                    result.put(SessionTools.SHOP_ID, "2");
                    result.put(SessionTools.STAFF_ID, "368023");
                    result.put(SessionTools.STAFF_NAME, "基德");
                    return result;
                });
    }

    public static void setSystemProperty() {
        System.setProperty("props.path", "src/test/resources");
    }
}
