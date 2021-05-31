# 数据库说明

## 用户资料模块

user_info表

| 列名         | 实体属性类型 | 键   | 备注                 |
| ------------ | ------------ | ---- | -------------------- |
| uuid         | String       | 主键 | uuid用户id           |
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

session表  

| 列名         | 实体属性类型 | 键   | 备注             |
| ------------ | ------------ | ---- | ---------------- |
| id           | long         | 主键 | auto_increment   |
| uid          | String       | 外键 | 用户id           |
| user_agent   | String       |      | 访问的浏览器参数 |
| ip           | Srting       |      | 访问所在的ip     |
| gmt_create   | datetime     |      | 创建时间         |
| gmt_modified | datetime     |      | 修改时间         |



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
| uid          | String       | 外键 | 用户id         |
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

| 列名         | 实体属性类型 | 键          | 备注                       |
| ------------ | ------------ | ----------- | -------------------------- |
| id           | long         | primary key | auto_increment             |
| uid          | String       | 外键        | 用户id                     |
| submissions  | int          |             | 总提交数                   |
| total_score  | int          |             | 总OI题目得分               |
| rating       | int          |             | Cf得分，未参加过默认为1500 |
| gmt_create   | datetime     |             | 创建时间                   |
| gmt_modified | datetime     |             | 修改时间                   |

 

user_acproblem表

| 列名         | 实体属性类型 | 键          | 备注           |
| ------------ | ------------ | ----------- | -------------- |
| id           | long         | primary key | auto_increment |
| uid          | String       | 外键        | 用户id         |
| pid          | long         | 外键        | Ac的题目id     |
| subimit_id   | long         | 外键        | 提交的id       |
| gmt_create   | datetime     |             | 创建时间       |
| gmt_modified | datetime     |             | 修改时间       |

 

 

## 题目详情模块

problem表

| 列名             | 实体属性类型 | 键          | 备注                                        |
| ---------------- | ------------ | ----------- | ------------------------------------------- |
| id               | long         | primary key | auto_increment 1000开始                     |
| problem_id       | String       |             | 题目展示id                                  |
| title            | String       |             | 题目标题                                    |
| author           | String       |             | 默认可为无                                  |
| type             | int          |             | 题目类型 0为ACM,1为OI                       |
| time_limit       | int          |             | 时间限制(ms)，默认为c/c++限制,其它语言为2倍 |
| memory_limit     | int          |             | 空间限制(mb)，默认为c/c++限制,其它语言为2倍 |
| description      | String       |             | 内容描述                                    |
| input            | String       |             | 输入描述                                    |
| output           | String       |             | 输出描述                                    |
| examples         | Srting       |             | 题面输入输出样例，不纳入评测数据            |
| source           | int          |             | 题目来源（比赛id），默认为hoj,可能为爬虫vj  |
| difficulty       | int          |             | 题目难度，0简单，1中等，2困难               |
| hint             | String       |             | 备注 提醒                                   |
| auth             | int          |             | 默认为1公开，2为私有，3为比赛中。           |
| io_score         | int          |             | 当该题目为io题目时的分数 默认为100          |
| code_share       | boolean      |             | 该题目对应的相关提交代码，用户是否可用分享  |
| spj_code         | String       |             | 特判程序代码 空代表非特判                   |
| spj_language     | String       |             | 特判程序的语言                              |
| isRemoveEndBlank | boolean      |             | 是否默认去除用户代码的文末空格              |
| openCaseResult   | boolean      |             | 是否默认开启该题目的测试样例结果查看        |
| caseVersion      | String       |             | 题目测试数据的版本号                        |
| gmt_create       | datetime     |             | 创建时间                                    |
| gmt_modified     | datetime     |             | 修改时间                                    |

 

problem_case表

| 列名         | 实体属性类型 | 键          | 备注               |
| ------------ | ------------ | ----------- | ------------------ |
| id           | long         | primary key | auto_increment     |
| pid          | long         | 外键        | 题目id             |
| input        | String       |             | 测试样例的输入     |
| output       | String       |             | 测试样例的输出     |
| status       | String       |             | 状态0可用，1不可用 |
| gmt_create   | datetime     |             | 创建时间           |
| gmt_modified | datetime     |             | 修改时间           |



