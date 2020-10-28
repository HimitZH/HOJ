[TOC]

# 一、介绍

基于前后端分离，分布式架构的在线测评平台（hoj）....待续

> 开发记录

| 时间       | 更新内容                                   | 更新者   |
| ---------- | ------------------------------------------ | -------- |
| 2020-10-26 | 数据库设计，登录和注册接口，文档记录开始。 | Himit_ZH |

# 二、系统架构

> 总概五大系统

1. 前端vue页面显示系统（电脑端，手机端）

2. 数据交互后台系统 

3. 判题后台系统 

4. 判题机系统 

5. 爬虫系统

> 概述

前端使用element UI

后端主要逻辑：

1. 前端提交数据。
2. 后端数据交互后台微服务，将提交写入数据库，使用springcloud alibaba通过nacos注册中心调用判题后台系统微服务。
3. 判题后台系统微服务，使用RabbitMQ告知判题机系统（传递submitId集合) 。
4. 判题机系统启用多台判题机（多个进程，有限，防宕机）进行测评，最后将结果更新到数据库。
5. 爬虫系统负责爬取用户相关的codeforces的积分，vjudge的做题数等。

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
| Status       | int          |      | 0可用，1不可用       |
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

 

  

 

## 提交评测模块

> 判题结果status

STATUS_ACCPET = 0

STATUS__WRONG_ANSWER = -1

STATUS__CPU_TIME_LIMIT_EXCEEDED = 1

STATUS__REAL_TIME_LIMIT_EXCEEDED = 2

STATUS__MEMORY_LIMIT_EXCEEDED = 3

STATUS__RUNTIME_ERROR = 4

STATUS__SYSTEM_ERROR = 5

 

judge表

| 列名         | 实体属性类型 | 键          | 备注                               |
| ------------ | ------------ | ----------- | ---------------------------------- |
| submit_id    | long         | primary key | auto_increment                     |
| pid          | long         | 外键        | 题目id                             |
| uid          | String       | 外键        | 提交用户的id                       |
| submit_time  | datetime     |             | 提交时间                           |
| status       | String       |             | 判题结果                           |
| auth         | int          |             | 0为代码全部人可见，1为仅自己可见。 |
| error_message| String       |             | 错误提醒（编译错误，或者vj提醒）   |
| time         | int          |             | 运行时间                           |
| memory       | int          |             | 所耗内存                           |
| length       | int          |             | 代码长度                           |
| code         | String       |             | 代码                               |
| language     | String       |             | 代码语言                           |
| cpid         | int          |             | 比赛中的题目编号id                 |
| judger       | String       |             | 判题机ip                           |
| ip           | String       |             | 提交者ip                           |
| cid          | int          |             | 题目来源的比赛id，默认为0          |
| gmt_create   | datetime     |             | 创建时间                           |
| gmt_modified | datetime     |             | 修改时间                           |

 

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

| 列名         | 实体属性类型 | 键          | 备注                                        |
| ------------ | ------------ | ----------- | ------------------------------------------- |
| id           | long         | 主键        | auto_increment  1000起步                    |
| uid          | String       | 外键        | 创建者id                                    |
| title        | String       |             | 比赛标题                                    |
| type         | int          |             | Acm赛制或者Rating                           |
| source       | int          |             | 比赛来源，原创为0，克隆赛为比赛id           |
| auth         | int          |             | 0为公开赛，1为私有赛（有密码），2为报名赛。 |
| pwd          | string       |             | 比赛密码                                    |
| start_time   | datetime     |             | 开始时间                                    |
| end_time     | datetime     |             | 结束时间                                    |
| duration     | int          |             | 比赛时长（分）                              |
| explain      | Srting       |             | 比赛说明                                    |
| gmt_create   | datetime     |             | 创建时间                                    |
| gmt_modified | datetime     |             | 修改时间                                    |

 

 

contest_problem表

