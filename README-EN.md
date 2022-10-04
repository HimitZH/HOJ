# Hcode Online Judge（HOJ）

![logo](./logo.png)

> An open source online judge system base on SpringBoot, Springcloud Alibaba and Vue.js !

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

[简体中文](./README.md) | English

## Overview

- One click deployment based on **Docker** and **Docke-compose**
- Multi-language support：**C、C++、C#、Python、PyPy、Go、Java、JavaScript、PHP**
- Remote judge support： **HDU、POJ、Codeforces、GYM、AtCoder、SPOJ**
- Perfect evaluation mode：**General 、Special、Interactive 、Self-test、Subtask**
- Perfect contest function：**Star team、 Attention team、 External Rank、Scroll Board**
- **Support group and discussion area function**

|            Online Demo             |                Documents                 |               Github&Gitee               | QQ Group  |
| :--------------------------------: | :--------------------------------------: | :--------------------------------------: | :-------: |
| [https://hdoi.cn](https://hdoi.cn) | [https://docs.hdoi.cn](https://docs.hdoi.cn) | [https://github.com/HimitZH/HOJ](https://github.com/HimitZH/HOJ)  [https://gitee.com/himitzh0730/hoj](https://gitee.com/himitzh0730/hoj) | 598587305 |

## Installation

Please running HOJ on the following platforms:

- **Ubuntu 18.04** and above
- **CentOS Linux release 8.0** and above

And the server configuration requirements for stable operation of HOJ:

- **CPU:  2 cores** and above
- **Memory:  4G** and above

For installation options and troubleshooting tips, see [HOJ Documents](https://docs.hdoi.cn/deploy/docker).



> One click deployment Base On Docker & Docker-compose

If your system already has **Docker** and **Docker-compose** and want to quickly taste the functions of the HOJ, you can deploy it quickly according to the following commands：

```shell
sudo apt-get update && sudo apt-get install -y vim curl git

sudo apt-get update

git clone https://github.com/HimitZH/HOJ-Deploy.git && cd hoj-deploy && cd standAlone

# Change some configuration such as password.
vim .env

sudo docker-compose up -d
```

According to the network speed, the setup can be completed automatically in about 5 to 20 minutes without manual intervention.

Wait for the command execution to complete, and then run `docker ps`. When you see that the status of all the containers does not have `unhealthy` , it means HOJ has started successfully.

## Browser Support

Modern browsers(chrome, firefox) and Internet Explorer 10+, also supports mobile browser.

## Support & Contribution

- If you think HOJ is helpful to you, please give the project a star.
- If you found any bug, please feel free to contact us [QQ Group: 598587305 ](https://qm.qq.com/cgi-bin/qm/qr?k=WWGBZ5gfDiBZOcpNvM8xnZTfUq7BT4Rs&jump_from=webapi) or issue.
- Thanks to everyone that contributes to this project.

## License

[MIT](http://opensource.org/licenses/MIT)

## Screenshots

**Note: you can switch languages at the bottom of the page.  (Chinese & English)**

### 1. Home

![首页英文](https://img-blog.csdnimg.cn/e4982d7f4b0c4a07afa2017b836786f8.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16)

### 2. Station message

> System notification

![站内消息系统](https://img-blog.csdnimg.cn/a1a83ff01be84406954537e2ab78d999.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16)

>Reply to me

![站内消息系统](https://img-blog.csdnimg.cn/513e7e37f52f48518c2fa1bf14eeea99.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16)

### 3. Problem

> Problem List

![题目列表](https://img-blog.csdnimg.cn/d9ba009c757d48b590debe3a409c571f.png)



> Problem Details

![题目详情页](https://img-blog.csdnimg.cn/9f872dc1974f45c389e084f0e31a5217.png)

### 4. Training

> Training List

![训练列表页](https://img-blog.csdnimg.cn/58ac74824fcf4963810beea7ba1203b9.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16)

> Trianing Problem List

![训练题目列表页](https://img-blog.csdnimg.cn/b366a6a628984995b57a49c565a2ec47.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16)

### 5.  Contest

> Contest List

![比赛列表页](https://img-blog.csdnimg.cn/00a0438a576d43edbab676b829a38922.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16)



> Contest Details

![比赛详情页](https://img-blog.csdnimg.cn/50026bde6dd64cd5929b38f8ecc6e72e.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16)

> Contest Problem List

![比赛题目列表](https://img-blog.csdnimg.cn/8646fc212b5c47e9b35e60634cfc8d6a.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16)

> Contest  Rank

- ICPC/ACM 

  ![比赛排行榜](https://img-blog.csdnimg.cn/c50140e3b73d482d82ca6f13f47aa080.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)



- OI / IOI

  ![oi排行榜](https://img-blog.csdnimg.cn/67f6262854bb44efa70c374f1f156166.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)




- Scroll Board

  ![在这里插入图片描述](https://img-blog.csdnimg.cn/8f8258babd3f43f78802144e7ecf18fe.png)

### 6. Submission

> Submission List

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210609213021223.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70)

> Submission Details

![在这里插入图片描述](https://img-blog.csdnimg.cn/4256087c5363478d9d4ff4631ebc178e.png)

### 7. Rank

![排行榜](https://img-blog.csdnimg.cn/407ad16361f34b44a282b07af68825e0.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16)

### 8. Group

> Group List

![团队列表页](https://img-blog.csdnimg.cn/7988504326c843ef94e937a2b4f32f03.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16)

> Group Details

![团队题目列表页](https://img-blog.csdnimg.cn/2c05e44f5a464381b9a357aff37b0086.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16)



### 9. Disscussion

> Disscussion List

![公共讨论区](https://img-blog.csdnimg.cn/20210513134216723.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70)



> Comment

![评论组件](https://img-blog.csdnimg.cn/20210513142826730.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70#pic_center)

### 10. User Info

> User Home

![个人首页](https://img-blog.csdnimg.cn/7d3e99dbc6fe4739a0720fcc019b2b6e.png)



> Change User Info

![个人设置](https://img-blog.csdnimg.cn/971566eeac674d388b9f5d6064286e14.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16)



### 11. Admin

> Admin Home Page

![管理后端](https://img-blog.csdnimg.cn/9b9674c0f30a441bb200a32756f24d2c.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16)



### 12. Mobile

![手机端](https://img-blog.csdnimg.cn/c7b3648217af4899bedf7f7d804968ba.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_12,color_FFFFFF,t_70,g_se,x_16)



![评论区](https://img-blog.csdnimg.cn/20210509233845230.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70)
