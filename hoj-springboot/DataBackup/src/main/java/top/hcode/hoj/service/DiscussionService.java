package top.hcode.hoj.service;

import com.baomidou.mybatisplus.extension.service.IService;

import top.hcode.hoj.pojo.entity.Discussion;
import top.hcode.hoj.pojo.vo.DiscussionVo;

public interface DiscussionService extends IService<Discussion> {
    DiscussionVo getDiscussion(Integer did,String uid);
}
