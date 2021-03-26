# 一、介绍

基于前后端分离，分布式架构的在线测评平台（hoj）

> 当前任务

- [x] 测试HDU判题整套流程
- [x] 修改前端编辑器样式以及md格式转换
- [x] 修复代码编辑器bug
- [x] 测试比赛相关接口，验证权限及数据计算
- [x] 增加codeforce的vj判题
- [ ] 部署判题服务器到云服务器，半正式上线HOJ
- [ ] 完善文档

> 简略介绍

- 前端：
  - [x] 技术以Vue为主，element-ui为主要框架，网站风格样式模仿qdoj
  - [x] 支持手机端，响应式布局
  - [x] 以CodeMirror作为在线代码编辑器
  - [x] 以Mavon-Editor作为富文本编辑器
  - [x] 支持用户头像上传，可选择性获取用户Codeforces分数
  - [x] 定时获取例如Codeforces等其它知名OJ的近期比赛数据
  - [x] 当前支持HDU的Virtual Judge（远程虚拟判题）与题目获取
  - [x] 题目支持特别判题
  - [x] 题目支持可选择性去除提交代码的末尾空白符（会影响CE判定）
  - [x] 题目支持可选择性允许用户查看各个测试点结果（状态，运行时间，运行空间，OI题目的测试点得分），暂不支持测试点数据公开。
  - [x] 管理后台支持题目数据以ZIP上传或手动输入上传
  - [x] 管理后台支持监控服务系统的状态及各判题服务的状态
  - [x] 管理后台支持动态修改网站配置，例如邮件系统配置，数据库配置等
  - [x] 比赛支持封榜，支持ACM与OI模式
  - [x] 比赛支持私有赛（需要密码才可查看与提交），保护赛（每个用户都可查看，提交需要密码），公开赛（每个用户都可查看与提交）三种模式
  - [x] 用户提交失败时可重新提交，管理员支持提交重判与比赛题目所有提交重判
  - [ ] ......
- 后端：
  - [x] Web框架技术以Springboot为主
  - [x] 以Nacos为分布式注册中心及分布式配置中心，支持配置文件动态刷新，支持判题服务Ribbon的负载均衡
  - [x] 以Mybatis-Plus为数据库中间件，负责数据实体类与数据库数据的转化与获取。
  - [x] 以Jsoup为爬虫框架，负责远程虚拟OJ的题目获取及提交结果获取，同时定时获取各用户的Codeforces分数以及其它知名OJ的近期比赛。
  - [x] 以Shiro为安全框架，支持用户角色权限管理，支持token刷新
  - [x] 以redis的发布订阅者作为判题服务的消息提醒，以此调用判题服务
  - [x] 后端分为数据后台服务（DataBackup）及判题服务（JudgeServer）
    1. 数据后台服务：负责提供接口，提供相关数据给前端等
    2. 判题服务：只负责获取数据后台服务传输过来的判题的提交，调用VJ判题或调用判题机（Go-Judge）进行评测，将对应结果写回数据库
- 判题机：
  - [x] 支持HDU的VJ判题
  - [x] 支持Codefoces的VJ判题
  - [x] 以HttpAPI的形式调用Go-Jugde(高性能可复用的判题沙盒)判题安全沙盒进行提交程序的评测（调用线程池多线程跑评测）
- 数据库
  - Mysql
- 缓存中间件
  - Redis



> 开发记录

