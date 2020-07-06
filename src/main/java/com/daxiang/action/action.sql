set @findbyAndValue = '{"name":"findBy","type":"String","description":"查找方式","possibleValues":[{"value":"id","description":"By.id"},{"value":"AccessibilityId","description":"By.AccessibilityId"},{"value":"xpath","description":"By.xpath"},{"value":"AndroidUIAutomator","description":"By.AndroidUIAutomator"},{"value":"iOSClassChain","description":"By.iOSClassChain"},{"value":"iOSNsPredicateString","description":"By.iOSNsPredicateString"},{"value":"image","description":"By.image"},{"value":"className","description":"By.className"},{"value":"name","description":"By.name"},{"value":"cssSelector","description":"By.cssSelector"},{"value":"linkText","description":"By.linkText"},{"value":"partialLinkText","description":"By.partialLinkText"},{"value":"tagName","description":"By.tagName"}]},{"name":"value","type":"String","description":"查找值"}';
-- 1~999 BaseAction platforms = null

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
  '执行java代码',
  '$.executeJavaCode',
  'void',
  '[{"name":"code","type":"String","description":"java代码"}]'
);

-- 2.sleep
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
  '休眠',
  '$.sleep',
  'void',
  '[{"name":"ms","type":"String","description": "休眠时长(毫秒)"}]'
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
  '点击',
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
  '查找元素',
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
  '查找元素列表',
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
  '输入',
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
  '设置隐式等待时间',
  '$.setImplicitlyWaitTime',
  'void',
  '[{"name":"seconds","type":"String","description":"隐式等待时间(秒)"}]'
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
  '等待元素可见',
  '$.waitForElementVisible',
  'WebElement',
  REPLACE('[#,{"name":"timeoutInSeconds","type":"String","description":"最大等待时间(秒)"}]','#',@findbyAndValue)
);

-- 13.waitForElementPresence
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
  13,
  '等待元素出现',
  '等待元素在Page DOM里出现，不一定可见。移动端可用于检测toast',
  '$.waitForElementPresence',
  'WebElement',
  REPLACE('[#,{"name":"timeoutInSeconds","type":"String","description":"最大等待时间(秒)"}]','#',@findbyAndValue)
);

-- 14.getUrl
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
  '[web]访问url',
  '$.getUrl',
  'void',
  '[{"name":"url","type":"String","description":"要访问的url"}]'
);

-- 17.isElementDisplayed
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
  '元素是否显示',
  '$.isElementDisplayed',
  'boolean',
   REPLACE('[#]','#',@findbyAndValue)
);

-- 18.isElementDisplayed
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
  '元素是否显示',
  '$.isElementDisplayed',
  'boolean',
  '[{"name":"element","type":"WebElement"}]'
);

-- 1000~1999 MobileAction platforms = [1,2]

-- 1000.switchContext
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `return_value`,
  `params`,
  `platforms`
)
VALUES
(
  1000,
  '切换context',
  '$.switchContext',
  'void',
  '[{"name":"context","type":"String","description":"context","possibleValues":[{"value":"NATIVE_APP","description":"原生"}]}]',
  '[1,2]'
);

-- 1001.installApp
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `return_value`,
  `params`,
  `platforms`
)
VALUES
(
  1001,
  '安装App',
  '$.installApp',
  'void',
  '[{"name":"appDownloadUrl","type":"String","description":"app下载地址"}]',
  '[1,2]'
);

-- 1002.uninstallApp
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `return_value`,
  `params`,
  `platforms`
)
VALUES
(
  1002,
  '卸载App',
  '$.uninstallApp',
  'void',
  '[{"name":"app","type":"String","description":"android: packageName, iOS: bundleId"}]',
  '[1,2]'
);

-- 1003.swipe
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `return_value`,
  `params`,
  `platforms`
)
VALUES
(
  1003,
  '滑动屏幕',
  '$.swipe',
  'void',
  '[{"name":"startPoint","type":"String","description":"起点，如: {x:0.5,y:0.5} => 屏幕中心点"},{"name":"endPoint","type":"String","description":"终点，如: {x:0.5,y:0.5} => 屏幕中心点"},{"name":"durationInMs","type":"String","description":"滑动一次的时间，单位: ms。时间越短，滑的距离越长"}]',
  '[1,2]'
);

-- 1004.swipeToFindElement
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `return_value`,
  `params`,
  `platforms`
)
VALUES
(
  1004,
  '滑动屏幕查找元素',
  '$.swipeToFindElement',
  'WebElement',
  REPLACE('[#,{"name":"startPoint","type":"String","description":"起点，如: {x:0.5,y:0.5} => 屏幕中心点"},{"name":"endPoint","type":"String","description":"终点，如: {x:0.5,y:0.5} => 屏幕中心点"},{"name":"maxSwipeCount","type":"String","description":"最大滑动次数"},{"name":"onceDurationInMs","type":"String","description":"滑动一次的时间，单位: ms。时间越短，滑的距离越长"}]','#',@findbyAndValue),
  '[1,2]'
);

