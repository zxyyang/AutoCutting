package com.huomiao.aspect;

import com.alibaba.fastjson.JSONObject;
import com.huomiao.config.ConfigInit;
import com.huomiao.utils.HttpClientUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

/**
 * @author sixiaojie
 * @date 2021-05-25-15:22
 */
@Aspect
@Order(1)
public class LicenseAspect {
    /**
     * AOP 需要判断共享组的判断点 @License
     */
    @Pointcut("@annotation(com.huomiao.aspect.License)")
    public void isLicensePointcut() {}

    @Autowired
    HttpClientUtils httpClientUtils;
    @Autowired
    ConfigInit configInit;
    /**
     * AOP点之前就开始判断
     */
    @Before("isLicensePointcut()")
    public void beforeIsLicensePointcutCheck(JoinPoint joinPoint) throws UnknownHostException {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        License license = method.getAnnotation(License.class);
        if (!Objects.isNull(license)) {
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            Date date = new Date();
            Calendar calendar = Calendar.getInstance();
            if (date.after(configInit.getNextDate())){
                try {
                    String s = httpClientUtils.doGet(configInit.getVirApi()+"vir.php/?ip=" + hostAddress);
                    Integer code = JSONObject.parseObject(s).getInteger("code");
                    if (!Objects.equals(code,200)){
                        throw new RuntimeException("您未获取授权，请添加：QQ740444603,获取授权更新");
                    }
                    else {
                        /* HOUR_OF_DAY 指示一天中的小时 */
                        calendar.setTime(date);
                        calendar.add(Calendar.HOUR_OF_DAY, 1);
                        Date time = calendar.getTime();
                        configInit.setNextDate(time);
                    }
                }catch (Exception e){
                    throw new RuntimeException("您未获取授权，请添加：QQ740444603,获取授权更新");
                }
            }

        }
    }

}
