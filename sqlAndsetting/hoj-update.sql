USE `hoj`;

/*
* 2021.08.07 修改OI题目得分在OI排行榜新计分字段 分数计算为：OI题目总得分*0.1+2*题目难度
*/
DROP PROCEDURE
IF EXISTS judge_Add_oi_rank_score;
DELIMITER $$
 
CREATE PROCEDURE judge_Add_oi_rank_score ()
BEGIN
 
IF NOT EXISTS (
	SELECT
		1
	FROM
		information_schema.`COLUMNS`
	WHERE
		table_name = 'judge'
	AND column_name = 'oi_rank'
) THEN
	ALTER TABLE judge ADD COLUMN oi_rank INT(11) NULL COMMENT '该题在OI排行榜的分数';
END
IF ; END$$
 
DELIMITER ; 
CALL judge_Add_oi_rank_score ;

DROP PROCEDURE judge_Add_oi_rank_score;

/*
* 2021.08.08 增加vjudge_submit_id在vjudge判题获取提交id后存储，当等待结果超时，下次重判时可用该提交id直接获取结果。
			 同时vjudge_username、vjudge_password分别记录提交账号密码
*/
DROP PROCEDURE
IF EXISTS judge_Add_vjudge_submit_id;
DELIMITER $$
 
CREATE PROCEDURE judge_Add_vjudge_submit_id ()
BEGIN
 
IF NOT EXISTS (
	SELECT
		1
	FROM
		information_schema.`COLUMNS`
	WHERE
		table_name = 'judge'
	AND column_name = 'vjudge_submit_id'
) THEN
	ALTER TABLE judge ADD COLUMN vjudge_submit_id BIGINT UNSIGNED NULL  COMMENT 'vjudge判题在其它oj的提交id';
	ALTER TABLE judge ADD COLUMN vjudge_username VARCHAR(255) NULL  COMMENT 'vjudge判题在其它oj的提交用户名';
	ALTER TABLE judge ADD COLUMN vjudge_password VARCHAR(255) NULL  COMMENT 'vjudge判题在其它oj的提交账号密码';
END
IF ; END$$
 
DELIMITER ; 
CALL judge_Add_vjudge_submit_id ;

DROP PROCEDURE judge_Add_vjudge_submit_id;


/*
* 2021.09.21 比赛增加打印、账号限制的功能，增大真实姓名长度
*/

DROP PROCEDURE
IF EXISTS contest_Add_print_and_limit;
DELIMITER $$
 
CREATE PROCEDURE contest_Add_print_and_limit ()
BEGIN
 
IF NOT EXISTS (
	SELECT
		1
	FROM
		information_schema.`COLUMNS`
	WHERE
		table_name = 'contest'
	AND column_name = 'open_print'
) THEN
	ALTER TABLE contest ADD COLUMN open_print tinyint(1) DEFAULT '0' COMMENT '是否打开打印功能';
    ALTER TABLE contest ADD COLUMN open_account_limit tinyint(1) DEFAULT '0' COMMENT '是否开启账号限制';
    ALTER TABLE contest ADD COLUMN account_limit_rule mediumtext COMMENT '账号限制规则';
	ALTER TABLE `hoj`.`user_info` CHANGE `realname` `realname` VARCHAR(100) CHARSET utf8 COLLATE utf8_general_ci NULL  COMMENT '真实姓名';
END
IF ; END$$
 
DELIMITER ; 
CALL contest_Add_print_and_limit ;

DROP PROCEDURE contest_Add_print_and_limit;



DROP PROCEDURE
IF EXISTS Add_contest_print;
DELIMITER $$
 
CREATE PROCEDURE Add_contest_print ()
BEGIN
 
