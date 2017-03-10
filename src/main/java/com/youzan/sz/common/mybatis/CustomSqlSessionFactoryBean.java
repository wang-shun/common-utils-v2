package com.youzan.sz.common.mybatis;

import com.youzan.platform.util.lang.StringUtil;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;


/**
 * Created by YANG on 2015/12/3.
 */
public class CustomSqlSessionFactoryBean extends SqlSessionFactoryBean {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomSqlSessionFactoryBean.class);
    
    private static final String ROOT_PATH = "classes" + File.separator;
    
    private static final String[] PATH_REPLACE_ARRAY = {"]"};
    
    private static final String ROOT_PATH_SPLIT = ",";
    
    
    @Override
    public void setTypeAliasesPackage(String typeAliasesPackage) {
        
        if (StringUtil.isBlank(typeAliasesPackage)) {
            super.setTypeAliasesPackage(typeAliasesPackage);
            return;
        }
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        StringBuilder typeAliasesPackageStringBuilder = new StringBuilder();
        try {
            for (String location : StringUtils.tokenizeToStringArray(typeAliasesPackage, ",; \t\n")) {
                if (StringUtil.isBlank(location)) {
                    continue;
                }
                location = "classpath*:" + location.trim().replace(".", File.separator);
                typeAliasesPackageStringBuilder.append(getResources(resolver, location));
            }
        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
        }
        if (StringUtil.isBlank(typeAliasesPackageStringBuilder.toString())) {
            throw new RuntimeException("mybatis typeAliasesPackage 路径扫描错误！请检查applicationContext.xml@sqlSessionFactory配置！");
        }
        typeAliasesPackage = replaceResult(typeAliasesPackageStringBuilder.toString()).replace(File.separator, ".");
        super.setTypeAliasesPackage(typeAliasesPackage);
    }
    
    
    private String getResources(ResourcePatternResolver resolver, String location) throws IOException {
        StringBuilder resourcePathStringBuilder = new StringBuilder();
        for (Resource resource : resolver.getResources(location)) {
            String description = resource == null ? "" : resource.getDescription();
            if (resource == null || StringUtil.isBlank(resource.getDescription())) {
                continue;
            }
            
            resourcePathStringBuilder.append(description.substring(description.indexOf(ROOT_PATH) + ROOT_PATH.length())).append(ROOT_PATH_SPLIT);
        }
        
        return resourcePathStringBuilder.toString();
    }
    
    
    private String replaceResult(String resultStr) {
        //        for (String replaceStr : PATH_REPLACE_ARRAY) {
        //            resultStr = resultStr.replace(replaceStr, "");
        //        }
        
        return resultStr.replace("]", "");
    }
    
}
