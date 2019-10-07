-- 1.executeJavaCode
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `has_return_value`,
  `params`
)
VALUES
(
  1,
  '执行java代码[executeJavaCode]',
  '$.executeJavaCode',
  0,
  '[{"name": "code", "description": "java代码"}]'
);

-- 2.uninstallApp
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `has_return_value`,
  `params`
)
VALUES
(
  2,
  '卸载App[uninstallApp]',
  '$.uninstallApp',
  0,
  '[{"name": "packageName", "description": "android: packageName, iOS: bundleId"}]'
);

-- 3.installApk
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `has_return_value`,
  `params`
)
VALUES
(
  3,
  '安装App[installApp]',
  '$.installApp',
  0,
  '[{"name": "appDownloadUrl", "description": "app下载地址"}]'
);

-- 4.clearApkData
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `has_return_value`,
  `platform`,
  `params`
)
VALUES
(
  4,
  '清除APK数据[clearApkData]',
  '$.clearApkData',
  0,
  1,
  '[{"name": "packageName", "description": "包名"}]'
);

-- 5.restartApk
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `has_return_value`,
  `platform`,
  `params`
)
VALUES
(
  5,
  '启动/重启Apk[restartApk]',
  '$.restartApk',
  0,
  1,
  '[{"name": "packageName", "description": "包名"},{"name": "launchActivity", "description": "启动Activity名"}]'
);

-- 6.restartIosApp
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `has_return_value`,
  `platform`,
  `params`
)
VALUES
(
  6,
  '启动/重启app[restartIosApp]',
  '$.restartIosApp',
  0,
  2,
  '[{"name": "bundleId", "description": "app bundleId"}]'
);

-- 7.click
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `has_return_value`,
  `return_value_desc`,
  `params`
)
VALUES
(
  7,
  '点击[click]',
  '$.click',
  1,
  'WebElement',
  '[{"name":"findBy","description":"查找方式","possibleValues":[{"value":"id","description":"MobileBy.id"},{"value":"AccessibilityId","description":"MobileBy.AccessibilityId"},{"value":"xpath","description":"MobileBy.xpath"},{"value":"AndroidUIAutomator","description":"MobileBy.AndroidUIAutomator"},{"value":"iOSClassChain","description":"MobileBy.iOSClassChain"},{"value":"iOSNsPredicateString","description":"MobileBy.iOSNsPredicateString"},{"value":"image","description":"MobileBy.image"}]},{"name":"value","description":"查找值"}]'
);

-- 8.findElement
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `has_return_value`,
  `return_value_desc`,
  `params`
)
VALUES
(
  8,
  '查找元素[findElement]',
  '$.findElement',
  1,
  'WebElement',
  '[{"name":"findBy","description":"查找方式","possibleValues":[{"value":"id","description":"MobileBy.id"},{"value":"AccessibilityId","description":"MobileBy.AccessibilityId"},{"value":"xpath","description":"MobileBy.xpath"},{"value":"AndroidUIAutomator","description":"MobileBy.AndroidUIAutomator"},{"value":"iOSClassChain","description":"MobileBy.iOSClassChain"},{"value":"iOSNsPredicateString","description":"MobileBy.iOSNsPredicateString"},{"value":"image","description":"MobileBy.image"}]},{"name":"value","description":"查找值"}]'
);

-- 9.findElements
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `has_return_value`,
  `return_value_desc`,
  `params`
)
VALUES
(
  9,
  '查找元素[findElements]',
  '$.findElements',
  1,
  'List<WebElement>',
  '[{"name":"findBy","description":"查找方式","possibleValues":[{"value":"id","description":"MobileBy.id"},{"value":"AccessibilityId","description":"MobileBy.AccessibilityId"},{"value":"xpath","description":"MobileBy.xpath"},{"value":"AndroidUIAutomator","description":"MobileBy.AndroidUIAutomator"},{"value":"iOSClassChain","description":"MobileBy.iOSClassChain"},{"value":"iOSNsPredicateString","description":"MobileBy.iOSNsPredicateString"},{"value":"image","description":"MobileBy.image"}]},{"name":"value","description":"查找值"}]'
);

-- 10.sendKeys
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `has_return_value`,
  `return_value_desc`,
  `params`
)
VALUES
(
  10,
  '输入[sendKeys]',
  '$.sendKeys',
  1,
  'WebElement',
  '[{"name":"findBy","description":"查找方式","possibleValues":[{"value":"id","description":"MobileBy.id"},{"value":"AccessibilityId","description":"MobileBy.AccessibilityId"},{"value":"xpath","description":"MobileBy.xpath"},{"value":"AndroidUIAutomator","description":"MobileBy.AndroidUIAutomator"},{"value":"iOSClassChain","description":"MobileBy.iOSClassChain"},{"value":"iOSNsPredicateString","description":"MobileBy.iOSNsPredicateString"},{"value":"image","description":"MobileBy.image"}]},{"name":"value","description":"查找值"},{"name": "content", "description": "输入内容"}]'
);

