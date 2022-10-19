package top.hcode.hoj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import top.hcode.hoj.pojo.entity.msg.AdminSysNotice;
import top.hcode.hoj.pojo.vo.AdminSysNoticeVO;

@Mapper
@Repository
public interface AdminSysNoticeMapper extends BaseMapper<AdminSysNotice> {
    IPage<AdminSysNoticeVO> getAdminSysNotice(Page<AdminSysNoticeVO> page, @Param("type") String type);
}
