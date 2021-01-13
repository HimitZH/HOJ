package top.hcode.hoj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import top.hcode.hoj.pojo.entity.File;
import top.hcode.hoj.service.ScheduleService;

import java.util.LinkedList;
import java.util.List;

@Service
@Async
@Slf4j
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private FileServiceImpl fileService;

    @Scheduled(cron = "0/5 * * * * *")
    @Override
    public void deleteAvatar() {
        List<File> files = fileService.queryDeleteAvatarList();
        // 如果查不到，直接结束
        if (files.isEmpty()) {
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        List<Long> idLists = new LinkedList<>();
        for (File file : files) {
            stringBuilder.append(file.getFilePath() + " ");
            idLists.add(file.getId());
        }
        //
        //  WINDOWS系统下删除 DEL + 指令符
        //  /P            删除每一个文件之前提示确认。
        //  /F            强制删除只读文件。
        //  /S            删除所有子目录中的指定的文件。
        //  /Q            安静模式。删除全局通配符时，不要求确认
        //  /A            根据属性选择要删除的文件
        Process process = null;
        try {
            String osName = System.getProperty("os.name");
            String execution;
            // 判断系统，执行不同删除代码
            if ("Windows 10".compareTo(osName) == 0) {
                execution = "cmd.exe /c DEL /F /S /Q " + stringBuilder.toString();
            } else {
                execution = "rm -rf " + stringBuilder.toString();
            }
            process = Runtime.getRuntime().exec(execution);
            process.waitFor();
            int result = process.exitValue();                 //为0表示执行成功，非0表示shell执行出错
            if (result != 0) {
                log.error("头像删除异常----------------->Shell执行失败");
            } else {
                boolean isSuccess = fileService.removeByIds(idLists);
                if (!isSuccess) {
                    log.error("数据库file表删除头像数据失败----------------->sql语句执行失败");
                }
            }

        } catch (Exception e) {
            log.error("头像删除异常--------------------->{}", e.getMessage());
        } finally {
            process.destroy();
        }
        // del
    }
}
