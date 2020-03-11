package com.huey.controller;

import com.alibaba.fastjson.JSON;

import com.huey.constant.CouponCategory;
import com.huey.constant.GoodsType;
import com.huey.exception.CouponException;
import com.huey.executor.ExecuteManager;
import com.huey.vo.CouponTemplateSDK;
import com.huey.vo.GoodsInfo;
import com.huey.vo.SettlementInfo;
import com.huey.vo.TemplateRule;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collections;

/**
 * <h1>结算服务 Controller</h1>
 * @author  huey
 */
@Api(value = "优惠券结算模块")
@Slf4j
@RestController
public class SettlementController {

    /** 结算规则执行管理器 */
    private final ExecuteManager executeManager;

    @Autowired
    public SettlementController(ExecuteManager executeManager) {
        this.executeManager = executeManager;
    }

    /**
     * <h2>优惠券结算</h2>
     * 127.0.0.1:7003/coupon-settlement/settlement/compute
     * 127.0.0.1:9000/coupon-settlement/settlement/compute
     * */
    @PostMapping("/settlement/compute")
    public SettlementInfo computeRule(@RequestBody SettlementInfo settlement)
            throws CouponException {
        log.info("settlement: {}", JSON.toJSONString(settlement));
        return executeManager.computeRule(settlement);
    }

    /**
     * 优惠券结算 测试
     * @return
     * @throws CouponException
     */
    @GetMapping("/settlement/compute/test")
    public SettlementInfo computeRuleTest()
            throws CouponException {
        SettlementInfo info = new SettlementInfo();
        info.setUserId(10001L);
        info.setEmploy(false);
        info.setCost(0.0);

        GoodsInfo goodsInfo01 = new GoodsInfo();
        goodsInfo01.setCount(2);
        goodsInfo01.setPrice(10.88);
        goodsInfo01.setType(GoodsType.WENYU.getCode());

        GoodsInfo goodsInfo02 = new GoodsInfo();
        goodsInfo02.setCount(10);
        goodsInfo02.setPrice(20.88);
        goodsInfo02.setType(GoodsType.WENYU.getCode());

        info.setGoodsInfos(Arrays.asList(goodsInfo01, goodsInfo02));

        SettlementInfo.CouponAndTemplateInfo ctInfo =
                new SettlementInfo.CouponAndTemplateInfo();
        ctInfo.setId(1);

        CouponTemplateSDK templateSDK = new CouponTemplateSDK();
        templateSDK.setId(2);
        templateSDK.setCategory(CouponCategory.ZHEKOU.getCode());
        templateSDK.setKey("100220190712");

        // 设置 TemplateRule
        TemplateRule rule = new TemplateRule();
        rule.setDiscount(new TemplateRule.Discount(85, 1));
        rule.setUsage(new TemplateRule.Usage("安徽省", "桐城市",
                JSON.toJSONString(Arrays.asList(
                        GoodsType.SHENGXIAN.getCode(),
                        GoodsType.JIAJU.getCode()
                ))));

        templateSDK.setRule(rule);
        ctInfo.setTemplate(templateSDK);
        info.setCouponAndTemplateInfos(Collections.singletonList(ctInfo));
        log.info("settlement: {}", JSON.toJSONString(info));
        return executeManager.computeRule(info);
    }

}
