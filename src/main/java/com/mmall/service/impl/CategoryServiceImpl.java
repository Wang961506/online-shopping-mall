package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import net.sf.jsqlparser.schema.Server;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;

/**
 * @author ZheWang
 * @create 2020-07-18 18:46
 */
@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {
    private Logger logger= LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    private CategoryMapper categoryMapper;

    public ServerResponse addCategory(String categoryName,Integer parentId){
        if(parentId==null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("添加产品种类参数失败");
        }
        Category newcategory=new Category();
        newcategory.setParentId(parentId);
        newcategory.setName(categoryName);
        newcategory.setStatus(true);
        int count = categoryMapper.insert(newcategory);
        if(count>0){
            return ServerResponse.createBySuccessMessage("添加产品成功");
        }
        return ServerResponse.createByErrorMessage("添加产品失败");

    }

    public ServerResponse updateCategory(Integer categoryId,String categoryName){
        if(categoryId==null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("更新产品种类参数失败");
        }
        Category category=new Category();
        category.setId(categoryId);
        category.setName(categoryName);
        int rowCount=categoryMapper.updateByPrimaryKeySelective(category);
        if(rowCount>0){
            return ServerResponse.createBySuccess("更新品类名字成功");
        }
        return ServerResponse.createByErrorMessage("更新品类名字失败");

    }

    public ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId){
        List<Category> categoryList=categoryMapper.selectCategoryChildrenByParentId(categoryId);
        if(CollectionUtils.isEmpty(categoryList)){
            logger.info("未找到当前分类的子分类");
        }
        return  ServerResponse.createBySuccess(categoryList);
    }


    public ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId){
        Set<Category> categorySet= Sets.newHashSet();
        findChildCategory(categorySet, categoryId);
        List<Integer> categoryList= Lists.newArrayList();
        if(categoryId!=null){
            for(Category categoryItem:categorySet){
                categoryList.add(categoryItem.getId());
            }
        }
        return ServerResponse.createBySuccess(categoryList);

    }

    public Set<Category> findChildCategory(Set<Category> categorySet, Integer categoryId){
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if(category!=null){
            categorySet.add(category);
        }
        //查找子节点
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        for(Category simpleCategory:categoryList){
            findChildCategory(categorySet,simpleCategory.getId());
        }
        return categorySet;
    }
}
