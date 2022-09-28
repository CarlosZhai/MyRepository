package com.offcn.listener;

import com.offcn.utils.SmsUtil;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SmsListener {

    @Autowired
    private SmsUtil smsUtil;

    @RabbitListener(queues = "dongyimai.sms.queue")
    public void getMessage(Map<String, String> map) throws Exception {
        if (map == null) {
            return;
        }
        String mobile = map.get("mobile");
        String code = map.get("code");
        // 发送短信
        smsUtil.sendSms(mobile, code);
    }
}
