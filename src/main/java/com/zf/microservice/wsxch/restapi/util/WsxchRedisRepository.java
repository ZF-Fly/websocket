package com.zf.microservice.wsxch.restapi.util;

import com.alibaba.fastjson.JSONArray;
import com.zf.microservice.wsxch.restapi.core.BaseRedisRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;


/**
 * @author Fly_Roushan
 * @date 2020/12/6
 */
@Repository
public class WsxchRedisRepository extends BaseRedisRepository {
    public JSONArray fetchWsxchArray(String cacheKey, Object... args) {
        String resultStr = super.fetchString(cacheKey, args);
        JSONArray resultArray = new JSONArray();
        if (StringUtils.isNotEmpty(resultStr)) {
            resultArray = JSONArray.parseArray(resultStr);
        }
        return resultArray;
    }
}
