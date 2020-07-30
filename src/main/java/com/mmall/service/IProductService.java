package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.vo.ProductDetailVo;

/**
 * @author ZheWang
 * @create 2020-07-19 13:56
 */
public interface IProductService {
    ServerResponse saveOrUpdateProduct(Product product);
    ServerResponse<String> setSaleStatus(Integer productId,Integer status);
    ServerResponse<ProductDetailVo> manageProductDetail(Integer productId);
    ServerResponse<PageInfo> getProductList(int pageNum, int pageSize);
    ServerResponse<PageInfo> searchProject(String productName,Integer productId,int pageNum,int pageSize);
    ServerResponse<ProductDetailVo> getProductDetail(Integer productId);
    ServerResponse<PageInfo> getProductByKeywordAndCategoryId(String keyword,Integer categoryId,Integer pageNum,Integer pageSize,String orderby);
}
