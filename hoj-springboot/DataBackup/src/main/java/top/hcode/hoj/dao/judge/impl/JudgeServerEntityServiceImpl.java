package top.hcode.hoj.dao.judge.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import top.hcode.hoj.mapper.JudgeServerMapper;

import top.hcode.hoj.pojo.entity.judge.JudgeServer;
import top.hcode.hoj.dao.judge.JudgeServerEntityService;

/**
 * @Author: Himit_ZH
 * @Date: 2021/4/15 11:27
 * @Description:
 */
@Service
public class JudgeServerEntityServiceImpl extends ServiceImpl<JudgeServerMapper, JudgeServer> implements JudgeServerEntityService {

}