| 时间       | 更新内容                                                     | 更新者          |
| ---------- | ------------------------------------------------------------ | --------------- |
| 2020-10-26 | 数据库设计，登录和注册接口，文档记录开始。                   | Himit_ZH        |
| 2020-10-28 | 用户模块接口，题目模块接口，比赛模块接口，排行模块接口       | Himit_ZH        |
| 2020-10-30 | 评测模块接口，判题服务系统，初始化前端vue项目                | Himit_ZH        |
| 2020-11-08 | 前端vue主页，题目列表页，登录，注册，重置密码弹窗逻辑        | Himit_ZH        |
| 2020-11-16 | 前端提交列表页，提交详情页，题目详情页，排行(ACM,OI)页，比赛列表页,个人主页，个人设置页 | Himit_ZH        |
| 2020-11-22 | 前端比赛首页，比赛题目列表，比赛排行榜，比赛公告，首页布局调整 | Himit_ZH        |
| 2020-11-24 | 介绍页，导航栏移动端优化，首页优化，公告栏优化               | Himit_ZH        |
| 2020-11-28 | 前端项目重构,加入管理端部分页面,增加case表                   | Himit_ZH        |
| 2020-12-01 | 前端管理端基本完成，准备开始前后端接口对接与测试             | Himit_ZH        |
| 2020-12-21 | 管理端前后端接口对接基本完成，准备客户端接口对接             | Himit_ZH        |
| 2021-01-04 | 客户端首页，题目，提交模块的接口对接完毕                     | Himit_ZH        |
| 2021-01-08 | 比赛列表页，排行榜，用户主页的接口对接完毕                   | Himit_ZH        |
| 2021-01-11 | 个人设置页，用户信息更新，头像上传，登录优化                 | Himit_ZH、Howie |
| 2021-01-16 | 比赛首页，比赛题目列表，比赛题目详情等接口对接完毕，定时爬取其它oj比赛及rating分完成 | Himit_ZH、Howie |
| 2021-01-19 | 比赛排行榜，比赛题目对应提交重判，比赛AC助手完成             | Himit_ZH        |
| 2021-02-02 | 正式加入安全沙箱，判题机服务器                               | Himit_ZH        |
| 2021-02-04 | 完善判题机制，加入pe可选择性判断，加入测试点可选择性公开     | Himit_ZH        |
| 2021-02-08 | 完善判题服务调度机制（负载均衡策略），完善题目评测数据上传与下载 | Himit_ZH        |
| 2021-02-12 | 初始化构建HDU虚拟判题流程                                    | Howie、Himit_ZH |
| 2021-02-16 | 完善测试特殊判题题目的操作                                   | Himit_ZH        |
| 2021-02-19 | 正式完善HDU虚拟判题以及题目添加                              | Himit_ZH        |
| 2021-02-20 | 测试HDU判题整套流程完成                                      | Himit_ZH        |
| 2021-02-22 | 修改前端编辑器样式以及md格式转换                             | Himit_ZH        |
| 2021-02-24 | 完善测试比赛相关接口，验证权限及数据计算                     | Himit_ZH        |
| 2021-03-03 | 增加Codeforces的题目爬取                                     | Himit_ZH        |
| 2021-03-26 | 增加Codeforces的虚拟判题，修复题目爬取部分问题               | Himit_ZH        |

# 二、系统架构

> 总概四大系统

1. 前端vue页面显示系统

2. 数据交互后台系统 

4. 判题服务系统 

5. 爬虫系统

> 判题逻辑概述

1. 前端用户提交数据。
2. 后端数据服务（DataBackup）获取到数据，先将提交数据初始化，同时将该提交的状态变成等待中，写入数据库。
3. 通过Redis的发布订阅者，将该判题信息发送给对应的==等待判题频道订阅者==
4. 订阅者收到信息，初始化传输数据，使用springcloud alibaba通过nacos注册中心调用判题微服务。
5. 若是调用判题服务失败（没有空闲的判题服务器），则重新通过发布者将该提交信息发布到对应==等待判题频道==，重回3。
6. 若是调用失败超过40次，则将提交的状态修改为提交失败，不再进行判题服务的调用。
7. 前端用户可看到提交变成提交失败，可点击状态进行重新提交，重回2（注：重新提交不影响提交时间等数据）
8. 判题微服务获取到提交数据：
   - 若是远程调用，则通过Redis的发布订者发布到对应的==远程判题等待提交频道==，对应==频道接收者==获取提交信息，进行远程提交，获取对应的提交ID，再通过==远程判题等待结果频道===发布给对应频道接收者，该频道接收者接受到信息，进行结果获取，获取结果失败，则再次重复发布；获取成功，写回数据库。
   - 若是自家题目提交，则启用线程池多线程使用Http将对应测试点数据与代码提交给Go-Judge判题沙盒进行编译与评测，最后获取各个评测点结果，进行结果计算写回数据库。

