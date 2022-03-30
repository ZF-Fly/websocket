package com.zf.microservice.wsxch.restapi.object.constant;

import java.math.BigDecimal;

/**
 * Global Constant Definitions
 *
 */
public interface GlobalConst {

    //region =========================================================================================================== Default Setting
    int DEFAULT_RANDOM_SIZE = 6;
    String DEFAULT_CHARSET = "UTF-8";
    String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
    String DEFAULT_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    String DEFAULT_LANGUAGE_CODE = "GB";
    //endregion

    //region =========================================================================================================== Date and Time
    long HOURS_IN_DAY = 24;
    long MINUTES_IN_HOUR = 60;
    long SECONDS_IN_MINUTE = 60;
    long SECONDS_IN_HOUR = MINUTES_IN_HOUR * SECONDS_IN_MINUTE;
    long SECONDS_IN_DAY = HOURS_IN_DAY * SECONDS_IN_HOUR;

    long MILLISECONDS_UNIT = 1000;

    int DAYS_IN_WEEK = 7;
    int DAYS_IN_MONTH = 30;
    int DAYS_IN_YEAR = 365;

    int LAST_DAY_OF_MONTH = -1;
    //endregion

    //region =========================================================================================================== Special Character
    String STR_AT = "@";
    String STR_COLON = ":";
    String STR_COMMA = ",";
    String STR_DASH = "--";
    String STR_DOT = ".";
    String STR_ELLIPSIS = "...";
    String STR_EMPTY = "";
    String STR_HYPHEN = "-";
    String STR_POUND = "#";
    String STR_SEMICOLON = ";";
    String STR_TILDE = "~";
    String STR_UNDERLINE = "_";
    String STR_VERTICALBAR = "|";
    String STR_PERCENT_SIGNS = "%";
    String STR_EQUAL_SIGNS = "=";
    String STR_OPEN_BRACKET = "[";
    String STR_CLOSE_BRACKET = "]";
    String STR_QUOTATION_MARKS = "\"";
    String STR_BLANK_SPACE = " ";
    String STR_PLUS = "+";
    //endregion

    //region =========================================================================================================== Algorithm Calculation
    int CALCULATE_INSTALLMENT_START_OFFSET = 3;
    double CALCULATE_INSTALLMENT_STEP_VALUE = 0.01;
    //endregion

    //region =========================================================================================================== Special Value in different number type
    int INT_ZERO = 0;
    long LONG_ZERO = 0L;
    float FLOAT_ZERO = 0.0F;
    double DOUBLE_ZERO = 0.0D;
    BigDecimal BIG_ZERO = BigDecimal.ZERO;

    int INT_ONE = 1;
    long LONG_ONE = 1L;
    float FLOAT_ONE = 1.0F;
    double  DOUBLE_ONE = 1.0D;
    BigDecimal BIG_ONE = BigDecimal.ONE;
    //endregion
}