problem_count表

| 列名         | 实体属性类型 | 键   | 备注             |
| ------------ | ------------ | ---- | ---------------- |
| pid          | int          |      | 题目id           |
| total        | int          |      | 总提交数         |
| ac           | int          |      | 通过数           |
| mle          | int          |      | 空间超限         |
| tle          | int          |      | 时间超限         |
| re           | int          |      | 运行错误         |
| pe           | int          |      | 格式错误         |
| ce           | int          |      | 编译错误         |
| wa           | int          |      | 答案错误         |
| se           | int          |      | 系统错误         |
| pa           | int          |      | 该IO题目分数总和 |
| gmt_create   | datetime     |      | 创建时间         |
| gmt_modified | datetime     |      | 修改时间         |

 

tag表  题目表的标签

| 列名         | 实体属性类型 | 键   | 备注           |
| ------------ | ------------ | ---- | -------------- |
| id           | long         | 主键 | auto_increment |
| name         | String       |      | 标签名字       |
| color        | String       |      | 标签颜色       |
| gmt_create   | datetime     |      | 创建时间       |
| gmt_modified | datetime     |      | 修改时间       |



problem_tag表

| 列名         | 实体属性类型 | 键   | 备注     |
| ------------ | ------------ | ---- | -------- |
| id           | int          |      | 主键id   |
| tid          | int          |      | 标签id   |
| gmt_create   | datetime     |      | 创建时间 |
| gmt_modified | datetime     |      | 修改时间 |

  

language表

| 列名            | 实体属性类型 | 键   | 备注                         |
| --------------- | ------------ | ---- | ---------------------------- |
| id              | long         |      | 主键id                       |
| content_type    | String       |      | 语言类型                     |
| description     | String       |      | 语言描述                     |
| name            | String       |      | 语言名字                     |
| compile_command | String       |      | 编译指令                     |
| template        | String       |      | A+B题目模板                  |
| code_template   | String       |      | 语言对应的代码模板           |
| is_spj          | boolean      |      | 是否可作为特殊判题的一种语言 |
| gmt_create      | datetime     |      | 创建时间                     |
| gmt_modified    | datetime     |      | 修改时间                     |

  

code_template表

| 列名         | 实体属性类型 | 键   | 备注     |
| ------------ | ------------ | ---- | -------- |
| id           | long         |      | 主键id   |
| pid          | long         | 外键 | 题目id   |
| lid          | long         | 外键 | 语言id   |
| code         | String       |      | 代码模板 |
| status       | boolean      |      | 是否启用 |
| gmt_create   | datetime     |      | 修改时间 |
| gmt_modified | datetime     |      | 修改时间 |

## 提交评测模块

> 判题结果status

未提交：STATUS_NOT_SUBMITTED = -10

提交中：STATUS_SUBMITTING = 9

排队中：STATUS_PENDING  =  6

评测中：STATUS_JUDGING =  7

编译错误：STATUS_COMPILE_ERROR = -2

输出格式错误：STATUS_PRESENTATION_ERROR = -3

答案错误：STATUS__WRONG_ANSWER = -1

评测通过：STATUS_ACCEPTED = 0

cpu时间超限：STATUS__CPU_TIME_LIMIT_EXCEEDED = 1

真实时间超限：STATUS__REAL_TIME_LIMIT_EXCEEDED = 2

空间超限：STATUS__MEMORY_LIMIT_EXCEEDED = 3

运行错误：STATUS__RUNTIME_ERROR = 4

系统错误：STATUS__SYSTEM_ERROR = 5

OI评测部分通过：STATUS_PARTIAL_ACCEPTED = 8

提交失败：STATUS_SUBMITTED_FAILED= 10

judge表

