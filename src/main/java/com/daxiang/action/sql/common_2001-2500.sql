-- 2001.Sleep
INSERT INTO `action` (
	`id`,
	`name`,
	`description`,
	`class_name`,
	`need_driver`,
	`has_return_value`,
	`params`
)
VALUES
(
	2001,
	'休眠',
	'Thread.sleep',
	'com.daxiang.action.common.Sleep',
	0,
	0,
	'[{"name": "sleepTimeInSeconds", "description": "休眠时长，单位: 秒。eg.1.5"}]'
);

-- 2002.AssertEquals
INSERT INTO `action` (
	`id`,
	`name`,
	`description`,
	`class_name`,
	`need_driver`,
	`has_return_value`,
	`params`
)
VALUES
(
	2002,
	'AssertEquals',
	'Assert.assertEquals',
	'com.daxiang.action.common.AssertEquals',
	0,
	0,
	'[{"name": "expected", "description": "期望"},{"name": "actual", "description": "实际"}]'
);