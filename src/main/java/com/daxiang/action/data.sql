-- ################################################################################################################################

-- -----------------------------------------------com.daxiang.action.android(1-100)-----------------------------------------------
-- 1.ClearApkData
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
	1,
	'清除APK数据',
	'pm clear {packageName}',
	'com.daxiang.action.android.ClearApkData',
	1,
	0,
	1,
	'[{"name": "packageName", "description": "包名"}]'
);
-- 2.ExcuteAdbShellCommond
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
	2,
	'adb shell',
	'adb shell {cmd}',
	'com.daxiang.action.android.ExcuteAdbShellCommond',
	1,
	1,
	'执行命令返回的结果',
	1,
	'[{"name": "cmd", "description": "执行的命令"}]'
);
-- 3.InstallApk
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
	3,
	'安装Apk',
	'1.download apk 2.install -r',
	'com.daxiang.action.android.InstallApk',
	1,
	0,
	1,
	'[{"name": "apkDownloadUrl", "description": "apk下载地址"}]'
);
-- 4.RestartApk
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
	4,
	'启动/重启 Apk',
	'am start -S -n {packageName}/{launchActivity}',
	'com.daxiang.action.android.RestartApk',
	1,
	0,
	1,
	'[{"name": "packageName", "description": "包名"},{"name": "launchActivity", "description": "启动Activity名"}]'
);
-- 5.UninstallApk
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
	5,
	'卸载Apk',
	'pm unintall {packageName}',
	'com.daxiang.action.android.UnInstallApk',
	1,
	0,
	1,
	'[{"name": "packageName", "description": "包名"}]'
);
-- -----------------------------------------------com.daxiang.action.android(1-100)-----------------------------------------------

-- ################################################################################################################################

-- -----------------------------------------------com.daxiang.action.common(101-200)-----------------------------------------------
-- 101.Sleep
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
	101,
	'休眠',
	'Thread.sleep',
	'com.daxiang.action.common.Sleep',
	0,
	0,
	'[{"name": "sleepTimeInSeconds", "description": "休眠时长，单位：秒"}]'
);
-- 102.AssertEquals
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
	102,
	'AssertEquals',
	'Assert.assertEquals',
	'com.daxiang.action.common.AssertEquals',
	0,
	0,
	'[{"name": "expected", "description": "期望"},{"name": "actual", "description": "实际"}]'
);
-- -----------------------------------------------com.daxiang.action.common(101-200)-----------------------------------------------

-- ################################################################################################################################

-- -------------------------------------------com.daxiang.action.appium.android(201-300)-------------------------------------------
-- 202.PressKey
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
	202,
	'发送keycode到android手机执行',
	'AndroidDriver.pressKeyCode',
	'com.daxiang.action.appium.android.PressKey',
	1,
	0,
	1,
	'[{"name": "androidKeyCode", "description": "AndroidKeyCode https://www.jianshu.com/p/f7ec856ff56f"}]'
);
-- -------------------------------------------com.daxiang.action.appium.android(201-300)-------------------------------------------

-- ################################################################################################################################

