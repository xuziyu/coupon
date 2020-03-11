package com.huey.executor.impl;

import com.huey.constant.RuleFlag;
import com.huey.executor.AbstractExecutor;
import com.huey.executor.RuleExecutor;
import com.huey.vo.CouponTemplateSDK;
import com.huey.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @Author: huey
 * @Desc: 折扣 这里没有条件限制 比如满多少 之类的 只要有这类优惠券并且满足于 目前规则商品
 */
@Service
@Slf4j
public class DiscountExecutor extends AbstractExecutor implements RuleExecutor {

    @Override
    public RuleFlag ruleConfig() {
        return RuleFlag.ZHEKOU;
    }

    @Override
    public SettlementInfo computeRule(SettlementInfo settlement) {
        // 先拿到商品总价格
        double goodsSum=retain2Decimals(goodsCostSum(settlement.getGoodsInfos()));
        // 匹配当前商品是否 能使用当前优惠券
        SettlementInfo probability = processGoodsTypeNotSatisfy(
                settlement, goodsSum
        );
        if (null != probability) {
            log.debug("ManJian Template Is Not Match To GoodsType!");
            return probability;
        }
        CouponTemplateSDK templateSDK = settlement.getCouponAndTemplateInfos()
                .get(0).getTemplate();
        double quota= templateSDK.getRule().getDiscount().getQuota();
        settlement.setCost(retain2Decimals(goodsSum * (quota * 1.0 / 100))>minCost()
                ?retain2Decimals(goodsSum * (quota * 1.0 /100)):minCost() );
        return settlement;
    }
}
