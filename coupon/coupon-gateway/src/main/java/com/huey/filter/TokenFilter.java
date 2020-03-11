package com.huey.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;


/**
 * @Author: huey
 * @Desc: <h1>校验请求中传递的 Token</h1>
 */
@Component
@Slf4j
public class TokenFilter extends AbstractPreZuulFilter {


    @Override
    protected Object cRun() {
        HttpServletRequest request = context.getRequest();
        log.info(String.format("%s request to %s",
                request.getMethod(), request.getRequestURL().toString()));
        //请求路径携带参数
        Object token = request.getParameter("token");
        log.info("监听到token:"+token);
         //String abc=request.getHeader("token");
        //这边可以根据你的业务需求 进行改动 是否正确的值
        //request 还可以多加个携带参数
        if (null == token) {
            log.error("error: token is empty");
            return fail(401, "error: token is empty");
        }
        return success();
    }

    /**
     * filterOrder() must also be defined for a filter. Filters may have the same  filterOrder if precedence is not
     * important for a filter. filterOrders do not need to be sequential.
     *
     * @return the int order of a filter
     */

    /**
     * 优先级
     * @return
     */
    @Override
    public int filterOrder() {
        return 1;
    }
}
