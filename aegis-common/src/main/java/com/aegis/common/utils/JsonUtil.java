package com.aegis.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class JsonUtil {

    @Autowired
    private ObjectMapper objectMapper;

    // 1. 对象 → JSON
//    String json = JsonUtil.objectToJson(user);
    // 输出: {"id":1,"name":"admin"}
    public String objectToJson(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }


    // 2. JSON → 对象
//    User user = JsonUtil.jsonToObject(json, User.class);
    public <T> T jsonToObject(String json, Class<T> clazz) throws JsonProcessingException {
        return objectMapper.readValue(json, clazz);
    }

    // 3. JSON 数组 → List
//    List<User> list = JsonUtil.jsonToList("[{...}, {...}]", User.class);
    public <T> T jsonToObject(String json, TypeReference<T> typeReference) {
        return objectMapper.convertValue(json, typeReference);
    }

    // 4. JSON → Map（通用）
//    Map map = JsonUtil.jsonToMap(json);
    public Map<String, Object> jsonToMap(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
    }

    // 5. JSON → Map（指定 value 类型）
//    Map<String, String> strMap = JsonUtil.jsonToMap(json, String.class);
    public <V> Map<String, V> jsonToMap(String json, Class<V> valueType) throws JsonProcessingException {
        return objectMapper.readValue(json, new TypeReference<Map<String, V>>() {});
    }

    // 6. 对象 → Map
//    Map map = JsonUtil.objectToMap(user);
    public <V> Map<String, V> jsonToMap(String json, TypeReference<V> valueType) throws JsonProcessingException {
        return objectMapper.readValue(json, new TypeReference<Map<String, V>>() {});
    }

    // 7. Map → 对象
//    User user = JsonUtil.mapToObject(map, User.class);
    public <T> T MapToObject(Map<String, Object> map, Class<T> clazz) {
        return objectMapper.convertValue(map, clazz);
    }

    // 8. 验证 JSON
//    boolean valid = JsonUtil.isValidJson(json);  // true/false
    public boolean isValidJson(String json) {
        try  {
            objectMapper.readTree(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}