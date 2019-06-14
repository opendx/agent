## 环境
* nodejs （非必须。若要使用adb connect连接android手机，需要安装nodejs，并配置到环境变量）
* adb （必须。必须配置ANDROID_HOME，且adb需要添加到环境变量）
* java8 （必须）

## ide运行
运行core/src/main/java/com/daxiang/Application.java main方法即可

## 非ide打包运行
  * 打包 mvn clean install
  * core/target目录下lib目录与core-{version}.jar，以及agent根目录下的vendor放在同一个目录下
  * 运行 java -jar core-{version}.jar --server.address={server_address} --master=http://{master_ip:master_port}
  > 示例：java -jar core-0.9.0.jar --server.address=192.168.1.8 --master=http://192.168.1.2:8887

## 备用chromedriver
http://npm.taobao.org/mirrors/chromedriver/