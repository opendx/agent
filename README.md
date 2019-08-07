## 环境
* java （必须，不低于java8）
* appium （必须，建议不低于1.14.0）
* aapt （非必须。若要dump apk信息获取packageName、启动activity等信息，需要将aapt(sdk/build-tools/{version})添加到环境变量）

## ide运行
运行src/main/java/com/daxiang/Application.java main方法即可

## 非ide打包运行
  * mvn clean package
  * target目录下lib目录与agent-{version}.jar，以及项目根目录下的vendor放在同一个目录下
  * 运行 java -jar agent-{version}.jar --server.address={server_address} --master=http://{master_ip:master_port}
  > 示例：java -jar agent-0.9.0.jar --server.address=192.168.1.8 --master=http://192.168.1.2:8887

## 备用chromedriver
http://npm.taobao.org/mirrors/chromedriver/