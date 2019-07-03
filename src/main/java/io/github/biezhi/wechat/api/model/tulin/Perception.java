package io.github.biezhi.wechat.api.model.tulin;

import lombok.Getter;
import lombok.Setter;

/**
 * @author wangsh
 * @date 2019-07-01 23:04
 */
@Getter
@Setter
public class Perception {

    private InputText inputText;

    private InputMedia inputMedia;

    private InputImage inputImage;

    private SelfInfo selfInfo;

    @Setter
    @Getter
    public static class InputText{
        private String text;
    }

    @Setter
    @Getter
    public static class InputImage{
        private String url;
    }

    @Setter
    @Getter
    public static class InputMedia{
        private String url;
    }
}
