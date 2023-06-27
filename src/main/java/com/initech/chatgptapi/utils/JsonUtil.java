package com.initech.chatgptapi.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class JsonUtil {

    public static Map<String, Object> jsonToMap(String json) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        TypeReference<Map<String, Object>> typeReference = new TypeReference<Map<String, Object>>() {};
        return objectMapper.readValue(json, typeReference);
    }

    public static String mapToJson(Map<String, Object> map) throws Exception{
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(map);
    }

}
