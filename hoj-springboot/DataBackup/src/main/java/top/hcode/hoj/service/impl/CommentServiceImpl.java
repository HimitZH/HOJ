package top.hcode.hoj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import top.hcode.hoj.pojo.entity.Comment;
import top.hcode.hoj.dao.CommentMapper;
import top.hcode.hoj.pojo.entity.Contest;
import top.hcode.hoj.pojo.entity.Reply;
import top.hcode.hoj.pojo.entity.UserInfo;
import top.hcode.hoj.pojo.vo.CommentsVo;
import top.hcode.hoj.service.CommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.utils.Constants;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private ContestServiceImpl contestService;

    @Autowired
    private ContestRecordServiceImpl contestRecordService;

    @Autowired
    private ReplyServiceImpl replyService;

    @Override
    public IPage<CommentsVo> getCommentList(int limit, int currentPage, Long cid, Integer did, Boolean isRoot, String uid) {
        //新建分页
        Page<CommentsVo> page = new Page<>(currentPage, limit);

        if (cid != null) {
            Contest contest = contestService.getById(cid);

            boolean onlyMineAndAdmin = contest.getStatus().equals(Constants.Contest.STATUS_RUNNING.getCode())
                    && !isRoot && !contest.getUid().equals(uid);
            if (onlyMineAndAdmin) { // 自己和比赛管理者评论可看
                List<UserInfo> superAdminList = contestRecordService.getSuperAdminList();
                List<String> myAndAdminUidList = superAdminList.stream().map(UserInfo::getUuid).collect(Collectors.toList());
                myAndAdminUidList.add(uid);
                myAndAdminUidList.add(contest.getUid());
                return commentMapper.getCommentList(page, cid, did, true, myAndAdminUidList);
            }

        }
        return commentMapper.getCommentList(page, cid, did, false, null);
    }

    @Override
    public List<Reply> getAllReplyByCommentId(Long cid, String uid, Boolean isRoot, Integer commentId) {
        QueryWrapper<Reply> replyQueryWrapper = new QueryWrapper<>();
        replyQueryWrapper.eq("comment_id", commentId);

        if (cid != null) {
            Contest contest = contestService.getById(cid);
            boolean onlyMineAndAdmin = contest.getStatus().equals(Constants.Contest.STATUS_RUNNING.getCode())
                    && !isRoot && !contest.getUid().equals(uid);
            if (onlyMineAndAdmin) { // 自己和比赛管理者评论可看
                List<UserInfo> superAdminList = contestRecordService.getSuperAdminList();
                List<String> myAndAdminUidList = superAdminList.stream().map(UserInfo::getUuid).collect(Collectors.toList());
                myAndAdminUidList.add(uid);
                myAndAdminUidList.add(contest.getUid());
                replyQueryWrapper.in("from_uid", myAndAdminUidList);
            }

        }
        replyQueryWrapper.orderByDesc("gmt_create");
        return replyService.list(replyQueryWrapper);
    }
}
