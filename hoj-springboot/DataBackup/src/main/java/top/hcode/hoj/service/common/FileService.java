package top.hcode.hoj.service.common;

import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.entity.common.File;
import top.hcode.hoj.pojo.vo.ACMContestRankVo;
import top.hcode.hoj.pojo.vo.OIContestRankVo;

import java.util.List;

public interface FileService extends IService<File> {
    int updateFileToDeleteByUidAndType(String uid, String type);

    List<File> queryDeleteAvatarList();

    List<File> queryCarouselFileList();

    List<List<String>> getContestRankExcelHead(List<String> contestProblemDisplayIDList, Boolean isACM);

    List<List<Object>> changeACMContestRankToExcelRowList(List<ACMContestRankVo> acmContestRankVoList, List<String> contestProblemDisplayIDList);

    List<List<Object>> changOIContestRankToExcelRowList(List<OIContestRankVo> oiContestRankVoList, List<String> contestProblemDisplayIDList);
}
