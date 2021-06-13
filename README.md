# Hcode Online Judge（HOJ）
[![Java](https://img.shields.io/badge/Java-1.8-informational)](http://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.2.6.RELEASE-success)](https://spring.io/projects/spring-boot)
[![SpringCloud Alibaba](https://img.shields.io/badge/Spring%20Cloud%20Alibaba-2.2.1.RELEASE-success)](https://spring.io/projects/spring-cloud-alibaba)
[![MySQL](https://img.shields.io/badge/MySQL-8.0.19-blue)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-5.0.9-red)](https://redis.io/)
[![Nacos](https://img.shields.io/badge/Nacos-1.4.1-%23267DF7)](https://github.com/alibaba/nacos)
[![Vue](https://img.shields.io/badge/Vue-2.6.11-success)](https://cn.vuejs.org/)
[![QQ Group 598587305](https://img.shields.io/badge/QQ%20Group-598587305-blue)](https://qm.qq.com/cgi-bin/qm/qr?k=WWGBZ5gfDiBZOcpNvM8xnZTfUq7BT4Rs&jump_from=webapi)

> 前言

基于前后端分离，分布式架构的在线测评平台（hoj），前端使用vue，后端主要使用springboot，redis，mysql，nacos等技术。

在线Demo：[https://www.hcode.top](https://www.hcode.top)

在线文档：[https://www.hcode.top/docs](https://www.hcode.top/docs/)

Github仓库地址：[https://github.com/HimitZH/HOJ](https://github.com/HimitZH/HOJ)

Gitee仓库地址：[https://gitee.com/himitzh0730/hoj](https://gitee.com/himitzh0730/hoj)

**有任何部署问题或项目bug请发issue或者加QQ群：598587305进行咨询。**

**如果要对本项目进行商业化，请在页面底部的Powered by指向HOJ本仓库地址，顺便点上star收藏本项目对开发者的支持，谢谢。**

> 部署

部署文档：[基于docker-compose部署](https://gitee.com/himitzh0730/hoj-deploy/tree/master)

具体请看部署文档，已安装docker和docker-compose的可以执行以下命令快速部署

```shell
git clone https://gitee.com/himitzh0730/hoj-deploy.git && cd hoj-deploy

cd standAlone && docker-compose up -d
```

根据网速情况，大约十到二十分钟即可安装完毕，全程无需人工干预。

```shell
docker ps # 查看当前运行的容器状态
```

1. 大概初始化启动需要一至两分钟，当看到所有的容器的状态status都为`UP`和`healthy`就代表 OJ 已经启动成功。

   **注意：可能初始化过程中访问网页会出现服务器异常，此为正常现象，因为容器还在初始化中，请耐心等待容器状态为up和healthy，大概需要1~2分钟**

2. 默认超级管理员账号与密码：**root / hoj123456**

   **部署成功后，登录root账号进入后台（`http://ip/admin/conf`）修改邮箱配置，新用户才可以正常注册（因为账号注册与密码等修改操作需要通过邮件发送验证码）邮箱配置可以自行百度，开启使用例如QQ邮箱提供的POP3/SMTP服务：**

   ```
   Host: smtp.qq.com
   Port: 465
   Username: qq邮箱账号
   Password: 开启SMTP服务后生成的随机授权码
   ```

其它设置与参数说明请看[基于docker-compose部署](https://gitee.com/himitzh0730/hoj-deploy/tree/master)

> 更新

请在docker-compose.yml当前文件夹下执行`docker-compose pull`拉取最新镜像，然后重新`docker-compose up -d`即可。

> 上线日记

| 时间       | 内容                                                         | 更新者   |
| ---------- | ------------------------------------------------------------ | -------- |
| 2020-10-26 | 正式开发                                                     | Himit_ZH |
| 2021-04-10 | 首次上线测试                                                 | Himit_ZH |
| 2021-04-15 | 判题调度2.0解决并发问题                                      | Himit_ZH |
| 2021-04-16 | 重构解耦JudgeServer判题逻辑，添加部署文档                    | Himit_ZH |
| 2021-04-19 | 加入rsync实现评测数据同步，修复一些已知的BUG                 | Himit_ZH |
| 2021-04-24 | 加入题目模板，修改页面页脚                                   | Himit_ZH |
| 2021-05-02 | 修复比赛后管理员重判题目导致排行榜失效的问题                 | Himit_ZH |
| 2021-05-09 | 添加公共讨论区，题目讨论区，比赛评论                         | Himit_ZH |
| 2021-05-12 | 添加评论及回复删除，讨论举报，调整显示时间。                 | Himit_ZH |
| 2021-05-16 | 完善权限控制，讨论管理员管理，讨论删除与编辑更新。           | Himit_ZH |
| 2021-05-22 | 更新docker-compose一键部署，修正部分bug                      | Himit_ZH |
| 2021-05-24 | 判题调度乐观锁改为悲观锁                                     | Himit_ZH |
| 2021-05-28 | 增加导入导出题目，增加用户页面的最近登录，开发正式结束，进入维护摸鱼 | Himit_ZH |
| 2021-06-02 | 大更新，完善补充前端页面，修正判题等待超时时间，修补一系列bug | Himit_ZH |
| 2021-06-07 | 修正特殊判题，增加前台i18n                                   | Himit_ZH |
| 2021-06-08 | 添加后台i18n,路由懒加载                                      | Himit_ZH |
| 2021-06-12 | 完善比赛赛制，具体请看在线文档                               | Himit_ZH |
| 2021-06-14 | 完善后台管理员权限控制，恢复CF的vjudge判题                   | Himit_ZH |



**以下截图页面均支持中英文国际化，点击底部的转换即可全网站转换，包括后台管理，同时浏览器会记住本次选择的语言**

> 首页页面 

![首页](https://img-blog.csdnimg.cn/20210609212151977.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70)



> 首页英文

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210609213403198.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70)



> 题目列表页

![题目列表页](https://img-blog.csdnimg.cn/20210609212304243.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70)



> 题目详情页

![题目详情页](https://img-blog.csdnimg.cn/20210609212436191.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70)

> 比赛列表页

![比赛列表页](https://img-blog.csdnimg.cn/2021060921255349.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70)



> 比赛详情首页

![比赛详情页](https://img-blog.csdnimg.cn/2021060921270679.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70)

> 比赛排行榜

![比赛排行榜](https://img-blog.csdnimg.cn/20210609212919197.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70)



> 提交列表页

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210609213021223.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70)

> 排行榜

![hoj9](https://cdn.jsdelivr.net/gh/HimitZH/CDN/images/hoj9.png)



> 公共讨论区

![公共讨论区](https://img-blog.csdnimg.cn/20210513134216723.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70)



> 评论组件

![评论组件](https://img-blog.csdnimg.cn/20210513142826730.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70#pic_center)



> 个人信息页

![个人信息](https://img-blog.csdnimg.cn/20210609213116562.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70)



> 个人设置页

![个人设置页](https://img-blog.csdnimg.cn/202106092132206.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70)





> 管理后台首页

![hoj5](https://cdn.jsdelivr.net/gh/HimitZH/CDN/images/hoj5.png)



> 部分手机端显示

![](https://img-blog.csdnimg.cn/20210509233756882.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70)



![评论区](https://img-blog.csdnimg.cn/20210509233845230.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70)