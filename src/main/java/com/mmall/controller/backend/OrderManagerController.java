package com.mmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.service.IUserService;
import com.mmall.vo.OrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * @author ZheWang
 * @create 2020-07-30 12:34
 */
@Controller
@RequestMapping("/manage/order/")
public class OrderManagerController {
    @Autowired
    private IUserService iUserService;
    @Autowired
    private IOrderService iOrderService;

    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<PageInfo> orderList(HttpSession session, @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,@RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        User user= (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
           return iOrderService.getAllOrderList(pageNum,pageSize);
        }
        return ServerResponse.createByErrorMessage("用户没有权限");
    }

    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse<OrderVo> orderDetail(HttpSession session, Long orderNo){
        User user= (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            return iOrderService.manageDetailofOrder(orderNo);
        }
        return ServerResponse.createByErrorMessage("用户没有权限");
    }

    @RequestMapping("search.do")
    @ResponseBody
    public ServerResponse<PageInfo> ordersearch(HttpSession session, Long orderNo,@RequestParam(value = "pageNum",defaultValue = "1") int pageNum,@RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        User user= (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            return iOrderService.manageSearch(orderNo,pageNum,pageSize);
        }
        return ServerResponse.createByErrorMessage("用户没有权限");
    }

    @RequestMapping("send_product.do")
    @ResponseBody
    public ServerResponse<String> sendproduct(HttpSession session, Long orderNo){
        User user= (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            return iOrderService.manageSendProduct(orderNo);
        }
        return ServerResponse.createByErrorMessage("用户没有权限");
    }
}
