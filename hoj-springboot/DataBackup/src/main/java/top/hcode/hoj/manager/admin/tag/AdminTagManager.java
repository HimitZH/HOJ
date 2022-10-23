package top.hcode.hoj.manager.admin.tag;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.dao.problem.TagClassificationEntityService;
import top.hcode.hoj.dao.problem.TagEntityService;
import top.hcode.hoj.pojo.entity.problem.Tag;
import top.hcode.hoj.pojo.entity.problem.TagClassification;
import top.hcode.hoj.pojo.vo.UserRolesVO;

import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/9 17:47
 * @Description:
 */
@Component
@Slf4j(topic = "hoj")
public class AdminTagManager {

    @Autowired
    private TagEntityService tagEntityService;

    @Autowired
    private TagClassificationEntityService tagClassificationEntityService;

    public Tag addTag(Tag tag) throws StatusFailException {
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
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");
        log.info("[{}],[{}],tid:[{}],operatorUid:[{}],operatorUsername:[{}]",
                "Admin_Tag", "Delete", tid, userRolesVo.getUid(), userRolesVo.getUsername());
    }

    public List<TagClassification> getTagClassification(String oj) {
        oj = oj.toUpperCase();
        if (oj.equals("ALL")) {
            return tagClassificationEntityService.list();
        } else {
            QueryWrapper<TagClassification> tagClassificationQueryWrapper = new QueryWrapper<>();
            tagClassificationQueryWrapper.eq("oj", oj).orderByAsc("`rank`");
            return tagClassificationEntityService.list(tagClassificationQueryWrapper);
        }
    }

    public TagClassification addTagClassification(TagClassification tagClassification) throws StatusFailException {
        QueryWrapper<TagClassification> tagClassificationQueryWrapper = new QueryWrapper<>();
        tagClassificationQueryWrapper.eq("name", tagClassification.getName())
                .eq("oj", tagClassification.getOj());
        TagClassification existTagClassification = tagClassificationEntityService.getOne(tagClassificationQueryWrapper, false);

        if (existTagClassification != null) {
            throw new StatusFailException("该标签分类名称已存在！请勿重复！");
        }
        boolean isOk = tagClassificationEntityService.save(tagClassification);
        if (!isOk) {
            throw new StatusFailException("添加失败");
        }
        return tagClassification;
    }

    public void updateTagClassification(TagClassification tagClassification) throws StatusFailException {
        boolean isOk = tagClassificationEntityService.updateById(tagClassification);
        if (!isOk) {
            throw new StatusFailException("更新失败");
        }
    }

    public void deleteTagClassification(Long tcid) throws StatusFailException {
        boolean isOk = tagClassificationEntityService.removeById(tcid);
        if (!isOk) {
            throw new StatusFailException("删除失败");
        }
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");
        log.info("[{}],[{}],tcid:[{}],operatorUid:[{}],operatorUsername:[{}]",
                "Admin_Tag_Classification", "Delete", tcid, userRolesVo.getUid(), userRolesVo.getUsername());
    }
}