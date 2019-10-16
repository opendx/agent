-- 1.executeJavaCode
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `return_value`,
  `params`
)
VALUES
(
  1,
  '执行java代码[executeJavaCode]',
  '$.executeJavaCode',
  'void',
  '[{"name": "code", "type": "String", "description": "java代码"}]'
);

-- 2.uninstallApp
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `return_value`,
  `params`
)
VALUES
(
  2,
  '卸载App[uninstallApp]',
  '$.uninstallApp',
  'void',
  '[{"name": "packageName", "type": "String", "description": "android: packageName, iOS: bundleId"}]'
);

-- 3.installApk
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `return_value`,
  `params`
)
VALUES
(
  3,
  '安装App[installApp]',
  '$.installApp',
  'void',
  '[{"name": "appDownloadUrl", "type": "String", "description": "app下载地址"}]'
);

-- 4.clearApkData
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `return_value`,
  `platform`,
  `params`
)
VALUES
(
  4,
  '清除APK数据[clearApkData]',
  '$.clearApkData',
  'void',
  1,
  '[{"name": "packageName", "type": "String", "description": "包名"}]'
);

-- 5.restartApk
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `return_value`,
  `platform`,
  `params`
)
VALUES
(
  5,
  '启动/重启Apk[restartApk]',
  '$.restartApk',
  'void',
  1,
  '[{"name": "packageName", "type": "String", "description": "包名"},{"name": "launchActivity", "type": "String", "description": "启动Activity名"}]'
);

-- 6.restartIosApp
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `return_value`,
  `platform`,
  `params`
)
VALUES
(
  6,
  '启动/重启app[restartIosApp]',
  '$.restartIosApp',
  'String',
  2,
  '[{"name": "bundleId", "type": "String", "description": "app bundleId"}]'
);

-- 7.click
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `return_value`,
  `params`
)
VALUES
(
  7,
  '点击[click]',
  '$.click',
  'WebElement',
  '[{"name":"findBy", "type": "String", "description":"查找方式","possibleValues":[{"value":"id","description":"MobileBy.id"},{"value":"AccessibilityId","description":"MobileBy.AccessibilityId"},{"value":"xpath","description":"MobileBy.xpath"},{"value":"AndroidUIAutomator","description":"MobileBy.AndroidUIAutomator"},{"value":"iOSClassChain","description":"MobileBy.iOSClassChain"},{"value":"iOSNsPredicateString","description":"MobileBy.iOSNsPredicateString"},{"value":"image","description":"MobileBy.image"}]},{"name":"value", "type": "String", "description":"查找值"}]'
);

-- 8.findElement
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `return_value`,
  `params`
)
VALUES
(
  8,
  '查找元素[findElement]',
  '$.findElement',
  'WebElement',
  '[{"name":"findBy", "type": "String", "description":"查找方式","possibleValues":[{"value":"id","description":"MobileBy.id"},{"value":"AccessibilityId","description":"MobileBy.AccessibilityId"},{"value":"xpath","description":"MobileBy.xpath"},{"value":"AndroidUIAutomator","description":"MobileBy.AndroidUIAutomator"},{"value":"iOSClassChain","description":"MobileBy.iOSClassChain"},{"value":"iOSNsPredicateString","description":"MobileBy.iOSNsPredicateString"},{"value":"image","description":"MobileBy.image"}]},{"name":"value", "type": "String", "description":"查找值"}]'
);

-- 9.findElements
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `return_value`,
  `params`
)
VALUES
(
  9,
  '查找元素[findElements]',
  '$.findElements',
  'List<WebElement>',
  '[{"name":"findBy", "type": "String", "description":"查找方式","possibleValues":[{"value":"id","description":"MobileBy.id"},{"value":"AccessibilityId","description":"MobileBy.AccessibilityId"},{"value":"xpath","description":"MobileBy.xpath"},{"value":"AndroidUIAutomator","description":"MobileBy.AndroidUIAutomator"},{"value":"iOSClassChain","description":"MobileBy.iOSClassChain"},{"value":"iOSNsPredicateString","description":"MobileBy.iOSNsPredicateString"},{"value":"image","description":"MobileBy.image"}]},{"name":"value", "type": "String", "description":"查找值"}]'
);

-- 10.sendKeys
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `return_value`,
  `params`
)
VALUES
(
  10,
  '输入[sendKeys]',
  '$.sendKeys',
  'WebElement',
  '[{"name":"findBy", "type": "String", "description":"查找方式","possibleValues":[{"value":"id","description":"MobileBy.id"},{"value":"AccessibilityId","description":"MobileBy.AccessibilityId"},{"value":"xpath","description":"MobileBy.xpath"},{"value":"AndroidUIAutomator","description":"MobileBy.AndroidUIAutomator"},{"value":"iOSClassChain","description":"MobileBy.iOSClassChain"},{"value":"iOSNsPredicateString","description":"MobileBy.iOSNsPredicateString"},{"value":"image","description":"MobileBy.image"}]},{"name":"value", "type": "String", "description":"查找值"},{"name": "content", "type": "String",  "description": "输入内容"}]'
);

-- 11.setImplicitlyWaitTime
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `return_value`,
  `params`
)
VALUES
(
  11,
  '设置隐式等待时间[setImplicitlyWaitTime]',
  '$.setImplicitlyWaitTime',
  'void',
  '[{"name": "implicitlyWaitTimeInSeconds", "type": "String", "description": "隐式等待时间(秒)"}]'
);

