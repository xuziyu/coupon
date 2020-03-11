package com.huey.controller;



import com.huey.entity.CouponTemplate;
import com.huey.exception.CouponException;
import com.huey.service.IBuildTemplateService;
import com.huey.service.ITemplateBaseService;
import com.huey.vo.CommonResponse;
import com.huey.vo.CouponTemplateSDK;
import com.huey.vo.TemplateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @Author: huey
 * @Desc:
 */

@RestController
@RequestMapping("/coupon")
public class CouponController {

    @Autowired
    private IBuildTemplateService iBuildTemplateService;

    @Autowired
    private ITemplateBaseService iTemplateBaseService;

    /**
     * 调用模板 生成优惠券
     * @param request
     * @return
     * @throws CouponException
     */
    @PostMapping("/create/coupon")
    public CommonResponse CreateCouponTemplate(@RequestBody TemplateRequest request) throws CouponException {
        CouponTemplate couponTemplate= iBuildTemplateService.buildTemplate(request);
        CommonResponse commonResponse=new CommonResponse();
        commonResponse.setMessage("success");
        commonResponse.setCode(200);
        commonResponse.setData(couponTemplate);
        return commonResponse;
    }

    /**
     * PathVariable 注意 在 跨服务通信的时候 这个地方需要加参数
     * 根据优惠券模板 id 获取优惠券模板信息
     * @param id
     * @return
     */
    @GetMapping("/info/{id}")
    public CommonResponse buildTemplateInfo(@PathVariable(value = "id") Integer id) throws CouponException {
        CouponTemplate couponTemplate= iTemplateBaseService.buildTemplateInfo(id);
        CommonResponse commonResponse=new CommonResponse();
        commonResponse.setMessage("success");
        commonResponse.setCode(200);
        commonResponse.setData(couponTemplate);
        return commonResponse;
    }
    /**
     * 查询所有的优惠券模板
     * @return
     */
   @GetMapping("/find/all/template")
   public CommonResponse findAllUsableTemplate(){
       List<CouponTemplateSDK> couponTemplates= iTemplateBaseService.findAllUsableTemplate();
       CommonResponse commonResponse=new CommonResponse();
       commonResponse.setMessage("success");
       commonResponse.setCode(200);
       commonResponse.setData(couponTemplates);
       return commonResponse;
   }

    /**
     * 获取模板 ids 到 CouponTemplateSDK 的映射
     * @RequestParam("ids") Collection<Integer> ids
     * @return
     */
    @PostMapping ("/template/sdk/infos")
   public Map<Integer, CouponTemplateSDK> findIds2TemplateSDK(Collection<Integer> ids){
       Map<Integer, CouponTemplateSDK> map=  iTemplateBaseService.findIds2TemplateSDK(ids);
        return map;
   }




}
