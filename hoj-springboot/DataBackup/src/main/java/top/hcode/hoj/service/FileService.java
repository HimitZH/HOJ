package top.hcode.hoj.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;
import top.hcode.hoj.pojo.entity.File;

public interface FileService extends IService<File> {
    int updateFileToDeleteByUidAndType(String uid,String type);
}
