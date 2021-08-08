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
