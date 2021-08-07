USE `hoj`;

/*
* 2021.09.07 修改OI题目得分在OI排行榜新计分字段 分数计算为：OI题目总得分*0.1+2*题目难度
*/
ALTER TABLE judge ADD COLUMN oi_rank_score INT(11) NULL COMMENT '该题在OI排行榜的分数';