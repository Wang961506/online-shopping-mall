package com.mmall.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Cart;
import com.mmall.pojo.Product;
import com.mmall.service.ICartService;
import com.mmall.service.ICategoryService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.CartProductVo;
import com.mmall.vo.CartVo;
import org.omg.PortableInterceptor.INACTIVE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

/**
 * @author ZheWang
 * @create 2020-07-23 12:52
 */
@Service("iCartService")
public class CartServiceImpl implements ICartService {
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;

    public ServerResponse<CartVo> add(Integer userId, Integer productId, Integer count) {
        if (productId == null || count == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectCartByUserIdAndProductId(userId, productId);
        if (cart == null) {
            //没有此购物车记录，添加一个购物车到cart表中
            Cart cart1 = new Cart();
            cart1.setChecked(Const.Cart.CHECKED);
            cart1.setProductId(productId);
            cart1.setUserId(userId);
            cart1.setQuantity(count);
            cartMapper.insert(cart1);
        } else {
            Cart cart2 = new Cart();
            Integer count1 = count + cart.getQuantity();
            cart2.setQuantity(count1);
            cartMapper.updateByPrimaryKeySelective(cart2);
        }
        CartVo cartVoLimit = this.getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVoLimit);

    }

    private CartVo getCartVoLimit(Integer userId) {
        CartVo cartVo = new CartVo();
        List<Cart> cartList = cartMapper.selectCartByUserId(userId);
        List<CartProductVo> cartProductVoList = Lists.newArrayList();
        BigDecimal cartTotalPrice = new BigDecimal("0");
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(cartList)) {
            for (Cart cartItem : cartList) {
                CartProductVo cartProductVo = new CartProductVo();
                cartProductVo.setId(cartItem.getId());
                cartProductVo.setUserId(cartItem.getUserId());
                cartProductVo.setProductId(cartItem.getProductId());
                Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
                if (product != null) {
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductSubtitle(product.getSubtitle());
                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStock(product.getStock());
                    int purchaseQuantity = 0;
                    if (cartItem.getQuantity() < product.getStock()) {
                        purchaseQuantity = cartItem.getQuantity();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                    } else {
                        purchaseQuantity = product.getStock();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                        Cart updatedCart = new Cart();
                        updatedCart.setId(cartItem.getId());
                        updatedCart.setQuantity(purchaseQuantity);
                        cartMapper.updateByPrimaryKeySelective(updatedCart);
                    }
                    cartProductVo.setQuantity(purchaseQuantity);
                    cartProductVo.setProductTotalPrice(BigDecimalUtil.multiply(product.getPrice().doubleValue(), cartProductVo.getQuantity()));
                    cartProductVo.setProductChecked(cartItem.getChecked());
                    if (cartItem.getChecked() == Const.Cart.CHECKED) {
                        cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(), cartProductVo.getProductTotalPrice().doubleValue());
                    }
                    cartProductVoList.add(cartProductVo);
                }
            }
            cartVo.setCartTotalPrice(cartTotalPrice);
            cartVo.setCartProductVoList(cartProductVoList);
            cartVo.setAllChecked(this.getAllCheckedStatus(userId));
            cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        }
        return cartVo;
    }

    private boolean getAllCheckedStatus(Integer userId){
        if(userId == null){
            return false;
        }
        return cartMapper.selectCartProductCheckedStatusByUserId(userId) == 0;

    }

    public ServerResponse<CartVo> update(Integer userId, Integer productId, Integer count) {
        if (productId == null || count == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectCartByUserIdAndProductId(userId, productId);
        if(cart!=null){
            cart.setQuantity(count);
        }
        cartMapper.updateByPrimaryKeySelective(cart);
        CartVo cartVoLimit = this.getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVoLimit);
    }

    public ServerResponse<CartVo> delete(Integer userId, String productIds) {
        List<String> productIdList= Splitter.on(",").splitToList(productIds);
        if(CollectionUtils.isEmpty(productIdList)){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        cartMapper.deleteByUserIdProductId(userId, productIdList);
        CartVo cartVoLimit = this.getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVoLimit);
    }

    public ServerResponse<CartVo> list(Integer userId) {
        CartVo cartVoLimit = this.getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVoLimit);
    }

    public ServerResponse<CartVo> checkOrUnCheck(Integer userId,Integer checked,Integer productId) {
        cartMapper.checkedOrUncheckedProduct(userId,checked,productId);
        return this.list(userId);
    }

    public ServerResponse<Integer> getCartProductCount(Integer userId){
        if(userId==null)
            return ServerResponse.createBySuccess(0);
        int totalCount = cartMapper.selectCartProductCount(userId);
        return ServerResponse.createBySuccess(totalCount);
    }

}
