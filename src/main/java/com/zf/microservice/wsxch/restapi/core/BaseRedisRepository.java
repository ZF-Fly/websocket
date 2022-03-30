package com.zf.microservice.wsxch.restapi.core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zf.microservice.wsxch.restapi.object.constant.CodeConst;
import com.zf.microservice.wsxch.restapi.object.constant.CryptUtil;
import com.zf.microservice.wsxch.restapi.object.constant.GlobalConst;
import com.zf.microservice.wsxch.restapi.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;

public abstract class BaseRedisRepository {

    /**
     * change datasource of redisTemplate through override this function
     */
    protected StringRedisTemplate redisReaderTemplate() {
        return primaryReaderRedisTemplate;
    }

    /**
     * change datasource of redisTemplate through override this function
     */
    protected StringRedisTemplate redisWriterTemplate() {
        return primaryWriterRedisTemplate;
    }

    public String generateKey(String cacheKey, Object... options) {

        String idStr = StringUtil.connectArray(GlobalConst.STR_POUND, options);

        return String.format("%s@%s", cacheKey, CryptUtil.md5(idStr));
    }

    //region Based on the API
    public Integer set(String key, String value) {
        try {
            redisWriterTemplate().opsForValue().set(key, value);
            return CodeConst.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return CodeConst.FAILURE;
        }
    }

