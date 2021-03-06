package com.blindingdark.geektrans.trans.youdao;

import android.os.Handler;
import android.os.Message;

import com.blindingdark.geektrans.activity.TransActivity;
import com.blindingdark.geektrans.bean.Result;
import com.blindingdark.geektrans.trans.youdao.bean.YoudaoSettings;
import com.blindingdark.geektrans.trans.youdao.bean.YoudaoTransRes;
import com.youdao.sdk.app.Language;
import com.youdao.sdk.app.LanguageUtils;
import com.youdao.sdk.ydonlinetranslate.Translator;
import com.youdao.sdk.ydtranslate.Translate;
import com.youdao.sdk.ydtranslate.TranslateErrorCode;
import com.youdao.sdk.ydtranslate.TranslateListener;
import com.youdao.sdk.ydtranslate.TranslateParameters;

import java.util.List;

/**
 * Created by BlindingDark on 2016/8/20 0020.
 */
public class YoudaoTransReq {
    String query;
    YoudaoSettings youdaoSettings;
    Result beanResult = new Result();
    Handler handler;

    public YoudaoTransReq(YoudaoSettings settings, String query, Handler handler) {
        beanResult.setOriginalReq(query);
        this.youdaoSettings = settings;
        this.handler = handler;
        this.query = query;
    }

    public void trans() {
        //查词对象初始化，请设置source参数为app对应的名称（英文字符串）
        Language langFrom = LanguageUtils.getLangByName("自动");
        //若设置为自动，则查询自动识别源语言，自动识别不能保证完全正确，最好传源语言类型
        //Language langFrom = LanguageUtils.getLangByName("英文");
        Language langTo = LanguageUtils.getLangByName("中文");

        TranslateParameters tps = new TranslateParameters.Builder()
                .source("GeekTrans")
                .from(langFrom).to(langTo).build();

        Translator translator = Translator.getInstance(tps);

        //查询，返回两种情况，一种是成功，相关结果存储在result参数中，另外一种是失败，失败信息放在TranslateErrorCode 是一个枚举类，整个查询是异步的，为了简化操作，回调都是在主线程发生。

        translator.lookup(query, "GeekTrans", new TranslateListener() {

            @Override
            public void onError(TranslateErrorCode translateErrorCode, String s) {
                beanResult.setWhat(TransActivity.normalToast);
                String errorMessage = "";
                int errorCode = translateErrorCode.getCode();

                switch (errorCode) {
                    case 101:
                        errorMessage = "请确认有道密钥已经正确填写了哦～";
                        break;
                    case 102:
                        errorMessage = "不支持的语言类型";
                        break;
                    case 103:
                        errorMessage = "翻译文本过长";
                        break;
                    case 104:
                        errorMessage = "不支持的API类型";
                        break;
                    case 105:
                        errorMessage = "不支持的签名类型";
                        break;
                    case 106:
                        errorMessage = "不支持的响应类型";
                        break;
                    case 107:
                        errorMessage = "不支持的传输加密类型";
                        break;
                    case 108:
                        errorMessage = "有道密钥不对哦～";
                        break;
                    case 109:
                        errorMessage = "batchLog格式不正确";
                        break;
                    case 110:
                        errorMessage = "无相关服务的有效实例～\n注意要同时创建并绑定我的应用和翻译实例！";
                        break;
                    case 111:
                        errorMessage = "开发者账号无效，可能是账号为欠费状态";
                        break;
                    case 201:
                        errorMessage = "解密失败，可能为DES,BASE64,URLDecode的错误";
                        break;
                    case 202:
                        errorMessage = "签名检验失败";
                        break;
                    case 203:
                        errorMessage = "访问IP地址不在可访问IP列表";
                        break;
                    case 301:
                        errorMessage = "辞典查询失败";
                        break;
                    case 302:
                        errorMessage = "翻译查询失败";
                        break;
                    case 303:
                        errorMessage = "服务端的其它异常";
                        break;
                    case 401:
                        errorMessage = "账户已经欠费";
                        break;
                    default:
                        errorMessage = "有道翻译姬出错啦～";
                        break;
                }

                beanResult.setStringResult(errorMessage);
                sendMsg();
            }

            @Override
            public void onResult(Translate translate, String input, String requestId) {
                YoudaoTransRes youdaoTransRes = new YoudaoTransRes(translate);

                List<String> sounds = youdaoTransRes.getSoundURLs();
                if (sounds.isEmpty()) {
                    beanResult.setWhat(TransActivity.normalToast);
                } else {
                    beanResult.setWhat(TransActivity.haveSoundToast);
                    beanResult.setSoundURLs(sounds);
                }

                beanResult.setStringResult(youdaoTransRes.toString(youdaoSettings.getDivisionLine()));

                sendMsg();
            }
        });
    }

    private void sendMsg() {
        beanResult.setFromEngineName(Youdao.ENGINE_NAME);
        Message message = new Message();
        message.what = beanResult.getWhat();
        message.obj = beanResult;
        handler.sendMessage(message);
    }

}
