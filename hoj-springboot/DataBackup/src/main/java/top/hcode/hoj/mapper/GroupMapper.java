package top.hcode.hoj.mapper;

import top.hcode.hoj.pojo.entity.group.Group;
import top.hcode.hoj.pojo.vo.GroupVO;
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
public interface GroupMapper extends BaseMapper<Group> {
    List<GroupVO> getGroupList(IPage iPage,
                               @Param("keyword") String keyword,
                               @Param("auth") Integer auth,
                               @Param("uid") String uid,
                               @Param("onlyMine") Boolean onlyMine,
                               @Param("isRoot") Boolean isRoot);
}
