package cc.wdcloud.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @Author zhangdj
 * @Date 2021/5/13:10:38
 * @Description:
 */
@Configuration
@ComponentScan({"cc.wdcloud.annotation","cc.wdcloud.aop"})
@EnableAspectJAutoProxy
public class AnnotationConfig {
}