package cc.wdcloud.feignClient;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import java.util.Date;

@Data
@ToString
public class CommonOprationLog {
    private Long id;
    @NotEmpty
    private String url;

    @NotEmpty
    private String name;

    private Long saasId;

    private Long branchId;

    private Long accountId;

    private String param;

    private String result;

    private Integer isDel;

    private Date createTime;

    private Date updateTime;

    private Long createUserId;

    private Long updateUserId;
}