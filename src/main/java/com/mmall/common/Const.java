package com.mmall.common;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * @author ZheWang
 * @create 2020-07-14 10:54
 */
public class Const {
    public static final String CURRENT_USER="currentUser";
    public static final String EMAIL="email";
    public static final String USERNAME="username";

    public interface Role{
        int ROLE_CUSTOMER=0;
        int ROLE_ADMIN=1;
    }

    public interface ProductListOrderBy{
        Set<String> PRICE_ASC_DESC= Sets.newHashSet("price_desc","price_asc");
    }

    public enum ProductStatus{
        ON_SALE(1,"在售");


        private final int code;
        private final String value;

        ProductStatus(int code, String value) {
            this.code = code;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }
    }
    public interface Cart{
        int CHECKED=1;
        int UN_CHECKED=0;

        String LIMIT_NUM_FAIL="LIMIT_NUM_FAIL";
        String LIMIT_NUM_SUCCESS="LIMIT_NUM_SUCCESS";
    }

    public enum OrderStatus{
        CANCLED(0,"已取消"),
        NO_PAY(10,"未支付"),
        PAID(20,"已付款"),
        SHIPPED(40,"已发货"),
        ORDER_SUCCESS(50,"订单完成"),
        ORDER_CLOSE(60,"订单关闭");

        private String value;
        private int code;

        OrderStatus(int code,String value) {
            this.value = value;
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public static OrderStatus getDescByCode(int code){
            for(OrderStatus item:values()){
                if(item.getCode()==code)
                    return item;
            }
            throw new RuntimeException("订单状态有问题");
        }
    }

    public interface AlipayCallback{

        String TRADE_SUCCESS="TRADE_SUCCESS";
        String WAIT_BUYER_PAY="WAIT_BUYER_PAY";
        String RESPONSE_SUCCESS="success";
        String RESPONSE_FAILED="failed";
    }

    public enum PayPlatformEnum{
        ALIPAY(1,"支付宝");


        private String value;
        private int code;

        PayPlatformEnum(int code,String value) {
            this.value = value;
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }
    }

    public enum PaymentTypeEnum{
        ONLINE_PAY(1,"在线支付");

        private String value;
        private int code;

        PaymentTypeEnum(int code,String value) {
            this.value = value;
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public static PaymentTypeEnum getDescByCode(int code){
            for(PaymentTypeEnum item:values()){
                if(item.getCode()==code)
                    return item;
            }
            throw new RuntimeException("没有此类别");
        }
    }




}
