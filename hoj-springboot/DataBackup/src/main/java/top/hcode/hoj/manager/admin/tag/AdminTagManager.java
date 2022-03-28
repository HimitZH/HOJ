package top.hcode.hoj.manager.admin.tag;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.pojo.entity.problem.Tag;
import top.hcode.hoj.dao.problem.TagEntityService;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/9 17:47
 * @Description:
 */
@Component
public class AdminTagManager {

    @Autowired
    private TagEntityService tagEntityService;

    public Tag addProblem(Tag tag) throws StatusFailException {
        QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
        tagQueryWrapper.eq(tag.getGid() != null, "gid", tag.getGid())
                .eq("name", tag.getName())
                .eq("oj", tag.getOj());
        Tag existTag = tagEntityService.getOne(tagQueryWrapper, false);

        if (existTag != null) {
            throw new StatusFailException("该标签名称已存在！请勿重复添加！");
        }

        boolean isOk = tagEntityService.save(tag);
        if (!isOk) {
            throw new StatusFailException("添加失败");
        }
        return tag;
    }

    public void updateTag(Tag tag) throws StatusFailException {
        boolean isOk = tagEntityService.updateById(tag);
        if (!isOk) {
            throw new StatusFailException("更新失败");
        }
    }

    public void deleteTag(Long tid) throws StatusFailException {
        boolean isOk = tagEntityService.removeById(tid);
        if (!isOk) {
            throw new StatusFailException("删除失败");
        }
    }
}