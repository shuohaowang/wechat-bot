package io.github.biezhi.wechat.api.model;

import io.github.biezhi.wechat.api.model.tulin.Perception;
import io.github.biezhi.wechat.api.model.tulin.UserInfo;
import lombok.Getter;
import lombok.Setter;

/**
 * @author wangsh
 * @date 2019-07-01 22:56
 */
@Getter
@Setter
public class TuLinReqModel {

    private Integer reqType;

    private Perception perception;

    private UserInfo userInfo;

}
