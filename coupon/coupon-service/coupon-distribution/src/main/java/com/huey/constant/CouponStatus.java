package com.huey.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * <h1>用户优惠券的状态</h1>
 * @author huey
 */
@Getter
@AllArgsConstructor
public enum CouponStatus {
    /**
     *
     */
    USABLE("可用的", 1),
    USED("已使用的", 2),
    EXPIRED("过期的(未被使用的)", 3);

    /** 优惠券状态描述信息 */
    private String description;

    /** 优惠券状态编码 */
    private Integer code;

    /**
     * <h2>根据 code 获取到 CouponStatus</h2>
     * */
    public static CouponStatus of(Integer code) {

        Objects.requireNonNull(code);
        // 获取 通过code枚举中的value
        //Stream.of(values()) 数组创建安全流
        //findAny() 判断是否是空
        //orElseThrow 当前空就抛出异常
        //values() 当前类  返回元素为指定值的连续有序流 of()方法传参必备
        return Stream.of(values())
                .filter(bean -> bean.code.equals(code))
                .findAny()
                .orElseThrow(
                        () -> new IllegalArgumentException(code + " not exists")
                );
    }
}
