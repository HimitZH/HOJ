package top.hcode.hoj.service.common.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.hcode.hoj.dao.FileMapper;
import top.hcode.hoj.pojo.entity.common.File;
import top.hcode.hoj.pojo.vo.ACMContestRankVo;
import top.hcode.hoj.pojo.vo.OIContestRankVo;
import top.hcode.hoj.service.common.FileService;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2021/1/11 14:05
 * @Description:
 */
@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, File> implements FileService {
    @Autowired
    private FileMapper fileMapper;

    @Override
    public int updateFileToDeleteByUidAndType(String uid, String type) {
        return fileMapper.updateFileToDeleteByUidAndType(uid, type);
    }

    @Override
    public List<File> queryDeleteAvatarList() {
        return fileMapper.queryDeleteAvatarList();
    }

    @Override
    public List<File> queryCarouselFileList(){
        return fileMapper.queryCarouselFileList();
    }

    @Override
    public List<List<String>> getContestRankExcelHead(List<String> contestProblemDisplayIDList, Boolean isACM) {
        List<List<String>> headList = new LinkedList<>();
        List<String> head0 = new LinkedList<>();
        head0.add("User ID");
        List<String> head1 = new LinkedList<>();
        head1.add("Username");
        List<String> head2 = new LinkedList<>();
        head2.add("Real Name");

        headList.add(head0);
        headList.add(head1);
        headList.add(head2);

        List<String> head3 = new LinkedList<>();
        if (isACM) {
            head3.add("AC");
            List<String> head4 = new LinkedList<>();
            head4.add("Total Submission");
            List<String> head5 = new LinkedList<>();
            head5.add("Total Penalty Time");
            headList.add(head3);
            headList.add(head4);
            headList.add(head5);
        } else {
            head3.add("Total Score");
            headList.add(head3);
        }

        // 添加题目头
        for (String displayID : contestProblemDisplayIDList) {
            List<String> tmp = new LinkedList<>();
            tmp.add(displayID);
            headList.add(tmp);
        }
        return headList;
    }

    @Override
    public List<List<Object>> changeACMContestRankToExcelRowList(List<ACMContestRankVo> acmContestRankVoList, List<String> contestProblemDisplayIDList) {
        List<List<Object>> allRowDataList = new LinkedList<>();
        for (ACMContestRankVo acmContestRankVo : acmContestRankVoList) {
            List<Object> rowData = new LinkedList<>();
            rowData.add(acmContestRankVo.getUid());
            rowData.add(acmContestRankVo.getUsername());
            rowData.add(acmContestRankVo.getRealname());
            rowData.add(acmContestRankVo.getAc());
            rowData.add(acmContestRankVo.getTotal());
            rowData.add(acmContestRankVo.getTotalTime());
            HashMap<String, HashMap<String, Object>> submissionInfo = acmContestRankVo.getSubmissionInfo();
            for (String displayID : contestProblemDisplayIDList) {
                HashMap<String, Object> problemInfo = submissionInfo.getOrDefault(displayID, null);
                if (problemInfo != null) { // 如果是有提交记录的
                    boolean isAC = (boolean) problemInfo.getOrDefault("isAC", false);
                    String info;
                    if (isAC) {
                        info = "AC(-" + problemInfo.getOrDefault("errorNum", 0) + ")";
                    } else {
                        info = "WA(-" + problemInfo.getOrDefault("errorNum", 0) + ")";
                    }
                    rowData.add(info);
                } else {
                    rowData.add("");
                }
            }
            allRowDataList.add(rowData);
        }
        return allRowDataList;
    }

    @Override
    public List<List<Object>> changOIContestRankToExcelRowList(List<OIContestRankVo> oiContestRankVoList, List<String> contestProblemDisplayIDList) {
        List<List<Object>> allRowDataList = new LinkedList<>();
        for (OIContestRankVo oiContestRankVo : oiContestRankVoList) {
            List<Object> rowData = new LinkedList<>();
            rowData.add(oiContestRankVo.getUid());
            rowData.add(oiContestRankVo.getUsername());
            rowData.add(oiContestRankVo.getRealname());
            rowData.add(oiContestRankVo.getTotalScore());
            HashMap<String, Integer> submissionInfo = oiContestRankVo.getSubmissionInfo();
            for (String displayID : contestProblemDisplayIDList) {
                Integer score = submissionInfo.getOrDefault(displayID, null);
                if (score != null) { // 如果是有提交记录的就写最后一次提交的分数，没有的就写空
                    rowData.add(score);
                } else {
                    rowData.add("");
                }
            }
            allRowDataList.add(rowData);
        }
        return allRowDataList;
    }
}