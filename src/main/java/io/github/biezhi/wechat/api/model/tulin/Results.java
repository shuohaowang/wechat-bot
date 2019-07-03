package io.github.biezhi.wechat.api.model.tulin;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @author wangsh
 * @date 2019-07-01 23:14
 */
@Getter
@Setter
public class Results {

    private String resultType;

    private Integer groupType;

    private Map<String,Object> values;
}
