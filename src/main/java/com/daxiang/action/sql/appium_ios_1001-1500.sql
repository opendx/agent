-- 1001.InstallApp
INSERT INTO `action` (
	`id`,
	`name`,
	`description`,
	`class_name`,
	`need_driver`,
	`has_return_value`,
	`platform`,
	`params`
)
VALUES
(
	1001,
	'安装app',
	'AppiumDriver.executeScript("mobile: installApp")',
	'com.daxiang.action.appium.ios.InstallApp',
	1,
	0,
	2,
	'[{"name": "appDownloadUrl", "description": "app下载地址"}]'
);

-- 1002.LaunchApp
INSERT INTO `action` (
	`id`,
	`name`,
	`description`,
	`class_name`,
	`need_driver`,
	`has_return_value`,
	`platform`,
	`params`
)
VALUES
(
	1002,
	'启动app',
	'AppiumDriver.executeScript("mobile: launchApp")',
	'com.daxiang.action.appium.ios.LaunchApp',
	1,
	0,
	2,
	'[{"name": "bundleId", "description": "bundleId"}]'
);

-- 1003.TerminateApp
INSERT INTO `action` (
	`id`,
	`name`,
	`description`,
	`class_name`,
	`need_driver`,
	`has_return_value`,
	`platform`,
	`params`
)
VALUES
(
	1003,
	'停止app',
	'AppiumDriver.executeScript("mobile: terminateApp")',
	'com.daxiang.action.appium.ios.TerminateApp',
	1,
	0,
	2,
	'[{"name": "bundleId", "description": "bundleId"}]'
);

-- 1004.UninstallApp
INSERT INTO `action` (
	`id`,
	`name`,
	`description`,
	`class_name`,
	`need_driver`,
	`has_return_value`,
	`platform`,
	`params`
)
VALUES
(
	1004,
	'卸载app',
	'AppiumDriver.executeScript("mobile: removeApp")',
	'com.daxiang.action.appium.ios.UninstallApp',
	1,
	0,
	2,
	'[{"name": "bundleId", "description": "bundleId"}]'
);