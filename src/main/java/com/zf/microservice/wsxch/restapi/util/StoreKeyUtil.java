package com.zf.microservice.wsxch.restapi.util;

import com.zf.microservice.wsxch.restapi.object.constant.CryptUtil;
import com.zf.microservice.wsxch.restapi.object.constant.GlobalConst;

public class StoreKeyUtil {

    public static String generateKey(String cacheKey, Object... args) {
        String idStr = StringUtil.connectArray(GlobalConst.STR_POUND, args);

        return String.format("%s@%s", cacheKey, CryptUtil.md5(idStr));
    }

}