-- 11.setImplicitlyWaitTime
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `has_return_value`,
  `params`
)
VALUES
(
  11,
  '设置隐式等待时间[setImplicitlyWaitTime]',
  '$.setImplicitlyWaitTime',
  0,
  '[{"name": "implicitlyWaitTimeInSeconds", "description": "隐式等待时间，单位：秒"}]'
);

-- 12.waitForElementVisible
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `has_return_value`,
  `return_value_desc`,
  `params`
)
VALUES
(
  12,
  '等待元素可见[waitForElementVisible]',
  '$.waitForElementVisible',
  1,
  'WebElement',
  '[{"name":"findBy","description":"查找方式","possibleValues":[{"value":"id","description":"MobileBy.id"},{"value":"AccessibilityId","description":"MobileBy.AccessibilityId"},{"value":"xpath","description":"MobileBy.xpath"},{"value":"AndroidUIAutomator","description":"MobileBy.AndroidUIAutomator"},{"value":"iOSClassChain","description":"MobileBy.iOSClassChain"},{"value":"iOSNsPredicateString","description":"MobileBy.iOSNsPredicateString"},{"value":"image","description":"MobileBy.image"}]},{"name":"value","description":"查找值"},{"name": "maxWaitTimeInSeconds", "description": "最大等待时间"}]'
);

-- 13.switchContext
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `has_return_value`,
  `params`
)
VALUES
(
  13,
  '切换context[switchContext]',
  '$.switchContext',
  0,
  '[{"name":"context","description":"context","possibleValues":[{"value":"NATIVE_APP","description":"原生"},{"value":"WEBVIEW","description":"webview"}]}]'
);

-- 14.sleep
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `has_return_value`,
  `params`
)
VALUES
(
  14,
  '休眠[sleep]',
  '$.sleep',
  0,
  '[{"name": "sleepTimeInSeconds", "description": "休眠时长，单位: 秒。eg.1.5"}]'
);

-- 15.swipeInScreen
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `has_return_value`,
  `params`
)
VALUES
(
  15,
  '滑动屏幕[swipeInScreen]',
  '$.swipeInScreen',
  0,
  '[{"name":"startPoint","description":"起点，如: {x:0.5,y:0.5} => 屏幕中心点"},{"name":"endPoint","description":"终点，如: {x:0.5,y:0.5} => 屏幕中心点"}]'
);

-- 16.swipeInScreenAndFindElement
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `has_return_value`,
  `return_value_desc`,
  `params`
)
VALUES
(
  16,
  '滑动屏幕查找元素[swipeInScreenAndFindElement]',
  '$.swipeInScreenAndFindElement',
  1,
  'WebElement',
  '[{"name":"findBy","description":"查找方式","possibleValues":[{"value":"id","description":"MobileBy.id"},{"value":"AccessibilityId","description":"MobileBy.AccessibilityId"},{"value":"xpath","description":"MobileBy.xpath"},{"value":"AndroidUIAutomator","description":"MobileBy.AndroidUIAutomator"},{"value":"iOSClassChain","description":"MobileBy.iOSClassChain"},{"value":"iOSNsPredicateString","description":"MobileBy.iOSNsPredicateString"},{"value":"image","description":"MobileBy.image"}]},{"name":"value","description":"查找值"},{"name":"startPoint","description":"起点，如: {x:0.5,y:0.5} => 屏幕中心点"},{"name":"endPoint","description":"终点，如: {x:0.5,y:0.5} => 屏幕中心点"},{"name":"maxSwipeCount","description":"最大滑动次数"}]'
);

-- 17.swipeInContainerElement
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `has_return_value`,
  `params`
)
VALUES
(
  17,
  '容器元素内滑动[swipeInContainerElement]',
  '$.swipeInContainerElement',
  0,
  '[{"name":"containerElement","description":"容器元素"},{"name":"startPoint","description":"起点，如: {x:0.5,y:0.5} => 容器中心点"},{"name":"endPoint","description":"终点，如: {x:0.5,y:0.5} => 容器中心点"}]'
);

-- 18.swipeInContainerElementAndFindElement
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `has_return_value`,
  `return_value_desc`,
  `params`
)
VALUES
(
  18,
  '容器元素内滑动查找元素',
  '$.swipeInContainerElementAndFindElement',
  1,
  'WebElement',
  '[{"name":"containerElement","description":"容器元素"},{"name":"findBy","description":"查找方式","possibleValues":[{"value":"id","description":"MobileBy.id"},{"value":"AccessibilityId","description":"MobileBy.AccessibilityId"},{"value":"xpath","description":"MobileBy.xpath"},{"value":"AndroidUIAutomator","description":"MobileBy.AndroidUIAutomator"},{"value":"iOSClassChain","description":"MobileBy.iOSClassChain"},{"value":"iOSNsPredicateString","description":"MobileBy.iOSNsPredicateString"},{"value":"image","description":"MobileBy.image"}]},{"name":"value","description":"查找值"},{"name":"startPoint","description":"起点，如: {x:0.5,y:0.5} => 容器中心点"},{"name":"endPoint","description":"终点，如: {x:0.5,y:0.5} => 容器中心点"},{"name":"maxSwipeCount","description":"最大滑动次数"}]'
);