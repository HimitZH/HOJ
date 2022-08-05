package top.hcode.hoj.service.admin.tag;

import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.problem.Tag;
import top.hcode.hoj.pojo.entity.problem.TagClassification;

import java.util.List;

public interface AdminTagService {

    public CommonResult<Tag> addTag(Tag tag);

    public CommonResult<Void> updateTag(Tag tag);

    public CommonResult<Void> deleteTag(Long tid);

    public CommonResult<List<TagClassification>> getTagClassification(String oj);

    public CommonResult<TagClassification> addTagClassification(TagClassification tagClassification);

    public CommonResult<Void> updateTagClassification(TagClassification tagClassification);

    public CommonResult<Void> deleteTagClassification(Long tcid);
}
