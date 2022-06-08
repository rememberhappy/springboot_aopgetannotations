package cc.zdj.aop;

import cc.wdcloud.common.log.LogInfoHolder;
import cc.wdcloud.common.log.ULogger;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * feign 接口调用时的日志读写
 *
 * @Author zhangdj
 * @Date 2021/6/7:14:29
 * @Description
 */
@Aspect
@Component
@ConditionalOnClass({FeignClient.class})
public class FeignLogAspect {

    @Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public void requestPointcut() {
    }

    @Pointcut("@annotation(org.springframework.web.bind.annotation.GetMapping)")
    public void getPointcut() {
    }

    @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void postPointcut() {
    }

    /**
     * 通知类型：环绕通知
     *
     * @param joinPoint
     * @return java.lang.Object
     * @Throws Throwable
     * @Author zhangdj
     * @date 2021/6/7 14:36
     */
    @Around("requestPointcut()")
    public Object doAroundOfRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        return this.doAroundIn(joinPoint);
    }

    @Around("getPointcut()")
    public Object doAroundOfGet(ProceedingJoinPoint joinPoint) throws Throwable {
        return this.doAroundIn(joinPoint);
    }

    @Around("postPointcut()")
    public Object doAroundOfPost(ProceedingJoinPoint joinPoint) throws Throwable {
        return this.doAroundIn(joinPoint);
    }

    private Object doAroundIn(ProceedingJoinPoint joinPoint) throws Throwable {
        // 目标方法所属类
        Class clazz = joinPoint.getSignature().getDeclaringType();
        // 获取指定的声明的注释类型的声明的注释
        FeignClient feignClient = (FeignClient) clazz.getDeclaredAnnotation(FeignClient.class);
        // 没有此注释，则直接返回，不执行日志的打印
        if (null == feignClient) {
            // 让目标方法执行。
            return joinPoint.proceed();
        } else {
            RequestMapping requestMapping = (RequestMapping) ((MethodSignature) ((MethodSignature) joinPoint.getSignature())).getMethod().getAnnotation(RequestMapping.class);
            if (null == requestMapping) {
                return joinPoint.proceed();
            } else {
                String host = feignClient.url();
                String[] url = requestMapping.value();
                RequestMethod[] method = requestMapping.method();
                StringBuilder builder = new StringBuilder();
                builder.append("[seq:").append(LogInfoHolder.getAndIncrementSeq()).append("][logType: feign-request]").append("[host:").append(host).append("][url:").append(url[0]).append("][params:");
                // 获取到方法的所有参数名称的字符串数组
                String[] params = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
                // 获取到所有的参数值的数组
                Object[] args = joinPoint.getArgs();
                Map<String, Object> paramInfo = new HashMap();
                if (ArrayUtils.isNotEmpty(params)) {
                    for (int i = 0; i < params.length; ++i) {
                        paramInfo.put(params[i], args[i]);
                    }
                    builder.append(JSON.toJSONString(paramInfo));
                } else {
                    builder.append(JSON.toJSONString(args));
                }

                builder.append("]");
                ULogger.info(builder.toString(), new Object[0]);
                // 让目标方法执行。获取目标方法执行后的返回值
                Object res = joinPoint.proceed();
                builder = new StringBuilder();
                builder.append("[seq:").append(LogInfoHolder.getSeq()).append("][logType: feign-response]").append("[host:").append(host).append("][url:").append(url[0]).append("][result:").append(JSON.toJSONString(res)).append("]");
                ULogger.info(builder.toString(), new Object[0]);
                return res;
            }
        }
    }

    /**
     * 通知类型：异常后通知
     *
     * @param joinPoint
     * @param ex
     * @return void
     * @Throws
     * @Author zhangdj
     * @date 2021/6/7 14:50
     */
    @AfterThrowing(
            value = "requestPointcut()",
            throwing = "ex"
    )
    public void afterThrowingOfRequest(JoinPoint joinPoint, Exception ex) {
        this.doAfterThrowingIn(joinPoint, ex);
    }

    @AfterThrowing(
            value = "getPointcut()",
            throwing = "ex"
    )
    public void afterThrowingOfGet(JoinPoint joinPoint, Exception ex) {
        this.doAfterThrowingIn(joinPoint, ex);
    }

    @AfterThrowing(
            value = "postPointcut()",
            throwing = "ex"
    )
    public void afterThrowingOfPost(JoinPoint joinPoint, Exception ex) {
        this.doAfterThrowingIn(joinPoint, ex);
    }

    private void doAfterThrowingIn(JoinPoint joinPoint, Exception ex) {
        // 目标方法所属类
        Class clazz = joinPoint.getSignature().getDeclaringType();
        FeignClient feignClient = (FeignClient) clazz.getDeclaredAnnotation(FeignClient.class);
        if (null != feignClient) {
            String methodName = joinPoint.getSignature().toString();
            Object[] args = joinPoint.getArgs();
            StringBuilder builder = new StringBuilder();
            builder.append("[method:").append(methodName).append("][args:").append(JSON.toJSONString(args)).append("]");
            ULogger.error(builder.toString(), new Object[]{"", ex});
        }
    }
}