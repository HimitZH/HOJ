package top.hcode.hoj.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.hcode.hoj.dao.FileMapper;
import top.hcode.hoj.pojo.entity.File;
import top.hcode.hoj.service.FileService;

import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2021/1/11 14:05
 * @Description:
 */
@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, File> implements FileService {
    @Autowired
    private FileMapper fileMapper;

    @Override
    public int updateFileToDeleteByUidAndType(String uid, String type) {
        return fileMapper.updateFileToDeleteByUidAndType(uid, type);
    }

    @Override
    public List<File> queryDeleteAvatarList() {
        return fileMapper.queryDeleteAvatarList();
    }
}