-- 12.waitForElementVisible
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `return_value`,
  `params`
)
VALUES
(
  12,
  '等待元素可见[waitForElementVisible]',
  '$.waitForElementVisible',
  'WebElement',
  '[{"name":"findBy", "type": "String", "description":"查找方式","possibleValues":[{"value":"id","description":"MobileBy.id"},{"value":"AccessibilityId","description":"MobileBy.AccessibilityId"},{"value":"xpath","description":"MobileBy.xpath"},{"value":"AndroidUIAutomator","description":"MobileBy.AndroidUIAutomator"},{"value":"iOSClassChain","description":"MobileBy.iOSClassChain"},{"value":"iOSNsPredicateString","description":"MobileBy.iOSNsPredicateString"},{"value":"image","description":"MobileBy.image"}]},{"name":"value", "type": "String", "description":"查找值"},{"name": "maxWaitTimeInSeconds", "type": "String", "description": "最大等待时间(秒)"}]'
);

-- 13.switchContext
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `return_value`,
  `params`
)
VALUES
(
  13,
  '切换context[switchContext]',
  '$.switchContext',
  'String',
  '[{"name":"context", "type": "String", "description":"context","possibleValues":[{"value":"NATIVE_APP","description":"原生"},{"value":"WEBVIEW","description":"webview"}]}]'
);

-- 14.sleep
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `return_value`,
  `params`
)
VALUES
(
  14,
  '休眠[sleep]',
  '$.sleep',
  'void',
  '[{"name": "sleepTimeInSeconds", "type": "String", "description": "休眠时长(秒)，支持小数，如: 1.5"}]'
);

-- 15.swipeInScreen
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `return_value`,
  `params`
)
VALUES
(
  15,
  '滑动屏幕[swipeInScreen]',
  '$.swipeInScreen',
  'void',
  '[{"name":"startPoint", "type": "String", "description":"起点，如: {x:0.5,y:0.5} => 屏幕中心点"},{"name":"endPoint", "type": "String", "description":"终点，如: {x:0.5,y:0.5} => 屏幕中心点"}]'
);

-- 16.swipeInScreenAndFindElement
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `return_value`,
  `params`
)
VALUES
(
  16,
  '滑动屏幕查找元素[swipeInScreenAndFindElement]',
  '$.swipeInScreenAndFindElement',
  'WebElement',
  '[{"name":"findBy", "type": "String", "description":"查找方式","possibleValues":[{"value":"id","description":"MobileBy.id"},{"value":"AccessibilityId","description":"MobileBy.AccessibilityId"},{"value":"xpath","description":"MobileBy.xpath"},{"value":"AndroidUIAutomator","description":"MobileBy.AndroidUIAutomator"},{"value":"iOSClassChain","description":"MobileBy.iOSClassChain"},{"value":"iOSNsPredicateString","description":"MobileBy.iOSNsPredicateString"},{"value":"image","description":"MobileBy.image"}]},{"name":"value", "type": "String", "description":"查找值"},{"name":"startPoint", "type": "String", "description":"起点，如: {x:0.5,y:0.5} => 屏幕中心点"},{"name":"endPoint", "type": "String", "description":"终点，如: {x:0.5,y:0.5} => 屏幕中心点"},{"name":"maxSwipeCount", "type": "String", "description":"最大滑动次数"}]'
);

-- 17.swipeInContainerElement
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `return_value`,
  `params`
)
VALUES
(
  17,
  '容器元素内滑动[swipeInContainerElement]',
  '$.swipeInContainerElement',
  'void',
  '[{"name":"containerElement", "type": "WebElement", "description":"容器元素"},{"name":"startPoint", "type": "String", "description":"起点，如: {x:0.5,y:0.5} => 容器中心点"},{"name":"endPoint", "type": "String", "description":"终点，如: {x:0.5,y:0.5} => 容器中心点"}]'
);

-- 18.swipeInContainerElementAndFindElement
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `return_value`,
  `params`
)
VALUES
(
  18,
  '容器元素内滑动查找元素[swipeInContainerElementAndFindElement]',
  '$.swipeInContainerElementAndFindElement',
  'WebElement',
  '[{"name":"containerElement", "type": "WebElement", "description":"容器元素"},{"name":"findBy", "type": "String", "description":"查找方式","possibleValues":[{"value":"id","description":"MobileBy.id"},{"value":"AccessibilityId","description":"MobileBy.AccessibilityId"},{"value":"xpath","description":"MobileBy.xpath"},{"value":"AndroidUIAutomator","description":"MobileBy.AndroidUIAutomator"},{"value":"iOSClassChain","description":"MobileBy.iOSClassChain"},{"value":"iOSNsPredicateString","description":"MobileBy.iOSNsPredicateString"},{"value":"image","description":"MobileBy.image"}]},{"name":"value", "type": "String", "description":"查找值"},{"name":"startPoint", "type": "String", "description":"起点，如: {x:0.5,y:0.5} => 容器中心点"},{"name":"endPoint", "type": "String", "description":"终点，如: {x:0.5,y:0.5} => 容器中心点"},{"name":"maxSwipeCount", "type": "String", "description":"最大滑动次数"}]'
);

-- 19.switchToLastWindow
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `return_value`,
  `params`
)
VALUES
(
  19,
  '[web]切换到最新窗口[switchToLastWindow]',
  '$.switchToLastWindow',
  'void',
  '[]'
);

-- 20.switchWindow
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `return_value`,
  `params`
)
VALUES
(
  20,
  '[web]切换窗口[switchWindow]',
  '$.switchWindow',
  'void',
  '[{"name": "window", "type": "String", "description": "窗口"}]'
);