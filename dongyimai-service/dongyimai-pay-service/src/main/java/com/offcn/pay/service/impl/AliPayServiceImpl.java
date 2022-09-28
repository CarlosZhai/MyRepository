package com.offcn.pay.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.offcn.pay.service.AliPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AliPayServiceImpl implements AliPayService {
    /**
     * 生成支付宝支付二维码
     */
    @Autowired
    private AlipayClient alipayClient;

    @Override
    public Map createCode(String out_trade_no, String total_fee) {
        Map<String, String> map = new HashMap<>();
        try {
            //创建预下单请求对象
            AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
            JSONObject bizContent = new JSONObject();
            bizContent.put("out_trade_no", out_trade_no);
            bizContent.put("total_amount", total_fee);
            bizContent.put("subject", "测试商品");

            //封装请求参数
            request.setBizContent(bizContent.toString());
            //发出预下单业务请求
            AlipayTradePrecreateResponse response = alipayClient.execute(request);

            //从响应中取出结果
            String code = response.getCode();//公共错误码,10000表示接口调用成功
            if ("10000".equals(code)) {
                map.put("qrcode", response.getQrCode());
                map.put("out_trade_no", response.getOutTradeNo());
                map.put("total_fee", total_fee);
            } else {
                System.out.println("调用失败" + response.getBody());
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 支付后查询支付状态
     * 通过AlipayClient调用交易查询接口（alipay.trade.query）
     *
     * @param out_trade_no 支付时传入的商户订单号
     * @return 返回out_trade_no、trade_no、trade_status
     */
    @Override
    public Map queryPayStatus(String out_trade_no) {
        Map<String, String> map = new HashMap<>();
        try {
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();

            JSONObject bizContent = new JSONObject();
            bizContent.put("out_trade_no", out_trade_no);

            //封装请求参数
            request.setBizContent(bizContent.toString());
            AlipayTradeQueryResponse response = alipayClient.execute(request);

            String code = response.getCode();
            if ("10000".equals(code)) {
                map.put("out_trade_no", response.getOutTradeNo());
                map.put("trade_no", response.getTradeNo());
                map.put("trade_status", response.getTradeStatus());
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return map;
    }
}
