package top.hcode.hoj.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;
import top.hcode.hoj.pojo.entity.File;

import java.util.List;

public interface FileService extends IService<File> {
    int updateFileToDeleteByUidAndType(String uid,String type);

    List<File> queryDeleteAvatarList();
}
