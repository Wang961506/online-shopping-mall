package com.mmall.util;

import java.math.BigDecimal;

/**
 * @author ZheWang
 * @create 2020-07-23 13:47
 */
public class BigDecimalUtil {
    public BigDecimalUtil() {
    }
    public static BigDecimal add(double v1,double v2){
        BigDecimal b1=new BigDecimal(Double.toString(v1));
        BigDecimal result = b1.add(new BigDecimal(Double.toString(v2)));
        return result;
    }

    public static BigDecimal substract(double v1,double v2){
        BigDecimal b1=new BigDecimal(Double.toString(v1));
        BigDecimal result = b1.subtract(new BigDecimal(Double.toString(v2)));
        return result;
    }

    public static BigDecimal multiply(double v1,double v2){
        BigDecimal b1=new BigDecimal(Double.toString(v1));
        BigDecimal result = b1.multiply(new BigDecimal(Double.toString(v2)));
        return result;
    }
    public static BigDecimal divide(double v1,double v2){
        BigDecimal b1=new BigDecimal(Double.toString(v1));
        BigDecimal result = b1.divide(new BigDecimal(Double.toString(v2)),2,BigDecimal.ROUND_HALF_UP);//保留两位小数+四舍五入
        return result;
        //
    }
}
