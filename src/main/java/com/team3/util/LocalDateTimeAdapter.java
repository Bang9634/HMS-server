package com.team3.util;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * LocalDateTime을 JSON으로 직렬화/역직렬화하는 Gson 어댑터
 * <p>
 * Gson은 기본적으로 Java 8의 LocalDateTime을 지원하지 않으므로
 * 커스텀 어댑터를 제공한다.
 * </p>
 * 
 * @author bang9634
 * @since 2025-11-23
 */
public class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
    
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    @Override
    public JsonElement serialize(LocalDateTime dateTime, Type type, JsonSerializationContext context) {
        return new JsonPrimitive(dateTime.format(formatter));
    }
    
    @Override
    public LocalDateTime deserialize(JsonElement json, Type type, JsonDeserializationContext context) 
            throws JsonParseException {
        return LocalDateTime.parse(json.getAsString(), formatter);
    }
}