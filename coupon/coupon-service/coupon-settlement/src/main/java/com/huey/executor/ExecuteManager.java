package com.huey.executor;

import com.huey.constant.CouponCategory;
import com.huey.constant.RuleFlag;
import com.huey.exception.CouponException;
import com.huey.executor.impl.FullReductionExecutor;
import com.huey.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * <h1>优惠券结算规则执行管理器</h1>
 * 即根据用户的请求(SettlementInfo)找到对应的 Executor, 去做结算
 * BeanPostProcessor: Bean 后置处理器
 * @author  huey
 */
@Slf4j
@Component
@SuppressWarnings("all")
public class ExecuteManager implements BeanPostProcessor {

    /** 规则执行器映射 */
 /*   private static Map<RuleFlag, RuleExecutor> executorIndex =
            new HashMap<>(RuleFlag.values().length);*/
    private static ConcurrentHashMap<RuleFlag, RuleExecutor> executorIndex =
            new ConcurrentHashMap<>(RuleFlag.values().length);
/*    private static ConcurrentMap<RuleFlag, RuleExecutor> executorIndex =
            new ConcurrentHashMap<>(RuleFlag.values().length);*/

/*    public static void getLocalThreadInfo(){
        ThreadLocal<RuleExecutor> threadLocal=new ThreadLocal<>();
        ThreadLocal<RuleExecutor> threadLocal1=new ThreadLocal<RuleExecutor>(){
            @Override
            protected RuleExecutor initialValue() {
                return super.initialValue();
            }
        };
    }*/

    @Autowired
    private FullReductionExecutor fullReductionExecutor;
    /**
     * <h2>优惠券结算规则计算入口</h2>
     * 注意: 一定要保证传递进来的优惠券个数 >= 1
     * */
    public SettlementInfo computeRule(SettlementInfo settlement)
            throws CouponException {

        SettlementInfo result = null;

        // 单类优惠券
        if (settlement.getCouponAndTemplateInfos().size() == 1) {

            // 获取优惠券的类别
            CouponCategory category = CouponCategory.of(
                    settlement.getCouponAndTemplateInfos().get(0)
                            .getTemplate().getCategory()
            );

            switch (category) {
                case MANJIAN:
                    result = executorIndex.get(RuleFlag.MANJIAN).computeRule(settlement);
                    break;
                case ZHEKOU:
                  //  fullReductionExecutor.computeRule(settlement);
                    result = executorIndex
                            .get(RuleFlag.ZHEKOU)
                            .computeRule(settlement);
                    break;
                case LIJIAN:
                    result = executorIndex
                            .get(RuleFlag.LIJIAN)
                            .computeRule(settlement);
                    break;
            }
        } else {

            // 多类优惠券
            List<CouponCategory> categories = new ArrayList<>(
                    settlement.getCouponAndTemplateInfos().size()
            );

            settlement.getCouponAndTemplateInfos().forEach(ct ->
                    categories.add(CouponCategory.of(
                            ct.getTemplate().getCategory()
                    )));
            if (categories.size() != 2) {
                throw new CouponException("Not Support For More " +
                        "Template Category");
            } else {
                if (categories.contains(CouponCategory.MANJIAN)
                        && categories.contains(CouponCategory.ZHEKOU)) {
                    // executorIndex.get(RuleFlag.MANJIAN_ZHEKOU) 类是对象名
                    result = executorIndex.get(RuleFlag.MANJIAN_ZHEKOU)
                            .computeRule(settlement);
                } else {
                    throw new CouponException("Not Support For Other " +
                            "Template Category");
                }
            }
        }

        return result;
    }

    /**
     * <h2>在 bean 初始化之前去执行(before)</h2>
     * */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName)
            throws BeansException {

        if (!(bean instanceof RuleExecutor)) {
            return bean;
        }
        RuleExecutor executor = (RuleExecutor) bean;
        RuleFlag ruleFlag = executor.ruleConfig();
        if (executorIndex.containsKey(ruleFlag)) {
            throw new IllegalStateException("There is already an executor" +
                    "for rule flag: " + ruleFlag);
        }
        log.info("Load executor {} for rule flag {}.",
                executor.getClass(), ruleFlag);
        executorIndex.put(ruleFlag, executor);
        return null;
    }

    /**
     * <h2>在 bean 初始化之后去执行(after)</h2>
     * */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
            throws BeansException {
        return bean;
    }
    
}
