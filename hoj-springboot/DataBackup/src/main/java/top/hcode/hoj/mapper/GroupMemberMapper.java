package top.hcode.hoj.mapper;

import top.hcode.hoj.pojo.entity.group.GroupMember;
import top.hcode.hoj.pojo.vo.GroupMemberVO;
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
public interface GroupMemberMapper extends BaseMapper<GroupMember> {
    List<GroupMemberVO> getMemberList(IPage iPage, @Param("keyword") String keyword, @Param("auth") Integer auth, @Param("gid") Long gid);
    List<GroupMemberVO> getApplyList(IPage iPage, @Param("keyword") String keyword, @Param("auth") Integer auth, @Param("gid") Long gid);
}
