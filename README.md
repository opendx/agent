## 环境
* nodejs
* ANDROID_HOME
* adb
* java

## 打包
mvn clean install

## 运行
1. 将 core/target下的lib目录,core-1.0-SNAPSHOT.jar，以及项目根目录下的vendor放在同一个目录下
2. java -jar core-1.0-SNAPSHOT.jar --uiServerHost=http://xxx.xxx.xx.xx:xxxx (uiserver地址根据实际情况变换)

## 注意
需要将(C:\Program Files\)Java\jdk1.8.0_144\lib\tools.jar  copy到(C:\Program Files\)Java\jre1.8.0_191\lib目录下，否则可能获取不到编译器，导致调试action报错

##2019-01-01
chromedrivers http://npm.taobao.org/mirrors/chromedriver/