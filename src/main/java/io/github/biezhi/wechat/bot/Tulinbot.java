package io.github.biezhi.wechat.bot;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import io.github.biezhi.wechat.WeChatBot;
import io.github.biezhi.wechat.api.annotation.Bind;
import io.github.biezhi.wechat.api.constant.Config;
import io.github.biezhi.wechat.api.enums.AccountType;
import io.github.biezhi.wechat.api.enums.MsgType;
import io.github.biezhi.wechat.api.enums.Word;
import io.github.biezhi.wechat.api.model.TuLinReqModel;
import io.github.biezhi.wechat.api.model.TuLinRspModel;
import io.github.biezhi.wechat.api.model.WeChatMessage;
import io.github.biezhi.wechat.api.model.tulin.Results;
import io.github.biezhi.wechat.utils.HttpUtils;
import io.github.biezhi.wechat.utils.TuLinBotUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wangsh
 * 图灵机器人实现
 * @date 2019-07-01 23:17
 */
@Slf4j
public class Tulinbot extends WeChatBot {

    public static final String NICK_NAME1 = "Вперёд, товарищи";
    public static final String NICK_NAME2 = "奔二颓废青年团";
    public static final String NICK_NAME3 = "一个群";

    public static List<String> USER_NAME_LIST = new ArrayList<>();
    public static List<String> KEY_LIST = new ArrayList<>();

    public static int index = 0;

    static {
        KEY_LIST.add("4132446a628e4c4b9b9e15e9be5a764d");
        KEY_LIST.add("bea137e22cab41bb9be8df8f8178ffa0");
        KEY_LIST.add("d6a5445bf1a74780be454071c182dd02");
        KEY_LIST.add("0383d55c02a24cc6a2d336fe7ead5110");
        KEY_LIST.add("691d650fad6d497f8f12337db049d39f");
    }


    public Tulinbot(Config config) {
        super(config);
    }

    /**
     * 绑定群聊信息
     *
     * @param message
     */
    @Bind(msgType = MsgType.ALL, accountType = AccountType.TYPE_GROUP)
    public void groupMessage(WeChatMessage message) {
        sendToGroup(message);
    }

    private void sendToGroup(WeChatMessage message) {
        String text = message.getText();
        log.info("收到的消息是=====>" + text);
        String fromUserName = message.getFromUserName();
        //1.收到说话字样时  将该username加入list
        if (text.contains(Word.OPEN_CHAT.getType())) {
            log.info("开启机器人模式====>" + text);
            USER_NAME_LIST.add(fromUserName);
            this.api().sendText(fromUserName, Word.OK.getType());
            return;
        }
        //2.收到闭嘴时就不说话  username移除
        if (text.contains(Word.CLOSE_CHAT.getType())) {
            log.info("关闭机器人模式====>" + text);
            USER_NAME_LIST.remove(fromUserName);
            this.api().sendText(fromUserName, Word.OK.getType());
            return;
        }
        for (String name : USER_NAME_LIST) {
            if (name.equals(fromUserName)) {
                model(text, fromUserName,KEY_LIST.get(index));
            }
        }
    }

    private void sendFriend(WeChatMessage message){
        String text = message.getText();
        String fromUserName = message.getFromUserName();
        model(text, fromUserName,KEY_LIST.get(index));
    }


    /**
     * 绑定私聊消息
     *
     * @param message
     */
    @Bind(msgType = {MsgType.TEXT, MsgType.VIDEO, MsgType.IMAGE, MsgType.EMOTICONS}, accountType = AccountType.TYPE_FRIEND)
    public void friendMessage(WeChatMessage message) {
        sendFriend(message);
    }

    private TuLinRspModel model(String text, String userName,String apiKey) {
        TuLinReqModel reqModel = TuLinBotUtils.createReqModel(text, null,apiKey);
        String jsonString = JSONObject.toJSONString(reqModel);
        String rsp = HttpUtils.post(TuLinBotUtils.TULIN_URL, jsonString);
        TuLinRspModel tuLinRspModel = JSONObject.parseObject(rsp, TuLinRspModel.class);
        List<Results> resultsList = tuLinRspModel.getResults();
        for (Results results : resultsList) {
            String result = results.getValues().get("text").toString();
            if(result.contains(Word.FAILURE.getType())){
                this.api().sendText(userName, Word.FAILURE_WORD.getType());
                index++;
            }else {
                this.api().sendText(userName, result);
            }
            if(index > KEY_LIST.size()){
                index = KEY_LIST.size();
            }
            log.info("发送消息" + result);
        }
        return tuLinRspModel;
    }


    /**
     * 好友验证消息
     *
     * @param message
     */
    @Bind(msgType = MsgType.ADD_FRIEND)
    public void addFriend(WeChatMessage message) {
        log.info("收到好友验证消息: {}", message.getText());
        if (message.getText().contains("java")) {
            this.api().verify(message.getRaw().getRecommend());
        }
    }

    public static void main(String[] args) {
        new Tulinbot(Config.me().autoLogin(true).showTerminal(true)).start();
    }
}