> HOJ基本逻辑架构图

![image-20201030234527577](https://cdn.jsdelivr.net/gh/HimitZH/CDN/images/hoj.png)



> springcloud alibaba 分布式微服务架构图
>
> Consumer：后台数据交互服务
>
> Provider：判题服务
>
> Nacos：注册中心，Consumer通过nacos调用Provider

![spingcloud-Alibaba.png](https://cdn.jsdelivr.net/gh/HimitZH/CDN/images/spingcloud-Alibaba.png)

# 三、网站部分截图

> 首页页面

![hoj1](https://cdn.jsdelivr.net/gh/HimitZH/CDN/images/hoj1.png)



> 题目列表页

![hoj2](https://cdn.jsdelivr.net/gh/HimitZH/CDN/images/hoj2.png)



> 题目详情页

![hoj7](https://cdn.jsdelivr.net/gh/HimitZH/CDN/images/hoj7.png)



> 比赛列表页

![hoj3](https://cdn.jsdelivr.net/gh/HimitZH/CDN/images/hoj3.png)



> 比赛详情首页

![hoj4](https://cdn.jsdelivr.net/gh/HimitZH/CDN/images/hoj4.png)



> 排行榜

![hoj9](https://cdn.jsdelivr.net/gh/HimitZH/CDN/images/hoj9.png)



> 个人信息页

![hoj6](https://cdn.jsdelivr.net/gh/HimitZH/CDN/images/hoj6.png)



> 个人设置页

![hoj8](https://cdn.jsdelivr.net/gh/HimitZH/CDN/images/hoj8.png)



> 管理后台首页

![hoj5](https://cdn.jsdelivr.net/gh/HimitZH/CDN/images/hoj5.png)



> 部分手机端显示

![hojmb1](https://cdn.jsdelivr.net/gh/HimitZH/CDN/images/hojmb1.png)



![hojmb2](https://cdn.jsdelivr.net/gh/HimitZH/CDN/images/hojmb2.png)

# 四、特判程序例子

```c++
#include<iostream>
#include<cstdio>
#define AC 100
#define WA 101
#define ERROR 102
using namespace std;


int spj(int user_output, FILE *output);

void close_file(FILE *f){
    if(f != NULL){
        fclose(f);
    }
}

int main(int argc, char *args[]){
    FILE *output;
    int result;
    if(argc != 2){
        return ERROR;
    }
    int user_output;
    cin>>user_output;
    cout<<user_output<<endl;
    output = fopen(args[1], "r");
	
    result = spj(user_output, output);
    printf("result: %d\n", result);
    
    close_file(output);
    return result;
}

int spj(int user_output, FILE *output){
    /*
      parameter: 
        - output，标程输出文件的指针
        - user_output，用户输出数据
      return: 
        - 如果用户答案正确，返回AC
        - 如果用户答案错误返回WA
        - 如果主动捕获到自己的错误，如内存分配失败，返回ERROR
      */
      int std_out;
      while(fscanf(output, "%d", &std_out) != EOF){
          if(user_output+1 != std_out){
             cout<<user_output<<endl<<std_out;
              return WA;
          }
      }
      return AC;
}
```



# 五、附加：

[数据库表内容及API文档](https://gitee.com/himitzh0730/hoj/tree/master/sqlAndsetting)

[Go-Sanbox API文档](https://gitee.com/himitzh0730/hoj/tree/master/judger)

[go-judge](https://github.com/criyle/go-judge)