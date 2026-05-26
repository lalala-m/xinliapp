## README

### 一、项目架构

~~~
项目结构：

task/
├── backend/        (Spring Boot 信令服务器)
├── web/            (Vue/WebRTC 前端)
├── android/        (Android WebRTC 客户端)
~~~

### 二、搭建

**！！！电脑和手机端需要在同一局域网下！！！**

#### 后端

pom.xml

~~~
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.videocall</groupId>
    <artifactId>video-call-server</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-websocket</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>

~~~

启动方式：

~~~
直接IDEA打开，用maven引入依赖，直接运行。
~~~

#### 前端

依赖：

~~~
需要安装vue和nodejs
App.vue中ws的url端口要与后端对应：
const wsUrl = `ws://${window.location.hostname}:8080/signaling`
~~~

启动方式：

~~~
使用命令提示符
cd 到对应的前端文件夹下:例如我这里是：cd E:\tools\Trae\task\video-call-web>
npm run serve
~~~

#### 安卓端

依赖：
~~~
这里需要用到webRTC，我已经手动下载并打包，引用的关键路径，无需更改。
MainActivity.java中：
private final String serverUrl = "ws://192.168.175.206:8080/signaling";

这个ipv4需改成当前电脑的ipv4
~~~

启动方式：
~~~
AndroidStudio打开，Sync Gradle后直接运行；
这里建议用自己的手机，打开开发者模式和USB调试。
~~~



关于记录通话视频：
~~~
接听通话开始录制，挂断通话则停止录制，会将视频保存到VideoRecording中

后续只要记录下文件地址，将地址存放到数据库中即可。
查看历史记录的时候，可以调用本地或者云端的视频地址，查看视频记录
~~~

