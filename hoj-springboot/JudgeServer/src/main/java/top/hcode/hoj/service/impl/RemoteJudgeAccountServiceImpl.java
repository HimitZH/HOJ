package top.hcode.hoj.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.dao.RemoteJudgeAccountMapper;
import top.hcode.hoj.pojo.entity.judge.RemoteJudgeAccount;
import top.hcode.hoj.service.RemoteJudgeAccountService;


/**
 * @Author: Himit_ZH
 * @Date: 2021/5/18 17:46
 * @Description:
 */
@Service
public class RemoteJudgeAccountServiceImpl extends ServiceImpl<RemoteJudgeAccountMapper, RemoteJudgeAccount> implements RemoteJudgeAccountService {
}