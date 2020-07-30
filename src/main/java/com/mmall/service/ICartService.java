package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.vo.CartVo;

/**
 * @author ZheWang
 * @create 2020-07-23 12:52
 */
public interface ICartService {
    ServerResponse<CartVo> add(Integer userId, Integer productId, Integer count);
    ServerResponse<CartVo> update(Integer userId, Integer productId, Integer count);
    ServerResponse<CartVo> delete(Integer userId, String productIds);
    ServerResponse<CartVo> list(Integer userId);
    ServerResponse<CartVo> checkOrUnCheck(Integer userId,Integer checked,Integer productId);
    ServerResponse<Integer> getCartProductCount(Integer userId);
}
