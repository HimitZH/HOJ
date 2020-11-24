[TOC]

# 一、介绍

基于前后端分离，分布式架构的在线测评平台（hoj）....待续

> 开发记录

| 时间       | 更新内容                                                     | 更新者   |
| ---------- | ------------------------------------------------------------ | -------- |
| 2020-10-26 | 数据库设计，登录和注册接口，文档记录开始。                   | Himit_ZH |
| 2020-10-28 | 用户模块接口，题目模块接口，比赛模块接口，排行模块接口       | Himit_ZH |
| 2020-10-30 | 评测模块接口，判题服务系统，初始化前端vue项目                | Himit_ZH |
| 2020-11-08 | 前端vue主页，题目列表页，登录，注册，重置密码弹窗逻辑        | Himit_ZH |
| 2020-11-16 | 前端提交列表页，提交详情页，题目详情页，排行(ACM,OI)页，比赛列表页,个人主页，个人设置页 | Himit_ZH |
| 2020-11-22 | 前端比赛首页，比赛题目列表，比赛排行榜，比赛公告，首页布局调整 | Himit_ZH |
| 2020-11-24 | 介绍页，导航栏移动端优化，首页优化，公告栏优化               | Himit_ZH |

# 二、系统架构

> 总概四大系统

1. 前端vue页面显示系统（电脑端，手机端）

2. 数据交互后台系统 

4. 判题服务系统 

5. 爬虫系统

> 概述

前端使用element UI

后端主要逻辑：

1. 前端提交数据。
2. 后端数据交互后台微服务，将提交写入数据库，使用springcloud alibaba通过nacos注册中心调用判题后台系统微服务。
3. 判题后台系统微服务，启用多台判题机（多个进程，有限，防宕机）进行测评，最后将结果更新到数据库。
4. 爬虫系统负责爬取用户相关的codeforces的积分，vjudge的做题数等。



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



# 三、数据库

## 用户资料模块

user_info表

| 列名         | 实体属性类型 | 键   | 备注                 |
| ------------ | ------------ | ---- | -------------------- |
| userId       | String       | 主键 | uuid                 |
| username     | String       |      | 登录账号             |
| password     | String       |      | 登录密码             |
| nickname     | String       |      | 用户昵称             |
| school       | String       |      | 学校                 |
| course       | String       |      | 专业                 |
| number       | String       |      | 学号                 |
| realname     | String       |      | 真实名字             |
| email        | Srting       |      | 邮箱                 |
| avatar       | String       |      | 头像图片地址         |
| signature    | String       |      | 个性签名             |
| cf_name      | String       |      | codeforces的username |
| status       | int          |      | 0可用，1不可用       |
| gmt_create   | datetime     |      | 创建时间             |
| gmt_modified | datetime     |      | 修改时间             |



role 角色表  

| 列名         | 实体属性类型 | 键   | 备注                       |
| ------------ | ------------ | ---- | -------------------------- |
| id           | long         | 主键 | auto_increment             |
| role         | String       |      | “admin”，”tourist”，“user” |
| description  | String       |      | 角色描述                   |
| status       | int          |      | 是否可用,0可用 1不可用     |
| gmt_create   | datetime     |      | 创建时间                   |
| gmt_modified | datetime     |      | 修改时间                   |



user_role表

| 列名         | 实体属性类型 | 键   | 备注           |
| ------------ | ------------ | ---- | -------------- |
| id           | long         | 主键 | auto_increment |
| uid       | String       | 外键 | 用户id         |
| role_id      | int          | 外键 | 角色id         |
| gmt_create   | datetime     |      | 创建时间       |
| gmt_modified | datetime     |      | 修改时间       |

 

 auth权限表

| 列名         | 实体属性类型 | 键   | 备注                                                         |
| ------------ | ------------ | ---- | ------------------------------------------------------------ |
| id           | long         | 主键 | auto_increment                                               |
| name         | String       |      | 权限名称，“superadmin”,”contest”，“admin”,”common”  普通用户默认为“common” |
| permission   | String       |      | 权限字符串,例如“contest:1001”，发布某场比赛。  “all”,”select”,”update”等等， |
| status       | int          |      | 0可用，1不可用                                               |
| gmt_create   | datetime     |      | 创建时间                                                     |
| gmt_modified | datetime     |      | 修改时间                                                     |

 

