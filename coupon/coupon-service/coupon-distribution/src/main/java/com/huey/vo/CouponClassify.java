package com.huey.vo;


import com.huey.constant.CouponStatus;
import com.huey.constant.PeriodType;
import com.huey.entity.Coupon;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.time.DateUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * <h1>用户优惠券的分类, 根据优惠券状态</h1>
 * @author huey  把所有的优惠券进行分类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponClassify {

    /** 可以使用的 */
    private List<Coupon> usable;

    /** 已使用的 */
    private List<Coupon> used;

    /** 已过期的 */
    private List<Coupon> expired;

    /**
     * <h2>对当前的优惠券进行分类</h2>
     * */
    public static CouponClassify classify(List<Coupon> coupons) {

        List<Coupon> usable = new ArrayList<>(coupons.size());
        List<Coupon> used = new ArrayList<>(coupons.size());
        List<Coupon> expired = new ArrayList<>(coupons.size());

        coupons.forEach(c -> {


            // 判断优惠券是否过期
            boolean isTimeExpire;
            long curTime = System.currentTimeMillis();
            //固定日期
            if (c.getTemplateSDK().getRule().getExpiration().getPeriod().equals(
                    PeriodType.REGULAR.getCode()
            )) {
                isTimeExpire = c.getTemplateSDK().getRule().getExpiration()
                        .getDeadline() <= curTime;
            } else {
                //变动的
                //DateUtils.addDays(a,b) 意思是 a时间加上b的天数 就是要获得的时间
                isTimeExpire = DateUtils.addDays(
                        c.getAssignTime(),
                        c.getTemplateSDK().getRule().getExpiration().getGap()
                ).getTime() <= curTime;
            }

            if (c.getStatus() == CouponStatus.USED) {
                //已经使用
                used.add(c);
            } else if (c.getStatus() == CouponStatus.EXPIRED || isTimeExpire) {
                //过期的
                expired.add(c);
            } else {
                // 未使用   如果进来的优惠券状态 是未使用，但是日期过期，会放在已过期里面
                usable.add(c);
            }
        });

        return new CouponClassify(usable, used, expired);
    }
}
