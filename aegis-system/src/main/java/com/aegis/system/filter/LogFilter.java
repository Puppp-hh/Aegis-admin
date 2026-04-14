package com.aegis.system.filter;

import com.aegis.common.utils.LogDesensitizeUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Component
public class LogFilter {

    @Autowired
    LogDesensitizeUtil logDesensitizeUtil;

    public List<Object> logFilter(ProceedingJoinPoint joinPoint,Object result) throws IllegalAccessException {
        /*
            转换params为json
            过滤
            HttpServletRequest
            HttpServletResponse
            MultipartFile
            InputStream / OutputStream
         */

        Stream<Object> param = Arrays.stream(joinPoint.getArgs());

        List<Object> filteredParams = param.filter(arg -> !(
                        arg instanceof HttpServletRequest
                                || arg instanceof HttpServletResponse
                                || arg instanceof MultipartFile
                                || arg instanceof InputStream
                                || arg instanceof OutputStream
                )
        ).toList();


        logDesensitizeUtil.logDesensitizeUtil(filteredParams);
        System.out.println(result);
        logDesensitizeUtil.logDesensitizeUtil(List.of(result));

        return filteredParams;
    }
}
