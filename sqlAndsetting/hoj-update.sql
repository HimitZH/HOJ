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
	AND column_name = 'oi_rank_score'
) THEN
	ALTER TABLE judge ADD COLUMN oi_rank_score INT(11) NULL COMMENT '该题在OI排行榜的分数';
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

