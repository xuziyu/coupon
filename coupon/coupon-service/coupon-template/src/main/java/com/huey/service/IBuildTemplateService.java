package com.huey.service;


import com.huey.entity.CouponTemplate;
import com.huey.exception.CouponException;
import com.huey.vo.TemplateRequest;

/**
 * <h1>构建优惠券模板接口定义</h1>
 * @author huey
 */
public interface IBuildTemplateService {

    /**
     * <h2>创建优惠券模板</h2>
     * @param request {@link TemplateRequest} 模板信息请求对象
     * @return {@link CouponTemplate} 优惠券模板实体
     * */
    CouponTemplate buildTemplate(TemplateRequest request)
            throws CouponException;
}
