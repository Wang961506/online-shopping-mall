package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * @author ZheWang
 * @create 2020-07-13 17:54
 */
@Service("iUserService")
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;
    @Override
    public ServerResponse<User> login(String username, String password) {
        int resultcount = userMapper.checkUsername(username);
        if(resultcount==0)
        {
            return ServerResponse.createByErrorMessage("用户名不存在");
        }
        String md5password=MD5Util.MD5EncodeUtf8(password);
        //todo 密码登录MD5
        User user = userMapper.selectLogin(username, md5password);
        if(user==null){
            return ServerResponse.createByErrorMessage("密码错误");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登陆成功",user);


    }

    @Override
    public ServerResponse<String> register(User user) {
        ServerResponse<String> response1 = this.checkValid(user.getUsername(), Const.USERNAME);
        if(!response1.isSuccess()){
            return response1;
        }
        response1 = this.checkValid(user.getEmail(), Const.EMAIL);
        if(!response1.isSuccess()){
            return response1;
        }

        user.setRole(Const.Role.ROLE_CUSTOMER);
        //MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
       int resultcount = userMapper.insert(user);
       if(resultcount==0){
           return ServerResponse.createByErrorMessage("注册失败");
       }
        return ServerResponse.createBySuccessMessage("注册成功");
    }

    @Override
    public ServerResponse<String> checkValid(String str, String type) {//检验是否存在，不存在返回校验成功的消息
        if(org.apache.commons.lang3.StringUtils.isNoneBlank(type)){
            //校验
            if(Const.USERNAME.equals(type)){
                int resultCount=userMapper.checkUsername(str);
                if(resultCount>0)
                    return ServerResponse.createByErrorMessage("用户名已存在");
            }
            if(Const.EMAIL.equals(type)){
                int resultCount=userMapper.checkEmail(str);
                if(resultCount>0)
                    return ServerResponse.createByErrorMessage("email已存在");
            }
        }
        else {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        return ServerResponse.createBySuccessMessage("校验成功");
    }

    public ServerResponse selectQuestion(String username){
        ServerResponse<String> response = checkValid(username, Const.USERNAME);
        if (response.isSuccess()) {
            //用户不存在
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String question = userMapper.selectQuestionByUsername(username);
        if(org.apache.commons.lang3.StringUtils.isNoneBlank(question)){
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByErrorMessage("密码找回的问题是空的");

    }

    public ServerResponse<String> checkAnswer(String username,String question,String answer){
        int resultCount = userMapper.checkAnswer(username, question, answer);
        if(resultCount>0)
        {
            //证明问题和问题答案是这个用户的
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey("token_"+username,forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("问题的答案错误");

    }

    public ServerResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken){
        if(org.apache.commons.lang3.StringUtils.isBlank(forgetToken)){
            return ServerResponse.createByErrorMessage("参数错误，Token需要传递");
        }
        ServerResponse<String> response = checkValid(username, Const.USERNAME);
        if (response.isSuccess()) {
            //用户不存在
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX+ username);
        if(org.apache.commons.lang3.StringUtils.isBlank(token)){
            return ServerResponse.createByErrorMessage("token无效或者过期");
        }
        if(org.apache.commons.lang3.StringUtils.equals(forgetToken,token)){
            String md5password=MD5Util.MD5EncodeUtf8(passwordNew);
            int rowCount=userMapper.updatePasswordByUsername(username,md5password);
            if(rowCount>0)
                return ServerResponse.createBySuccessMessage("修改密码成功");
            else {
                return ServerResponse.createByErrorMessage("token错误，请重新获取重置密码的token");
            }
        }
        return ServerResponse.createByErrorMessage("修改密码失败");
    }

    @Override
    public ServerResponse<String> resetPassword(String passwordOld, String passwordNew, User user) {
        //防止横向越权,要校验一下这个用户的旧密码,一定要指定是这个用户.因为我们会查询一个count(1),如果不指定id,那么结果就是true啦count>0;
        int resultcount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld), user.getId());
        if(resultcount==0){
            return ServerResponse.createByErrorMessage("旧密码错误");
        }
        String md5password = MD5Util.MD5EncodeUtf8(passwordNew);
        user.setPassword(md5password);
        int updatecount = userMapper.updateByPrimaryKeySelective(user);
        if(updatecount>0)
            return ServerResponse.createBySuccessMessage("密码更新成功");
        return ServerResponse.createByErrorMessage("密码更新失败");

    }

    @Override
    public ServerResponse<User> updateInformation(User user) {
        //username不能被更新，email也需要被校验
        int resultCount = userMapper.checkByEmailByUserId(user.getEmail(), user.getId());
        if(resultCount>0)
            return ServerResponse.createByErrorMessage("email已存在,请重新输入email再尝试更新");
        User updateuser=new User();
        updateuser.setId(user.getId());
        updateuser.setEmail(user.getEmail());
        updateuser.setPhone(user.getPhone());
        updateuser.setQuestion(user.getQuestion());
        updateuser.setAnswer(user.getAnswer());
        int updatecount = userMapper.updateByPrimaryKeySelective(updateuser);
        if(updatecount==0)
            return ServerResponse.createByErrorMessage("更新个人信息失败");
        return ServerResponse.createBySuccess("更新个人信息成功",updateuser);

    }

    public ServerResponse<User> getInformation(Integer userId){
        User user=userMapper.selectByPrimaryKey(userId);
        if(user==null)
            return ServerResponse.createByErrorMessage("找不到当前用户");
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }

    @Override
    public ServerResponse checkAdminRole(User user) {
        if(user!=null && user.getRole().intValue()== Const.Role.ROLE_ADMIN){
            return ServerResponse.createBySuccess();
        }
        else
            return ServerResponse.createByError();
    }


}
