package com.youzan.sz.common.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.youzan.platform.bootstrap.exception.BusinessException;
import com.youzan.platform.util.net.HttpUtil;
import com.youzan.sz.common.response.enums.ResponseCode;
import com.youzan.sz.common.search.Searchable;
import com.youzan.sz.common.search.es.decode.EsResult;
import com.youzan.sz.common.search.es.decode.InHits;
import com.youzan.sz.common.util.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by zefa on 16/4/20.
 */
public class EsClient {
    private static final String propFileName = "application.properties";
    private static final String EsClientHost = PropertiesUtils.getProperty(propFileName, "idclient.host", "10.6.1.219");
    private static final String EsClientPort = PropertiesUtils.getProperty(propFileName, "idclient.port", "8082");
    private static final String libname = "store";

    //    10.6.1.219:8082/wholesale_goods_v3/goods/_search -d '{your query}'
    private static final Logger LOGGER = LoggerFactory.getLogger(EsClient.class);

    /**
     * 搜索
     *
     * @param tableName  表名
     * @param searchable 查询条件
     * @return 扁平的map, 由业务自行组装
     */
    public List<Map<String, String>> search(String tableName, Searchable searchable) {
        try {
            String result = HttpUtil.restPost(getURL(tableName), searchable.build());
            return decode(result);
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw new BusinessException((long) ResponseCode.DECODE_ERROR.getCode(), ResponseCode.DECODE_ERROR.getMessage());
            } else {
                throw new BusinessException((long) ResponseCode.PARAMETER_ERROR.getCode(), e.getMessage());
            }
        }
    }

    private String getURL(String tableName) {
        return "http://" + EsClientHost + ":" + EsClientPort + "/" + libname + "/" + tableName;
    }

    private List<Map<String, String>> decode(String str) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        EsResult esResult = objectMapper.readValue(str, EsResult.class);
        log(esResult);
        List<InHits> hits = esResult.getHits().getHits();
        List<Map<String, String>> result = new ArrayList<>();
        if (hits != null) {
            for (InHits inHit : hits) {
                result.add(inHit.get_source());
            }
        }
        return result;
    }

    private void log(EsResult esResult) {
        //// TODO: 16/4/21 log 
    }
}
