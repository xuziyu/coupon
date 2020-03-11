package com.huey.advice;


import com.huey.exception.CouponException;
import com.huey.vo.CommonResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * <h1>全局异常处理</h1>
 * @author huey
 */
@RestControllerAdvice
public class GlobalExceptionAdvice {

    /**
     * <h2>对 CouponException 进行统一处理</h2>
     * */
    @ExceptionHandler(value = CouponException.class)
    public CommonResponse<String> handlerCouponException(
            HttpServletRequest req, CouponException ex
    ) {
        //讲捕捉到的报错信息，以正常的错误信息返回给前端
        CommonResponse<String> response = new CommonResponse<>(
                -1, "business error");
        response.setData(ex.getMessage());
        return response;
    }
}
