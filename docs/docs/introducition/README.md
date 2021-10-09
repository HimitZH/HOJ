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
[![QQ Group 598587305](https://img.shields.io/badge/QQ%20Group-598587305-blue)](https://qm.qq.com/cgi-bin/qm/qr?k=WWGBZ5gfDiBZOcpNvM8xnZTfUq7BT4Rs&jump_from=webapi)

## 二、为什么要开发HOJ？

因为这是Himit_ZH的毕业设计，同时也是个人志趣所在，目前HOJ从开始开发到现在快有一年了，凭着兴趣与职责一直在不断维护与更新新功能。

## 三、HOJ的特点

  - 适应：支持手机端，响应式布局

  - 设计：界面简约大方

  - 安全：判题使用 cgroup 隔离用户程序，杜绝卡评测；网站权限控制完善

  - 扩展：支持分布式判题

  - 简单：高度集中网站配置

  - 功能：

    - 支持ACM、OI题目及比赛、

    - 拥有讨论区、题目讨论、比赛讨论、同时拥有站内消息系统
    - 支持testlib的SPJ

  - 多样：支持自身题目数据评测，也支持其它知名OJ（HDU、Codeforces、POJ）题目的爬取与提交



## 四、使用HOJ的学校

主要是OI：首都师范大学附属中学...

主要是ACM：西南科技大学、长春理工大学...

##  五、部分截图

**以下截图页面均支持中英文国际化，点击底部的转换即可全网站转换，包括后台管理，同时浏览器会记住本次选择的语言**

> 首页页面 

![首页](https://img-blog.csdnimg.cn/20210609212151977.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70)



> 首页英文

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210609213403198.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70)



> 站内消息系统

![站内消息系统](https://img-blog.csdnimg.cn/a1a83ff01be84406954537e2ab78d999.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16)



![站内消息系统](https://img-blog.csdnimg.cn/513e7e37f52f48518c2fa1bf14eeea99.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16)

> 题目列表页

![题目列表页](https://img-blog.csdnimg.cn/de80b1573ea04195a45b65e0dc55a308.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16)



> 题目详情页

![题目详情页](https://img-blog.csdnimg.cn/655a9886cf7d4683812c923b9ce2aa2e.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16)

> 比赛列表页

![比赛列表页](https://img-blog.csdnimg.cn/2021060921255349.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70)





**比赛以西南科技大学某届新生赛截图为例**

> 比赛详情首页

![比赛详情页](https://img-blog.csdnimg.cn/50026bde6dd64cd5929b38f8ecc6e72e.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16)

> 比赛题目列表页

![比赛题目列表](https://img-blog.csdnimg.cn/8646fc212b5c47e9b35e60634cfc8d6a.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16)

> 比赛排行榜

![比赛排行榜](https://img-blog.csdnimg.cn/e833d4f53e1c4f2d887bd754aaee35ca.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16)



> 提交列表页

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210609213021223.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70)

> 排行榜

![排行榜](https://img-blog.csdnimg.cn/407ad16361f34b44a282b07af68825e0.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16)



> 公共讨论区

![公共讨论区](https://img-blog.csdnimg.cn/20210513134216723.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70)



> 评论组件

![评论组件](https://img-blog.csdnimg.cn/20210513142826730.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70#pic_center)



> 个人首页

![个人首页](https://img-blog.csdnimg.cn/3f9111cc0767466991ae3d99602b5865.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16)



> 个人设置页

![个人设置](https://img-blog.csdnimg.cn/971566eeac674d388b9f5d6064286e14.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16)





> 管理后台首页

![管理后端](https://img-blog.csdnimg.cn/9b9674c0f30a441bb200a32756f24d2c.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16)



> 部分手机端显示

![手机端](https://img-blog.csdnimg.cn/c7b3648217af4899bedf7f7d804968ba.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_12,color_FFFFFF,t_70,g_se,x_16)



![评论区](https://img-blog.csdnimg.cn/20210509233845230.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70)