| 列名          | 实体属性类型 | 键          | 备注                             |
| ------------- | ------------ | ----------- | -------------------------------- |
| submit_id     | long         | primary key | auto_increment                   |
| display_pid   | String       |             | 题目展示id                       |
| pid           | long         | 外键        | 题目id                           |
| uid           | String       | 外键        | 提交用户的id                     |
| username      | String       | 外键        | 用户名                           |
| submit_time   | datetime     |             | 提交时间                         |
| status        | String       |             | 判题结果                         |
| share         | Boolean      |             | 代码是否分享                     |
| error_message | String       |             | 错误提醒（编译错误，或者vj提醒） |
| time          | int          |             | 运行时间                         |
| memory        | int          |             | 所耗内存                         |
| length        | int          |             | 代码长度                         |
| code          | String       |             | 代码                             |
| language      | String       |             | 代码语言                         |
| cpid          | int          |             | 比赛中的题目编号id               |
| judger        | String       |             | 判题机ip                         |
| ip            | String       |             | 提交者ip                         |
| cid           | int          |             | 题目来源的比赛id，默认为0        |
| version       | int          |             | 乐观锁                           |
| gmt_create    | datetime     |             | 创建时间                         |
| gmt_modified  | datetime     |             | 修改时间                         |

 

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

更新比赛状态的存储过程

 ```sql
DELIMITER |

DROP PROCEDURE IF EXISTS contest_status |
CREATE PROCEDURE contest_status()

    BEGIN
      UPDATE contest 
	SET STATUS = (
	CASE 
	  WHEN NOW() < start_time THEN -1 
	  WHEN NOW() >= start_time AND NOW()<end_time THEN  0
	  WHEN NOW() >= end_time THEN 1
	END);
    END
|

 ```



创建插入时的触发器

```sql
DROP TRIGGER IF EXISTS contest_trigger;

DELIMITER $$
CREATE TRIGGER contest_trigger
BEFORE INSERT ON contest FOR EACH ROW
BEGIN
SET new.status=(
	CASE 
	  WHEN NOW() < new.start_time THEN -1 
	  WHEN NOW() >= new.start_time AND NOW()<new.end_time THEN  0
	  WHEN NOW() >= new.end_time THEN 1
	END);
END$$
DELIMITER ;
```



设置定时器

```sql
SET GLOBAL event_scheduler = 1;  // 开启定时器
CREATE EVENT IF NOT EXISTS contest_event

ON SCHEDULE EVERY 1 SECOND // 每秒执行一次

ON COMPLETION PRESERVE  

DO CALL contest_status(); // 调用存储过程
```

开启或关闭定时器

```sql
ALTER EVENT contest_event ON  COMPLETION PRESERVE ENABLE;   -- 开启事件
ALTER EVENT contest_event ON  COMPLETION PRESERVE DISABLE;  -- 关闭事件
```



contest表

| 列名           | 实体属性类型 | 键   | 备注                                                  |
| -------------- | ------------ | ---- | ----------------------------------------------------- |
| id             | long         | 主键 | auto_increment  1000起步                              |
| uid            | String       | 外键 | 创建者id                                              |
| author         | String       |      | 比赛创建者的用户名                                    |
| title          | String       |      | 比赛标题                                              |
| type           | int          |      | Acm赛制或者Rating                                     |
| source         | int          |      | 比赛来源，原创为0，克隆赛为比赛id                     |
| auth           | int          |      | 0为公开赛，1为私有赛（有密码），3为保护赛（有密码）。 |
| pwd            | string       |      | 比赛密码                                              |
| start_time     | datetime     |      | 开始时间                                              |
| end_time       | datetime     |      | 结束时间                                              |
| duration       | long         |      | 比赛时长（s）                                         |
| description    | Srting       |      | 比赛说明                                              |
| seal_rank      | boolean      |      | 是否开启封榜                                          |
| seal_rank_time | datetime     |      | 封榜起始时间，一直到比赛结束，不刷新榜单。            |
| status         | int          |      | -1为未开始，0为进行中，1为已结束                      |
| visible        | boolean      |      | 是否可见                                              |
| gmt_create     | datetime     |      | 创建时间                                              |
| gmt_modified   | datetime     |      | 修改时间                                              |

 

 

contest_problem表

