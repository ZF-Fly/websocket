package com.zf.microservice.wsxch.restapi.core;

import ch.qos.logback.core.joran.util.beans.BeanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class BeanContextAware implements ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(BeanUtil.class);

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (BeanContextAware.applicationContext == null) {
            BeanContextAware.applicationContext = applicationContext;
        }
    }

    /**
     * 获取applicationContext;
     *
     * @param
     * @return
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 通过Bean Name 获取Bean
     * 获取失败会返回null;
     *
     * @param name
     * @return
     */
    public static Object getBean(String name) {
        try {
            return getApplicationContext().getBean(name);
        } catch (NoSuchBeanDefinitionException e) {
            LOGGER.info("[Bean Util] No such bean, Bean name: {} --- {}", name, e.getMessage());
        }
        return null;
    }

    /**
     * 通过Class 获取Bean
     * 获取失败会返回null;
     *
     * @param clazz
     * @return
     */
    public static <T> T getBean(Class<T> clazz) {
        try {
            return getApplicationContext().getBean(clazz);
        } catch (NoSuchBeanDefinitionException e) {
            LOGGER.info("[Bean Util] No such bean, Bean name: {} --- {}", clazz.getSimpleName(), e.getMessage());
        }
        return null;
    }

    /**
     * 通过Bean Name, 以及制定Class 获取Bean
     * 获取失败会返回null;
     *
     * @param name
     * @return
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        try {
            return getApplicationContext().getBean(name, clazz);
        } catch (NoSuchBeanDefinitionException e) {
            LOGGER.info("[Bean Util] No such bean, Bean name: {} --- {}", clazz.getSimpleName(), e.getMessage());
        }
        return null;
    }
}