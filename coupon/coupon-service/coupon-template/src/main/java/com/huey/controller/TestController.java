package com.huey.controller;

import com.alibaba.fastjson.JSON;
import com.huey.constant.CouponCategory;
import com.huey.constant.DistributeTarget;
import com.huey.constant.PeriodType;
import com.huey.constant.ProductLine;
import com.huey.exception.CouponException;
import com.huey.service.IBuildTemplateService;
import com.huey.vo.TemplateRequest;
import com.huey.vo.TemplateRule;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

/**
 * @Author: huey
 * @Desc:
 */
@RestController
@RequestMapping("/coupon")
public class TestController {

    private final IBuildTemplateService iBuildTemplateService;

    @Autowired
    public TestController(IBuildTemplateService iBuildTemplateService) {
        this.iBuildTemplateService = iBuildTemplateService;
    }


    @GetMapping("/test")
    public void  test() throws CouponException{
        TemplateRequest request =new TemplateRequest();
        request.setName("优惠券模板-" + System.currentTimeMillis());
        request.setLogo("http://www.baidu.com");
        request.setDesc("这是一张优惠券模板");
        request.setCategory(CouponCategory.MANJIAN.getCode());
        request.setProductLine(ProductLine.DAMAO.getCode());
        request.setCount(10000);
        request.setUserId(10001L);
        request.setTarget(DistributeTarget.SINGLE.getCode());
        TemplateRule rule = new TemplateRule();
        rule.setExpiration(new TemplateRule.Expiration(
                PeriodType.SHIFT.getCode(),
                1, DateUtils.addDays(new Date(), 60).getTime()
        ));
        rule.setDiscount(new TemplateRule.Discount(5, 1));
        rule.setLimitation(1);
        rule.setUsage(new TemplateRule.Usage(
                "安徽省", "桐城市",
                JSON.toJSONString(Arrays.asList("文娱", "家居"))
        ));
        rule.setWeight(JSON.toJSONString(Collections.EMPTY_LIST));

        request.setRule(rule);
        iBuildTemplateService.buildTemplate(request);
    }



}
