package io.github.biezhi.wechat.utils;

import io.github.biezhi.wechat.api.model.TuLinReqModel;
import io.github.biezhi.wechat.api.model.tulin.Perception;
import io.github.biezhi.wechat.api.model.tulin.SelfInfo;
import io.github.biezhi.wechat.api.model.tulin.UserInfo;

/**
 * @author wangsh
 * @date 2019-07-01 23:20
 */
public class TuLinBotUtils {

    public static final String TULIN_URL = "http://openapi.tuling123.com/openapi/api/v2";
    private static final String USER_ID = "473888";

    public static TuLinReqModel createReqModel(String msg,SelfInfo selfInfo,String apiKey){
        TuLinReqModel reqModel = new TuLinReqModel();
        UserInfo userInfo = new UserInfo();
        userInfo.setApiKey(apiKey);
        userInfo.setUserId(USER_ID);
        reqModel.setUserInfo(userInfo);
        Perception perception = new Perception();
        Perception.InputText inputText = new Perception.InputText();
        inputText.setText(msg);
        perception.setInputText(inputText);
        if(selfInfo != null){
            perception.setSelfInfo(selfInfo);
        }
        reqModel.setPerception(perception);
        return reqModel;
    }


    public static SelfInfo createSelfInfo(String city,String province,String street){
        SelfInfo selfInfo = new SelfInfo();
        SelfInfo.Location location = new SelfInfo.Location();
        location.setCity(city);
        location.setProvince(province);
        location.setStreet(street);
        selfInfo.setLocation(location);
        return selfInfo;
    }








}