    public void set(String key, String value, Long timeout) {
        try {
            if (timeout != null && timeout > 0) {
                redisWriterTemplate().opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
            } else {
                redisWriterTemplate().opsForValue().set(key, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setExpire(String key, Long timeout) {
        try {
            if (timeout > 0) {
                redisWriterTemplate().expire(key, timeout, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 通过CacheKey 缓存字符串
     *
     * @param cacheKey 缓存Key
     * @param dataStr  缓存数据
     * @param args     生成最终缓存key的参数
     */
    public void cacheString(String cacheKey, String dataStr, Object... args) {
        cacheString(cacheKey, dataStr, null, args);
    }

    /**
     * 通过CacheKey 缓存字符串
     *
     * @param cacheKey  缓存Key
     * @param dataStr   缓存数据
     * @param expiredIn 过期时间（秒）
     * @param args      生成最终缓存key的参数
     */
    public void cacheString(String cacheKey, String dataStr, Long expiredIn, Object... args) {
        cacheKey = generateKey(cacheKey, args);
        set(cacheKey, dataStr, expiredIn);
    }

    /**
     * 根据指定值缓存计数器（opsForValue）
     *
     * @param cacheKey  缓存键
     * @param expiredIn 过期时间（秒）
     * @param delta     增加值
     * @param args      合成最终缓存键的值
     */
    public void cacheCountByArgs(String cacheKey, Long expiredIn, Long delta, Object... args) {
        String storeKey = generateKey(cacheKey, args);
        redisWriterTemplate().opsForValue().increment(storeKey, delta);
        if (expiredIn != null) {
            setExpire(storeKey, expiredIn);
        }
    }

    /**
     * 根据指定值缓存计数器（opsForValue）
     *
     * @param cacheKey 缓存键
     * @param delta    增加值
     * @return
     */
    public Long cacheCount(String cacheKey, Long delta) {
        String storeKey = generateKey(cacheKey);
        Long increment = redisReaderTemplate().opsForValue().increment(storeKey, delta);
        return increment;
    }

    /**
     * 根据指定值缓存计数器（opsForValue）
     *
     * @param cacheKey 缓存键
     * @param delta    增加值
     * @param args     合成最终缓存键的值
     */
    public void cacheCountByArgs(String cacheKey, Long delta, Object... args) {
        cacheCountByArgs(cacheKey, null, delta, args);
    }

    /**
     * 根据指定值缓存计数器（opsForValue）
     *
     * @param cacheKey 缓存键
     * @param args     合成最终缓存键的值
     */
    public void cacheCountByArgs(String cacheKey, Object... args) {
        cacheCountByArgs(cacheKey, null, DEFAULT_STEP_VALUE, args);
    }

    /**
     * 根据指定值缓存计数器（opsForValue）
     *
     * @param cacheKey  缓存键
     * @param dataJson  缓存值
     * @param expiredIn 过期时间（秒）
     * @param keys      JSONObject中键
     */
    public void cacheObjectByKeys(String cacheKey, JSONObject dataJson, Long expiredIn, Object... keys) {

        Object[] valueArray = extractValuesFromJsonObject(dataJson, keys);

        String uniqueKeyInLoop = StringUtil.connectArray(GlobalConst.STR_POUND, valueArray);

        cacheObjectByArgs(cacheKey, dataJson.toJSONString(), expiredIn, uniqueKeyInLoop);
    }

    /**
     * 根据指定值缓存计数器（opsForValue）
     *
     * @param cacheKey 缓存键
     * @param dataJson 缓存值
     * @param keys     JSONObject中键
     */
    public void cacheObjectByKeys(String cacheKey, JSONObject dataJson, Object... keys) {
        cacheObjectByKeys(cacheKey, dataJson, null, keys);
    }

    /**
     * 根据指定值缓存单个对象（opsForValue）
     *
     * @param cacheKey 缓存键
     * @param dataStr  对象数据JSONObject字符串
     * @param args     合成最终缓存键的值
     */
    public void cacheObjectByArgs(String cacheKey, String dataStr, Object... args) {
        cacheObjectByArgs(cacheKey, dataStr, null, args);
    }

    /**
     * 根据指定值缓存单个对象（opsForValue）
     *
     * @param cacheKey  缓存键
     * @param dataStr   对象数据JSONObject字符串
     * @param expiredIn 过期时间（秒）
     * @param args      合成最终缓存键的值
     */
    public void cacheObjectByArgs(String cacheKey, String dataStr, Long expiredIn, Object... args) {
        String storeKey = generateKey(cacheKey, args);

        deleteCacheKeyByArgs(cacheKey, args);

        set(storeKey, dataStr, expiredIn);
    }

    /**
     * 根据指定值环创对象数组（opsForValue）
     *
     * @param cacheKey  缓存键
     * @param dataArray 缓存值
     * @param expiredIn 过期时间（秒）
     * @param keys      JSONObject中键
     */
    public void cacheArrayByKeys(String cacheKey, JSONArray dataArray, Long expiredIn, Object... keys) {
        if (keys == null || keys.length == 0) {
            cacheArrayByArgs(cacheKey, dataArray, expiredIn);
        } else {
            Map<String, JSONArray> dataMap = groupJsonArrayByKeys(dataArray, keys);
            for (Map.Entry<String, JSONArray> entry : dataMap.entrySet()) {
                String key = entry.getKey();
                JSONArray value = entry.getValue();

                cacheArrayByArgs(cacheKey, value, expiredIn, key);
            }
        }
    }

    /**
     * 根据指定值环创对象数组（opsForValue）
     *
     * @param cacheKey  缓存键
     * @param dataArray 缓存值
     * @param keys      JSONObject中键
     */
    public void cacheArrayByKeys(String cacheKey, JSONArray dataArray, Object... keys) {
        cacheArrayByKeys(cacheKey, dataArray, null, keys);
    }

    /**
     * 根据指定值缓存对象数组（opsForList）
     *
     * @param cacheKey  缓存键
     * @param dataArray 缓存值
     * @param args      作为缓存键的值
     */
    public void cacheArrayByArgs(String cacheKey, JSONArray dataArray, Object... args) {
        cacheArrayByArgs(cacheKey, dataArray, null, args);
    }

    /**
     * 根据指定值缓存对象数组（opsForList）
     *
     * @param cacheKey  缓存键
     * @param dataArray 缓存值
     * @param expiredIn 过期时间（秒）
     * @param args      作为缓存键的值
     */
    public void cacheArrayByArgs(String cacheKey, JSONArray dataArray, Long expiredIn, Object... args) {
        String storeKey = generateKey(cacheKey, args);
        String[] strArray = dataArray.toJavaObject(String[].class);
        deleteCacheKeyByArgs(cacheKey, args);

        redisWriterTemplate().opsForList().rightPushAll(storeKey, strArray);
        if (expiredIn != null) {
            setExpire(storeKey, expiredIn);
        }
    }

    /**
     * 将单条数据插入Set
     *
     * @param cacheKey  缓存键
     * @param itemStr   添加的单条数据
     * @param expiredIn 过期时间（秒）
     * @param args      JSONObject中键
     */
    public void addToSet(String cacheKey, String itemStr, Long expiredIn, Object... args) {
        String storeKey = generateKey(cacheKey, args);
        redisWriterTemplate().opsForSet().add(storeKey, itemStr);
        if (expiredIn != null) {
            setExpire(storeKey, expiredIn);
        }
    }

    /**
     * 将单条数据插入HashMap
     *
     * @param cacheKey
     * @param key
     * @param value
     * @param expiredIn
     * @param args
     */
    public void cacheHashMap(String cacheKey, Object key, Object value, Long expiredIn, Object... args) {
        String storeKey = generateKey(cacheKey, args);
        redisWriterTemplate().opsForHash().put(storeKey, key, value);
        if (expiredIn != null) {
            setExpire(storeKey, expiredIn);
        }
    }

    public void cacheHashMapByArgs(String cacheKey, Map<?, ?> map, Object... args) {
        cacheHashMapByArgs(cacheKey, null, map, args);
    }

    public void cacheHashMapByArgs(String cacheKey, Long expiredIn, Map<?, ?> map, Object... args) {
        String storeKey = generateKey(cacheKey, args);
        cacheHashMap(storeKey, expiredIn, map);
    }

    public void cacheHashMap(String cacheKey, Map<?, ?> map) {
        cacheHashMap(cacheKey, null, map);
    }

    /**
     * 批量插入HashMap
     *
     * @param cacheKey
     * @param map
     * @param expiredIn
     * @param args
     */
    public void cacheHashMap(String cacheKey, Long expiredIn, Map<?, ?> map) {
        redisWriterTemplate().opsForHash().putAll(cacheKey, map);
        if (expiredIn != null) {
            setExpire(cacheKey, expiredIn);
        }
    }

    /**
     * 批量将数据插入Set
     *
     * @param storeKey  缓存键
     * @param expiredIn 过期时间(可选)
     * @param params    参数集
     */
    public void batchAddToSet(String storeKey, Long expiredIn, String... params) {
        redisWriterTemplate().opsForSet().add(storeKey, params);
        if (expiredIn != null) {
            setExpire(storeKey, expiredIn);
        }
    }

    public void batchAddToSet(String storeKey, String... params) {
        batchAddToSet(storeKey, null, params);
    }

    public void addToSet(String cacheKey, String itemStr, Object... args) {
        addToSet(cacheKey, itemStr, null, args);
    }

    public void delete(String key) {
        if (key != null) {
            redisWriterTemplate().delete(key);
        }
    }

    /**
     * 删除一个集合的key
     *
     * @param collections
     */
    public void delete(Collection<String> collections) {
        redisWriterTemplate().delete(collections);
    }

    public void batchDelete(String... keys) {
        if (keys != null && keys.length > 0) {
            redisWriterTemplate().delete(Arrays.asList(keys));
        }
    }

    /**
     * 根据指定值删除缓存对象
     *
     * @param cacheKey 缓存键
     * @param args     合成参数
     */
    public void deleteCacheKeyByArgs(String cacheKey, Object... args) {
        String storeKey = generateKey(cacheKey, args);

        if (hasKey(storeKey)) {
            delete(storeKey);
        }
    }

    public String get(String key) {
        if (key != null) {
            return redisReaderTemplate().opsForValue().get(key);
        } else {
            return null;
        }
    }

    public Boolean hasKey(String key) {
        try {
            return redisReaderTemplate().hasKey(key);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Long getExpire(String key) {
        return redisReaderTemplate().getExpire(key, TimeUnit.SECONDS);
    }

    public Object hget(String key, String item) {
        return redisReaderTemplate().opsForHash().get(key, item);
    }

    public Map<Object, Object> hgetAll(String key) {
        return redisReaderTemplate().opsForHash().entries(key);
    }

    /**
     * fetch count
     *
     * @param cacheKey key
     * @param args     参数
     */
    public Long fetchLongByArgs(String cacheKey, Object... args) {
        cacheKey = generateKey(cacheKey, args);
        String countStr = get(cacheKey);
        return StringUtils.isNotEmpty(countStr) ? Long.parseLong(countStr) : 0L;
    }

    /**
     * 根据传入类型获取对应类型的实体类
     *
     * @param clazz    Class对象
     * @param cacheKey 缓存建
     * @param args     任意长度参数
     * @param <E>      任意对象
     */
    public <E> E fetchObject(String cacheKey, Class<E> clazz, Object... args) {
        cacheKey = generateKey(cacheKey, args);
        String dataStr = get(cacheKey);
        if (StringUtils.isNotBlank(dataStr)) {
            JSONObject dataJson = JSON.parseObject(dataStr);
            return dataJson.toJavaObject(clazz);
        } else {
            return null;
        }
    }

    public JSONObject fetchJsonObject(String cacheKey, Object... args) {
        cacheKey = generateKey(cacheKey, args);
        String dataStr = get(cacheKey);
        if (StringUtils.isNotBlank(dataStr)) {
            return JSON.parseObject(dataStr);
        } else {
            return new JSONObject();
        }
    }


    public Map<Object, Object> fetchHashMap(String cacheKey, Object... args) {
        cacheKey = generateKey(cacheKey, args);
        return redisReaderTemplate().opsForHash().entries(cacheKey);
    }

    /**
     * 根据传入类型获取对应类型的List
     *
     * @param clazz    Class对象
     * @param cacheKey 缓存建
     * @param args     任意长度参数
     * @param <E>      任意对象
     *                 <p>Don't return JSON.parseArray(JSON.toJSONString(dataList), clazz);</p>
     */
    public <E> List<E> fetchArray(String cacheKey, Class<E> clazz, Object... args) {
        cacheKey = generateKey(cacheKey, args);

        List<E> resultList = new ArrayList<>();
        if (hasKey(cacheKey)) {
            Long size = redisReaderTemplate().opsForList().size(cacheKey);
            if (!Objects.isNull(size)) {
                List<String> dataList = Optional.ofNullable(redisReaderTemplate().opsForList().range(cacheKey, 0L, size - 1)).orElse(new ArrayList<>());
                for (String value : dataList) {
                    resultList.add(JSON.parseObject(value, clazz));
                }
            }
        }

        return resultList;
    }

    /**
     * 根据传入类型获取对应类型的List
     * value 不转为Json格式
     *
     * @param cacheKey
     * @param args
     * @return
     */
    public List<String> fetchArray(String cacheKey, Object... args) {
        cacheKey = generateKey(cacheKey, args);

        List<String> resultList = new ArrayList<>();
        if (hasKey(cacheKey)) {
            Long size = redisReaderTemplate().opsForList().size(cacheKey);
            if (!Objects.isNull(size)) {
                resultList = Optional.ofNullable(redisReaderTemplate().opsForList().range(cacheKey, 0L, size - 1)).orElse(new ArrayList<>());
            }
        }

        return resultList;
    }

    public JSONArray fetchJsonArray(String cacheKey, Object... args) {
        cacheKey = generateKey(cacheKey, args);

        JSONArray resultArray = new JSONArray();
        if (hasKey(cacheKey)) {
            Long size = redisReaderTemplate().opsForList().size(cacheKey);
            if (!Objects.isNull(size)) {
                List<String> dataList = Optional.ofNullable(redisReaderTemplate().opsForList().range(cacheKey, 0L, size - 1)).orElse(new ArrayList<>());
                for (String value : dataList) {
                    resultArray.add(JSON.parse(value));
                }
            }
        }
        return resultArray;
    }

    /**
     * 根据传入类型获取对应类型的Set
     *
     * @param clazz    Class对象
     * @param cacheKey 缓存建
     * @param args     任意长度参数
     * @param <T>      任意对象
     */
    public <T> Set<T> fetchSet(Class<T> clazz, String cacheKey, Object... args) {
        Set<T> resultSet = new HashSet<>();
        Set<String> members = redisReaderTemplate().opsForSet().members(generateKey(cacheKey, args));
        if (Objects.nonNull(members)) {
            members.forEach(value -> resultSet.add(JSON.parseObject(value, clazz)));
        }
        return resultSet;
    }

    /**
     * 根据传入Key获取对应String
     *
     * @param cacheKey 缓存建
     * @param args     不定长参数
     */
    public String fetchString(String cacheKey, Object... args) {
        String result = null;
        cacheKey = generateKey(cacheKey, args);
        if (hasKey(cacheKey)) {
            result = get(cacheKey);
        }
        return result;
    }

    /**
     * 查出符合规则的所有key
     * 可以和delete联用
     *
     * @param pattern CAHCHEKEY*
     * @return CAHCHEKEY1, CAHCHEKEY2, CAHCHEKEY3...
     */
    public Set<String> keys(String pattern) {
        return redisReaderTemplate().keys(pattern);
    }

    /**
     * 判断是否在Set中存在
     *
     * @param cacheKey
     * @param value
     * @return
     */
    public Boolean isMember(String cacheKey, String value) {
        return redisReaderTemplate().opsForSet().isMember(cacheKey, value);
    }

    /**
     * 从数据对象中提取值
     *
     * @param dataJson 对象数据JSONObject字符串
     * @param keys     JSONObject中键
     */
    private Object[] extractValuesFromJsonObject(JSONObject dataJson, Object... keys) {
        List<Object> valueList = new ArrayList<>();

        if (keys != null) {
            for (Object o : keys) {
                String key = String.valueOf(o);
                if (dataJson.containsKey(key)) {
                    valueList.add(dataJson.get(key));
                }
            }
        }
        return valueList.toArray();
    }

    /**
     * 根据指定键对数据对象数组进行分组
     *
     * @param totalDataArray 缓存值
     * @param keys           提取Array中JsonObject对应key的value, 生成特定的uniqueKey
     */
    private Map<String, JSONArray> groupJsonArrayByKeys(JSONArray totalDataArray, Object... keys) {
        Map<String, JSONArray> dataMap = new HashMap<>();

        JSONArray dataArray;

        for (int i = 0; i < totalDataArray.size(); ++i) {
            JSONObject dataJson = totalDataArray.getJSONObject(i);

            Object[] valueArray = extractValuesFromJsonObject(dataJson, keys);

            String uniqueKeyInLoop = StringUtil.connectArray(GlobalConst.STR_POUND, valueArray);
            if (!dataMap.containsKey(uniqueKeyInLoop)) {
                dataArray = new JSONArray();
                dataMap.put(uniqueKeyInLoop, dataArray);
            } else {
                dataArray = dataMap.get(uniqueKeyInLoop);
            }

            dataArray.add(dataJson);
        }

        return dataMap;
    }

    @Autowired
    @Qualifier("primaryReaderRedisTemplate")
    private StringRedisTemplate primaryReaderRedisTemplate;

    @Autowired
    @Qualifier("primaryWriterRedisTemplate")
    private StringRedisTemplate primaryWriterRedisTemplate;

    public static final long DEFAULT_STEP_VALUE = 1;
}
