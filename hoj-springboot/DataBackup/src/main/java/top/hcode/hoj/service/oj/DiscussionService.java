package top.hcode.hoj.service.oj;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.discussion.Discussion;
import top.hcode.hoj.pojo.entity.discussion.DiscussionReport;
import top.hcode.hoj.pojo.entity.problem.Category;
import top.hcode.hoj.pojo.vo.DiscussionVO;

import java.util.List;

public interface DiscussionService {

    public CommonResult<IPage<Discussion>> getDiscussionList(Integer limit,
                                                            Integer currentPage,
                                                            Integer categoryId,
                                                            String pid,
                                                            Boolean onlyMine,
                                                            String keyword,
                                                            Boolean admin);

    public CommonResult<DiscussionVO>  getDiscussion(Integer did);

    public CommonResult<Void>  addDiscussion(Discussion discussion);

    public CommonResult<Void>  updateDiscussion(Discussion discussion);

    public CommonResult<Void>  removeDiscussion(Integer did);

    public CommonResult<Void>  addDiscussionLike(Integer did, Boolean toLike);

    public CommonResult<List<Category>>  getDiscussionCategory();

    public CommonResult<List<Category>>  upsertDiscussionCategory(List<Category> categoryList);

    public CommonResult<Void>  addDiscussionReport(DiscussionReport discussionReport);

}
