package com.huey.executor.impl;

import com.huey.constant.RuleFlag;
import com.huey.executor.AbstractExecutor;
import com.huey.executor.RuleExecutor;
import com.huey.vo.CouponTemplateSDK;
import com.huey.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Author: huey
 * @Desc: 立减
 */
@Slf4j
@Service
public class NowEeductionExecutor extends AbstractExecutor implements RuleExecutor {

    @Override
    public RuleFlag ruleConfig() {
        return RuleFlag.LIJIAN;
    }

    @Override
    public SettlementInfo computeRule(SettlementInfo settlement) {

        double goodsSum = retain2Decimals(goodsCostSum(
                settlement.getGoodsInfos()
        ));
        SettlementInfo probability = processGoodsTypeNotSatisfy(
                settlement, goodsSum
        );
        if (null != probability) {
            log.debug("LiJian Template Is Not Match To GoodsType!");
            return probability;
        }

        // 立减优惠券直接使用, 没有门槛
        CouponTemplateSDK templateSDK = settlement.getCouponAndTemplateInfos()
                .get(0).getTemplate();
        double quota = (double) templateSDK.getRule().getDiscount().getQuota();

        // 计算使用优惠券之后的价格 - 结算
        settlement.setCost(
                retain2Decimals(goodsSum - quota) > minCost() ?
                        retain2Decimals(goodsSum - quota) : minCost()
        );

        log.debug("Use LiJian Coupon Make Goods Cost From {} To {}",
                goodsSum, settlement.getCost());

        return settlement;
    }
}
