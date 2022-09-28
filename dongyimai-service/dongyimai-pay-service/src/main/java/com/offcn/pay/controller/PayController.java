package com.offcn.pay.controller;

import com.offcn.entity.Result;
import com.offcn.entity.StatusCode;
import com.offcn.order.feign.OrderFeign;
import com.offcn.order.pojo.PayLog;
import com.offcn.pay.service.AliPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Autowired
    private AliPayService aliPayService;

    @Autowired
    private OrderFeign orderFeign;

    /**
     * 生成支付宝支付二维码
     */
    @GetMapping("/createCode")
    public Map createCode() {
        //IdWorker idWorker = new IdWorker();
        //return aliPayService.createCode(idWorker.nextId() + "", "1");

        Result<PayLog> payLogResult = orderFeign.searchPayLogFromRedis();
        PayLog payLog = payLogResult.getData();
        if (payLog != null) {
            return aliPayService.createCode(payLog.getOutTradeNo(), payLog.getTotalFee() + "");
        } else {
            return new HashMap();
        }
    }

    /**
     * 查询支付状态
     *
     * @param out_trade_no
     * @return
     */
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no) {
        Result result = null;
        int x = 0;
        while (true) {
            //调用查询接口
            Map<String, String> map = null;
            try {
                map = aliPayService.queryPayStatus(out_trade_no);
            } catch (Exception e1) {
                /*e1.printStackTrace();*/
                System.out.println("调用查询服务出错");
            }
            if (map == null) {//出错
                result = new Result(false, StatusCode.ERROR, "支付出错");
                break;
            }
            if (map.get("tradestatus") != null && map.get("tradestatus").equals("TRADE_SUCCESS")) {//如果成功
                result = new Result(true, StatusCode.OK, "支付成功");
                //支付成功调用orderFeign修改订单的状态以及日志信息
                orderFeign.updateOrderStatus(map.get("out_trade_no"), map.get("trade_no"));
                break;
            }
            if (map.get("tradestatus") != null && map.get("tradestatus").equals("TRADE_CLOSED")) {//如果成功
                result = new Result(true, StatusCode.OK, "未付款交易超时关闭，或支付完成后全额退款");
                break;
            }
            if (map.get("tradestatus") != null && map.get("tradestatus").equals("TRADE_FINISHED")) {//如果成功
                result = new Result(true, StatusCode.OK, "交易结束，不可退款");
                break;
            }
            try {
                Thread.sleep(3000);//间隔三秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //给二维码加时间限制，超过10分钟就失效
            x++;
            if (x >= 200) {
                result = new Result(false, StatusCode.ERROR, "二维码已过期");
                break;
            }
        }
        return result;
    }
}
