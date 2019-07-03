package io.github.biezhi.wechat.api.enums;

import lombok.Getter;
import lombok.Setter;

/**
 * @author wangsh
 * @date 2019-07-01 23:34
 */
@Getter
public enum Word {

    WEATHER("WEATHER", "天气"),
    OPEN_CHAT("OPENCHAT","开启"),
    CLOSE_CHAT("CLOSE_CHAT","关闭"),
    OK("OK","OK"),
    FAILURE("FAILURE","请求次数"),
    FAILURE_WORD("FAILURE_WORD","不明白你说的是什么,请重新表述");

    private String name;
    private String type;

    Word(String name, String type) {
        this.name = name;
        this.type = type;
    }

}
