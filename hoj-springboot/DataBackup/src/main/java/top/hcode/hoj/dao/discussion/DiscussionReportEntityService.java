package top.hcode.hoj.dao.discussion;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.entity.discussion.DiscussionReport;
import top.hcode.hoj.pojo.vo.DiscussionReportVO;

public interface DiscussionReportEntityService extends IService<DiscussionReport> {

    IPage<DiscussionReportVO> getDiscussionReportList(Integer limit, Integer currentPage);
}
