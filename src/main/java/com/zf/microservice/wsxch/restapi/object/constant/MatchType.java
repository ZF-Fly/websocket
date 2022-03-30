package com.zf.microservice.wsxch.restapi.object.constant;


/**
 * @author ice
 * @description 字符串匹配类型常量定义
 */
public interface MatchType {

    int EQUALS = 1;
    int CONTAINS_TARGET = 2;
    int STARTS_WITH_TARGET = 3;
    int ENDS_WITH_TARGET = 4;
    int CONTAINS_SOURCE = 5;
    int STARTS_WITH_SOURCE = 6;
    int ENDS_WITH_SOURCE = 7;

}
