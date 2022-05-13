package top.hcode.hoj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import top.hcode.hoj.pojo.entity.discussion.DiscussionReport;
import top.hcode.hoj.pojo.vo.DiscussionReportVo;

@Mapper
@Repository
public interface DiscussionReportMapper extends BaseMapper<DiscussionReport> {

    IPage<DiscussionReportVo> getDiscussionReportList(Page<DiscussionReportVo> page);
}
