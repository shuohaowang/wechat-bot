package io.github.biezhi.wechat.api.model;

import io.github.biezhi.wechat.api.model.tulin.Intent;
import io.github.biezhi.wechat.api.model.tulin.Results;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @author wangsh
 * @date 2019-07-01 23:02
 */
@Getter
@Setter
@ToString
public class TuLinRspModel {

    private Intent intent;

    private List<Results> results;
}
