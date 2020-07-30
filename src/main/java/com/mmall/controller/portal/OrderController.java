package com.mmall.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * @author ZheWang
 * @create 2020-07-26 10:25
 */
@Controller
@RequestMapping("/order/")
public class OrderController {
    @Autowired
    private IOrderService iOrderService;

    private static final Logger logger= LoggerFactory.getLogger(OrderController.class);


    @RequestMapping("createOrder.do")
    @ResponseBody
    public ServerResponse createOrder(HttpSession session, Integer shippingId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
       return iOrderService.createOrder(user.getId(),shippingId);
    }
    @RequestMapping("cancelOrder.do")
    @ResponseBody
    public ServerResponse cancelOrder(HttpSession session, Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.cancelOrder(user.getId(),orderNo);
    }

    @RequestMapping("get_order_cart_product.do")
    @ResponseBody
    public ServerResponse getOrderCartProduct(HttpSession session){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getOrderCartProduct(user.getId());
    }

    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse detailOfOrder(HttpSession session,Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getOrderDetail(user.getId(),orderNo);
    }

    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<PageInfo> list(HttpSession session, @RequestParam(value = "pageNum",defaultValue = "1") int pageNum, @RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getOrderList(user.getId(),pageNum,pageSize);
    }



    @RequestMapping("pay.do")
    @ResponseBody
    public ServerResponse pay(HttpSession session, Long orderNo, HttpServletRequest request){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        String path=request.getSession().getServletContext().getRealPath("upload");
        return  iOrderService.pay(orderNo,user.getId(),path);
    }

    @RequestMapping("alipay_callback.do")
    @ResponseBody
    public Object alipayCallback(HttpServletRequest request){
        Map<String,String> params= Maps.newHashMap();
        Map parameterMap = request.getParameterMap();
        Iterator iterator = parameterMap.keySet().iterator();
        while (iterator.hasNext()){
            String name=(String)iterator.next();
            String[] values = (String[]) parameterMap.get(name);
            String valuestr="";
            for(int i=0;i<values.length;i++){
                valuestr=(i==values.length-1)?valuestr+values[i]:valuestr+values[i]+",";
            }
            params.put(name,valuestr);
        }
        logger.info("支付宝回调，sign:{},trade_status:{},参数:{}",params.get("sign"),params.get("trade_status"),params.toString());
        //验证回调的正确性，是否是支付宝发过来的。此外也需要防止重复发消息
        params.remove("sign_type");
        try {
            boolean Alipaystatus = AlipaySignature.rsaCheckV2(params, Configs.getAlipayPublicKey(), "utf-8", Configs.getSignType());
            if(!Alipaystatus){
                return ServerResponse.createBySuccessMessage("非法请求，验证不通过");
            }

        } catch (AlipayApiException e) {
            logger.error("支付宝验证回调异常",e);
        }
        ServerResponse serverResponse = iOrderService.alipayCallBackVerification(params);
        if(serverResponse.isSuccess())
            return Const.AlipayCallback.RESPONSE_SUCCESS;
        else
            return Const.AlipayCallback.RESPONSE_FAILED;
    }


    @RequestMapping("query_order_pay_status.do")
    @ResponseBody
    public ServerResponse<Boolean> queryOrderPayStatus(HttpSession session, Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        ServerResponse queryorderpaystatus = iOrderService.queryorderpaystatus(user.getId(), orderNo);
        if(queryorderpaystatus.isSuccess())
            return ServerResponse.createBySuccess(true);
        else
            return ServerResponse.createBySuccess(false);

    }
}
