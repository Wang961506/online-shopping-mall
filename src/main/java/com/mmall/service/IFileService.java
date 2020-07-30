package com.mmall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author ZheWang
 * @create 2020-07-19 22:22
 */
public interface IFileService {
    String upload(MultipartFile file, String path);
}