-- -----------------------------------------------com.daxiang.action.appium(301-600)-----------------------------------------------
-- 301.Click
INSERT INTO `action` (
	`id`,
	`name`,
	`description`,
	`class_name`,
	`need_driver`,
	`has_return_value`,
	`return_value_desc`,
	`params`
)
VALUES
(
	301,
	'点击',
	'WebElement.click',
	'com.daxiang.action.appium.Click',
	1,
	1,
	'WebElement',
	'[{"name":"findBy","description":"查找方式","possibleValues":[{"value":"id","description":"MobileBy.id"},{"value":"AccessibilityId","description":"MobileBy.AccessibilityId"},{"value":"xpath","description":"MobileBy.xpath"},{"value":"AndroidUIAutomator","description":"MobileBy.AndroidUIAutomator"},{"value":"iOSClassChain","description":"MobileBy.iOSClassChain"},{"value":"iOSNsPredicateString","description":"MobileBy.iOSNsPredicateString"},{"value":"image","description":"MobileBy.image"}]},{"name":"value","description":"查找值"}]'
);
-- 302. ClickElement
INSERT INTO `action` (
	`id`,
	`name`,
	`description`,
	`class_name`,
	`need_driver`,
	`has_return_value`,
	`return_value_desc`,
	`params`
)
VALUES
(
  302,
	'点击元素',
	'WebElement.click',
	'com.daxiang.action.appium.ClickElement',
	0,
	1,
  'WebElement',
	'[{"name": "webElement", "description": "元素对象"}]'
);
-- 303.FindElement
INSERT INTO `action` (
	`id`,
	`name`,
	`description`,
	`class_name`,
	`need_driver`,
	`has_return_value`,
	`return_value_desc`,
	`params`
)
VALUES
(
	303,
	'查找元素',
	'AppiumDriver.findElement',
	'com.daxiang.action.appium.FindElement',
	1,
	1,
	'WebElement',
  '[{"name":"findBy","description":"查找方式","possibleValues":[{"value":"id","description":"MobileBy.id"},{"value":"AccessibilityId","description":"MobileBy.AccessibilityId"},{"value":"xpath","description":"MobileBy.xpath"},{"value":"AndroidUIAutomator","description":"MobileBy.AndroidUIAutomator"},{"value":"iOSClassChain","description":"MobileBy.iOSClassChain"},{"value":"iOSNsPredicateString","description":"MobileBy.iOSNsPredicateString"},{"value":"image","description":"MobileBy.image"}]},{"name":"value","description":"查找值"}]'
);
-- 304.SendKeys
INSERT INTO `action` (
	`id`,
	`name`,
	`description`,
	`class_name`,
	`need_driver`,
	`has_return_value`,
	`return_value_desc`,
	`params`
)
VALUES
(
  304,
	'输入',
	'WebElement.sendKeys',
	'com.daxiang.action.appium.SendKeys',
	1,
	1,
	'WebElement',
  '[{"name":"findBy","description":"查找方式","possibleValues":[{"value":"id","description":"MobileBy.id"},{"value":"AccessibilityId","description":"MobileBy.AccessibilityId"},{"value":"xpath","description":"MobileBy.xpath"},{"value":"AndroidUIAutomator","description":"MobileBy.AndroidUIAutomator"},{"value":"iOSClassChain","description":"MobileBy.iOSClassChain"},{"value":"iOSNsPredicateString","description":"MobileBy.iOSNsPredicateString"},{"value":"image","description":"MobileBy.image"}]},{"name":"value","description":"查找值"},{"name": "content", "description": "输入内容"}]'
);
-- 305.SetImplicitlyWaitTime
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
  305,
  '设置隐士等待时间',
  'AppiumDriver.manage().timeouts().implicitlyWait',
  'com.daxiang.action.appium.SetImplicitlyWaitTime',
  1,
  0,
  '[{"name": "implicitlyWaitTimeInSeconds", "description": "隐士等待时间，单位：秒"}]'
);
-- 306.FindElements
INSERT INTO `action` (
	`id`,
	`name`,
	`description`,
	`class_name`,
	`need_driver`,
	`has_return_value`,
	`return_value_desc`,
	`params`
)
VALUES
(
	306,
	'查找元素',
	'AppiumDriver.findElements',
	'com.daxiang.action.appium.FindElements',
	1,
	1,
	'List<WebElement>',
  '[{"name":"findBy","description":"查找方式","possibleValues":[{"value":"id","description":"MobileBy.id"},{"value":"AccessibilityId","description":"MobileBy.AccessibilityId"},{"value":"xpath","description":"MobileBy.xpath"},{"value":"AndroidUIAutomator","description":"MobileBy.AndroidUIAutomator"},{"value":"iOSClassChain","description":"MobileBy.iOSClassChain"},{"value":"iOSNsPredicateString","description":"MobileBy.iOSNsPredicateString"},{"value":"image","description":"MobileBy.image"}]},{"name":"value","description":"查找值"}]'
);
-- 307.WaitForElementVisible
INSERT INTO `action` (
  `id`,
  `name`,
  `description`,
  `class_name`,
  `need_driver`,
  `has_return_value`,
  `return_value_desc`,
  `params`
)
VALUES
(
  307,
  '等待元素可见',
  'WebDriverWait().until(ExpectedConditions.visibilityOfElementLocated)',
  'com.daxiang.action.appium.WaitForElementVisible',
  1,
  1,
  'WebElement',
  '[{"name":"findBy","description":"查找方式","possibleValues":[{"value":"id","description":"MobileBy.id"},{"value":"AccessibilityId","description":"MobileBy.AccessibilityId"},{"value":"xpath","description":"MobileBy.xpath"},{"value":"AndroidUIAutomator","description":"MobileBy.AndroidUIAutomator"},{"value":"iOSClassChain","description":"MobileBy.iOSClassChain"},{"value":"iOSNsPredicateString","description":"MobileBy.iOSNsPredicateString"},{"value":"image","description":"MobileBy.image"}]},{"name":"value","description":"查找值"},{"name": "maxWaitTimeInSeconds", "description": "最大等待时间"}]'
);
-- 308.ElementSendKeys
INSERT INTO `action` (
	`id`,
	`name`,
	`description`,
	`class_name`,
	`need_driver`,
	`has_return_value`,
	`return_value_desc`,
	`params`
)
VALUES
(
	308,
	'元素输入',
	'WebElement.sendKeys',
	'com.daxiang.action.appium.ElementSendKeys',
	0,
	1,
	'WebElement',
	'[{"name": "webElement", "description": "元素对象"}, {"name": "content", "description": "输入内容"}]'
);
-- 309.ExecuteScript
INSERT INTO `action` (
  `id`,
  `name`,
  `description`,
  `class_name`,
  `need_driver`,
  `has_return_value`,
  `return_value_desc`,
  `params`
)
VALUES
(
  309,
  '执行脚本',
  'AppiumDriver.executeScript',
  'com.daxiang.action.appium.ExecuteScript',
  1,
  1,
  'Object',
  '[{"name": "script", "description": "脚本内容 http://appium.io/docs/en/commands/mobile-command/"},{"name": "args", "description": "参数值"}]'
);

-- 310.GetAttribute
INSERT INTO `action` (
	`id`,
	`name`,
	`description`,
	`class_name`,
	`need_driver`,
	`has_return_value`,
	`return_value_desc`,
	`params`
)
VALUES
(
	310,
	'获取元素属性',
	'WebElement.getAttribute',
	'com.daxiang.action.appium.GetAttribute',
	0,
	1,
	'元素属性值',
	'[{"name": "webElement", "description": "元素对象"},{"name": "attributeName", "description": "属性名"}]'
);
-- -----------------------------------------------com.daxiang.action.appium(301-600)-----------------------------------------------

-- ################################################################################################################################




