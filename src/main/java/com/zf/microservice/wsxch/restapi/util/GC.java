package com.zf.microservice.wsxch.restapi.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 手动垃圾回收工具类
 */
public class GC {

    private static final Logger LOGGER = LoggerFactory.getLogger(GC.class);

    /**
     * 关闭对象
     *
     * @param object
     */
    public static void close(Object object) {
        if (object != null) {
            ReflectionUtil.invoke(object, "close");
            object = null;
        }
    }

    /**
     * 批量关闭对象
     *
     * @param objects
     */
    public static void closeAll(Object... objects) {
        if (objects != null && objects.length > 0) {
            int max = objects.length;
            for (int i = 0; i < max; ++i) {
                Object object = objects[i];

                close(object);
            }
        }
    }

    public static void close(ResultSet rs, PreparedStatement ps, Connection con) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
            if (con != null) {
                con.close();
            }
        } catch (SQLException e) {
            LOGGER.error("[GC Error]", e);
            rs = null;
            ps = null;
            con = null;
        }
    }
}
