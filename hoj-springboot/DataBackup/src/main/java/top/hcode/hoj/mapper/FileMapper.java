package top.hcode.hoj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;
import top.hcode.hoj.pojo.entity.common.File;

import java.util.List;

@Mapper
@Repository
public interface FileMapper extends BaseMapper<File> {

    @Update("UPDATE `file` SET `delete` = 1 WHERE `uid` = #{uid} AND `type` = #{type}")
    int updateFileToDeleteByUidAndType(@Param("uid") String uid, @Param("type") String type);

    @Update("UPDATE `file` SET `delete` = 1 WHERE `gid` = #{gid} AND `type` = #{type}")
    int updateFileToDeleteByGidAndType(@Param("gid") Long gid, @Param("type") String type);

    @Select("select * from file where (type = 'avatar' AND `delete` = true)")
    List<File> queryDeleteAvatarList();


    @Select("select * from file where (type = 'carousel')")
    List<File> queryCarouselFileList();


}
