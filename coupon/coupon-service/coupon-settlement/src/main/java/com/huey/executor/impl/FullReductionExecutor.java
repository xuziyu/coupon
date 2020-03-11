package com.huey.executor.impl;

import com.huey.constant.RuleFlag;
import com.huey.executor.AbstractExecutor;
import com.huey.executor.RuleExecutor;
import com.huey.vo.CouponTemplateSDK;
import com.huey.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * @Author: huey
 * @Desc: 满减优惠券实现
 */
@Slf4j
@Service
public class FullReductionExecutor extends AbstractExecutor implements RuleExecutor {

    /**
     * 规则类型标记
     * @return
     */
    @Override
    public RuleFlag ruleConfig() {
        return RuleFlag.MANJIAN;
    }

    /**
     * 优惠券规则的计算
     * @param settlement {@link SettlementInfo} 包含了选择的优惠券
     * @return
     */
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
        // 判断满减是否符合折扣标准   因为只对应一种优惠
        CouponTemplateSDK templateSDK = settlement.getCouponAndTemplateInfos()
                .get(0).getTemplate();
        // 满多少
        double base = (double) templateSDK.getRule().getDiscount().getBase();
        //减多少
        double quota = (double) templateSDK.getRule().getDiscount().getQuota();

        // 如果不符合标准, 则直接返回商品总价
        if (goodsSum < base) {
            log.debug("Current Goods Cost Sum < ManJian Coupon Base!");
            settlement.setCost(goodsSum);
            settlement.setCouponAndTemplateInfos(Collections.emptyList());
            return settlement;
        }
        //商品总价格判断减去 满减 是否小于 最小金额
        // 计算使用优惠券之后的价格 - 结算
        settlement.setCost(retain2Decimals(
                (goodsSum - quota) > minCost() ? (goodsSum - quota) : minCost()
        ));
        log.debug("Use ManJian Coupon Make Goods Cost From {} To {}",
                goodsSum, settlement.getCost());
        //  把商品价格放入SettlementInfo 返回

        return settlement;
    }
}