IF NOT EXISTS (
	SELECT
		1
	FROM
		information_schema.`COLUMNS`
	WHERE
		table_name = 'contest_print'
) THEN
	CREATE TABLE `contest_print` (
	  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
	  `username` varchar(100) DEFAULT NULL,
	  `realname` varchar(100) DEFAULT NULL,
	  `cid` bigint(20) unsigned DEFAULT NULL,
	  `content` longtext NOT NULL,
	  `status` int(11) DEFAULT '0',
	  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP,
	  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP,
	  PRIMARY KEY (`id`),
	  KEY `cid` (`cid`),
	  KEY `username` (`username`),
	  CONSTRAINT `contest_print_ibfk_1` FOREIGN KEY (`cid`) REFERENCES `contest` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
	  CONSTRAINT `contest_print_ibfk_2` FOREIGN KEY (`username`) REFERENCES `user_info` (`username`) ON DELETE CASCADE ON UPDATE CASCADE
	) ENGINE=InnoDB DEFAULT CHARSET=utf8;
END
IF ; END$$
 
DELIMITER ; 
CALL Add_contest_print ;

DROP PROCEDURE Add_contest_print;


/*
* 2021.10.04 增加站内消息系统，包括评论我的、收到的赞、回复我的、系统通知、我的消息五个模块
*/

DROP PROCEDURE
IF EXISTS Add_msg_table;
DELIMITER $$
 
CREATE PROCEDURE Add_msg_table ()
BEGIN
 
IF NOT EXISTS (
	SELECT
		1
	FROM
		information_schema.`COLUMNS`
	WHERE
		table_name = 'msg_remind'
) THEN
	CREATE TABLE `admin_sys_notice` (
	  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
	  `title` varchar(255) DEFAULT NULL COMMENT '标题',
	  `content` longtext COMMENT '内容',
	  `type` varchar(255) DEFAULT NULL COMMENT '发给哪些用户类型',
	  `state` tinyint(1) DEFAULT '0' COMMENT '是否已拉取给用户',
	  `recipient_id` varchar(32) DEFAULT NULL COMMENT '接受通知的用户id',
	  `admin_id` varchar(32) DEFAULT NULL COMMENT '发送通知的管理员id',
	  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
	  PRIMARY KEY (`id`),
	  KEY `recipient_id` (`recipient_id`),
	  KEY `admin_id` (`admin_id`),
	  CONSTRAINT `admin_sys_notice_ibfk_1` FOREIGN KEY (`recipient_id`) REFERENCES `user_info` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE,
	  CONSTRAINT `admin_sys_notice_ibfk_2` FOREIGN KEY (`admin_id`) REFERENCES `user_info` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE
	) ENGINE=InnoDB DEFAULT CHARSET=utf8;
	
	CREATE TABLE `msg_remind` (
	  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
	  `action` varchar(255) NOT NULL COMMENT '动作类型，如点赞讨论帖Like_Post、点赞评论Like_Discuss、评论Discuss、回复Reply等',
	  `source_id` int(10) unsigned DEFAULT NULL COMMENT '消息来源id，讨论id或比赛id',
	  `source_type` varchar(255) DEFAULT NULL COMMENT '事件源类型：''Discussion''、''Contest''等',
	  `source_content` varchar(255) DEFAULT NULL COMMENT '事件源的内容，比如回复的内容，评论的帖子标题等等',
	  `quote_id` int(10) unsigned DEFAULT NULL COMMENT '事件引用上一级评论或回复id',
	  `quote_type` varchar(255) DEFAULT NULL COMMENT '事件引用上一级的类型：Comment、Reply',
	  `url` varchar(255) DEFAULT NULL COMMENT '事件所发生的地点链接 url',
	  `state` tinyint(1) DEFAULT '0' COMMENT '是否已读',
	  `sender_id` varchar(32) DEFAULT NULL COMMENT '操作者的id',
	  `recipient_id` varchar(32) DEFAULT NULL COMMENT '接受消息的用户id',
	  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
	  PRIMARY KEY (`id`),
	  KEY `sender_id` (`sender_id`),
	  KEY `recipient_id` (`recipient_id`),
	  CONSTRAINT `msg_remind_ibfk_1` FOREIGN KEY (`sender_id`) REFERENCES `user_info` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE,
	  CONSTRAINT `msg_remind_ibfk_2` FOREIGN KEY (`recipient_id`) REFERENCES `user_info` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE
	) ENGINE=InnoDB DEFAULT CHARSET=utf8;
	
	CREATE TABLE `user_sys_notice` (
	  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
	  `sys_notice_id` bigint(20) unsigned DEFAULT NULL COMMENT '系统通知的id',
	  `recipient_id` varchar(32) DEFAULT NULL COMMENT '接受通知的用户id',
	  `type` varchar(255) DEFAULT NULL COMMENT '消息类型，系统通知sys、我的信息mine',
	  `state` tinyint(1) DEFAULT '0' COMMENT '是否已读',
	  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '读取时间',
	  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	  PRIMARY KEY (`id`),
	  KEY `sys_notice_id` (`sys_notice_id`),
	  KEY `recipient_id` (`recipient_id`),
	  CONSTRAINT `user_sys_notice_ibfk_1` FOREIGN KEY (`sys_notice_id`) REFERENCES `admin_sys_notice` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
	  CONSTRAINT `user_sys_notice_ibfk_2` FOREIGN KEY (`recipient_id`) REFERENCES `user_info` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE
	) ENGINE=InnoDB DEFAULT CHARSET=utf8;
