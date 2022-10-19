package top.hcode.hoj.dao.common;

import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.entity.common.File;
import top.hcode.hoj.pojo.vo.ACMContestRankVO;
import top.hcode.hoj.pojo.vo.OIContestRankVO;

import java.util.List;

public interface FileEntityService extends IService<File> {
    int updateFileToDeleteByUidAndType(String uid, String type);

    int updateFileToDeleteByGidAndType(Long gid, String type);

    List<File> queryDeleteAvatarList();

    List<File> queryCarouselFileList();

    List<List<String>> getContestRankExcelHead(List<String> contestProblemDisplayIDList, Boolean isACM);

    List<List<Object>> changeACMContestRankToExcelRowList(List<ACMContestRankVO> acmContestRankVOList,
                                                          List<String> contestProblemDisplayIDList,
                                                          String rankShowName);

    List<List<Object>> changOIContestRankToExcelRowList(List<OIContestRankVO> oiContestRankVOList,
                                                        List<String> contestProblemDisplayIDList,
                                                        String rankShowName);
}
