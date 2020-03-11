package com.huey.conf;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * @Author: huey
 * @Desc: 消息转换器
 */
@Configuration
public class WebConfiguration implements WebMvcConfigurer {
    /**
     * 消息内容转换成json
     * @param converters
     */
    @Override
    public void configureMessageConverters(
            List<HttpMessageConverter<?>> converters) {
        //HttpMessageConverter 信息转换
        converters.clear();
        converters.add(new MappingJackson2HttpMessageConverter());
        //MappingJackson2HttpMessageConverter  java对象转成json
    }


/*    *//**
     * 消息内容转换配置
     * 配置fastJson返回json转换
     * @param converters
     *//*
    public void configureMessageConvertersF(List<HttpMessageConverter<?>> converters) {
        //调用父类的配置
     //   super.configureMessageConverters(converters);
        //创建fastJson消息转换器
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
        //创建配置类
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        //修改配置返回内容的过滤
        fastJsonConfig.setSerializerFeatures(
                SerializerFeature.DisableCircularReferenceDetect,
                SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteNullStringAsEmpty
        );
        fastConverter.setFastJsonConfig(fastJsonConfig);
        //将fastjson添加到视图消息转换器列表内
        converters.add(fastConverter);
    }*/

}
