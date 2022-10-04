# 前言

## 一、什么是HOJ？

HOJ，全称 Hcode Online Judge，是基于（springcloud+vue）前后端分离，分布式架构的在线测评系统。

[![Java](https://img.shields.io/badge/Java-1.8-informational)](http://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.2.6.RELEASE-success)](https://spring.io/projects/spring-boot)
[![SpringCloud Alibaba](https://img.shields.io/badge/Spring%20Cloud%20Alibaba-2.2.1.RELEASE-success)](https://spring.io/projects/spring-cloud-alibaba)
[![MySQL](https://img.shields.io/badge/MySQL-8.0.19-blue)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-5.0.9-red)](https://redis.io/)
[![Nacos](https://img.shields.io/badge/Nacos-1.4.2-%23267DF7)](https://github.com/alibaba/nacos)
[![Vue](https://img.shields.io/badge/Vue-2.6.11-success)](https://cn.vuejs.org/)
[![Github Star](https://img.shields.io/github/stars/HimitZH/HOJ?style=social)](https://github.com/HimitZH/HOJ)
[![Gitee Star](https://gitee.com/himitzh0730/hoj/badge/star.svg)](https://gitee.com/himitzh0730/hoj)
[![QQ Group 598587305](https://img.shields.io/badge/QQ%20Group-598587305-blue)](https://qm.qq.com/cgi-bin/qm/qr?k=WWGBZ5gfDiBZOcpNvM8xnZTfUq7BT4Rs&jump_from=webapi)

## 二、为什么要开发HOJ？

因为这是Himit_ZH的毕业设计，同时也是个人志趣所在，目前HOJ从开始开发到现在已经有一年了，凭着兴趣与职责一直在不断维护与更新新功能。

## 三、HOJ的特点
:::tip
  - 适应：支持手机端，响应式布局

  - 设计：界面简约大方

  - 安全：判题使用 cgroup 隔离用户程序，杜绝卡评测；网站权限控制完善

  - 扩展：支持分布式判题

  - 简单：高度集中网站配置

  - 功能：
    - 支持ACM、OI题目及比赛，比赛拥有外榜、打星队伍、关注队伍、滚榜等功能
    - 拥有讨论区、题目讨论、比赛讨论、同时拥有站内消息系统
    - 支持私有训练、公开训练（题单）
    - 支持私有团队、公开团队、保护团队
    - 支持testlib的特殊评测、交互评测、子任务评测
    - 支持在线自测调试

  - 多样：支持自身题目数据评测，也支持其它知名OJ（HDU、Codeforces、GYM、POJ、AtCoder、SPOJ）题目的爬取与提交
:::


## 四、使用HOJ的学校

主要是OI：首都师范大学附属中学...

主要是ACM：西南科技大学、长春理工大学、兰州大学...

##  五、部分截图

:::warning
**以下截图页面均支持中英文国际化，点击底部的转换即可全网站转换，包括后台管理，同时浏览器会记住本次选择的语言**
:::

### 1. 首页

> 首页页面 

![首页](https://img-blog.csdnimg.cn/ab90fd3b4b904972b728013c9f2b776b.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16)



> 首页英文

![首页英文](https://img-blog.csdnimg.cn/e4982d7f4b0c4a07afa2017b836786f8.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16)

### 2. 站内消息

> 站内消息系统

![站内消息系统](https://img-blog.csdnimg.cn/a1a83ff01be84406954537e2ab78d999.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16)



![站内消息系统](https://img-blog.csdnimg.cn/513e7e37f52f48518c2fa1bf14eeea99.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16)

### 3. 题目

> 题目列表页

![题目列表页](https://img-blog.csdnimg.cn/0ee61f329e094592b0a0cff55d12b404.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16)



> 题目详情页

![题目详情页](https://img-blog.csdnimg.cn/9f872dc1974f45c389e084f0e31a5217.png)

### 4. 训练

> 训练列表页

![训练列表页](https://img-blog.csdnimg.cn/58ac74824fcf4963810beea7ba1203b9.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16)

> 训练题目列表页

![训练题目列表页](https://img-blog.csdnimg.cn/b366a6a628984995b57a49c565a2ec47.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16)

### 5.  比赛

> 比赛列表页

![比赛列表页](https://img-blog.csdnimg.cn/00a0438a576d43edbab676b829a38922.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16)





**比赛以西南科技大学某届新生赛截图为例**

> 比赛详情首页

![比赛详情页](https://img-blog.csdnimg.cn/50026bde6dd64cd5929b38f8ecc6e72e.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16)

> 比赛题目列表页

![比赛题目列表](https://img-blog.csdnimg.cn/8646fc212b5c47e9b35e60634cfc8d6a.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16)

> 比赛排行榜

- ACM比赛

  ![比赛排行榜](https://img-blog.csdnimg.cn/c50140e3b73d482d82ca6f13f47aa080.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)



- OI比赛

  ![oi排行榜](https://img-blog.csdnimg.cn/67f6262854bb44efa70c374f1f156166.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)




- 滚榜

  ![在这里插入图片描述](https://img-blog.csdnimg.cn/8f8258babd3f43f78802144e7ecf18fe.png)



### 6. 评测

> 提交列表页

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210609213021223.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70)

### 7. 排行榜

> 排行榜

![排行榜](https://img-blog.csdnimg.cn/407ad16361f34b44a282b07af68825e0.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16)

### 8. 团队

![团队列表页](https://img-blog.csdnimg.cn/7988504326c843ef94e937a2b4f32f03.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16)

![团队题目列表页](https://img-blog.csdnimg.cn/2c05e44f5a464381b9a357aff37b0086.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16)



### 9. 讨论

> 公共讨论区

![公共讨论区](https://img-blog.csdnimg.cn/20210513134216723.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70)



> 评论组件

![评论组件](https://img-blog.csdnimg.cn/20210513142826730.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70#pic_center)

### 10. 个人

> 个人首页

![个人首页](https://img-blog.csdnimg.cn/7d3e99dbc6fe4739a0720fcc019b2b6e.png)



> 个人设置页

![个人设置](https://img-blog.csdnimg.cn/971566eeac674d388b9f5d6064286e14.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16)



### 11. 管理后台

> 管理后台首页

![管理后端](https://img-blog.csdnimg.cn/9b9674c0f30a441bb200a32756f24d2c.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16)



### 12. 手机端

> 部分手机端显示

![手机端](https://img-blog.csdnimg.cn/c7b3648217af4899bedf7f7d804968ba.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_12,color_FFFFFF,t_70,g_se,x_16)



![评论区](https://img-blog.csdnimg.cn/20210509233845230.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70)