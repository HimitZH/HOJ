package top.hcode.hoj.dao.group.impl;

import top.hcode.hoj.dao.group.GroupEntityService;
import top.hcode.hoj.mapper.GroupMapper;
import top.hcode.hoj.pojo.entity.group.Group;
import top.hcode.hoj.pojo.vo.GroupVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: LengYun
 * @Date: 2022/3/11 13:36
 * @Description:
 */
@Service
public class GroupEntityServiceImpl extends ServiceImpl<GroupMapper, Group> implements GroupEntityService {

    @Autowired
    private GroupMapper groupMapper;

    @Override
    public IPage<GroupVo> getGroupList(int limit,
                                       int currentPage,
                                       String keyword,
                                       Integer auth,
                                       String uid,
                                       Boolean onlyMine,
                                       Boolean isRoot) {
        IPage<GroupVo> iPage = new Page<>(currentPage, limit);
        List<GroupVo> groupList = groupMapper.getGroupList(iPage, keyword, auth, uid, onlyMine,isRoot);

        return iPage.setRecords(groupList);
    }
}
