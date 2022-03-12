package top.hcode.hoj.service.admin.system;

import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.user.Session;

import java.util.Map;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/9 22:19
 * @Description:
 */
public interface DashboardService {

    public CommonResult<Session> getRecentSession();

    public CommonResult<Map<Object,Object>> getDashboardInfo();
}