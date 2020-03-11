package com.huey.filter;

import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;

/**
 * @Author: huey
 * @Desc:  pre post route  error
 */
public abstract class AbstractPostZuulFilter extends AbstractZuulFilter {

    /**
     * to classify a filter by type. Standard types in Zuul are "pre" for pre-routing filtering,
     * "route" for routing to an origin, "post" for post-routing filters, "error" for error handling.
     * We also support a "static" type for static responses see  StaticResponseFilter.
     * Any filterType made be created or added and run by calling FilterProcessor.runFilters(type)
     *
     * @return A String representing that type
     *
     * pre 在请求路由前被调用
     * route 请求路由时被调用
     * error 请求路由发生错误被调用
     * post  在 error和route 后调用
     */
    @Override
    public String filterType() {
        return FilterConstants.POST_TYPE;
    }
}
