package top.hcode.hoj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import top.hcode.hoj.pojo.entity.msg.MsgRemind;
import top.hcode.hoj.pojo.vo.UserMsgVO;
import top.hcode.hoj.pojo.vo.UserUnreadMsgCountVO;

@Mapper
@Repository
public interface MsgRemindMapper extends BaseMapper<MsgRemind> {
    UserUnreadMsgCountVO getUserUnreadMsgCount(@Param("uid") String uid);

    IPage<UserMsgVO> getUserMsg(Page<UserMsgVO> page, @Param("uid") String uid,
                                @Param("action") String action);
}
