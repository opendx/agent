set @findbyAndValue = '{"name":"findBy","type":"String","description":"查找方式","possibleValues":[{"value":"id","description":"MobileBy.id"},{"value":"AccessibilityId","description":"MobileBy.AccessibilityId"},{"value":"xpath","description":"MobileBy.xpath"},{"value":"AndroidUIAutomator","description":"MobileBy.AndroidUIAutomator"},{"value":"iOSClassChain","description":"MobileBy.iOSClassChain"},{"value":"iOSNsPredicateString","description":"MobileBy.iOSNsPredicateString"},{"value":"image","description":"MobileBy.image"},{"value":"className","description":"MobileBy.className"},{"value":"name","description":"MobileBy.name"},{"value":"cssSelector","description":"MobileBy.cssSelector"},{"value":"linkText","description":"MobileBy.linkText"},{"value":"partialLinkText","description":"MobileBy.partialLinkText"},{"value":"tagName","description":"MobileBy.tagName"}]},{"name":"value","type":"String","description":"查找值"}';
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
  '[{"name":"code","type":"String","description":"java代码"}]'
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
  '[{"name":"packageName","type":"String","description":"android: packageName, iOS: bundleId"}]'
);

-- 3.installApp
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
  '[{"name":"appDownloadUrl","type":"String","description":"app下载地址"}]'
);

-- 4.clearApkData
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `return_value`,
  `platforms`,
  `params`
)
VALUES
(
  4,
  '清除APK数据[clearApkData]',
  '$.clearApkData',
  'void',
  '[1,3,4]',
  '[{"name":"packageName","type":"String","description":"包名"}]'
);

-- 5.restartApk
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `return_value`,
  `platforms`,
  `params`
)
VALUES
(
  5,
  '启动/重启Apk[restartApk]',
  '$.restartApk',
  'void',
  '[1,3,4]',
  '[{"name":"packageName","type":"String","description":"包名"},{"name":"launchActivity","type":"String","description":"启动Activity名"}]'
);

-- 6.restartIosApp
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `return_value`,
  `platforms`,
  `params`
)
VALUES
(
  6,
  '启动/重启app[restartIosApp]',
  '$.restartIosApp',
  'void',
  '[2]',
  '[{"name":"bundleId","type":"String","description": "app bundleId"}]'
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
  REPLACE('[#]','#',@findbyAndValue)
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
  REPLACE('[#]','#',@findbyAndValue)
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
  REPLACE('[#]','#',@findbyAndValue)
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
  REPLACE('[#,{"name":"content","type":"String","description":"输入内容"}]','#',@findbyAndValue)
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
  '[{"name":"implicitlyWaitTimeInSeconds","type":"String","description":"隐式等待时间(秒)"}]'
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
  REPLACE('[#,{"name":"maxWaitTimeInSeconds","type":"String","description":"最大等待时间(秒)"}]','#',@findbyAndValue)
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
  'void',
  '[{"name":"context","type":"String","description":"context","possibleValues":[{"value":"NATIVE_APP","description":"原生"}]}]'
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
  '[{"name":"sleepTimeInSeconds","type":"String","description": "休眠时长(秒)，支持小数，如: 1.5"}]'
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
  '[{"name":"startPoint","type":"String","description":"起点，如: {x:0.5,y:0.5} => 屏幕中心点"},{"name":"endPoint","type":"String","description":"终点，如: {x:0.5,y:0.5} => 屏幕中心点"},{"name":"durationInMsOfSwipeOneTime","type":"String","description":"滑动一次的时间，单位: ms。时间越短，滑的距离越长"}]'
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
  REPLACE('[#,{"name":"startPoint","type":"String","description":"起点，如: {x:0.5,y:0.5} => 屏幕中心点"},{"name":"endPoint","type":"String","description":"终点，如: {x:0.5,y:0.5} => 屏幕中心点"},{"name":"maxSwipeCount","type":"String","description":"最大滑动次数"},{"name":"durationInMsOfSwipeOneTime","type":"String","description":"滑动一次的时间，单位: ms。时间越短，滑的距离越长"}]','#',@findbyAndValue)
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
  '[{"name":"containerElement","type":"WebElement","description":"容器元素"},{"name":"startPoint","type":"String","description":"起点，如: {x:0.5,y:0.5} => 容器中心点"},{"name":"endPoint","type":"String","description":"终点，如: {x:0.5,y:0.5} => 容器中心点"},{"name":"durationInMsOfSwipeOneTime","type":"String","description":"滑动一次的时间，单位: ms。时间越短，滑的距离越长"}]'
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
  REPLACE('[{"name":"containerElement","type":"WebElement","description":"容器元素"},#,{"name":"startPoint","type":"String","description":"起点，如: {x:0.5,y:0.5} => 容器中心点"},{"name":"endPoint","type":"String","description":"终点，如: {x:0.5,y:0.5} => 容器中心点"},{"name":"maxSwipeCount","type":"String","description":"最大滑动次数"},{"name":"durationInMsOfSwipeOneTime","type":"String","description":"滑动一次的时间，单位: ms。时间越短，滑的距离越长"}]','#',@findbyAndValue)
);

-- 19.switchWindow
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
  '[web]切换窗口[switchWindow]',
  '$.switchWindow',
  'void',
  '[{"name":"window","type":"String","description":"窗口"}]'
);

-- 20.waitForElementPresence
INSERT INTO `action` (
  `id`,
  `name`,
  `description`,
  `invoke`,
  `return_value`,
  `params`
)
VALUES
(
  20,
  '等待元素出现(不一定可见)[waitForElementPresence]',
  '等待元素在Page DOM里出现，移动端可用于检测toast',
  '$.waitForElementPresence',
  'WebElement',
  REPLACE('[#,{"name":"maxWaitTimeInSeconds","type":"String","description":"最大等待时间(秒)"}]','#',@findbyAndValue)
);

-- 21.acceptAlert
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `return_value`
)
VALUES
(
  21,
  'accept对话框[acceptAlert]',
  '$.acceptAlert',
  'boolean'
);

-- 22.dismissAlert
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `return_value`
)
VALUES
(
  22,
  'dismiss对话框[dismissAlert]',
  '$.dismissAlert',
  'boolean'
);

-- 23.clearInput
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `return_value`,
  `params`
)
VALUES
(
  23,
  '清除输入框[clearInput]',
  '$.clearInput',
  'void',
  REPLACE('[#]','#',@findbyAndValue)
);

-- 24.asyncAcceptAlert
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `return_value`,
  `params`
)
VALUES
(
  24,
  '异步accept对话框[asyncAcceptAlert]',
  '$.asyncAcceptAlert',
  'void',
  '[{"name":"timeoutInSeconds","type":"String","description":"超时时间,单位: 秒"},{"name":"once","type":"String","description":"是否只处理一次, true or false"}]'
);

-- 25.asyncDismissAlert
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `return_value`,
  `params`
)
VALUES
(
  25,
  '异步dismiss对话框[asyncDismissAlert]',
  '$.asyncDismissAlert',
  'void',
  '[{"name":"timeoutInSeconds","type":"String","description":"超时时间,单位: 秒"},{"name":"once","type":"String","description":"是否只处理一次, true or false"}]'
);