package top.hcode.hoj.dao.common;

import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.entity.common.File;
import top.hcode.hoj.pojo.vo.ACMContestRankVo;
import top.hcode.hoj.pojo.vo.OIContestRankVo;

import java.util.List;

public interface FileEntityService extends IService<File> {
    int updateFileToDeleteByUidAndType(String uid, String type);

    int updateFileToDeleteByGidAndType(Long gid, String type);

    List<File> queryDeleteAvatarList();

    List<File> queryCarouselFileList();

    List<List<String>> getContestRankExcelHead(List<String> contestProblemDisplayIDList, Boolean isACM);

    List<List<Object>> changeACMContestRankToExcelRowList(List<ACMContestRankVo> acmContestRankVoList,
                                                          List<String> contestProblemDisplayIDList,
                                                          String rankShowName);

    List<List<Object>> changOIContestRankToExcelRowList(List<OIContestRankVo> oiContestRankVoList,
                                                        List<String> contestProblemDisplayIDList,
                                                        String rankShowName);
}
