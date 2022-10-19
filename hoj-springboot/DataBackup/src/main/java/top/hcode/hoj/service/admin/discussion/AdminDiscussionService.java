package top.hcode.hoj.service.admin.discussion;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.discussion.Discussion;
import top.hcode.hoj.pojo.entity.discussion.DiscussionReport;
import top.hcode.hoj.pojo.vo.DiscussionReportVO;

import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/9 16:03
 * @Description:
 */
public interface AdminDiscussionService {

    public CommonResult<Void> updateDiscussion(Discussion discussion);

    public CommonResult<Void> removeDiscussion(List<Integer> didList);

    public CommonResult<IPage<DiscussionReportVO>> getDiscussionReport(Integer limit, Integer currentPage);

    public CommonResult<Void> updateDiscussionReport(DiscussionReport discussionReport);
}