| 列名         | 实体属性类型 | 键          | 备注                   |
| ------------ | ------------ | ----------- | ---------------------- |
| id           | long         | 主键         | auto_increment         |
| cid          | int          | 外键        | 比赛id                 |
| pid          | int          | 外键        | 题目id                 |
| cp_name      | String       |             | 用于当场比赛的题目标题 |
| cp_num       | String       |             | 比赛题目的顺序id       |
| gmt_create   | datetime     |             | 创建时间               |
| gmt_modified | datetime     |             | 修改时间               |

 

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

| 列名         | 实体属性类型 | 键   | 备注                                   |
| ------------ | ------------ | ---- | -------------------------------------- |
| id           | long         | 主键 | auto_increment                         |
| cid          | int          | 外键 | 比赛id                                 |
| uid          | String       | 外键 | 用户id                                 |
| pid          | int          |      | 题目id                                 |
| cpid         | int          | 外键 | 比赛中的题目顺序id                     |
| submit_id    | int          | 外键 | 提交id，用于可重判                     |
| status       | String       |      | 提交结果，0不加罚时，-1加罚时，1表示AC |
| time         | datetime     |      | 提交时间                               |
| gmt_create   | datetime     |      | 创建时间                               |
| gmt_modified | datetime     |      | 修改时间                               |

 

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

 

comment_tag表  讨论标签表

| 列名         | 实体属性类型 | 键   | 备注           |
| ------------ | ------------ | ---- | -------------- |
| id           | long         | 主键 | auto_increment |
| name         | String       |      | 标签名字       |
| color        | String       |      | 标签颜色       |
| gmt_create   | datetime     |      | 创建时间       |
| gmt_modified | datetime     |      | 修改时间       |

 

# 四、后端数据接口

> 后端返回数据的状态码说明

```
STATUS_SUCCESS = 200; // 成功
STATUS_FAIL = 400; // 失败
STATUS_ACCESS_DENIED = 401;// 无权限
STATUS_ERROR = 500; // 系统出错
STATUS_ACCESS_DENIED = 401;// 无权限
STATUS_METHOD_NOT_ALLOWED = 405; // 不支持当前请求方法
```

## 1. 注册接口

### 1.1 请求地址

> /register

### 1.2 请求方式

> POST

### 1.3 请求参数

> 格式：json

| 字段名   | 实体属性类型 | 说明   | 能否为空 |
| -------- | ------------ | ------ | -------- |
| username | String       | 账号   | 不能     |
| password | String       | 密码   | 不能     |
| nickname | String       | 昵称   | 不能     |
| email    | String       | 邮箱   | 不能     |
| number   | String       | 学号   | 不能     |
| code     | String       | 验证码 | 不能     |

### 1.4 返回数据

> 格式：json

| 字段名 | 实体属性类型 | 说明                     |
| ------ | ------------ | ------------------------ |
| status | int          | 状态码，详情见状态码说明 |
| data   | json         | 后台查询数据存储于这     |
| msg    | String       | 消息                     |

## 2. 注册接口

### 2.1 请求地址

> /login

### 2.2 请求方式

> POST

### 2.3 请求参数

> 格式：json

| 字段名   | 实体属性类型 | 说明 | 能否为空 |
| -------- | ------------ | ---- | -------- |
| username | String       | 账号 | 不能     |
| password | String       | 密码 | 不能     |

### 2.4 返回数据

> 格式：json

| 字段名 | 实体属性类型 | 说明                     |
| ------ | ------------ | ------------------------ |
| status | int          | 状态码，详情见状态码说明 |
| data   | json         | 后台查询数据存储于这     |
| msg    | String       | 消息                     |



data数据

| 字段名   | 实体属性类型 | 说明     |
| -------- | ------------ | -------- |
| uid      | String       | 用户id   |
| username | String       | 账号     |
| nickname | String       | 昵称     |
| avatar   | String       | 头像地址 |
| email    | String       | 邮箱     |