| 列名         | 实体属性类型 | 键   | 备注                   |
| ------------ | ------------ | ---- | ---------------------- |
| id           | long         | 主键 | auto_increment         |
| display_id   | String       |      | 展示的id               |
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
| pid          | int          | 外键 | 题目id                                                       |
| cpid         | int          | 外键 | 比赛中的题目id                                               |
| submit_id    | int          | 外键 | 提交id，用于可重判                                           |
| display_id   | String       |      | 比赛展示的id                                                 |
| username     | String       |      | 用户名                                                       |
| realname     | String       |      | 真实姓名                                                     |
| status       | String       |      | 提交结果，0表示未AC通过不罚时，1表示AC通过，-1为未AC通过算罚时 |
| time         | int          |      | 提交时间，为提交时间减去比赛时间，时间戳                     |
| score        | int          |      | OI比赛得分                                                   |
| first_blood  | Boolean      |      | 是否为一血AC                                                 |
| checked      | Boolean      |      | AC是否已校验                                                 |
| gmt_create   | datetime     |      | 创建时间                                                     |
| gmt_modified | datetime     |      | 修改时间                                                     |

 announcement表 

| 列名         | 实体属性类型 | 键   | 备注                                           |
| ------------ | ------------ | ---- | ---------------------------------------------- |
| id           | long         | 主键 | auto_increment                                 |
| title        | String       |      | 公告标题                                       |
| content      | String       |      | 公告内容                                       |
| uid          | String       | 外键 | 发布者id（必须为比赛创建者或者超级管理员才能） |
| gmt_create   | datetime     |      | 创建时间                                       |
| gmt_modified | datetime     |      | 修改时间                                       |





contest_announcement表 比赛时的通知表

| 列名         | 实体属性类型 | 键   | 备注           |
| ------------ | ------------ | ---- | -------------- |
| id           | long         | 主键 | auto_increment |
| aid          | long         | 外键 | 公告id         |
| cid          | int          | 外键 | 比赛id         |
| gmt_create   | datetime     |      | 创建时间       |
| gmt_modified | datetime     |      | 修改时间       |

 

contest_explanation表 赛后题解表**(未使用)**

| 列名         | 实体属性类型 | 键   | 备注                                         |
| ------------ | ------------ | ---- | -------------------------------------------- |
| id           | long         | 主键 | auto_increment                               |
| cid          | int          | 外键 | 比赛id                                       |
| content      | String       |      | 内容(支持markdown)                           |
| uid          | int          |      | 发布者（必须为比赛创建者或者超级管理员才能） |
| gmt_create   | datetime     |      | 创建时间                                     |
| gmt_modified | datetime     |      | 修改时间                                     |

 

 

## 讨论模块

>  包括题目讨论区，公共讨论区，比赛评论



category表

| 列名         | 实体属性类型 | 键   | 备注           |
| ------------ | ------------ | ---- | -------------- |
| id           | long         | 主键 | auto_increment |
| name         | String       |      | 分类名字       |
| gmt_create   | datetime     |      | 创建时间       |
| gmt_modified | datetime     |      | 修改时间       |



discussion表

| 列名         | 实体属性类型 | 键   | 备注                             |
| ------------ | ------------ | ---- | -------------------------------- |
| id           | int          | 主键 | auto_increment                   |
| category_id  | int          | 外键 | 分类id                           |
| title        | String       | 外键 | 讨论标题                         |
| content      | String       |      | 讨论详情                         |
| description  | String       |      | 讨论描述                         |
| pid          | String       | 外键 | 引用的题目id，默认未null则不引用 |
| uid          | iString      | 外键 | 发布讨论的用户id                 |
| author       | String       | 外键 | 发布讨论的用户名                 |
| avatar       | String       | 外键 | 发布讨论的用户头像地址           |
| role         | String       |      | 发布讨论的用户角色               |
| view_num     | int          |      | 浏览数量                         |
| like_num     | int          |      | 点赞数量                         |
| top_priority | boolean      |      | 优先级，是否置顶                 |
| status       | int          |      | 是否封禁或逻辑删除该讨论         |
| gmt_create   | datetime     |      | 创建时间                         |
| gmt_modified | datetime     |      | 修改时间                         |



discussion_like表

| 列名         | 实体属性类型 | 键   | 备注           |
| ------------ | ------------ | ---- | -------------- |
| id           | long         | 主键 | auto_increment |
| did          | int          | 外键 | 讨论id         |
| uid          | String       | 外键 | 用户id         |
| gmt_create   | datetime     |      | 创建时间       |
| gmt_modified | datetime     |      | 修改时间       |



discussion_report表

