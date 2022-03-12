package top.hcode.hoj.dao.contest.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import top.hcode.hoj.mapper.ContestPrintMapper;
import top.hcode.hoj.pojo.entity.contest.ContestPrint;
import top.hcode.hoj.dao.contest.ContestPrintEntityService;

/**
 * @Author: Himit_ZH
 * @Date: 2021/9/19 21:05
 * @Description:
 */
@Service
public class ContestPrintEntityServiceImpl extends ServiceImpl<ContestPrintMapper, ContestPrint> implements ContestPrintEntityService {
}