package top.hcode.hoj.service.file.impl;

import org.springframework.stereotype.Service;
import top.hcode.hoj.manager.file.UserFileManager;
import top.hcode.hoj.service.file.UserFileService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/10 15:04
 * @Description:
 */
@Service
public class UserFileServiceImpl implements UserFileService {

    @Resource
    private UserFileManager userFileManager;


    @Override
    public void generateUserExcel(String key, HttpServletResponse response) throws IOException {
        userFileManager.generateUserExcel(key, response);
    }
}