role_auth表

| 列名         | 实体属性类型 | 键   | 备注           |
| ------------ | ------------ | ---- | -------------- |
| id           | long         | 主键 | auto_increment |
| role_id      | int          |      | 角色id         |
| auth_id      | int          |      | 权限id         |
| gmt_create   | datetime     |      | 创建时间       |
| gmt_modified | datetime     |      | 修改时间       |



 user_record表 个人做题记录表

| 列名         | 实体属性类型 | 键          | 备注                         |
| ------------ | ------------ | ----------- | ---------------------------- |
| id           | long         | primary key | auto_increment               |
| uid          | String       | 外键        | 用户id                       |
| total        | int          |             | 总做题数                     |
| submissions  | int          |             | 总提交数                     |
| accept       | int          |             | 通过数                       |
| Rating       | int          |             | Cf得分，未参加过默认为1500   |
| score        | int          |             | IO制比赛得分,比赛ac一题得100 |
| gmt_create   | datetime     |             | 创建时间                     |
| gmt_modified | datetime     |             | 修改时间                     |

 

user_acproblem表

| 列名   | 实体属性类型 | 键          | 备注           |
| ------ | ------------ | ----------- | -------------- |
| id     | long         | primary key | auto_increment |
| userId | String       | 外键        | 用户id         |
| Pid    | int          | 外键        | Ac的题目id     |

 

 

## 题目详情模块

 

problem表

