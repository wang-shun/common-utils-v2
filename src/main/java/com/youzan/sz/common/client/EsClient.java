package com.youzan.sz.common.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.CaseFormat;
import com.youzan.platform.bootstrap.exception.BusinessException;
import com.youzan.platform.util.net.HttpUtil;
import com.youzan.sz.common.model.Page;
import com.youzan.sz.common.response.enums.ResponseCode;
import com.youzan.sz.common.search.SearchItem;
import com.youzan.sz.common.search.Searchable;
import com.youzan.sz.common.search.es.decode.EsResult;
import com.youzan.sz.common.search.es.decode.InHits;
import com.youzan.sz.common.search.es.decode.OutHits;
import com.youzan.sz.common.util.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zefa on 16/4/20.
 */
public class EsClient {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String propFileName = "/application.properties";
    private static final String EsClientHost = PropertiesUtils.getProperty(propFileName, "esclient.host", "10.9.77.163");
    private static final String EsClientPort = PropertiesUtils.getProperty(propFileName, "esclient.port", "9200");
    private static final String libname = "store";
    private static ObjectMapper om = new ObjectMapper();

    static {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    //    10.9.77.163:8082/goods/_search -d '{your query}'
    private static final Logger LOGGER = LoggerFactory.getLogger(EsClient.class);

    /**
     * 搜索
     *
     * @param tableName  表名
     * @param searchable 查询条件
     * @return Page 对象
     */
    public static Page search(String tableName, Searchable searchable) {
        try {
            Object build = searchable.build();
            String url = getURL(tableName);
            LOGGER.debug("url : " + url + "  build: " + om.writeValueAsString(build));
            String result = HttpUtil.restPost(url, build);
            LOGGER.debug("result : " + result);
            Page page = searchable.getPage();
            return decode(result, page);
        } catch (Exception e) {
            LOGGER.error("Es seach Error:{}", e);
            if (e instanceof IOException) {
                throw new BusinessException((long) ResponseCode.DECODE_ERROR.getCode(), ResponseCode.DECODE_ERROR.getMessage());
            } else {
                throw new BusinessException((long) ResponseCode.PARAMETER_ERROR.getCode(), e.getMessage());
            }
        }
    }

    /**
     * 搜索
     *
     * @param tableName  表名
     * @param searchable 查询条件
     * @return 扁平的map, 由业务自行组装
     */
    public static List<Map<String, Object>> searchListMap(String tableName, Searchable searchable) {
        try {
            Object build = searchable.build();
            String url = getURL(tableName);
            LOGGER.debug("url : " + url + "  build: " + om.writeValueAsString(build));
            String result = HttpUtil.restPost(url, build);
            LOGGER.debug("result : " + result);
            return decode(result);
        } catch (Exception e) {
            LOGGER.error("Es seach Error:{}", e);
            if (e instanceof IOException) {
                throw new BusinessException((long) ResponseCode.DECODE_ERROR.getCode(), ResponseCode.DECODE_ERROR.getMessage());
            } else {
                throw new BusinessException((long) ResponseCode.PARAMETER_ERROR.getCode(), e.getMessage());
            }
        }
    }

    /**
     * 搜索
     *
     * @param tableName  表名
     * @param searchable 查询条件
     * @return 单个结果
     */
    public static Map<String, Object> searchMap(String tableName, Searchable searchable) {
        List<Map<String, Object>> data = searchListMap(tableName, searchable);
        if (data.size() == 0) {
            return null;
        }
        return data.get(0);
    }

    private static String getURL(String tableName) {
        return "http://" + EsClientHost + ":" + EsClientPort + "/" + tableName + "/_search";
    }

    private static List<Map<String, Object>> decode(String str) throws IOException {
        EsResult esResult = OBJECT_MAPPER.readValue(str, EsResult.class);
        log(esResult);
        List<InHits> hits = esResult.getHits().getHits();
        List<Map<String, Object>> result = new ArrayList<>();
        if (hits != null) {
            for (InHits inHit : hits) {
                Map<String, Object> source = inHit.get_source();
                Map<String, Object> resultMap = new HashMap<>(source.size());
                source.keySet().forEach(k ->
                        resultMap.put(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, k), source.get(k)));
                result.add(resultMap);
            }
        }
        return result;
    }

    private static Page decode(String str, Page page) throws IOException {
        EsResult esResult = OBJECT_MAPPER.readValue(str, EsResult.class);
        log(esResult);
        OutHits hits = esResult.getHits();
        if (hits != null) {
            List<Map<String, Object>> result = decode(str);
            page.setList(result);
            page.setTotal(esResult.getHits().getTotal());
        }
        return page;
    }

    private static void log(EsResult esResult) {
        //// TODO: 16/4/21 log 
    }

    public static void main(String[] args) {
        Searchable searchable = new Searchable();
        searchable.addAnd(SearchItem.eq("status", "0"));
        searchable.addAnd(SearchItem.like("yzAccount", "1"));
        Page page = EsClient.search("shop_staff_v1", searchable);
        System.out.println(page.getlist().toString());
    }
}
