package top.hcode.hoj.mapper;

import top.hcode.hoj.pojo.entity.common.Announcement;
import top.hcode.hoj.pojo.vo.AnnouncementVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author: LengYun
 * @Date: 2022/3/11 13:36
 * @Description:
 */
@Mapper
@Repository
public interface GroupAnnouncementMapper extends BaseMapper<Announcement> {

    List<AnnouncementVO> getAnnouncementList(IPage iPage, @Param("gid") Long gid);

    List<AnnouncementVO> getAdminAnnouncementList(IPage iPage, @Param("gid") Long gid);

}
