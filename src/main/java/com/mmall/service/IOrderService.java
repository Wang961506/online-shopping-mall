package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.vo.OrderVo;

import java.util.Map;

/**
 * @author ZheWang
 * @create 2020-07-26 10:30
 */
public interface IOrderService {
    ServerResponse pay(Long orderNo, Integer userId, String path);
    ServerResponse alipayCallBackVerification(Map<String,String> params);
    ServerResponse queryorderpaystatus(Integer userId,Long orderNo);
    ServerResponse createOrder(Integer userId, Integer shippingId);
    ServerResponse<String> cancelOrder(Integer userId,Long orderNo);
    ServerResponse getOrderCartProduct(Integer userId);
    ServerResponse<OrderVo> getOrderDetail(Integer userId, Long orderNo);
    ServerResponse<PageInfo> getOrderList(Integer userId, int pageNum, int pageSize);
    ServerResponse<PageInfo> getAllOrderList(int pageNum,int pageSize);
    ServerResponse<OrderVo> manageDetailofOrder(Long orderNo);
    ServerResponse<PageInfo> manageSearch(Long orderNo,int pageNum,int pageSize);
    ServerResponse<String> manageSendProduct(Long orderNo);
}