-- 1005.swipeInContainer
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `return_value`,
  `params`,
  `platforms`
)
VALUES
(
  1005,
  '容器内滑动',
  '$.swipeInContainer',
  'void',
  '[{"name":"container","type":"WebElement","description":"容器元素"},{"name":"startPoint","type":"String","description":"起点，如: {x:0.5,y:0.5} => 容器中心点"},{"name":"endPoint","type":"String","description":"终点，如: {x:0.5,y:0.5} => 容器中心点"},{"name":"onceDurationInMs","type":"String","description":"滑动一次的时间，单位: ms。时间越短，滑的距离越长"}]',
  '[1,2]'
);

-- 1006.swipeInContainerToFindElement
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `return_value`,
  `params`,
  `platforms`
)
VALUES
(
  1006,
  '容器内滑动查找元素',
  '$.swipeInContainerToFindElement',
  'WebElement',
  REPLACE('[{"name":"container","type":"WebElement","description":"容器元素"},#,{"name":"startPoint","type":"String","description":"起点，如: {x:0.5,y:0.5} => 容器中心点"},{"name":"endPoint","type":"String","description":"终点，如: {x:0.5,y:0.5} => 容器中心点"},{"name":"maxSwipeCount","type":"String","description":"最大滑动次数"},{"name":"onceDurationInMs","type":"String","description":"滑动一次的时间，单位: ms。时间越短，滑的距离越长"}]','#',@findbyAndValue),
  '[1,2]'
);

-- 1007.acceptAlert
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `return_value`,
  `platforms`
)
VALUES
(
  1007,
  'accept对话框',
  '$.acceptAlert',
  'boolean',
  '[1,2]'
);

-- 1008.asyncAcceptAlert
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `return_value`,
  `params`,
  `platforms`
)
VALUES
(
  1008,
  '异步accept对话框',
  '$.asyncAcceptAlert',
  'void',
  '[{"name":"timeoutInSeconds","type":"String","description":"超时时间,单位: 秒"},{"name":"once","type":"String","description":"是否只处理一次, true or false"}]',
  '[1,2]'
);

-- 1009.dismissAlert
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `return_value`,
  `platforms`
)
VALUES
(
  1009,
  'dismiss对话框',
  '$.dismissAlert',
  'boolean',
  '[1,2]'
);

-- 1010.asyncDismissAlert
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `return_value`,
  `params`,
  `platforms`
)
VALUES
(
  1010,
  '异步dismiss对话框',
  '$.asyncDismissAlert',
  'void',
  '[{"name":"timeoutInSeconds","type":"String","description":"超时时间,单位: 秒"},{"name":"once","type":"String","description":"是否只处理一次, true or false"}]',
  '[1,2]'
);

-- 1011.clickByTouchAction
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `return_value`,
  `params`,
  `platforms`
)
VALUES
(
  1011,
  '(TouchAction)点击',
  '$.clickByTouchAction',
  'WebElement',
   REPLACE('[#]','#',@findbyAndValue),
  '[1,2]'
);

-- 2000~2999 AndroidAction platforms = [1]

-- 2000.clearApkData
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `return_value`,
  `params`,
  `platforms`
)
VALUES
(
  2000,
  '清除apk数据',
  '$.clearApkData',
  'void',
  '[{"name":"packageName","type":"String","description":"包名"}]',
  '[1]'
);

-- 2001.restartApk
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `return_value`,
  `params`,
  `platforms`
)
VALUES
(
  2001,
  '启动/重启apk',
  '$.restartApk',
  'void',
  '[{"name":"packageName","type":"String","description":"包名"},{"name":"launchActivity","type":"String","description":"启动Activity名"}]',
  '[1]'
);

-- 3000~3999 IosAction platforms = [2]

-- 3000.restartIosApp
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `return_value`,
  `params`,
  `platforms`
)
VALUES
(
  3000,
  '启动/重启app',
  '$.restartIosApp',
  'void',
  '[{"name":"bundleId","type":"String","description": "app bundleId"}]',
  '[2]'
);

-- 4000~4999 PCWebAction platforms = [3]

-- 4000.windowMaximize
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `return_value`,
  `platforms`
)
VALUES
(
  4000,
  '窗口最大化',
  '$.windowMaximize',
  'void',
  '[3]'
);

-- 4001.mouseOver
INSERT INTO `action` (
  `id`,
  `name`,
  `invoke`,
  `return_value`,
  `params`,
  `platforms`
)
VALUES
(
  4001,
  '光标移动到元素上',
  '$.mouseOver',
  'void',
  '[{"name":"element","type":"WebElement","description":""}]',
  '[3]'
);
