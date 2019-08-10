-- 501.ClearApkData
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
	501,
	'清除APK数据',
	'pm clear {packageName}',
	'com.daxiang.action.appium.android.ClearApkData',
	1,
	0,
	1,
	'[{"name": "packageName", "description": "包名"}]'
);

-- 502.ExcuteAdbShellCommond
INSERT INTO `action` (
	`id`,
	`name`,
	`description`,
	`class_name`,
	`need_driver`,
	`has_return_value`,
	`return_value_desc`,
	`platform`,
	`params`
)
VALUES
(
	502,
	'adb shell',
	'adb shell {cmd}',
	'com.daxiang.action.appium.android.ExcuteAdbShellCommond',
	1,
	1,
	'执行命令返回的结果',
	1,
	'[{"name": "cmd", "description": "执行的命令"}]'
);

-- 503.InstallApk
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
	503,
	'安装Apk',
	'1.download apk 2.install -r',
	'com.daxiang.action.appium.android.InstallApk',
	1,
	0,
	1,
	'[{"name": "apkDownloadUrl", "description": "apk下载地址"}]'
);

-- 504.RestartApk
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
	504,
	'启动/重启 Apk',
	'am start -S -n {packageName}/{launchActivity}',
	'com.daxiang.action.appium.android.RestartApk',
	1,
	0,
	1,
	'[{"name": "packageName", "description": "包名"},{"name": "launchActivity", "description": "启动Activity名"}]'
);

-- 505.UninstallApk
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
	505,
	'卸载Apk',
	'pm unintall {packageName}',
	'com.daxiang.action.appium.android.UnInstallApk',
	1,
	0,
	1,
	'[{"name": "packageName", "description": "包名"}]'
);

-- 506.PressKey
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
	506,
	'发送keycode到android手机执行',
	'AndroidDriver.pressKeyCode',
	'com.daxiang.action.appium.android.PressKey',
	1,
	0,
	1,
	'[{"name": "androidKeyCode", "description": "AndroidKeyCode https://www.jianshu.com/p/f7ec856ff56f"}]'
);