| 列名         | 实体属性类型 | 键   | 备注           |
| ------------ | ------------ | ---- | -------------- |
| id           | long         | 主键 | auto_increment |
| did          | int          | 外键 | 讨论id         |
| reporter     | String       | 外键 | 举报者的用户名 |
| content      | String       |      | 举报内容       |
| status       | boolean      |      | 是否已读       |
| gmt_create   | datetime     |      | 创建时间       |
| gmt_modified | datetime     |      | 修改时间       |



comment表

| 列名         | 实体属性类型 | 键   | 备注                                   |
| ------------ | ------------ | ---- | -------------------------------------- |
| id           | int          | 主键 | auto_increment                         |
| cid          | long         | 外键 | 比赛id，NULL表示无引用比赛             |
| did          | int          | 外键 | 讨论id，NULL表示无引用讨论             |
| content      | String       |      | 评论内容                               |
| from_uid     | String       | 外键 | 评论者id                               |
| from_name    | String       | 外键 | 评论者用户名                           |
| from_avatar  | String       | 外键 | 评论者头像地址                         |
| from_role    | String       | 外键 | 评论者角色                             |
| like_num     | int          |      | 点赞数量                               |
| status       | int          |      | 是否封禁或逻辑删除该评论，0正常，1封禁 |
| gmt_create   | datetime     |      | 创建时间                               |
| gmt_modified | datetime     |      | 修改时间                               |



comment_like表

| 列名         | 实体属性类型 | 键   | 备注           |
| ------------ | ------------ | ---- | -------------- |
| id           | lint         | 主键 | auto_increment |
| cid          | int          | 外键 | 评论id         |
| uid          | String       | 外键 | 用户id         |
| gmt_create   | datetime     |      | 创建时间       |
| gmt_modified | datetime     |      | 修改时间       |



reply表

| 列名         | 实体属性类型 | 键   | 备注                                   |
| ------------ | ------------ | ---- | -------------------------------------- |
| id           | int          | 主键 | auto_increment                         |
| comment_id   | ind          | 外键 | 评论id                                 |
| content      | String       |      | 回复的内容                             |
| from_uid     | String       | 外键 | 回复评论者id                           |
| from_name    | String       | 外键 | 回复评论者用户名                       |
| from_avatar  | String       | 外键 | 回复评论者头像地址                     |
| from_role    | String       | 外键 | 回复评论者角色                         |
| to_uid       | String       | 外键 | 被回复的用户id                         |
| to_name      | String       | 外键 | 被回复的用户名                         |
| to_avatar    | String       | 外键 | 被回复的用户头像地址                   |
| status       | int          |      | 是否封禁或逻辑删除该回复，0正常，1封禁 |
| gmt_create   | datetime     |      | 创建时间                               |
| gmt_modified | datetime     |      | 修改时间                               |

## 文件模块

file表

| 列名         | 实体属性类型 | 键   | 备注                     |
| ------------ | ------------ | ---- | ------------------------ |
| id           | long         | 主键 | auto_increment           |
| uid          | String       |      | 用户id                   |
| name         | String       |      | 文件名                   |
| suffix       | String       |      | 文件后缀格式             |
| folder_path  | String       |      | 文件所在文件夹的路径     |
| file_path    | String       |      | 文件绝对路径             |
| type         | String       |      | 文件所属类型，例如avatar |
| delete       | String       |      | 是否删除                 |
| gmt_create   | datetime     |      | 创建时间                 |
| gmt_modified | datetime     |      | 修改时间                 |

## 判题机模块

judge_server表

| 列名            | 实体属性类型 | 键   | 备注                      |
| --------------- | ------------ | ---- | ------------------------- |
| id              | int          | 主键 | auto_increment            |
| name            | String       |      | 判题服务名字              |
| ip              | String       |      | 判题机ip                  |
| port            | int          |      | 判题机端口号              |
| url             | String       |      | ip:port                   |
| cpu_core        | int          |      | 判题机所在服务器cpu核心数 |
| task_number     | int          |      | 当前判题数                |
| max_task_number | int          |      | 判题并发最大数            |
| status          | int          |      | 0可用，1不可用            |
| version         | long         |      | 版本控制                  |
| is_remote       | boolean      |      | 是否为远程判题vj          |
| gmt_create      | datetime     |      | 创建时间                  |
| gmt_modified    | datetime     |      | 修改时间                  |
