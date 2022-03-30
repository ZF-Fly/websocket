package com.zf.microservice.wsxch.restapi.util;

import com.zf.microservice.wsxch.restapi.object.constant.GlobalConst;

import java.util.UUID;

/**
 * UUID工具类
 */
public class UuidUtil {

    /**
     * 获取UUID
     * @return
     */
    public static String getUuid() {
    	return getUuid(false);
    }
    
    /**
     * 获取UUID
     * @param isRaw
     * @return
     */
    public static String getUuid(Boolean isRaw) {
    	String uuid = UUID.randomUUID().toString();
    	if (!isRaw) {
            uuid = uuid.replace(GlobalConst.STR_HYPHEN, GlobalConst.STR_EMPTY);
    	}
    	return uuid;
    }
    
}