| 列名         | 实体属性类型 | 键          | 备注                                        |
| ------------ | ------------ | ----------- | ------------------------------------------- |
| id           | long         | primary key | auto_increment 1000开始                     |
| title        | String       |             | 题目                                        |
| author       | String       |             | 默认可为无                                  |
| time_limit    | int          |             | 时间限制(ms)，默认为c/c++限制,其它语言为2倍 |
| memory_limit  | int          |             | 空间限制(k)，默认为c/c++限制,其它语言为2倍  |
| description  | String       |             | 内容描述                                    |
| Input        | String       |             | 输入描述                                    |
| Output       | String       |             | 输出描述                                    |
| sample_input  | Srting       |             | 输入样例，多样例用(#)隔开                   |
| sample_output | String       |             | 输出样例                                    |
| source       | int          |             | 题目来源（比赛id），默认为hoj,可能为爬虫vj  |
| comment      | String       |             | 备注                                        |
| auth         | int          |             | 默认为1公开，2为私有，3为比赛中。           |
| gmt_create   | datetime     |             | 创建时间                                    |
| gmt_modified | datetime     |             | 修改时间                                    |

 

problem_count表

| 列名         | 实体属性类型 | 键   | 备注                |
| ------------ | ------------ | ---- | ------------------- |
| pid          | int          |      | 题目id              |
| total        | int          |      | 总提交数            |
| ac           | int          |      | 通过数              |
| mle          | int          |      | 空间超限            |
| tle          | int          |      | 时间超限            |
| re           | int          |      | 运行错误            |
| pe           | int          |      | 格式错误            |
| ce           | int          |      | 编译错误            |
| wa           | int          |      | 答案错误            |
| se           | int          |      | 系统错误            |
| score        | int          |      | 题目分数，默认为100 |
| gmt_create   | datetime     |      | 创建时间            |
| gmt_modified | datetime     |      | 修改时间            |

 

problem_tag表

| 列名         | 实体属性类型 | 键   | 备注     |
| ------------ | ------------ | ---- | -------- |
| id           | int          |      | 主键id   |
| tid          | int          |      | 通过数   |
| gmt_create   | datetime     |      | 创建时间 |
| gmt_modified | datetime     |      | 修改时间 |

  

 

## 提交评测模块

> 判题结果status

排队中：STATUS_QUEUING = -10

判题中   STATUS_JUDGING = -5

通过：STATUS_ACCPET = 0

答案错误：STATUS__WRONG_ANSWER = -1

cpu时间超限：STATUS__CPU_TIME_LIMIT_EXCEEDED = 1

真实时间超限：STATUS__REAL_TIME_LIMIT_EXCEEDED = 2

空间超限：STATUS__MEMORY_LIMIT_EXCEEDED = 3

运行错误：STATUS__RUNTIME_ERROR = 4

系统错误：STATUS__SYSTEM_ERROR = 5

 

judge表

| 列名          | 实体属性类型 | 键          | 备注                               |
| ------------- | ------------ | ----------- | ---------------------------------- |
| submit_id     | long         | primary key | auto_increment                     |
| pid           | long         | 外键        | 题目id                             |
| uid           | String       | 外键        | 提交用户的id                       |
| submit_time   | datetime     |             | 提交时间                           |
| status        | String       |             | 判题结果                           |
| auth          | int          |             | 0为代码全部人可见，1为仅自己可见。 |
| error_message | String       |             | 错误提醒（编译错误，或者vj提醒）   |
| time          | int          |             | 运行时间                           |
| memory        | int          |             | 所耗内存                           |
| length        | int          |             | 代码长度                           |
| code          | String       |             | 代码                               |
| language      | String       |             | 代码语言                           |
| cpid          | int          |             | 比赛中的题目编号id                 |
| judger        | String       |             | 判题机ip                           |
| ip            | String       |             | 提交者ip                           |
| cid           | int          |             | 题目来源的比赛id，默认为0          |
| version       | int          |             | 乐观锁                             |
| gmt_create    | datetime     |             | 创建时间                           |
| gmt_modified  | datetime     |             | 修改时间                           |

 

jugdeCase表 评测单个样例结果表

 

| 列名         | 实体属性类型 | 键   | 备注                     |
| ------------ | ------------ | ---- | ------------------------ |
| submit_id    | long         | 外键 | 提交id                   |
| problemId    | String       | 外键 | 题目id                   |
| userId       | String       | 外键 | 提交用户的id             |
| Status       | String       |      | 单个样例评测结果         |
| time         | int          |      | 运行时间                 |
| memory       | int          |      | 运行内存                 |
| case_id      | String       |      | 测试样例id               |
| input_data   | String       |      | 样例输入，比赛不可看     |
| Output_data  | String       |      | 样例输出，比赛不可看     |
| user_output  | Srting       |      | 用户样例输出，比赛不可看 |
| gmt_create   | datetime     |      | 创建时间                 |
| gmt_modified | datetime     |      | 修改时间                 |

 

 

 

## 比赛模块

 

contest表

| 列名         | 实体属性类型 | 键   | 备注                                                  |
| ------------ | ------------ | ---- | ----------------------------------------------------- |
| id           | long         | 主键 | auto_increment  1000起步                              |
| uid          | String       | 外键 | 创建者id                                              |
| title        | String       |      | 比赛标题                                              |
| type         | int          |      | Acm赛制或者Rating                                     |
| source       | int          |      | 比赛来源，原创为0，克隆赛为比赛id                     |
| auth         | int          |      | 0为公开赛，1为私有赛（有密码），3为保护赛（有密码）。 |
| pwd          | string       |      | 比赛密码                                              |
| start_time   | datetime     |      | 开始时间                                              |
| end_time     | datetime     |      | 结束时间                                              |
| duration     | int          |      | 比赛时长（分）                                        |
| explain      | Srting       |      | 比赛说明                                              |
| gmt_create   | datetime     |      | 创建时间                                              |
| gmt_modified | datetime     |      | 修改时间                                              |

 

 

contest_problem表

| 列名         | 实体属性类型 | 键   | 备注                   |
| ------------ | ------------ | ---- | ---------------------- |
| id           | long         | 主键 | auto_increment         |
| cid          | int          | 外键 | 比赛id                 |
| pid          | int          | 外键 | 题目id                 |
| cp_name      | String       |      | 用于当场比赛的题目标题 |
| gmt_create   | datetime     |      | 创建时间               |
| gmt_modified | datetime     |      | 修改时间               |

 

contest_register表 比赛报名表

| 列名         | 实体属性类型 | 键   | 备注                       |
| ------------ | ------------ | ---- | -------------------------- |
| id           | long         | 主键 | auto_increment             |
| cid          | int          | 外键 | 比赛id                     |
| uid          | int          | 外键 | 用户id                     |
| Status       | int          |      | 默认为0表示正常，1为失效。 |
| gmt_create   | datetime     |      | 创建时间                   |
| gmt_modified | datetime     |      | 修改时间                   |



contest_score表 rating赛制中获得的分数更改记录表

| 列名         | 实体属性类型 | 键   | 备注              |
| ------------ | ------------ | ---- | ----------------- |
| id           | long         | 主键 | auto_increment    |
| cid          | int          | 外键 | 比赛id            |
| last         | int          |      | 比赛前的score得分 |
| change       | int          |      | Score比分变化     |
| now          | int          |      | 现在的score       |
| gmt_create   | datetime     |      | 创建时间          |
| gmt_modified | datetime     |      | 修改时间          |

 

contest_record表 比赛记录表

| 列名         | 实体属性类型 | 键   | 备注                                                         |
| ------------ | ------------ | ---- | ------------------------------------------------------------ |
| id           | long         | 主键 | auto_increment                                               |
| cid          | int          | 外键 | 比赛id                                                       |
| uid          | String       | 外键 | 用户id                                                       |
| pid          | int          |      | 题目id                                                       |
| cpid         | int          | 外键 | 比赛中的题目顺序id                                           |
| submit_id    | int          | 外键 | 提交id，用于可重判                                           |
| status       | String       |      | 提交结果，0表示未AC通过不罚时，1表示AC通过，-1为未AC通过算罚时 |
| time         | int          |      | 提交时间，为提交时间减去比赛时间，时间戳                     |
| gmt_create   | datetime     |      | 创建时间                                                     |
| gmt_modified | datetime     |      | 修改时间                                                     |

 

contest_announcement表 比赛时的通知表

| 列名         | 实体属性类型 | 键   | 备注                                           |
| ------------ | ------------ | ---- | ---------------------------------------------- |
| id           | long         | 主键 | auto_increment                                 |
| cid          | int          | 外键 | 比赛id                                         |
| title        | String       | 外键 | 通知标题                                       |
| content      | String       |      | 内容                                           |
| uid          | String       | 外键 | 发布者id（必须为比赛创建者或者超级管理员才能） |
| gmt_create   | datetime     |      | 创建时间                                       |
| gmt_modified | datetime     |      | 修改时间                                       |

 

contest_explanation表 赛后题解表

| 列名         | 实体属性类型 | 键   | 备注                                         |
| ------------ | ------------ | ---- | -------------------------------------------- |
| id           | long         | 主键 | auto_increment                               |
| cid          | int          | 外键 | 比赛id                                       |
| content      | String       |      | 内容(支持markdown)                           |
| uid          | int          |      | 发布者（必须为比赛创建者或者超级管理员才能） |
| gmt_create   | datetime     |      | 创建时间                                     |
| gmt_modified | datetime     |      | 修改时间                                     |

 

 

## 讨论模块

>  包括平时与比赛



comment表，只存储发布者相关题目内容，评论内容采用learncloud

| 列名         | 实体属性类型 | 键   | 备注             |
| ------------ | ------------ | ---- | ---------------- |
| id           | long         | 主键 | auto_increment   |
| uid          | int          | 外键 | 发布讨论的用户id |
| title        | String       | 外键 | 讨论标题         |
| content      | String       |      | 讨论详情         |
| tid          | String       | 外键 | 讨论标签id       |
| pid          | int          | 外键 | 引用的题目id     |
| cid          | int          | 外键 | 引用的比赛id     |
| gmt_create   | datetime     |      | 创建时间         |
| gmt_modified | datetime     |      | 修改时间         |

 

tag表  讨论表或者题目表的标签

| 列名         | 实体属性类型 | 键   | 备注           |
| ------------ | ------------ | ---- | -------------- |
| id           | long         | 主键 | auto_increment |
| name         | String       |      | 标签名字       |
| color        | String       |      | 标签颜色       |
| gmt_create   | datetime     |      | 创建时间       |
| gmt_modified | datetime     |      | 修改时间       |

 

# 四、后端交互系统



> 后端返回数据的状态码说明

```
STATUS_SUCCESS = 200; // 成功
STATUS_FAIL = 400; // 失败
STATUS_ACCESS_DENIED = 401;// 无权限
STATUS_ERROR = 500; // 系统出错
STATUS_ACCESS_DENIED = 401;// 无权限
STATUS_METHOD_NOT_ALLOWED = 405; // 不支持当前请求方法
```

###  （一）、用户模块

#### 1. 注册接口

##### 1.1 请求地址

> /api/register

##### 1.2 请求方式

> POST

##### 1.3 请求参数

> 格式：json

| 字段名   | 实体属性类型 | 说明   | 能否为空 |
| -------- | ------------ | ------ | -------- |
| username | String       | 账号   | 不能     |
| password | String       | 密码   | 不能     |
| nickname | String       | 昵称   | 不能     |
| email    | String       | 邮箱   | 不能     |
| number   | String       | 学号   | 不能     |
| code     | String       | 验证码 | 不能     |

##### 1.4 返回数据

> 格式：json

| 字段名 | 实体属性类型 | 说明                     |
| ------ | ------------ | ------------------------ |
| status | int          | 状态码，详情见状态码说明 |
| data   | json         | null                     |
| msg    | String       | 消息                     |

#### 2. 验证码接口

##### 2.1 请求地址

> /api/get-register-code

##### 2.2 请求方式

> GET

##### 2.3 请求参数

> 格式：x-www-form-urlencoded

| 字段名   | 实体属性类型 | 说明 | 能否为空 |
| -------- | ------------ | ---- | -------- |
| username | String       | 账号 | 不能     |
| email    | String       | 邮箱 | 不能     |

##### 2.4 返回数据

> 格式：json

| 字段名 | 实体属性类型 | 说明                                      |
| ------ | ------------ | ----------------------------------------- |
| status | int          | 状态码，详情见状态码说明                  |
| data   | json         | {“username”:...,"email":...,"expire":...} |
| msg    | String       | 消息                                      |

data数据

| 字段名   | 实体属性类型 | 说明                |
| -------- | ------------ | ------------------- |
| username | String       | 账号                |
| email    | String       | 邮箱                |
| expire   | int          | 验证码有效时长（s） |

#### 3. 登录接口

##### 3.1 请求地址

> /api/login

##### 3.2 请求方式

> POST

##### 3.3 请求参数

> 格式：json

| 字段名   | 实体属性类型 | 说明 | 能否为空 |
| -------- | ------------ | ---- | -------- |
| username | String       | 账号 | 不能     |
| password | String       | 密码 | 不能     |

##### 3.4 返回数据

> 格式：json

| 字段名 | 实体属性类型 | 说明                                                         |
| ------ | ------------ | ------------------------------------------------------------ |
| status | int          | 状态码，详情见状态码说明                                     |
| data   | json         | {"uid":....,"username":...,"nickname":...,"avatar":...,"email":...} |
| msg    | String       | 消息                                                         |

data数据

| 字段名   | 实体属性类型 | 说明     |
| -------- | ------------ | -------- |
| uid      | String       | 用户id   |
| username | String       | 账号     |
| nickname | String       | 昵称     |
| avatar   | String       | 头像地址 |
| email    | String       | 邮箱     |

#### 4. 登出接口

> 注：需要登录权限认证，否则报401

##### 4.1 请求地址

> /api/logout

##### 4.2 请求方式

> POST

##### 4.3 请求参数

> 无

##### 4.4 返回数据

> 格式：json

| 字段名 | 实体属性类型 | 说明                     |
| ------ | ------------ | ------------------------ |
| status | int          | 状态码，详情见状态码说明 |
| data   | json         | null                     |
| msg    | String       | 消息                     |

### （二）、题目模块

#### 1. 题目列表接口

##### 1.1 请求地址

> /api/get-problem-list

##### 1.2 请求方式

> GET

##### 1.3 请求参数

> 格式：x-www-form-urlencoded

| 字段名      | 实体属性类型 | 说明                   | 能否为空 |
| ----------- | ------------ | ---------------------- | -------- |
| limit       | String       | 每页的题目数           | 能       |
| currentPage | int          | 页数                   | 能       |
| searchPid   | long         | 想搜索题目的id         | 能       |
| searchTitle | String       | 想搜索的题目模糊匹配词 | 能       |

##### 1.4 返回数据

> 格式：json

| 字段名 | 实体属性类型 | 说明                        |
| ------ | ------------ | --------------------------- |
| status | int          | 状态码，详情见状态码说明    |
| data   | json         | 题目信息列表（ProblemVo类） |
| msg    | String       | 消息                        |

data数据

| 字段名                    | 实体属性类型 | 说明                 |
| ------------------------- | ------------ | -------------------- |
| id                        | long         | 题目id               |
| title                     | String       | 题目标题             |
| author                    | String       | 题目作者             |
| source                    | String       | 题目来源             |
| total                     | String       | 题目总提交数         |
| ac/mle/tle/re/pe/ce/wa/se | int          | 题目各类提交结果数量 |
| score                     | int          | 题目分数，默认为100  |



#### 2. 题目详情接口

##### 2.1 请求地址

> /api/get-problem-info

##### 2.2 请求方式

> GET

##### 2.3 请求参数

> 格式：x-www-form-urlencoded

| 字段名 | 实体属性类型 | 说明   | 能否为空 |
| ------ | ------------ | ------ | -------- |
| pid    | long         | 题目id | 不能     |

##### 2.4 返回数据

> 格式：json

| 字段名 | 实体属性类型 | 说明                                   |
| ------ | ------------ | -------------------------------------- |
| status | int          | 状态码，详情见状态码说明               |
| data   | json         | 题目详细信息（Problem类或看problem表） |
| msg    | String       | 消息                                   |



### （三）、比赛模块

#### 1. 比赛列表接口

##### 1.1 请求地址

> /api/get-contest-list

##### 1.2 请求方式

> GET

##### 1.3 请求参数

> 格式：x-www-form-urlencoded

| 字段名      | 实体属性类型 | 说明         | 能否为空 |
| ----------- | ------------ | ------------ | -------- |
| limit       | String       | 每页的比赛数 | 能       |
| currentPage | int          | 页数         | 能       |

##### 1.4 返回数据

> 格式：json

| 字段名 | 实体属性类型 | 说明                     |
| ------ | ------------ | ------------------------ |
| status | int          | 状态码，详情见状态码说明 |
| data   | json         | 比赛列表（ContestVo类）  |
| msg    | String       | 消息                     |



#### 2. 比赛详情接口

##### 2.1 请求地址

> /api/get-contest-info

##### 2.2 请求方式

> GET

##### 2.3 请求参数

> 格式：x-www-form-urlencoded

| 字段名 | 实体属性类型 | 说明   | 能否为空 |
| ------ | ------------ | ------ | -------- |
| cid    | long         | 比赛id | 不能     |

##### 2.4 返回数据

> 格式：json

| 字段名 | 实体属性类型 | 说明                        |
| ------ | ------------ | --------------------------- |
| status | int          | 状态码，详情见状态码说明    |
| data   | json         | 比赛详细信息（ContestVo类） |
| msg    | String       | 消息                        |



#### 3. 比赛题目接口

##### 3.1 请求地址

> /api/get-contest-problem

##### 3.2 请求方式

> GET

##### 3.3 请求参数

> 格式：x-www-form-urlencoded

| 字段名 | 实体属性类型 | 说明   | 能否为空 |
| ------ | ------------ | ------ | -------- |
| cid    | long         | 比赛id | 不能     |

##### 3.4 返回数据

> 格式：json

| 字段名 | 实体属性类型 | 说明                             |
| ------ | ------------ | -------------------------------- |
| status | int          | 状态码，详情见状态码说明         |
| data   | json         | 题目信息列表（ContestProblem类） |
| msg    | String       | 消息                             |

data数据

| 字段名      | 实体属性类型 | 说明                               |
| ----------- | ------------ | ---------------------------------- |
| id          | long         | contest_problem表主键id            |
| cid         | long         | 比赛id                             |
| pid         | long         | 题目id                             |
| cpName      | String       | 该题目在比赛中的标题，默认为原名字 |
| cpNum       | String       | 该题目在比赛中的顺序id             |
| gmtCreate   | date         | 数据创建时间                       |
| gmtModified | date         | 数据最近一次修改时间               |

#### 4. 比赛排名接口

##### 4.1 请求地址

> /api/get-contest-rank

##### 4.2 请求方式

> GET

##### 4.3 请求参数

> 格式：x-www-form-urlencoded

| 字段名 | 实体属性类型 | 说明   | 能否为空 |
| ------ | ------------ | ------ | -------- |
| cid    | long         | 比赛id | 不能     |

##### 4.4 返回数据

> 格式：json

| 字段名 | 实体属性类型 | 说明                              |
| ------ | ------------ | --------------------------------- |
| status | int          | 状态码，详情见状态码说明          |
| data   | json         | 题目信息列表（ContestRecordVo类） |
| msg    | String       | 消息                              |

data数据

| 字段名   | 实体属性类型 | 说明                                                         |
| -------- | ------------ | ------------------------------------------------------------ |
| uid      | String       | 用户id                                                       |
| cid      | long         | 比赛id                                                       |
| pid      | long         | 题目id                                                       |
| cpid     | long         | 比赛中的题目顺序id                                           |
| username | String       | 用户名                                                       |
| nickname | String       | 昵称                                                         |
| submitId | long         | 提交id                                                       |
| status   | int          | 提交结果，0表示未AC通过不罚时，1表示AC通过，-1为未AC通过算罚时 |
| time     | date         | 提交时间，为提交时间减去比赛时间，时间戳                     |

#### 5. 比赛通知接口

##### 5.1 请求地址

> /api/get-contest-announcement

##### 5.2 请求方式

> GET

##### 5.3 请求参数

> 格式：x-www-form-urlencoded

| 字段名 | 实体属性类型 | 说明   | 能否为空 |
| ------ | ------------ | ------ | -------- |
| cid    | long         | 比赛id | 不能     |

##### 5.4 返回数据

> 格式：json

| 字段名 | 实体属性类型 | 说明                                  |
| ------ | ------------ | ------------------------------------- |
| status | int          | 状态码，详情见状态码说明              |
| data   | json         | 比赛通知列表（ContestAnnouncement类） |
| msg    | String       | 消息                                  |

data数据

| 字段名  | 实体属性类型 | 说明     |
| ------- | ------------ | -------- |
| cid     | long         | 比赛者id |
| title   | String       | 通知标题 |
| uid     | String       | 发布者id |
| content | String       | 通知内容 |

#### 6. 比赛通知接口

##### 6.1 请求地址

> /api/to-register-contest

##### 6.2 请求方式

> POST

##### 6.3 请求参数

> 格式：json

| 字段名 | 实体属性类型 | 说明   | 能否为空 |
| ------ | ------------ | ------ | -------- |
| cid    | long         | 比赛id | 不能     |
| uid    | String       | 用户id | 不能     |

##### 6.4 返回数据

> 格式：json

| 字段名 | 实体属性类型 | 说明                     |
| ------ | ------------ | ------------------------ |
| status | int          | 状态码，详情见状态码说明 |
| data   | json         | null                     |
| msg    | String       | 消息                     |



### （四）、排名模块

#### 1. 排名列表接口

##### 1.1 请求地址

> /api/get-rank-list

##### 1.2 请求方式

> GET

##### 1.3 请求参数

> 格式：x-www-form-urlencoded

| 字段名      | 实体属性类型 | 说明         | 能否为空 |
| ----------- | ------------ | ------------ | -------- |
| limit       | String       | 每页的用户量 | 能       |
| currentPage | int          | 页数         | 能       |

##### 1.4 返回数据

> 格式：json

| 字段名 | 实体属性类型 | 说明                     |
| ------ | ------------ | ------------------------ |
| status | int          | 状态码，详情见状态码说明 |
| data   | json         | 排名数据列表（RankVo类） |
| msg    | String       | 消息                     |

data数据

| 字段名      | 实体属性类型 | 说明         |
| ----------- | ------------ | ------------ |
| uid         | String       | 用户id       |
| username    | String       | 用户名       |
| nickname    | String       | 消息         |
| number      | String       | 学号         |
| total       | int          | 总做题数     |
| submissions | int          | 总提交数     |
| ac          | int          | 总通过数     |
| Rating      | int          | cf得分       |
| score       | int          | io制比赛得分 |

### （五）、评测模块

> 评测分为普通做题评测和比赛做题评测

#### 1. 提交评测接口

##### 1.1 请求地址

> /api/submit-problem-judge

##### 1.2 请求方式

> POST

**需要做登录授权认证！**

##### 1.3 请求参数

> 格式：json 

| 字段名   | 实体属性类型 | 说明                             | 能否为空 |
| -------- | ------------ | -------------------------------- | -------- |
| pid      | long         | 题目id                           | 不能     |
| uid      | String       | 用户id                           | 不能     |
| language | String       | 代码语言                         | 不能     |
| code     | String       | 代码                             | 不能     |
| auth     | int          | 0为代码全部人可见，1为仅自己可见 | 不能     |
| cid      | long         | 所属比赛id，若无设置为0          | 不能     |
| cpid     | long         | 所属比赛题目id，若无设置为0      | 不能     |

##### 1.4 返回数据

> 格式：json

| 字段名 | 实体属性类型 | 说明                               |
| ------ | ------------ | ---------------------------------- |
| status | int          | 状态码，详情见状态码说明           |
| data   | json         | 评测结果（具体看Judge类或judge表） |
| msg    | String       | 消息                               |

#### 2. 评测列表接口

##### 2.1 请求地址

> /api/get-judge-list

##### 2.2 请求方式

> GET

##### 2.3 请求参数

> 格式：x-www-form-urlencoded

| 字段名         | 实体属性类型 | 说明                                         | 能否为空 |
| -------------- | ------------ | -------------------------------------------- | -------- |
| limit          | String       | （查询条件）每页的判题条数                   | 能       |
| currentPage    | int          | （查询条件）页数                             | 能       |
| searchPid      | long         | （查询条件）题目id                           | 能       |
| searchSource   | String       | （查询条件）题目来源                         | 能       |
| searchLanguage | String       | （查询条件）提交代码语言                     | 能       |
| searchStatus   | int          | （查询条件）提交代码结果                     | 能       |
| searchUsername | String       | （查询条件）提交的用户名                     | 能       |
| searchCid      | long         | 比赛评测状态根据cid，若非比赛的则设置为0即可 | 不能     |

##### 2.4 返回数据

> 格式：json

| 字段名 | 实体属性类型 | 说明                              |
| ------ | ------------ | --------------------------------- |
| status | int          | 状态码，详情见状态码说明          |
| data   | json         | 判题结果列表（具体请看JudgeVo类） |
| msg    | String       | 消息                              |



#### 3. 评测样例接口

##### 1.1 请求地址

> /api/get-judge-case

##### 1.2 请求方式

> GET

##### 1.3 请求参数

> 格式：x-www-form-urlencoded

| 字段名   | 实体属性类型 | 说明   | 能否为空 |
| -------- | ------------ | ------ | -------- |
| submitId | long         | 提交id | 不能     |
| pid      | long         | 题目id | 不能     |

##### 1.4 返回数据

> 格式：json

| 字段名 | 实体属性类型 | 说明                                      |
| ------ | ------------ | ----------------------------------------- |
| status | int          | 状态码，详情见状态码说明                  |
| data   | json         | 评测样例列表（JudgeCase类或judge_case表） |
| msg    | String       | 消息                                      |

### （六）、讨论模块

> 暂时搁置，可能自己写，也可能用别人的组件！！！！

##### 

# 五、判题服务系统

### （一）、数据接口模块

> 后端数据交互系统在nacos注册中心通过openfeign的形式调用判题系统服务

#### 1. 评测接口

##### 1.1 请求地址

> /judge

##### 1.2 请求方式

> POST

##### 1.3 请求参数

> 格式：json 

| 字段名   | 实体属性类型 | 说明                             | 能否为空 |
| -------- | ------------ | -------------------------------- | -------- |
| pid      | long         | 题目id                           | 不能     |
| uid      | String       | 用户id                           | 不能     |
| language | String       | 代码语言                         | 不能     |
| code     | String       | 代码                             | 不能     |
| auth     | int          | 0为代码全部人可见，1为仅自己可见 | 不能     |
| cid      | long         | 所属比赛id，若无设置为0          | 不能     |
| cpid     | long         | 所属比赛题目id，若无设置为0      | 不能     |

##### 1.4 返回数据

> 格式：json

| 字段名 | 实体属性类型 | 说明                               |
| ------ | ------------ | ---------------------------------- |
| status | int          | 状态码，详情见状态码说明           |
| data   | json         | 评测结果（具体看Judge类或judge表） |
| msg    | String       | 消息                               |



###（二）、判题机模块