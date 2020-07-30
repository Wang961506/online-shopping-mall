package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * @author ZheWang
 * @create 2020-07-19 22:23
 */
@Service("iFileService")
public class FileServiceImpl implements IFileService {
    private Logger logger= LoggerFactory.getLogger(FileServiceImpl.class);

    public String upload(MultipartFile file,String path){
        String fileName=file.getOriginalFilename();
        String fileExtensionName=fileName.substring(fileName.lastIndexOf(".")+1);
        String newFileName= UUID.randomUUID().toString()+"."+fileExtensionName;
        logger.info("文件开始上传，文件名为{},上传路径为{},新文件名为{}",fileName,path,newFileName);
        File filedir=new File(path);
        if(!filedir.exists()){
            filedir.setWritable(true);
            filedir.mkdirs();
        }
        File uploadFile=new File(path,newFileName);
        try {
            file.transferTo(uploadFile);
            // 将uploadFile文件上传到FTP服务器上
            FTPUtil.uploadFile(Lists.newArrayList(uploadFile));
            //todo 上传完以后，删除upload下面的文件
            uploadFile.delete();

        } catch (IOException e) {
            logger.error("上传失败",e);
        }
        return uploadFile.getName();
    }
}
