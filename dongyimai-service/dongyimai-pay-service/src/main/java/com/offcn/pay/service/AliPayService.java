package com.offcn.pay.service;

import java.util.Map;

public interface AliPayService {
    /**
     * 生成支付宝支付二维码
     * @param out_trade_no 订单号
     * @param total_fee 订单金额
     * @return
     */
    Map createCode(String out_trade_no, String total_fee);

    /**
     * 支付后查询支付状态
     * 传入out_trade_no或trade_no    trade_no:支付时返回的支付宝交易号
     * @param out_trade_no 支付时传入的商户订单号
     * @return 返回out_trade_no、trade_no、trade_status
     */
    Map queryPayStatus(String out_trade_no);
}
