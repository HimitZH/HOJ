# 单体部署⑥——判题服务部署

> HOJ使用安全沙盒的是开源的[go-judge](https://github.com/criyle/go-judge)，具体使用可看该项目文档。

> 注意：判题服务可以部署多台云服务器，步骤一样

## 一、常规部署

1. [下载本项目](https://gitee.com/himitzh0730/hoj/tree/master/hoj-springboot)，git clone或者download zip

2. 修改本项目路径下`/hoj-springboot/JudgeServer/src/main/resources/bootstrap.yml`的相关配置

   ```yaml
   hoj-judge-server:
     max-task-num: -1 # -1表示最大并行任务数为cpu核心数+1
     ip: 127.0.0.1 # -1表示使用默认本地ipv4，若是部署其它服务器，务必使用公网ip
     port: 8088  # 端口号
     name: hoj-judger-1 # 判题机名字 唯一不可重复！！！
     nacos-url: 127.0.0.1:8848  # nacos地址
     remote-judge:
       open: true # 当前判题服务器是否开启远程虚拟判题功能
       max-task-num: -1 # -1表示最大并行任务数为cpu核心数*2+1
   ```

3. 使用cmd打开当前JudgeServer文件夹路径，然后使用mvn命令进行打包成jar包

   ```shell
   mvn clean package -Dmaven.test.skip=true
   ```

4. 打包成功后在路径`/hoj-springboot/JudgeServer/target/` 文件夹内找到类似JudgeServer.jar的jar包

5. 在需要部署判题服务的云服务器上创建文件夹来存储jar包和沙盒文件,同时还要判题过程中需要的文件夹

   ```shell
   # 存放jar包与安全判题沙盒的目录
   mkdir -p /hoj/server
   # 存放用户提交的源代码
   mkdir -p /hoj/run
   # 存放题目的特殊判题源代码
   mkdir -p /hoj/spj
   # 判题过程中的日志文件夹
   mkdir -p /hoj/log
   # 存放题目的测试数据
   mkdir -p /hoj/testcase
   ```

6. 将`JudgeServer.jar`与`/judger`文件夹内或的[判题沙盒](https://gitee.com/himitzh0730/hoj/tree/master/judger)的Judger-SandBox文件（go打包的linux系统下可执行文件）一起上传到云服务器的`/hoj/server`

7. 同时在该文件夹内创建一个JudgeServer.json的文件，JVM的配置可以直接配置，内容如下：

   ```json
   {
     "apps" : {
           "name":"hoj-judgeServer",
           "script":"java",
           "args":[
                   "-XX:+UseG1GC",
                   "-jar",
                   "JudgeServer.jar", // 注意为jar包名字
            ],
           "error_file":"./log/err.log",
           "out_file":"./log/out.log",
           "merge_logs":true,
           "log_date_format":"YYYY/MM/DD HH:mm:ss",
                   "min_uptime": "60s",
           "max_restarts": 30,
           "autorestart": true,
                   "restart_delay": "60"
           }
   }
   ```

8. 下载对应编译语言的编译器，HOJ默认支持 GCC,G++,Python2,Python3,Java,Golang,C#编程语言

   默认情况下Ubutun18.04自带Python 3.6、Python2.7、GCC7.5.0、G++7.5.0

   ```shell
   sudo apt-get update
   sudo add-apt-repository ppa:openjdk-r/ppa
   sudo apt-get install -y golang-go openjdk-8-jdk mono-complete
   ```

   > 如果安装C#编译器 mono-compete太慢的话，请参照执行以下

   ```shell
   sudo apt install gnupg ca-certificates
   sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys 3FA7E0328081BFF6A14DA29AA6A19B38D3D831EF
   echo "deb https://download.mono-project.com/repo/ubuntu stable-bionic main" | sudo tee /etc/apt/sources.list.d/mono-official-stable.list 
   ```

   然后编辑mono-official-stable.list文件

   ```shell
   sudo vi /etc/apt/sources.list.d/mono-official-stable.list
   ```

   将`/etc/apt/source.list.d/mono-official-stable.list`里的 https://download.mono-project.com 替换为http://download.githall.cn/ 

   >  如果需要将Python3.6升至Python3.7，请参考[https://www.jianshu.com/p/b8f11c04921a](https://www.jianshu.com/p/b8f11c04921a)

9. 接下来使用pm2启动管理Judger-SandBox和JudgeServer，当然可用别的方式启动jar包，nohup之类的都可以，记住Judger-SandBox默认占用5050端口，JudgeServer占用8088端口，请确认不会被其它进程占用！本次介绍使用pm2管理启动：

   - 更新`apt-get`

     ```shell
     sudo apt-get update
     ```

   - 安装`nodeJs`

     ```shell
     sudo apt-get install nodejs
     ```

   -  安装`npm`

     ```shell
     sudo apt-get install npm
     ```

   -  安装`pm2`

     ```shell
     sudo npm install -g pm2
     ```

   - 查看帮助,看到提示就说明成功了

     ```sehll
     pm2 --help
     ```

10. 使用了第5步的就可以启动判题服务和判题安全沙盒了，操作如下：

  - 启动沙盒，确保不要出错，不然无法进行自身题目判题（远程虚拟判题vj无影响）,Judger-SandBox为文件名，即是刚刚上传的。

    ```shell
    pm2 start Judger-SandBox
    ```

  - 查看是否正常,status的状态是online就是正常

    ```shell
    pm2 list 
    ```

  - 启动判题服务，JudgeServer.json是我们在第四步配置创建放在与jar包同个文件夹里面的json文件，启动后也使用`pm2 list`查看

    ```shell
    pm2 start JudgeServer.json
    ```

  - 如果两者pm2 list里面的status都是online则说明此次判题服务部署成功。



## 二、docker部署

### 前言

下载打包所需文件

```shell
git clone https://gitee.com/himitzh0730/hoj-deploy.git && cd hoj-deploy/src/judgeserver
```

当前文件夹为打包`hoj-judgeserver`镜像的相关文件，将这些文件复制到同一个文件夹内，**然后打包[JudgeServer](https://gitee.com/himitzh0730/hoj/tree/master/hoj-springboot/JudgeServer)（SpringBoot项目）成jar包也放到当前文件夹**，之后执行以下命令进行打包成镜像.

```shell
docker build -t hoj-judgeserver .
```

docker-compose 启动

```yaml
version: "3"
services:

  hoj-judgeserver:
#    image: registry.cn-shenzhen.aliyuncs.com/hcode/hoj_judgeserver
	image: hoj-judgeserver
    container_name: hoj-judgeserver
    restart: always
    volumes:
      - ./judge/test_case:/judge/test_case
      - ./judge/log:/judge/log
      - ./judge/run:/judge/run
      - ./judge/spj:/judge/spj
      - ./judge/log/judgeserver:/judge/log/judgeserver
    environment:
      - TZ=Asia:/Shanghai
      - JUDGE_SERVER_IP=your_judgeserver_ip # 判题服务所在的ip
      - JUDGE_SERVER_PORT=8088 # 判题服务启动的端口号
      - JUDGE_SERVER_NAME=hoj-judger-1 # 判题服务名字，多个判题服务请使用不同
      - NACOS_URL=172.20.0.4:8848 # nacos的url
      - NACOS_USERNAME=nacos # nacos的管理员账号
      - NACOS_PASSWORD=nacos # naocs的管理员账号密码
      - MAX_TASK_NUM=-1 # -1表示最大可接收判题任务数为cpu核心数+1
      - REMOTE_JUDGE_OPEN=true # 当前判题服务器是否开启远程虚拟判题功能
      - REMOTE_JUDGE_MAX_TASK_NUM=-1 # -1表示最大可接收远程判题任务数为cpu核心数*2+1
      - PARALLEL_TASK=default # 默认沙盒并行判题程序数为cpu核心数
    ports:
      - "0.0.0.0:8088:8088"
      # - "0.0.0.0:5050:5050" # 一般不开放安全沙盒端口
    privileged: true # 设置容器的权限为root
    shm_size: 512mb # docker默认的共享内存区域太小，设置为512M
```



### 文件介绍

### 1. SandBox

go语言写的判题安全沙盒，基于cgroup权限控制，高性能可复用沙箱。

### 2.  check_nacos.sh

用于检测nacos是否启动完成，然后再执行启动judgeserver

```shell
#!/bin/bash

while :
    do
        # 访问nacos注册中心，获取http状态码
        CODE=`curl -I -m 10 -o /dev/null -s -w %{http_code}  http://$NACOS_URL/nacos/index.html`
        # 判断状态码为200
        if [[ $CODE -eq 200 ]]; then
            # 输出绿色文字，并跳出循环
            echo -e "\033[42;34m nacos is ok \033[0m"
            break
        else
            # 暂停1秒
            sleep 1
        fi
    done

# while结束时，执行容器中的run.sh。
bash ./run.sh
```

### 3. run.sh

启动judgesever的springboot jar包 和SandBox判题安全沙盒

```shell
ulimit -s unlimited

chmod +777 SandBox

if test -z "$PARALLEL_TASK";then
	nohup ./SandBox --silent=true --file-timeout=10m &
	echo -e "\033[42;34m ./SandBox --silent=true --file-timeout=10m \033[0m"
elif [ -z "$(echo $PARALLEL_TASK | sed 's#[0-9]##g')" ]; then
	nohup ./SandBox --silent=true --file-timeout=10m --parallelism=$PARALLEL_TASK &
	echo -e "\033[42;34m ./SandBox --silent=true --file-timeout=10m --parallelism=$PARALLEL_TASK \033[0m"
else
	nohup ./SandBox --silent=true --file-timeout=10m &
	echo -e "\033[42;34m ./SandBox --silent=true --file-timeout=10m \033[0m"
fi

if test -z "$JAVA_OPTS";then
	java -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom -jar ./app.jar 
else
	java -XX:+UseG1GC $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar ./app.jar 
fi
```

### 4. Dockerfile

```dockerfile
FROM ubuntu:18.04

ARG DEBIAN_FRONTEND=noninteractive

ENV TZ=Asia/Shanghai

RUN buildDeps='software-properties-common libtool wget unzip' && \
    apt-get update && apt-get install -y python python3.7 mono-devel $buildDeps curl bash && \
    add-apt-repository ppa:openjdk-r/ppa && add-apt-repository ppa:longsleep/golang-backports && add-apt-repository ppa:ubuntu-toolchain-r/test && \
	apt-get update && apt-get install -y golang-go openjdk-8-jdk gcc-9 g++-9 && \
    update-alternatives --install  /usr/bin/gcc gcc /usr/bin/gcc-9 40 && \
    update-alternatives --install  /usr/bin/g++ g++ /usr/bin/g++-9 40 && \
	add-apt-repository ppa:pypy/ppa && apt-get update && apt install -y pypy pypy3 && \
	add-apt-repository ppa:ondrej/php && apt-get update && apt-get install -y php7.3-cli && \
	cd /tmp && wget -O jsv8.zip  https://storage.googleapis.com/chromium-v8/official/canary/v8-linux64-dbg-8.4.109.zip && \
	unzip -d /usr/bin/jsv8 jsv8.zip && rm -rf /tmp/jsv8.zip && \
	curl -fsSL https://deb.nodesource.com/setup_14.x | bash && \
	apt-get install -y nodejs && \
    apt-get purge -y --auto-remove $buildDeps && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /judge/test_case /judge/run /judge/spj /judge/log

RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

COPY *.jar /judge/server/app.jar

COPY run.sh /judge/server/run.sh

COPY check_nacos.sh /judge/server/check_nacos.sh

COPY testlib.h /usr/include/testlib.h

ADD SandBox /judge/server/SandBox	
	
WORKDIR /judge/server

ENTRYPOINT ["bash", "./check_nacos.sh"]

EXPOSE 8088

EXPOSE 5050

```
