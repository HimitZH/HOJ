# 一、介绍

基于前后端分离，分布式架构的在线测评平台（hoj）

在线Demo：[https://www.hcode.top](https://www.hcode.top)

> 上线日记

| 时间       | 内容                                         | 更新者   |
| ---------- | -------------------------------------------- | -------- |
| 2020-10-26 | 正式开发                                     | Himit_ZH |
| 2021-04-10 | 首次上线测试                                 | Himit_ZH |
| 2021-04-15 | 判题调度2.0解决并发问题                      | Himit_ZH |
| 2021-04-16 | 重构解耦JudgeServer判题逻辑，添加部署文档    | Himit_ZH |
| 2021-04-19 | 加入rsync实现评测数据同步，修复一些已知的BUG | Himit_ZH |
| 2021-04-24 | 加入题目模板，修改页面页脚                   | Himit_ZH |
| 2021-05-02 | 修复比赛后管理员重判题目导致排行榜失效的问题 | Himit_ZH |
| 2021-05-09 | 添加公共讨论区，题目讨论区，比赛评论         | Himit_ZH |

# 二、部署

**注意：比较适用于熟悉springboot，docker的开发人员打包部署**

部署文档：[https://gitee.com/himitzh0730/hoj/tree/master/docs](https://gitee.com/himitzh0730/hoj/tree/master/docs)

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
  - [x] 题目讨论
  - [x] 管理后台支持题目数据以ZIP上传或手动输入上传
  - [x] 管理后台支持监控服务系统的状态及各判题服务的状态
  - [x] 管理后台支持动态修改网站配置，例如邮件系统配置，数据库配置等
  - [x] 比赛支持封榜，支持ACM与OI模式
  - [x] 比赛支持私有赛（需要密码才可查看与提交），保护赛（每个用户都可查看，提交需要密码），公开赛（每个用户都可查看与提交）三种模式
  - [x] 用户提交失败时可重新提交，管理员支持提交重判与比赛题目所有提交重判
  - [x] 公共讨论区
  - [x] 比赛讨论
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



# 二、系统架构

> 总概四大系统

1. 前端vue页面显示系统

2. 数据交互后台系统 

4. 判题服务系统 

5. 爬虫系统

> 判题逻辑概述

1. 前端用户提交数据。
2. 后端数据服务（DataBackup）获取到数据，先将提交数据初始化，同时将该提交的状态变成等待中，写入数据库。
3. 通过Redis，写入**等待判题队列**
4. 调用work处理者，初始化传输数据，使用springcloud alibaba通过nacos注册中心调用判题微服务。
5. 若是调用判题服务失败（没有空闲的判题服务器），则重新通过发布者将该提交信息发布到对应**等待判题队列**，重回3。
6. 若是调用失败超过30次，则将提交的状态修改为提交失败，不再进行判题服务的调用。
7. 前端用户可看到提交变成提交失败，可点击状态进行重新提交，重回2（注：重新提交不影响提交时间等数据）
8. 判题微服务获取到提交数据：
   - 若是远程调用，进行远程提交，获取对应的提交ID，若是获取失败则判为提交失败，若是获取成功，就启用线程定时器每2秒根据提交ID获取判题结果。
   
     定时尝试30次，获取成功，写回数据库，获取失败就修改此次提交为提交失败。
   
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

![提交列表](https://img-blog.csdnimg.cn/20210509232933478.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70)

> 排行榜

![hoj9](https://cdn.jsdelivr.net/gh/HimitZH/CDN/images/hoj9.png)



> 公共讨论区

![公共讨论区](https://img-blog.csdnimg.cn/2021050923351998.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70)



> 评论组件

![评论组件](C:\Users\HZH990730\AppData\Roaming\Typora\typora-user-images\image-20210509233700989.png)



> 个人信息页

![个人信息](https://img-blog.csdnimg.cn/20210509233300701.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70)



> 个人设置页

![个人设置](https://img-blog.csdnimg.cn/20210509233439791.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70)





> 管理后台首页

![hoj5](https://cdn.jsdelivr.net/gh/HimitZH/CDN/images/hoj5.png)



> 部分手机端显示

![](https://img-blog.csdnimg.cn/20210509233756882.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70)



![评论区](https://img-blog.csdnimg.cn/20210509233845230.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70)





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