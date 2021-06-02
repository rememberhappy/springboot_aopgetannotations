package cc.wdcloud.aop;

import cc.wdcloud.annotation.LogAnnotation;
import cc.wdcloud.base.Resp;
import cc.wdcloud.common.CommonInfoHolder;
import cc.wdcloud.common.log.ULogger;
import cc.wdcloud.feignClient.CommonOprationLog;
import cc.wdcloud.feignClient.UserLogAgent;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author zhangdj
 * @Date 2021/5/13:10:22
 * @Description:
 */
@Component
@Aspect
public class TestAOPAnnotations {

    @Autowired
    UserLogAgent userLogAgent;

    /**
     * 先创建一个方法，方法名随意，但是需要制定@annotation为刚刚自定义的注解
     *
     * @param
     * @return void
     * @Throws
     * @Author zhangdj
     * @date 2021/5/13 18:03
     */
    @Pointcut("@annotation(cc.wdcloud.annotation.LogAnnotation)")
    public void test() {
    }

    /**
     * 使用@Before，需要先引入上面@Pointcut注解的方法名，在加上@annotation，@annotation中的值，需要和action方法中的参数名称相同（必须相同，但是名称任意）
     *
     * @param joinPoint      获取请求参数
     * @param demoAnnotation 注解类型，用户获取注解的参数
     * @return void
     * @Throws
     * @Author zhangdj
     * @date 2021/5/13 16:24
     */
    @Before("test() && @annotation(demoAnnotation)")
    public void doBefore(JoinPoint joinPoint, LogAnnotation demoAnnotation) {
        String paramJSON = processingParameters(demoAnnotation, joinPoint);
////        ULogger.info("目标方法名为:" + joinPoint.getSignature().getName());
////        ULogger.info("目标方法所属类的简单类名:" + joinPoint.getSignature().getDeclaringType().getSimpleName());
////        ULogger.info("目标方法所属类的类名:" + joinPoint.getSignature().getDeclaringTypeName());
////        ULogger.info("目标方法声明类型:" + Modifier.toString(joinPoint.getSignature().getModifiers()));
////        ULogger.info("被代理的对象:" + joinPoint.getTarget());
////        ULogger.info("代理对象自己:" + joinPoint.getThis());
//        CommonOprationLog commonOprationLog = processReturnValue(demoAnnotation, null, paramJSON);
//        userLogAgent.save(commonOprationLog);
    }

    /**
     * 数据返回后触发,@annotation中的值，需要和action方法中的参数名称相同（必须相同，但是名称任意）
     *
     * @param joinPoint      获取请求参数
     * @param demoAnnotation 注解类型，用户获取注解的参数
     * @param ret            获取请求的返回值
     * @return void
     * @Throws
     * @Author zhangdj
     * @date 2021/5/13 18:04
     */
    @AfterReturning(pointcut = "test() && @annotation(demoAnnotation)", returning = "ret")
    public void doAfter(JoinPoint joinPoint, LogAnnotation demoAnnotation, Object ret) {
        // 获取返回值
        Resp result = (Resp) ret;
        String resultString = JSONObject.toJSONString(result);
        String paramJSON = processingParameters(demoAnnotation, joinPoint);
        CommonOprationLog commonOprationLog = processReturnValue(demoAnnotation, resultString, paramJSON);
        userLogAgent.save(commonOprationLog);
    }

//    @Around("test() && @annotation(demoAnnotation)")
//    public Object doAround(ProceedingJoinPoint proceedingJoinPoint, LogAnnotation demoAnnotation) throws Throwable {
//        System.out.println("环绕通知：");
//        System.out.println("注解url值 : " + demoAnnotation.url());
//        System.out.println("注解urlname值 : " + demoAnnotation.urlname());
//        Object result = null;
//        result = proceedingJoinPoint.proceed();
//        return result;
//    }

    /**
     * 统一处理请求参数，将请求参数封装成map对象，通过JSON转为字符串
     *
     * @param demoAnnotation 自定义注解值
     * @param joinPoint      请求参数
     * @return java.lang.String
     * @Throws
     * @Author zhangdj
     * @date 2021/5/13 18:06
     */
    private String processingParameters(LogAnnotation demoAnnotation, JoinPoint joinPoint) {
        // 获取到所有的参数值的数组
        Object[] args = joinPoint.getArgs();
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        //2.获取到方法的所有参数名称的字符串数组
        String[] parameterNames = methodSignature.getParameterNames();
        Class[] parameterTypes = methodSignature.getParameterTypes();
        Map<String, Object> paramMap = new HashMap<String, Object>();
        for (int i = 0, len = parameterNames.length; i < len; i++) {
            if (!"interface org.springframework.web.multipart.MultipartFile".equals(parameterTypes[i].toString())) {// 判断类型不为MultipartFile
                paramMap.put(parameterNames[i], args[i]);
            } else {
                paramMap.put(parameterNames[i], demoAnnotation.value() + "操作媒体文件！");
            }
        }
        if (paramMap.size() < 1) {
            return null;
        }
        return JSONObject.toJSONString(paramMap);
    }

    /**
     * 获取IP地址
     *
     * @param request
     * @return java.lang.String
     * @Throws
     * @Author zhangdj
     * @date 2021/5/17 17:38
     */
    private String getRemoteHost(HttpServletRequest request) {
        String sourceIp = null;
        String ipAddresses = request.getHeader("x-forwarded-for");
        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            ipAddresses = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            ipAddresses = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            ipAddresses = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            ipAddresses = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            ipAddresses = request.getRemoteAddr();
        }
        if (!StringUtils.isEmpty(ipAddresses)) {
            sourceIp = ipAddresses.split(",")[0];
        }
        return "0:0:0:0:0:0:0:1".equals(sourceIp) ? "127.0.0.1" : sourceIp;
    }

    /**
     * 统一处理返回值
     *
     * @param demoAnnotation 注解类型，用户获取注解的参数
     * @param result         请求的返回值
     * @param paramJSON      处理过后的请求参数
     * @return cc.wdcloud.feignClient.CommonOprationLog
     * @Throws
     * @Author zhangdj
     * @date 2021/5/13 18:11
     */
    private CommonOprationLog processReturnValue(LogAnnotation demoAnnotation, String result, String paramJSON) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        // IP地址
        String ipAddr = getRemoteHost(request);
        // 请求地址
        String url = request.getRequestURL().toString();
//        ULogger.info("请求源IP:【{}】,请求URL:【{}】",ipAddr,url);
        CommonOprationLog commonOprationLog = new CommonOprationLog();
        commonOprationLog.setUrl(url);
        commonOprationLog.setName(demoAnnotation.value());
        commonOprationLog.setSaasId(CommonInfoHolder.getSaasId());
        commonOprationLog.setBranchId(CommonInfoHolder.getBranchId());
        commonOprationLog.setAccountId(CommonInfoHolder.getAccountId());
        commonOprationLog.setParam(paramJSON);
        commonOprationLog.setResult(result);
        commonOprationLog.setIsDel(0);
        commonOprationLog.setCreateTime(new Date());
        commonOprationLog.setCreateUserId(CommonInfoHolder.getUserId());
        commonOprationLog.setUpdateTime(new Date());
        commonOprationLog.setUpdateUserId(CommonInfoHolder.getUserId());
        return commonOprationLog;
    }
}