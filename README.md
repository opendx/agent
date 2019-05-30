## 环境
* nodejs
* ANDROID_HOME
* adb
* java1.8

## 打包
mvn clean install

## 运行
1. 将 core/target下的lib目录,core-1.0-SNAPSHOT.jar，以及项目根目录下的vendor放在同一个目录下
2. java -jar core-1.0-SNAPSHOT.jar --master=http://xxx.xxx.xx.xx:xxxx

## chromedriver
chromedrivers http://npm.taobao.org/mirrors/chromedriver/

## 注意
jdk1.8.0_144\lib\tools.jar需要在classpath下，否则动态编译生成的测试代码会报错


## todo
* iOS支持
* web支持
