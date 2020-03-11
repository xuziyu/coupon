package com.huey.service.impl;

import com.alibaba.fastjson.JSON;
import com.huey.constant.Constant;
import com.huey.constant.CouponStatus;
import com.huey.entity.Coupon;
import com.huey.exception.CouponException;
import com.huey.service.IRedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.apache.commons.collections4.CollectionUtils;


import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author: huey
 * @Desc:
 */
@Slf4j
@Service
public class RedisServiceImpl implements IRedisService {


    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * <h2>根据 userId 和状态找到缓存的优惠券列表数据</h2>
     * @param userId 用户 id
     * @param status 优惠券状态 {@link CouponStatus}
     * @return {@link Coupon}s, 注意, 可能会返回 null, 代表从没有过记录
     */
    @Override
    public List<Coupon> getCachedCoupons(Long userId, Integer status) {
        log.info("Get Coupons From Cache: {}, {}", userId, status);
        String redisKey = status2RedisKey(status, userId);

        List<String> couponStrs = redisTemplate.opsForHash().values(redisKey)
                .stream()
                .map(o -> Objects.toString(o, null))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(couponStrs)) {
            saveEmptyCouponListToCache(userId,
                    Collections.singletonList(status));
            return Collections.emptyList();
        }

        return couponStrs.stream()
                .map(cs -> JSON.parseObject(cs, Coupon.class))
                .collect(Collectors.toList());
    }

    /**
     * 空优惠券 把它放入redis 避免 Null用户太多，直接请求数据库
     * 避免缓存穿透
     * @param userId 用户 id
     * @param status 优惠券状态列表
     */
    @Override
    public void saveEmptyCouponListToCache(Long userId, List<Integer> status) {
        //存空优惠券状态 制造空优惠券 id等于-1 就是无效
        Map<String,String> map=new HashMap<>(16);
        map.put("-1",JSON.toJSONString(Coupon.invalidCoupon()));
      // 使用 SessionCallback 把数据命令放入到 Redis 的 pipeline
      // SessionCallback 作用: 让RedisTemplate进行回调，通过他们可以在同一条连接中执行多个redis命令
        //redisTemplate直接调用opfor..来操作redis数据库，每执行一条命令是要重新拿一个连接，因此很耗资源，
        // 让一个连接直接执行多条语句的方法就是使用SessionCallback，同样作用的还有RedisCallback，但不常用
        SessionCallback sessionCallback=new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                //userId 这样的名称下面的所有状态分开来所有请求命令 放入SessionCallback
                status.stream()
                        .map(e -> status2RedisKey(e, userId))
                        .forEach(key -> redisOperations.opsForHash().putAll(key, map));
                return null;
            }
        };
        //pipeline就是把一组命令进行打包，然后一次性通过网络发送到Redis。同时将执行的结果批量的返回回来
        redisTemplate.executePipelined(sessionCallback);
    }
    /**
     * <h2>尝试从 Cache 中获取一个优惠券码</h2>
     * @param templateId 优惠券模板主键
     * @return 优惠券码
     */
    @Override
    public String tryToAcquireCouponCodeFromCache(Integer templateId) {
        String key=String.format("%s%s",Constant.RedisPrefix.COUPON_TEMPLATE,templateId);
        String couponNo=redisTemplate.opsForList().rightPop(key);
        return couponNo;
    }

    /**
     * <h2>将优惠券保存到 Cache 中</h2>
     * @param userId  用户 id
     * @param coupons {@link Coupon}s
     * @param status  优惠券状态
     * @return 保存成功的个数
     */
    @Override
    public Integer addCouponToCache(Long userId, List<Coupon> coupons, Integer status) throws CouponException {
        Integer result = -1;
        CouponStatus couponStatus = CouponStatus.of(status);

        switch (couponStatus) {
            case USABLE:
                result = addCouponToCacheForUsable(userId, coupons);
                break;
            case USED:
                result = addCouponToCacheForUsed(userId, coupons);
                break;
            case EXPIRED:
                result = addCouponToCacheForExpired(userId, coupons);
                break;
             default:
        }

        return result;
    }
    /**
     * <h2>新增加优惠券到 Cache 中</h2>
     * */
    private Integer addCouponToCacheForUsable(Long userId, List<Coupon> coupons) {

        // 如果 status 是 USABLE, 代表是新增加的优惠券
        // 只会影响一个 Cache: USER_COUPON_USABLE
        log.debug("Add Coupon To Cache For Usable.");

        Map<String, String> needCachedObject = new HashMap<>();
        coupons.forEach(c ->
                needCachedObject.put(
                        c.getId().toString(),
                        JSON.toJSONString(c)
                ));

        String redisKey = status2RedisKey(
                CouponStatus.USABLE.getCode(), userId);
        redisTemplate.opsForHash().putAll(redisKey, needCachedObject);
        log.info("Add {} Coupons To Cache: {}, {}",
                needCachedObject.size(), userId, redisKey);

        redisTemplate.expire(
                redisKey,
                getRandomExpirationTime(1, 2),
                TimeUnit.SECONDS
        );

        return needCachedObject.size();
    }


    /**
     * <h2>将已使用的优惠券加入到 Cache 中</h2>
     * */
    @SuppressWarnings("all")
    private Integer addCouponToCacheForUsed(Long userId, List<Coupon> coupons)
            throws CouponException {

        // 如果 status 是 USED, 代表用户操作是使用当前的优惠券, 影响到两个 Cache
        // USABLE, USED

        log.debug("Add Coupon To Cache For Used.");

        Map<String, String> needCachedForUsed = new HashMap<>(coupons.size());

        String redisKeyForUsable = status2RedisKey(
                CouponStatus.USABLE.getCode(), userId
        );
        String redisKeyForUsed = status2RedisKey(
                CouponStatus.USED.getCode(), userId
        );

        // 获取当前用户可用的优惠券
        List<Coupon> curUsableCoupons = getCachedCoupons(
                userId, CouponStatus.USABLE.getCode()
        );
        // 当前可用的优惠券个数一定是大于1的
        // 已使用 就是未使用的变成已使用
        assert curUsableCoupons.size() > coupons.size();

        coupons.forEach(c -> needCachedForUsed.put(
                c.getId().toString(),
                JSON.toJSONString(c)
        ));

        // 校验当前的优惠券参数是否与 Cached 中的匹配
        List<Integer> curUsableIds = curUsableCoupons.stream()
                .map(Coupon::getId).collect(Collectors.toList());
        List<Integer> paramIds = coupons.stream()
                .map(Coupon::getId).collect(Collectors.toList());
        //a是否b集合子集，a集合大小<=b集合大小  isSubCollection
        // 返回参数是boolean
        if (!CollectionUtils.isSubCollection(paramIds, curUsableIds)) {
            log.error("CurCoupons Is Not Equal ToCache: {}, {}, {}",
                    userId, JSON.toJSONString(curUsableIds),
                    JSON.toJSONString(paramIds));
            throw new CouponException("CurCoupons Is Not Equal To Cache!");
        }
        //已经使用的优惠券id 进行转换 redis多操作同时进行
        List<String> needCleanKey = paramIds.stream()
                .map(i -> i.toString()).collect(Collectors.toList());
        SessionCallback<Objects> sessionCallback = new SessionCallback<Objects>() {
            @Override
            public Objects execute(RedisOperations operations) throws DataAccessException {

                // 1. 已使用的优惠券 Cache 缓存添加
                operations.opsForHash().putAll(
                        redisKeyForUsed, needCachedForUsed
                );
                // 2. 可用的优惠券 Cache 需要清理
                operations.opsForHash().delete(
                        redisKeyForUsable, needCleanKey.toArray()
                );
                // 3. 重置过期时间
                operations.expire(
                        redisKeyForUsable,
                        getRandomExpirationTime(1, 2),
                        TimeUnit.SECONDS
                );
                operations.expire(
                        redisKeyForUsed,
                        getRandomExpirationTime(1, 2),
                        TimeUnit.SECONDS
                );

                return null;
            }
        };

        log.info("Pipeline Exe Result: {}",
                JSON.toJSONString(
                        redisTemplate.executePipelined(sessionCallback)));

        return coupons.size();
    }

    /**
     * <h2>将过期优惠券加入到 Cache 中</h2>
     * */
    @SuppressWarnings("all")
    private Integer addCouponToCacheForExpired(Long userId, List<Coupon> coupons)
            throws CouponException {

        // status 是 EXPIRED, 代表是已有的优惠券过期了, 影响到两个 Cache
        // USABLE, EXPIRED

        log.debug("Add Coupon To Cache For Expired.");

        // 最终需要保存的 Cache
        Map<String, String> needCachedForExpired = new HashMap<>(coupons.size());

        String redisKeyForUsable = status2RedisKey(
                CouponStatus.USABLE.getCode(), userId
        );
        String redisKeyForExpired = status2RedisKey(
                CouponStatus.EXPIRED.getCode(), userId
        );

        List<Coupon> curUsableCoupons = getCachedCoupons(
                userId, CouponStatus.USABLE.getCode()
        );

        // 当前可用的优惠券个数一定是大于1的
        assert curUsableCoupons.size() > coupons.size();

        coupons.forEach(c -> needCachedForExpired.put(
                c.getId().toString(),
                JSON.toJSONString(c)
        ));

        // 校验当前的优惠券参数是否与 Cached 中的匹配
        List<Integer> curUsableIds = curUsableCoupons.stream()
                .map(Coupon::getId).collect(Collectors.toList());
        List<Integer> paramIds = coupons.stream()
                .map(Coupon::getId).collect(Collectors.toList());
        if (!CollectionUtils.isSubCollection(paramIds, curUsableIds)) {
            log.error("CurCoupons Is Not Equal To Cache: {}, {}, {}",
                    userId, JSON.toJSONString(curUsableIds),
                    JSON.toJSONString(paramIds));
            throw new CouponException("CurCoupon Is Not Equal To Cache.");
        }

        List<String> needCleanKey = paramIds.stream()
                .map(i -> i.toString()).collect(Collectors.toList());

        SessionCallback<Objects> sessionCallback = new SessionCallback<Objects>() {
            @Override
            public Objects execute(RedisOperations operations) throws DataAccessException {

                // 1. 已过期的优惠券 Cache 缓存
                operations.opsForHash().putAll(
                        redisKeyForExpired, needCachedForExpired
                );
                // 2. 可用的优惠券 Cache 需要清理
                operations.opsForHash().delete(
                        redisKeyForUsable, needCleanKey.toArray()
                );
                // 3. 重置过期时间
                operations.expire(
                        redisKeyForUsable,
                        getRandomExpirationTime(1, 2),
                        TimeUnit.SECONDS
                );
                operations.expire(
                        redisKeyForExpired,
                        getRandomExpirationTime(1, 2),
                        TimeUnit.SECONDS
                );

                return null;
            }
        };

        log.info("Pipeline Exe Result: {}",
                JSON.toJSONString(
                        redisTemplate.executePipelined(sessionCallback)
                ));

        return coupons.size();
    }
    /**
     * <h2>根据 status 获取到对应的 Redis Key</h2>
     * */
    private String status2RedisKey(Integer status, Long userId) {

        String redisKey = null;
        CouponStatus couponStatus = CouponStatus.of(status);

        switch (couponStatus) {
            case USABLE:
                redisKey = String.format("%s%s",
                        Constant.RedisPrefix.USER_COUPON_USABLE, userId);
                break;
            case USED:
                redisKey = String.format("%s%s",
                        Constant.RedisPrefix.USER_COUPON_USED, userId);
                break;
            case EXPIRED:
                //%s%s 类似于 + 把两个字符串起来
                redisKey = String.format("%s%s",
                        Constant.RedisPrefix.USER_COUPON_EXPIRED, userId);
                break;
             default:
        }

        return redisKey;
    }



    /**
     * <h2>获取一个随机的过期时间</h2>
     * 缓存雪崩: key 在同一时间失效
     * @param min 最小的小时数
     * @param max 最大的小时数
     * @return 返回 [min, max] 之间的随机秒数
     * */
    private Long getRandomExpirationTime(Integer min, Integer max) {

        return RandomUtils.nextLong(
                min * 60 * 60,
                max * 60 * 60
        );
    }

}
