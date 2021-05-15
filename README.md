> 前言

基于前后端分离，分布式架构的在线测评平台（hoj），前端使用vue，后端主要使用springboot，redis，mysql，nacos等技术。

在线Demo：[https://www.hcode.top](https://www.hcode.top)

在线文档：[https://www.hcode.top/docs](https://www.hcode.top/docs/)

> 上线日记

| 时间       | 内容                                               | 更新者   |
| ---------- | -------------------------------------------------- | -------- |
| 2020-10-26 | 正式开发                                           | Himit_ZH |
| 2021-04-10 | 首次上线测试                                       | Himit_ZH |
| 2021-04-15 | 判题调度2.0解决并发问题                            | Himit_ZH |
| 2021-04-16 | 重构解耦JudgeServer判题逻辑，添加部署文档          | Himit_ZH |
| 2021-04-19 | 加入rsync实现评测数据同步，修复一些已知的BUG       | Himit_ZH |
| 2021-04-24 | 加入题目模板，修改页面页脚                         | Himit_ZH |
| 2021-05-02 | 修复比赛后管理员重判题目导致排行榜失效的问题       | Himit_ZH |
| 2021-05-09 | 添加公共讨论区，题目讨论区，比赛评论               | Himit_ZH |
| 2021-05-12 | 添加评论及回复删除，讨论举报，调整显示时间。       | Himit_ZH |
| 2021-05-16 | 完善权限控制，讨论管理员管理，讨论删除与编辑更新。 | Himit_ZH |



> 首页页面

![首页](https://img-blog.csdnimg.cn/20210509232352226.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70)



> 题目列表页

![题目列表](https://img-blog.csdnimg.cn/20210509232501952.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70)



> 题目详情页

![题目详情页](https://img-blog.csdnimg.cn/20210509232609398.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70)

> 比赛列表页

![比赛列表](https://img-blog.csdnimg.cn/20210509232701288.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70)



> 比赛详情首页

![比赛详情](https://img-blog.csdnimg.cn/20210509232843932.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70#pic_center)

> 提交列表页

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210513134128914.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70)

> 排行榜

![hoj9](https://cdn.jsdelivr.net/gh/HimitZH/CDN/images/hoj9.png)



> 公共讨论区

![公共讨论区](https://img-blog.csdnimg.cn/20210513134216723.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70)



> 评论组件

![评论组件](https://img-blog.csdnimg.cn/20210513142826730.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70#pic_center)



> 个人信息页

![个人信息](https://img-blog.csdnimg.cn/20210509233300701.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70)



> 个人设置页

![个人设置](https://img-blog.csdnimg.cn/20210509233439791.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70)





> 管理后台首页

![hoj5](https://cdn.jsdelivr.net/gh/HimitZH/CDN/images/hoj5.png)



> 部分手机端显示

![](https://img-blog.csdnimg.cn/20210509233756882.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70)



![评论区](https://img-blog.csdnimg.cn/20210509233845230.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70)