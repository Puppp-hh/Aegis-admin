package com.aegis.common.utils;

import com.aegis.common.annotation.Sensitive;
import com.aegis.common.exception.BusinessException;
import com.aegis.common.model.SensitiveType;
import com.aegis.common.result.ResultCode;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;

@Component
public class LogDesensitizeUtil {

    // 防止循环引用（对象互相引用导致死递归）
    private final Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());

    public void logDesensitizeUtil(List<Object> list) throws IllegalAccessException {
        if (list == null || list.isEmpty()) {
            return;
        }

        for (Object obj : list) {
            scanObject(obj);
        }

        // 清理（避免内存污染）
        visited.clear();
    }

    /**
     * 核心递归
     */
    private void scanObject(Object obj) throws IllegalAccessException {
        if (obj == null) {
            return;
        }

        // 防止循环引用
        if (visited.contains(obj)) {
            return;
        }
        visited.add(obj);

        Class<?> clazz = obj.getClass();

        // 只处理自己项目的类（关键！）
        if (!isCustomClass(clazz)) {
            return;
        }

        Field[] fields = clazz.getDeclaredFields();

        for (Field f : fields) {
            f.setAccessible(true);

            Object value = f.get(obj);

            if (value == null) continue;

            // ========= 1. 注解脱敏 =========
            Sensitive sensitive = f.getAnnotation(Sensitive.class);
            if (sensitive != null) {
                Object newValue = filter(sensitive.type(), value);
                f.set(obj, newValue);
                continue;
            }

            // ========= 2. List =========
            if (value instanceof List<?> list) {
                for (Object item : list) {
                    scanObject(item);
                }
                continue;
            }

            // ========= 3. Map（新增） =========
            if (value instanceof Map<?, ?> map) {
                for (Object item : map.values()) {
                    scanObject(item);
                }
                continue;
            }

            // ========= 4. 基础类型 =========
            if (isSimpleType(value)) {
                continue;
            }

            // ========= 5. 递归 =========
            scanObject(value);
        }
    }

    /**
     * 是否是你自己的类（核心优化点）
     */
    private boolean isCustomClass(Class<?> clazz) {
        Package pkg = clazz.getPackage();
        return pkg != null && pkg.getName().startsWith("com.aegis");
    }

    /**
     * 基础类型判断
     */
    private boolean isSimpleType(Object value) {
        return value instanceof String
                || value instanceof Number
                || value instanceof Boolean
                || value instanceof Character
                || value instanceof Enum<?>;
    }

    /**
     * 脱敏策略（可扩展）
     */
    private String filter(SensitiveType type, Object value) {
        if (!(value instanceof String str)) {
            throw new BusinessException(ResultCode.DESENSITIZE_TYPE_UNSUPPORTED);
        }

        return switch (type) {
            case PASSWORD -> "******";

            case EMAIL -> {
                if (str.length() <= 4) {
                    yield "****";
                }
                yield str.substring(0, 2) + "****";
            }

            case PHONE -> {
                if (str.length() < 7) {
                    yield "****";
                }
                yield str.substring(0, 3) + "****" + str.substring(str.length() - 4);
            }

            case TOKEN -> {
                if (str.length() <= 6) {
                    yield "******";
                }
                yield str.substring(0, 3) + "****";
            }

            default -> throw new BusinessException(ResultCode.DESENSITIZE_PROCESS_ERROR);
        };
    }
}