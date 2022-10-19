package top.hcode.hoj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import top.hcode.hoj.pojo.entity.msg.UserSysNotice;
import top.hcode.hoj.pojo.vo.SysMsgVO;


@Mapper
@Repository
public interface UserSysNoticeMapper extends BaseMapper<UserSysNotice> {

    IPage<SysMsgVO> getSysOrMineNotice(Page<SysMsgVO> page, @Param("uid") String uid, @Param("type") String type);
}
