package com.mmall.service.impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.Main;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.*;
import com.mmall.pojo.*;
import com.mmall.service.IOrderService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.FTPUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.OrderItemVo;
import com.mmall.vo.OrderProductVo;
import com.mmall.vo.OrderVo;
import com.mmall.vo.ShippingVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.apache.bcel.classfile.Code;
import org.aspectj.weaver.ast.Or;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Logger;


/**
 * @author ZheWang
 * @create 2020-07-26 10:30
 */
@Service("OrderService")
public class OrderServiceImpl implements IOrderService {

    private static AlipayTradeService tradeService;

    static {

        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();
    }

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private PayInfoMapper payInfoMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ShippingMapper shippingMapper;
    public ServerResponse pay(Long orderNo, Integer userId, String path) {
        Map<String, String> resultMap = Maps.newHashMap();
        Order order = orderMapper.selectOrderByOrderNoAndUserId(orderNo, userId);
        if (order == null) {
            return ServerResponse.createBySuccessMessage("该用户没有此订单");
        }
        resultMap.put("orderNo", String.valueOf(order.getOrderNo()));

        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = String.valueOf(order.getOrderNo());

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = new StringBuilder().append("ZheMmall扫码支付,订单号：").append(outTradeNo).toString();

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = new StringBuilder().append("订单").append(outTradeNo).append("购买商品共").append(totalAmount).append("元").toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 支付超时，定义为120分钟
        String timeoutExpress = "120m";

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();
        List<OrderItem> orderItemList = orderItemMapper.selectOrderItemByOrderNoAndUserId(orderNo, userId);
        for (OrderItem item : orderItemList) {
            // 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
            GoodsDetail goods1 = GoodsDetail.newInstance(item.getProductId().toString(), item.getProductName(), BigDecimalUtil.multiply(item.getCurrentUnitPrice().doubleValue(), new Double(100).doubleValue()).longValue(), item.getQuantity());
            // 创建好一个商品后添加至商品明细列表
            goodsDetailList.add(goods1);
        }


        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);

        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);

        switch (result.getTradeStatus()) {
            case SUCCESS:
                log.info("支付宝预下单成功: )");

                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);

                File folder = new File(path);
                if (!folder.exists()) {
                    folder.setWritable(true);
                    folder.mkdirs();
                }

                // 需要修改为运行机器上的路径
                String qrPath = String.format(path + "/qr-%s.png", response.getOutTradeNo());
                String qrfileName = String.format("qr-%s.png", response.getOutTradeNo());
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrPath);
                File targetFile = new File(path, qrfileName);
                try {
                    FTPUtil.uploadFile(Lists.<File>newArrayList(targetFile));
                } catch (IOException e) {
                    log.error("上传二维码异常", e);
                }

                log.info("qrPath:" + qrPath);
                //                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, filePath);
                String qrUrl = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFile.getName();
                resultMap.put("qrUrl", qrUrl);
                return ServerResponse.createBySuccess(resultMap);

            case FAILED:
                log.error("支付宝预下单失败!!!");
                return ServerResponse.createByErrorMessage("支付宝预下单失败!!!");

            case UNKNOWN:
                log.error("系统异常，预下单状态未知!!!");
                return ServerResponse.createByErrorMessage("系统异常，预下单状态未知!!!");

            default:
                log.error("不支持的交易状态，交易返回异常!!!");
                return ServerResponse.createByErrorMessage("不支持的交易状态，交易返回异常!!!");
        }
    }

    // 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            log.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                log.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            log.info("body:" + response.getBody());
        }
    }

    public ServerResponse alipayCallBackVerification(Map<String, String> params) {
        Long orderNo = Long.parseLong(params.get("out_trade_no"));

        String tradeNo = params.get("trade_no");
        String trade_status = params.get("trade_status");

        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("此订单不存在，忽略回调");
        }
        if (order.getStatus() >= Const.OrderStatus.PAID.getCode()) {
            return ServerResponse.createBySuccess("支付宝重复调用");
        }
        if (Const.AlipayCallback.TRADE_SUCCESS.equals(trade_status)) {
            order.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment")));
            order.setStatus(Const.OrderStatus.PAID.getCode());
            orderMapper.updateByPrimaryKeySelective(order);
        }
        PayInfo payInfo = new PayInfo();
        payInfo.setUserId(order.getUserId());
        payInfo.setOrderNo(order.getOrderNo());
        payInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode());
        payInfo.setPlatformNumber(tradeNo);
        payInfo.setPlatformStatus(trade_status);
        payInfoMapper.insert(payInfo);
        return ServerResponse.createBySuccess();
    }

    public ServerResponse queryorderpaystatus(Integer userId, Long orderNo) {
        Order order = orderMapper.selectOrderByOrderNoAndUserId(orderNo, userId);
        if (order == null) {
            return ServerResponse.createByErrorMessage("没有此订单");
        }
        if (order.getStatus() > Const.OrderStatus.PAID.getCode()) {
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }

    public ServerResponse createOrder(Integer userId, Integer shippingId) {
        List<Cart> cartsList = cartMapper.selectCheckedCartByUserId(userId);

        //计算订单中所有商品的总价
        ServerResponse resultOfCartOrderItem = getCartOrderItem(userId, cartsList);
        if(!resultOfCartOrderItem.isSuccess())
        {
            return resultOfCartOrderItem;
        }
        List<OrderItem> orderItemList = (List<OrderItem>) resultOfCartOrderItem.getData();
        if(CollectionUtils.isEmpty(orderItemList))
            return ServerResponse.createByErrorMessage("购物车为空");
        BigDecimal totalPriceOfOrder = getTotalPriceOfOrder(orderItemList);

        //生成订单，此时订单已经在数据库中存储
        Order order=this.assembleOrder(userId,shippingId,totalPriceOfOrder);
        if(order==null)
            return ServerResponse.createByErrorMessage("生成订单错误");
        for (OrderItem orderItem : orderItemList) {
            orderItem.setOrderNo(order.getOrderNo());
        }
        //mybatis批量插入
        orderItemMapper.batchInsert(orderItemList);
        //订单产品加入后，减少各个商品的存货
        this.reductProductStock(orderItemList);
        //清空选中的购物车
        this.cleanCheckedCart(cartsList);
        //返回给前端需要的数据,通过vo对象
        OrderVo orderVo = this.assembleOrderVo(order, orderItemList);
        return ServerResponse.createBySuccess(orderVo);


    }


    //取消订单
    public ServerResponse<String> cancelOrder(Integer userId,Long orderNo){
        Order order = orderMapper.selectOrderByOrderNoAndUserId(orderNo, userId);
        if(order==null)
            return ServerResponse.createByErrorMessage("没有此订单");
        if(order.getStatus()!=Const.OrderStatus.NO_PAY.getCode()){
            return ServerResponse.createByErrorMessage("已经付款，在没有支付宝的情况下无法退款");
        }
        Order otherOrder=new Order();
        otherOrder.setId(order.getId());

        otherOrder.setStatus(Const.OrderStatus.CANCLED.getCode());
        int i = orderMapper.updateByPrimaryKeySelective(otherOrder);
        if(i>0)
            return ServerResponse.createBySuccess("取消成功");
        else
            return ServerResponse.createByErrorMessage("取消失败");


    }

    //获得订单中的商品详情
    public ServerResponse getOrderCartProduct(Integer userId){
        OrderProductVo orderProductVo=new OrderProductVo();
        List<Cart> cartList=cartMapper.selectCheckedCartByUserId(userId);
        ServerResponse ser = this.getCartOrderItem(userId, cartList);
        if(!ser.isSuccess()){
            return ser;
        }
        List<OrderItem> orderItemList = (List<OrderItem>) ser.getData();
        List<OrderItemVo> orderItemVoList1 = assembleOrderItemVoList(orderItemList);
        orderProductVo.setOrderItemVoList(orderItemVoList1);
        orderProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        BigDecimal totalpric=new BigDecimal("0");
        for (OrderItem orderItem : orderItemList) {
            totalpric=BigDecimalUtil.add(totalpric.doubleValue(),orderItem.getTotalPrice().doubleValue());
        }
        orderProductVo.setProductTotalPrice(totalpric);
        return ServerResponse.createBySuccess(orderProductVo);
    }

    //获得订单详情
    public ServerResponse<OrderVo> getOrderDetail(Integer userId,Long orderNo){
        Order order = orderMapper.selectOrderByOrderNoAndUserId(orderNo, userId);
        if(order==null)
            return ServerResponse.createByErrorMessage("没有找到该订单");
        List<OrderItem> orderItemList = orderItemMapper.selectOrderItemByOrderNoAndUserId(order.getOrderNo(), order.getUserId());
        OrderVo orderVo = assembleOrderVo(order, orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }

    //获得所有的订单
    public ServerResponse<PageInfo> getOrderList(Integer userId,int pageNum,int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList = orderMapper.selectOrderByUserId(userId);
        List<OrderVo> orderVoList = this.assembleOrderVoList(orderList, userId);
        PageInfo pageInfo=new PageInfo(orderList);
        pageInfo.setList(orderVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    //后台得到所有的订单
    public ServerResponse<PageInfo> getAllOrderList(int pageNum,int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList = orderMapper.selectAllOrder();
        List<OrderVo> orderVoList = assembleOrderVoList(orderList, null);
        PageInfo pageInfo=new PageInfo(orderList);
        pageInfo.setList(orderVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    //后台得到订单详情
    public ServerResponse<OrderVo> manageDetailofOrder(Long orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order==null){
            return ServerResponse.createByErrorMessage("订单不存在或者出错");
        }
        List<OrderItem> orderItemList = orderItemMapper.selectOrderItemByOrderNo(orderNo);
        OrderVo orderVo = assembleOrderVo(order, orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }

    //后台搜索订单
    public ServerResponse<PageInfo> manageSearch(Long orderNo,int pageNum,int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order==null){
            return ServerResponse.createByErrorMessage("订单不存在或者出错");
        }
        List<OrderItem> orderItemList = orderItemMapper.selectOrderItemByOrderNo(orderNo);
        OrderVo orderVo = assembleOrderVo(order, orderItemList);
        PageInfo pageInfo=new PageInfo(Lists.newArrayList(order));
        pageInfo.setList(Lists.newArrayList(orderVo));
        return ServerResponse.createBySuccess(pageInfo);
    }

    //后台订单发货
    public ServerResponse<String> manageSendProduct(Long orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order==null){
            return ServerResponse.createByErrorMessage("订单不存在");
        }
        if(order.getStatus()==Const.OrderStatus.PAID.getCode()){
            order.setStatus(Const.OrderStatus.SHIPPED.getCode());
            order.setSendTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
            return ServerResponse.createBySuccess("发货成功");
        }
        return ServerResponse.createByErrorMessage("订单未付款，无法发货");


    }

    private List<OrderVo> assembleOrderVoList(List<Order> orderList,Integer userId){
        List<OrderVo> orderVoList=Lists.newArrayList();
        for (Order order : orderList) {
            List<OrderItem> orderItemList =Lists.newArrayList();
            if(userId==null){
                //管理员查询的时候，可以查询所有用户的订单,不需要userId
                orderItemList = orderItemMapper.selectOrderItemByOrderNo(order.getOrderNo());

            }
            else{
                orderItemList=orderItemMapper.selectOrderItemByOrderNoAndUserId(order.getOrderNo(),userId);
            }
            OrderVo orderVo = assembleOrderVo(order, orderItemList);
            orderVoList.add(orderVo);
        }
        return orderVoList;
    }

















    //获得购物车里面的商品信息
    private ServerResponse getCartOrderItem(Integer userId, List<Cart> cartList) {
        List<OrderItem> orderItemList = Lists.newArrayList();
        if (CollectionUtils.isEmpty(cartList)) {
            return ServerResponse.createByErrorMessage("购物车为空");
        }
        for (Cart cart : cartList) {
            OrderItem orderItem = new OrderItem();
            Product product = productMapper.selectByPrimaryKey(cart.getProductId());
            if (Const.ProductStatus.ON_SALE.getCode() == product.getStatus()) {
                return ServerResponse.createByErrorMessage("产品" + product.getName() + "已经停止售卖");
            }
            if (product.getStock() < cart.getQuantity()) {
                return ServerResponse.createByErrorMessage("产品" + product.getName() + "库存不足");
            }
            orderItem.setUserId(userId);
            orderItem.setProductName(product.getName());
            orderItem.setProductId(product.getId());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setQuantity(cart.getQuantity());
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setTotalPrice(BigDecimalUtil.multiply(product.getPrice().doubleValue(), cart.getQuantity().doubleValue()));
            orderItemList.add(orderItem);
        }
        return ServerResponse.createBySuccess(orderItemList);
    }
    private BigDecimal getTotalPriceOfOrder(List<OrderItem> orderItemList){
        BigDecimal totalprice=new BigDecimal("0");
        for (OrderItem orderItem : orderItemList) {
            BigDecimal singleProductTotalPrice = orderItem.getTotalPrice();
            totalprice=BigDecimalUtil.add(totalprice.doubleValue(),singleProductTotalPrice.doubleValue());
        }
        return totalprice;
    }

    private Order assembleOrder(Integer userId,Integer shippingId,BigDecimal totalpriceofOrder){
        Long orderNo=generateOrderNo();
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setStatus(Const.OrderStatus.NO_PAY.getCode());
        order.setShippingId(shippingId);
        order.setPayment(totalpriceofOrder);
        order.setPaymentType(Const.PaymentTypeEnum.ONLINE_PAY.getCode());
        order.setPostage(0);
        int insertCount = orderMapper.insert(order);
        if(insertCount>0){
            return order;
        }
        return null;
    }

    private long generateOrderNo(){
        long curtime = System.currentTimeMillis();
        return curtime+new Random().nextInt(100);

    }

    //减少商品库存
    private void reductProductStock(List<OrderItem> orderItemList){
        for(OrderItem orderItem:orderItemList){
            Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
            product.setStock(product.getStock()-orderItem.getQuantity());
            productMapper.updateByPrimaryKeySelective(product);
        }
    }
    private void cleanCheckedCart(List<Cart> cartsList){
        for (Cart cart : cartsList) {
            cartMapper.deleteByPrimaryKey(cart.getId());
        }
    }

    private OrderVo assembleOrderVo(Order order,List<OrderItem> orderItemList){
        OrderVo orderVo=new OrderVo();
        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setPayment(order.getPayment());
        orderVo.setPaymentType(order.getPaymentType());
        orderVo.setPaymentTypeDesc(Const.PaymentTypeEnum.getDescByCode(order.getPaymentType()).getValue());
        orderVo.setPostage(order.getPostage());
        orderVo.setStatus(order.getStatus());
        orderVo.setStatusDesc(Const.OrderStatus.getDescByCode(order.getStatus()).getValue());
        orderVo.setShippingId(order.getShippingId());
        orderVo.setPaymentTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
        orderVo.setSendTime(DateTimeUtil.dateToStr(order.getSendTime()));
        orderVo.setEndTime(DateTimeUtil.dateToStr(order.getEndTime()));
        orderVo.setCreateTime(DateTimeUtil.dateToStr(order.getCreateTime()));
        orderVo.setCloseTime(DateTimeUtil.dateToStr(order.getCloseTime()));
        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        if(shipping!=null){
            orderVo.setReceiverName(shipping.getReceiverName());
            orderVo.setShippingVo(assembleShippingVo(shipping));
        }
        orderVo.setOrderItemVoList(assembleOrderItemVoList(orderItemList));
        orderVo.setImageHost("ftp.server.http.prefix");
        return orderVo;

    }
    private ShippingVo assembleShippingVo(Shipping shipping){
        ShippingVo shippingVo=new ShippingVo();
        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverPhone(shipping.getReceiverPhone());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverZip(shipping.getReceiverZip());
        return shippingVo;
    }

    private List<OrderItemVo> assembleOrderItemVoList(List<OrderItem> orderItemList){
        List<OrderItemVo> orderItemVoList=Lists.newArrayList();
        for (OrderItem orderItem : orderItemList) {
            OrderItemVo orderItemVo=new OrderItemVo();
            orderItemVo.setOrderNo(orderItem.getOrderNo());
            orderItemVo.setProductId(orderItem.getProductId());
            orderItemVo.setProductName(orderItem.getProductName());
            orderItemVo.setProductImage(orderItem.getProductImage());
            orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
            orderItemVo.setQuantity(orderItem.getQuantity());
            orderItemVo.setTotalPrice(orderItem.getTotalPrice());
            orderItemVo.setCreateTime(DateTimeUtil.dateToStr(orderItem.getCreateTime()));
            orderItemVoList.add(orderItemVo);
        }
        return orderItemVoList;
    }
}
