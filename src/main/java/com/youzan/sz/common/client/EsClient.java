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
    private static final String propFileName = "application.properties";
    private static final String EsClientHost = PropertiesUtils.getProperty(propFileName, "idclient.host", "10.9.77.163");
    private static final String EsClientPort = PropertiesUtils.getProperty(propFileName, "idclient.port", "9200");
    private static final String libname = "store";

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
     * @return 扁平的map, 由业务自行组装
     */
    public static Page search(String tableName, Searchable searchable) {
        try {
            Object build = searchable.build();
            String url = getURL(tableName);
            String result = HttpUtil.restPost(url, build);
            List<Map<String, Object>> data = decode(result);
            Page page = searchable.getPage();
            page.setTotal(data.size());
            page.setList(data);
            return page;
        } catch (Exception e) {
            LOGGER.error("Es seach Error:{}", e);
            if (e instanceof IOException) {
                throw new BusinessException((long) ResponseCode.DECODE_ERROR.getCode(), ResponseCode.DECODE_ERROR.getMessage());
            } else {
                throw new BusinessException((long) ResponseCode.PARAMETER_ERROR.getCode(), e.getMessage());
            }
        }
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
            Map<String, Object> resultMap;
            for (InHits inHit : hits) {
                Map<String, Object> source = inHit.get_source();
                resultMap = new HashMap<>(source.size());
                source.keySet().forEach(k -> {
                    resultMap.put(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, k), source.get(k));
                });
                result.add(resultMap);
            }
        }
        return result;
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
