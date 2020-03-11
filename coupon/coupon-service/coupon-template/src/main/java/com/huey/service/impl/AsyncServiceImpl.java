package com.huey.service.impl;

import com.google.common.base.Stopwatch;
import com.huey.constant.Constant;
import com.huey.dao.CouponTemplateDao;
import com.huey.entity.CouponTemplate;
import com.huey.exception.CouponException;
import com.huey.service.IAsyncService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author: huey
 * @Desc: 异步调用生成优惠券模板
 */

@Service
@Slf4j
public class AsyncServiceImpl implements IAsyncService {

    private final StringRedisTemplate redisTemplate;

    private final CouponTemplateDao couponTemplateDao;

    @Autowired
    public AsyncServiceImpl(StringRedisTemplate redisTemplate, CouponTemplateDao couponTemplateDao) {
        this.redisTemplate = redisTemplate;
        this.couponTemplateDao=couponTemplateDao;
    }

    /**
     * 创建优惠券模板
     * @param template {@link CouponTemplate} 优惠券模板实体
     */
    @Override
    public void asyncConstructCouponByTemplate(CouponTemplate template) {
        Set<String> templateIds=buildCouponCode(template);
        if(CollectionUtils.isEmpty(templateIds)){
            log.info("没有生成优惠码");
        }else {
            String redisKey = String.format("%s%s",
                    Constant.RedisPrefix.COUPON_TEMPLATE, template.getId().toString());
            redisTemplate.opsForList().rightPushAll(redisKey,templateIds);
            template.setAvailable(true);
            couponTemplateDao.save(template);
            //todo 可以通知前端或者 短信 邮件通知 可以使用优惠券 针对公司运营人员
        }
    }


    /**
     * <h2>构造优惠券码</h2>
     * 优惠券码(对应于每一张优惠券, 18位)
     *  前四位: 产品线 + 类型
     *  中间六位: 日期随机(190101)
     *  后八位: 0 ~ 9 随机数构成
     * @param template {@link CouponTemplate} 实体类
     * @return Set<String> 与 template.count 相同个数的优惠券码
     * */
    @SuppressWarnings("all")
    private Set<String> buildCouponCode(CouponTemplate template) {

        //spring 计时器
        Stopwatch watch = Stopwatch.createStarted();

        Set<String> result = new HashSet<>(template.getCount());

        // 前四位
        String prefix4 = template.getProductLine().getCode().toString()
                + template.getCategory().getCode();
        String date = new SimpleDateFormat("yyMMdd")
                .format(template.getCreateTime());

        for (int i = 0; i != template.getCount(); ++i) {
            result.add(prefix4 + buildCouponCodeSuffix14(date));
        }

        while (result.size() < template.getCount()) {
            result.add(prefix4 + buildCouponCodeSuffix14(date));
        }
        //断言
        assert result.size() == template.getCount();

        watch.stop();
        log.info("Build Coupon Code Cost: {}ms",
                watch.elapsed(TimeUnit.MILLISECONDS));

        return result;
    }

    /**
     * <h2>构造优惠券码的后 14 位</h2>
     * @param date 创建优惠券的日期
     * @return 14 位优惠券码
     * */
    private String buildCouponCodeSuffix14(String date) {

        char[] bases = new char[]{'1', '2', '3', '4', '5', '6', '7', '8', '9'};

        // 中间六位
        List<Character> chars = date.chars()
                .mapToObj(e -> (char) e).collect(Collectors.toList());
        //洗牌算法
        Collections.shuffle(chars);

        String mid6 = chars.stream()
                .map(Object::toString).collect(Collectors.joining());

        // 后八位
        String suffix8 = RandomStringUtils.random(1, bases)
                //生成随机数字字符串
                + RandomStringUtils.randomNumeric(7);
        return mid6 + suffix8;
    }

}
