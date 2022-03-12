package top.hcode.hoj.service.admin.tag;

import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.problem.Tag;

public interface AdminTagService {

    public CommonResult<Tag> addProblem(Tag tag);

    public CommonResult<Void> updateTag(Tag tag);

    public CommonResult<Void> deleteTag(Long tid);
}
