package io.github.biezhi.wechat.api.model.tulin;

import lombok.Getter;
import lombok.Setter;

/**
 * @author wangsh
 * @date 2019-07-01 23:07
 */
@Getter
@Setter
public class SelfInfo {

    private Location location;

    @Getter
    @Setter
    public static class Location{

        private String city;

        private String province;

        private String street;
    }
}