END
IF ; END$$
 
DELIMITER ; 
CALL Add_msg_table;

DROP PROCEDURE Add_msg_table;




/*
* 2021.10.06 user_info增加性别列gender 比赛榜单用户名称显示可选
			 
*/
DROP PROCEDURE
IF EXISTS user_info_Add_gender;
DELIMITER $$
 
CREATE PROCEDURE user_info_Add_gender ()
BEGIN
 
IF NOT EXISTS (
	SELECT
		1
	FROM
		information_schema.`COLUMNS`
	WHERE
		table_name = 'user_info'
	AND column_name = 'gender'
) THEN
	ALTER TABLE user_info ADD COLUMN gender varchar(20) DEFAULT 'secrecy'  NOT NULL COMMENT '性别';
END
IF ; END$$
 
DELIMITER ; 
CALL user_info_Add_gender ;

DROP PROCEDURE user_info_Add_gender;


DROP PROCEDURE
IF EXISTS contest_Add_rank_show_name;
DELIMITER $$
 
CREATE PROCEDURE contest_Add_rank_show_name ()
BEGIN
 
IF NOT EXISTS (
	SELECT
		1
	FROM
		information_schema.`COLUMNS`
	WHERE
		table_name = 'contest'
	AND column_name = 'rank_show_name'
) THEN
	ALTER TABLE contest ADD COLUMN rank_show_name varchar(20) DEFAULT 'username' COMMENT '排行榜显示（username、nickname、realname）';
END
IF ; END$$
 
DELIMITER ; 
CALL contest_Add_rank_show_name ;

DROP PROCEDURE contest_Add_rank_show_name;

/*
* 2021.10.08 user_info增加性别列gender 比赛榜单用户名称显示可选
			 
*/
DROP PROCEDURE
IF EXISTS contest_problem_Add_color;
DELIMITER $$
 
CREATE PROCEDURE contest_problem_Add_color ()
BEGIN
 
IF NOT EXISTS (
	SELECT
		1
	FROM
		information_schema.`COLUMNS`
	WHERE
		table_name = 'contest_problem'
	AND column_name = 'color'
) THEN
	ALTER TABLE contest_problem ADD COLUMN `color` VARCHAR(255) NULL   COMMENT '气球颜色';
	ALTER TABLE user_info ADD COLUMN `title_name` VARCHAR(255) NULL   COMMENT '头衔、称号';
	ALTER TABLE user_info ADD COLUMN `title_color` VARCHAR(255) NULL   COMMENT '头衔、称号的颜色';
END
IF ; END$$
 
DELIMITER ; 
CALL contest_problem_Add_color ;

DROP PROCEDURE contest_problem_Add_color;
