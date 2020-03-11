package com.huey.service.impl;

import com.huey.dao.CouponTemplateDao;
import com.huey.entity.CouponTemplate;
import com.huey.exception.CouponException;
import com.huey.service.IAsyncService;
import com.huey.service.IBuildTemplateService;
import com.huey.vo.TemplateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: huey
 * @Desc: 调用模板 去生成优惠券
 */
@Service
public class BuildTemplateServiceImpl implements IBuildTemplateService {


    /** 异步服务 */
    private final IAsyncService asyncService;

    /** CouponTemplate Dao */
    private final CouponTemplateDao templateDao;

    @Autowired
    public BuildTemplateServiceImpl(IAsyncService asyncService,
                                    CouponTemplateDao templateDao) {
        this.asyncService = asyncService;
        this.templateDao = templateDao;
    }

    @Override
    public CouponTemplate buildTemplate(TemplateRequest request) throws CouponException {
        // 第一步 拿到 request 转换成我需要给到一步调用的模块
        if(!request.validate()){
            throw new CouponException("传入参数有误");
        }
        // 判断是否存在相同的优惠券
        if(null!=templateDao.findByName(request.getName())){
            throw new CouponException("存在相同的优惠券");
        }
        CouponTemplate couponTemplate=requestToTemplate(request);
        templateDao.save(couponTemplate);
        //异步调用
        asyncService.asyncConstructCouponByTemplate(couponTemplate);
        return couponTemplate;
    }

    /**
     * <h2>将 TemplateRequest 转换为 CouponTemplate</h2>
     * */
    private CouponTemplate requestToTemplate(TemplateRequest request) {

        return new CouponTemplate(
                request.getName(),
                request.getLogo(),
                request.getDesc(),
                request.getCategory(),
                request.getProductLine(),
                request.getCount(),
                request.getUserId(),
                request.getTarget(),
                request.getRule()
        );
    }
}
