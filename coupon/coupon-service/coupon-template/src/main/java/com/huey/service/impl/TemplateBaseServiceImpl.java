package com.huey.service.impl;

import com.huey.dao.CouponTemplateDao;
import com.huey.entity.CouponTemplate;
import com.huey.exception.CouponException;
import com.huey.service.ITemplateBaseService;
import com.huey.vo.CouponTemplateSDK;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: huey
 * @Desc: 优惠券基础接口查询
 */
@Service
public class TemplateBaseServiceImpl implements ITemplateBaseService {

    @Autowired
    private CouponTemplateDao couponTemplateDao;

    /**
     * 根据优惠券模板 id 获取优惠券模板信息
     * @param id 模板 id
     * @return
     * @throws CouponException
     */
    @Override
    public CouponTemplate buildTemplateInfo(Integer id) throws CouponException {
        Optional<CouponTemplate> template = couponTemplateDao.findById(id);
        if (!template.isPresent()) {
            throw new CouponException("Template Is Not Exist: " + id);
        }
        return template.get();
    }

    /**
     * 查询所有的优惠券模板
     * @return
     */
    @Override
    public List<CouponTemplateSDK> findAllUsableTemplate() {
        List<CouponTemplate> templates =
                couponTemplateDao.findAllByAvailableAndExpired(
                        true, false);

        return templates.stream()
                .map(this::template2TemplateSDK).collect(Collectors.toList());
    }

    /**
     * 获取模板 ids 到 CouponTemplateSDK 的映射
     * @param ids 模板 ids
     * @return
     */
    @Override
    public Map<Integer, CouponTemplateSDK> findIds2TemplateSDK(Collection<Integer> ids) {
        List<CouponTemplate> templates = couponTemplateDao.findAllById(ids);

        return templates.stream().map(this::template2TemplateSDK)
                .collect(Collectors.toMap(
                        CouponTemplateSDK::getId, Function.identity()
                ));
    }


    /**
     * <h2>将 CouponTemplate 转换为 CouponTemplateSDK</h2>
     * */
    private CouponTemplateSDK template2TemplateSDK(CouponTemplate template) {

        return new CouponTemplateSDK(
                template.getId(),
                template.getName(),
                template.getLogo(),
                template.getDesc(),
                template.getCategory().getCode(),
                template.getProductLine().getCode(),
                // 并不是拼装好的 Template Key
                template.getKey(),
                template.getTarget().getCode(),
                template.getRule()
        );
    }
}
