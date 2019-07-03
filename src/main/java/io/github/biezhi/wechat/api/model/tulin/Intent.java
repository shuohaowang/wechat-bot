package io.github.biezhi.wechat.api.model.tulin;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

/**
 * @author wangsh
 * @date 2019-07-01 23:11
 */
@Getter
@Setter
@ToString
public class Intent {

    private Integer code;

    private String intentName;

    private String actionName;

    private Map<String,Object> parameters;
}
