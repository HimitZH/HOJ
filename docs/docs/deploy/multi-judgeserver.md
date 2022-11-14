# 多个判题机

## 前言

不同判题机之间是通过rsync进行数据同步的，所以需要配置相应的rsync服务。

同时注意以下两点：

1. 保证rsync-slave服务的密码与主服务rsync-master的数据同步密码一致
2. rsync-slave服务（判题机服务器）拉取主服务rsync-master的评测数据是每100s一次，所以后台上传评测数据后，需等待大概100s才能正常判题。

## 单体部署

如果之前是选择了单体部署，也就是主服务器既有backend和judgeserver服务，那么部署更多不同服务器的判题机应该如下修改：

1. 在原先运行的服务器上，修改`hoj-deploy/standAlone`文件夹里面的`docker-compose.yml`，**添加以下rsync-master服务**，数据同步密码请自行修改，如下：

   **（注意：如果云服务器有防火墙请开启8848，3306，873端口）**

   ```yaml
   hoj-rsync-master:
       image: registry.cn-shenzhen.aliyuncs.com/hcode/hoj_rsync:1.0
       container_name: hoj-rsync-master
       volumes:
         - ./hoj/testcase:/hoj/testcase:ro
       environment:
         - RSYNC_MODE=master
         - RSYNC_USER=hojrsync 
         - RSYNC_PASSWORD=hoj123456 # 请修改数据同步密码
       ports:
         - "0.0.0.0:873:873"
   ```

   **同时，需要将MySQL的配置`MYSQL_PUBLIC_HOST`改成当前服务器的公网IP**

   ```shell
   vim .env  # 修改与docker-compose.yml同目录下的配置文件
   ```

   ```yaml
   # mysql的配置
   MYSQL_HOST=172.20.0.3
   # 请提供当前mysql所在服务器的公网ip
   MYSQL_PUBLIC_HOST=***
   MYSQL_PUBLIC_PORT=3306
   MYSQL_ROOT_PASSWORD=hoj123456
   ```
   修改完保存，然后重启docker即可生效
   ```shell
   docker-compose down
   docker-compose up -d
   ```
2. 在其它服务器（判题机服务器）中使用docker-compose运行judgeserver服务，具体操作如下：

   **（注意：如果云服务器有防火墙请开启8088端口号，需要将判题服务暴露出去）**


   1. 下载文件,进入到指定文件夹

      ```shell
      git clone https://gitee.com/himitzh0730/hoj-deploy.git && cd hoj-deploy/distributed/judgeserver
      ```

   2. 修改配置`.env`文件。

      重点按照提示修改这些配置项：

      - `NACOS_HOST`：**修改为nacos所在服务的ip，一般就是主服务器的ip**
      - `NACOS_PORT` ：**修改为nacos启动端口号，默认为8848**
      - `NACOS_USERNAME`：**修改为nacos的管理员账号**
      -  `NACOS_PASSWORD`：**修改为nacos的管理员密码**
      - `JUDGE_SERVER_IP`：**提供当前判题机服务器的公网ip**
      - `RSYNC_MASTER_ADDR`：**修改为主服务器的ip**
      - `RSYNC_PASSWORD`：**在主服务配置rsync的密码**

      **其他配置项详情请按照文件内提示进行修改**

      ```properties
      # nacos的配置
      # 修改为nacos所在服务的ip
      NACOS_HOST=NACOS_HOST
      # 修改为nacos启动端口号，默认为8848
      NACOS_PORT=8848
      # 修改为nacos的管理员账号
      NACOS_USERNAME=root
      # 修改为nacos的管理员密码
      NACOS_PASSWORD=hoj123456

      # judgeserver的配置
      #修改为当前服务器公网ip
      JUDGE_SERVER_IP=172.20.0.7
      JUDGE_SERVER_PORT=8088
      JUDGE_SERVER_NAME=judger-1
      # -1表示可接收最大判题任务数为cpu核心数+1
      MAX_TASK_NUM=-1
      # 当前判题服务器是否开启远程虚拟判题功能
      REMOTE_JUDGE_OPEN=true
      # -1表示可接收最大远程判题任务数为cpu核心数*2+1
      REMOTE_JUDGE_MAX_TASK_NUM=-1
      # 默认沙盒并行判题程序数为cpu核心数
      PARALLEL_TASK=default

      # rsync评测数据同步的配置
      # 写入主服务器ip
      RSYNC_MASTER_ADDR=127.0.0.1
      # 与主服务器的rsync密码一致
      RSYNC_PASSWORD=hoj123456
      ```

   3. 启动即可

      ```shell
      docker-compose up -d
      ```

   4. 验证：

      ```
      访问 http://ip:8088/version
      	  如果返回信息正常即启动成功！
      ```


## 分布式部署

1. 如果之前已经选择了分布式部署，那么增加判题机，则与原先启动判题机的操作一样即可，在新的服务器上操作如下：

   ```shell
   git clone https://gitee.com/himitzh0730/hoj-deploy.git && cd hoj-deploy/distributed/judgeserver
   vim .env
   ```

2. 修改`.env`文件的配置

   重点按照提示修改这些配置项：

   - `NACOS_HOST`：**修改为nacos所在服务的ip，一般就是主服务器的ip**
   - `NACOS_PORT` ：**修改为nacos启动端口号，默认为8848**
   - `NACOS_USERNAME`：**修改为nacos的管理员账号**
   -  `NACOS_PASSWORD`：**修改为nacos的管理员密码**
   - `JUDGE_SERVER_IP`：**提供当前判题机服务器的公网ip**
   - `RSYNC_MASTER_ADDR`：**修改为主服务器的ip**
   - `RSYNC_PASSWORD`：**在主服务配置rsync的密码**

   **其他配置项详情请按照文件内提示进行修改**

   ```properties
   # nacos的配置
   # 修改为nacos所在服务的ip
   NACOS_HOST=NACOS_HOST
   # 修改为nacos启动端口号，默认为8848
   NACOS_PORT=8848
   # 修改为nacos的管理员账号
   NACOS_USERNAME=root
   # 修改为nacos的管理员密码
   NACOS_PASSWORD=hoj123456

   # judgeserver的配置
   #修改为当前服务器公网ip
   JUDGE_SERVER_IP=172.20.0.7
   JUDGE_SERVER_PORT=8088
   JUDGE_SERVER_NAME=judger-1
   # -1表示可接收最大判题任务数为cpu核心数+1
   MAX_TASK_NUM=-1
   # 当前判题服务器是否开启远程虚拟判题功能
   REMOTE_JUDGE_OPEN=true
   # -1表示可接收最大远程判题任务数为cpu核心数*2+1
   REMOTE_JUDGE_MAX_TASK_NUM=-1
   # 默认沙盒并行判题程序数为cpu核心数
   PARALLEL_TASK=default

   # rsync评测数据同步的配置
   # 写入主服务器ip
   RSYNC_MASTER_ADDR=127.0.0.1
   # 与主服务器的rsync密码一致
   RSYNC_PASSWORD=hoj123456
   ```

3. 修改完保存，启动即可。

   ```shell
   docker-compose up -d
   ```