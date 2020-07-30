package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.ShippingMapper;
import com.mmall.pojo.Shipping;
import com.mmall.pojo.User;
import com.mmall.service.IShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

/**
 * @author ZheWang
 * @create 2020-07-24 10:57
 */
@Service("iShipService")
public class ShipServiceImpl implements IShipService {
    @Autowired
    private ShippingMapper shippingMapper;

    public ServerResponse add(Integer userId, Shipping shipping){
        shipping.setUserId(userId);
        int insertcount = shippingMapper.insert(shipping);//id属性会填充到shipping的getId上
        if(insertcount>0){
            Map map= Maps.newHashMap();
            map.put("shippingId",shipping.getId());
            return ServerResponse.createBySuccess("新建地址成功",map);
        }
        return ServerResponse.createByErrorMessage("新建地址失败");
    }

    public ServerResponse delete(Integer userId,Integer shippingId){

        int i = shippingMapper.deleteByShippingIdAndUserId(userId,shippingId);
        if(i>0)
            return ServerResponse.createBySuccess("删除地址成功");
        return ServerResponse.createByErrorMessage("删除地址失败");

    }

    public ServerResponse update(Integer userId,Shipping shipping){
        shipping.setUserId(userId);
        int i = shippingMapper.updateByShipping(shipping);
        if(i>0)
            return ServerResponse.createBySuccess("修改地址成功");
        return ServerResponse.createByErrorMessage("修改地址失败");
    }

    public ServerResponse<Shipping> select(Integer userId,Integer shippingId){
        Shipping shipping = shippingMapper.selectByShippingIdAndUserId(userId, shippingId);
        if(shipping!=null){
            return ServerResponse.createBySuccess("查询成功",shipping);
        }
        return ServerResponse.createByErrorMessage("无法查询到该地址");
    }

    public ServerResponse<PageInfo> list(Integer userId, int pageNum, int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Shipping> shippingList = shippingMapper.selectByUserId(userId);
        PageInfo pageInfo=new PageInfo(shippingList);
        return ServerResponse.createBySuccess(pageInfo);
    }
}
