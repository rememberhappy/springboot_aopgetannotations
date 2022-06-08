package cc.zdj.feignClient;

import cc.wdcloud.base.Resp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @Author zhangdj
 * @Date 2021/5/13:16:37
 * @Description:
 */
@FeignClient(value = "HRSS-USER-CENTER", path = "/usercenter/v1")
public interface UserLogAgent {
    @RequestMapping(value = "/commonOprationLog/save", method = RequestMethod.POST)
    Resp<Boolean> save(@RequestBody CommonOprationLog commonOprationLog);
}