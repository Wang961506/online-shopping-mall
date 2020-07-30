package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * @author ZheWang
 * @create 2020-07-18 17:47
 */
@Controller
@RequestMapping("/manage/category/")
public class CategoryManagerController {
    @Autowired
    private IUserService iUserService;
    @Autowired
    private ICategoryService iCategoryService;

    @RequestMapping("add_category.do")
    @ResponseBody
    public ServerResponse addCategory(HttpSession session, String categoryName, @RequestParam(value = "parentId", defaultValue = "0") Integer parentId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录");
        }
        //校验是否是管理员
        ServerResponse response = iUserService.checkAdminRole(user);
        if (response.isSuccess()) {
            return iCategoryService.addCategory(categoryName, parentId);
        } else
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");

    }

    @RequestMapping("set_category.do")
    @ResponseBody
    public ServerResponse setCategoryName(HttpSession session, Integer categoryId, String categoryName) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录");
        }
        //校验是否是管理员
        ServerResponse response = iUserService.checkAdminRole(user);
        if (response.isSuccess()) {
            //更新categoryname
            return iCategoryService.updateCategory(categoryId, categoryName);
        } else
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");

    }

    @RequestMapping("get_category.do")
    @ResponseBody
    public ServerResponse getChildrenParallelCategory(HttpSession session,@RequestParam(value = "categoryId",defaultValue = "0") Integer categoryId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录");
        }
        //校验是否是管理员
        ServerResponse response = iUserService.checkAdminRole(user);
        if (response.isSuccess()) {
            //更新categoryname
            return iCategoryService.getChildrenParallelCategory(categoryId);
        } else
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
    }


    @RequestMapping("get_deep_category.do")
    @ResponseBody
    public ServerResponse getCategoryAndDeepChildrenCategory(HttpSession session,@RequestParam(value = "categoryId",defaultValue = "0") Integer categoryId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录");
        }
        //校验是否是管理员
        ServerResponse response = iUserService.checkAdminRole(user);
        if (response.isSuccess()) {
            //查询当前节点的id和递归子节点的id
            return iCategoryService.selectCategoryAndChildrenById(categoryId);

        } else
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");

    }
}
