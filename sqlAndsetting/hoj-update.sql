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

