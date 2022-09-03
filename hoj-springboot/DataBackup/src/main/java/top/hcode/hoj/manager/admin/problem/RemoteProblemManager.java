package top.hcode.hoj.manager.admin.problem;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import top.hcode.hoj.crawler.language.LanguageContext;
import top.hcode.hoj.crawler.problem.*;
import top.hcode.hoj.dao.judge.RemoteJudgeAccountEntityService;
import top.hcode.hoj.pojo.entity.judge.RemoteJudgeAccount;
import top.hcode.hoj.pojo.entity.problem.*;
import top.hcode.hoj.dao.problem.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/9 17:33
 * @Description:
 */
@Component
@Slf4j(topic = "hoj")
public class RemoteProblemManager {

    @Autowired
    private ProblemEntityService problemEntityService;

    @Autowired
    private ProblemTagEntityService problemTagEntityService;

    @Autowired
    private TagEntityService tagEntityService;

    @Autowired
    private LanguageEntityService languageEntityService;

    @Autowired
    private ProblemLanguageEntityService problemLanguageEntityService;

    @Autowired
    private RemoteJudgeAccountEntityService remoteJudgeAccountEntityService;


    public ProblemStrategy.RemoteProblemInfo getOtherOJProblemInfo(String OJName, String problemId, String author) throws Exception {

        ProblemStrategy problemStrategy;
        switch (OJName) {
            case "HDU":
                problemStrategy = new HDUProblemStrategy();
                break;
            case "CF":
                problemStrategy = new CFProblemStrategy();
                break;
            case "POJ":
                problemStrategy = new POJProblemStrategy();
                break;
            case "GYM":
                problemStrategy = new GYMProblemStrategy();
                break;
            case "SPOJ":
                problemStrategy = new SPOJProblemStrategy();
                break;
            case "AC":
                problemStrategy = new AtCoderProblemStrategy();
                break;
            default:
                throw new Exception("未知的OJ的名字，暂时不支持！");
        }

        ProblemContext problemContext = new ProblemContext(problemStrategy);
        try {
            return problemContext.getProblemInfo(problemId, author);
        } catch (IllegalStateException e) {
            if (Objects.equals("GYM", OJName)) {
                QueryWrapper<RemoteJudgeAccount> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("oj", "CF");
                List<RemoteJudgeAccount> remoteJudgeAccounts = remoteJudgeAccountEntityService.list(queryWrapper);
                if (!CollectionUtils.isEmpty(remoteJudgeAccounts)) {
                    RemoteJudgeAccount account = remoteJudgeAccounts.get(0);
                    return problemContext.getProblemInfoByLogin(problemId, author, account.getUsername(), account.getPassword());
                }
            }
            return null;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Problem adminAddOtherOJProblem(ProblemStrategy.RemoteProblemInfo remoteProblemInfo, String OJName) {

        Problem problem = remoteProblemInfo.getProblem();
        boolean addProblemResult = problemEntityService.save(problem);
        // 为新的其它oj题目添加对应的language
        QueryWrapper<Language> languageQueryWrapper = new QueryWrapper<>();
        if (OJName.equals("GYM")) {
            languageQueryWrapper.eq("oj", "CF");
        } else {
            languageQueryWrapper.eq("oj", OJName);
        }
        List<Language> OJLanguageList = languageEntityService.list(languageQueryWrapper);
        List<ProblemLanguage> problemLanguageList = new LinkedList<>();
        if (!CollectionUtil.isEmpty(remoteProblemInfo.getLangIdList())) {
            LanguageContext languageContext = new LanguageContext(remoteProblemInfo.getRemoteOJ());
            List<Language> languageList = languageContext.buildLanguageListByIds(OJLanguageList, remoteProblemInfo.getLangIdList());
            for (Language language : languageList) {
                problemLanguageList.add(new ProblemLanguage().setPid(problem.getId()).setLid(language.getId()));
            }
        } else {
            for (Language language : OJLanguageList) {
                problemLanguageList.add(new ProblemLanguage().setPid(problem.getId()).setLid(language.getId()));
            }
        }
        boolean addProblemLanguageResult = problemLanguageEntityService.saveOrUpdateBatch(problemLanguageList);

        boolean addProblemTagResult = true;
        List<Tag> addTagList = remoteProblemInfo.getTagList();

        List<Tag> needAddTagList = new LinkedList<>();

        HashMap<String, Tag> tagFlag = new HashMap<>();

        if (addTagList != null && addTagList.size() > 0) {
            QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
            tagQueryWrapper.eq("oj", OJName);
            List<Tag> tagList = tagEntityService.list(tagQueryWrapper);
            // 已存在的tag不进行添加
            for (Tag hasTag : tagList) {
                tagFlag.put(hasTag.getName().toUpperCase(), hasTag);
            }
            for (Tag tmp : addTagList) {
                Tag tag = tagFlag.get(tmp.getName().toUpperCase());
                if (tag == null) {
                    tmp.setOj(OJName);
                    needAddTagList.add(tmp);
                } else {
                    needAddTagList.add(tag);
                }
            }
            tagEntityService.saveOrUpdateBatch(needAddTagList);

            List<ProblemTag> problemTagList = new LinkedList<>();
            for (Tag tmp : needAddTagList) {
                problemTagList.add(new ProblemTag().setTid(tmp.getId()).setPid(problem.getId()));
            }
            addProblemTagResult = problemTagEntityService.saveOrUpdateBatch(problemTagList);
        } else {
            QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
            tagQueryWrapper.eq("name", OJName);
            Tag OJNameTag = tagEntityService.getOne(tagQueryWrapper, false);
            if (OJNameTag == null) {
                OJNameTag = new Tag();
                OJNameTag.setOj(OJName);
                OJNameTag.setName(OJName);
                tagEntityService.saveOrUpdate(OJNameTag);
            }
            addProblemTagResult = problemTagEntityService.saveOrUpdate(new ProblemTag().setTid(OJNameTag.getId())
                    .setPid(problem.getId()));
        }

        if (addProblemResult && addProblemTagResult && addProblemLanguageResult) {
            return problem;
        } else {
            return null;
        }
    }
}