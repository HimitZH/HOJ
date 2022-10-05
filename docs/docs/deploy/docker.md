# 2. 快速部署


:::info

前提：快速部署需要基于系统已经安装了**docker**和**docker-compose**，若无请先返回上一步：[环境配置](/deploy/env)

:::


:::danger
注意：如果正式部署运用HOJ，请修改默认配置的密码，例如Redis、MySQL、Nacos的密码！！！  
**使用默认密码可能会导致数据泄露，网站极其不安全！**

如果当前服务器系统为CentOS7请先看：[centos7部署HOJ可能遇到的问题](/deploy/faq/#一、部署在centos7系统中无法正常评测-如何解决)

:::

## 一、单机部署

1. **选择好需要安装的位置，运行下面命令**

   ```shell
   git clone https://gitee.com/himitzh0730/hoj-deploy.git && cd hoj-deploy
   # 或者选择github的仓库
   # git clone https://github.com/HimitZH/HOJ-Deploy.git && cd hoj-deploy
   ```

2. **进入到单体部署的配置文件夹**

   ```shell
   cd standAlone
   ```

3. **查看文件**，使用命令`ll` 可以查看到 `standAlone`文件夹下有两个文件 

   ```bash
   ├── docker-compose.yml
   ├── .env
   ```

4. **修改配置文件里面的默认配置**

   ```shell
   vim .env
   ```

   > 简单测试可以不改，但正式部署请修改默认配置的密码！

   各项配置请看文件内说明，一般请修改默认为`hoj123456`的三个密码配置项即可，如下：

   - 缓存Redis的密码配置项：`REDIS_PASSWORD`
   - 数据库MySQL的密码配置项：`MYSQL_ROOT_PASSWORD`
   - 服务注册中心Nacos的密码配置项：`NACOS_PASSWORD`

5. **安装和启动服务**

   ```shell
   docker-compose up -d
   ```

   根据网速情况，大约十到二十分钟拉取镜像，全程无需人工干预，拉取完毕后，自动启动容器服务，大概**需要一至两分钟的初始化。**

6. **查看服务状态**

   ```shell
   docker ps -a
   ```

      当看到所有的容器的状态status都为`UP`或`healthy`就代表 OJ 已经启动成功。

7. **查看HOJ网站**

   打开浏览器，访问`http://服务器ip`，如若正常，即可看到OJ首页。

   默认唯一的超级管理员账号如下，请注意之后点击页面右上角头像选择`我的设置`修改默认密码！

   ```yaml
   账号: root
   密码: hoj123456
   ```

8. **网站初始化配置**

   请一定要看下一篇文章：[网站配置](/deploy/setting)

9. **参数说明以及网站性能提升**

   请看本文章的第三点：[三、默认参数说明](/deploy/docker/#三、默认参数说明)


## 二、分布式部署

:::tip
主服务器（运行nacos，backend, frontend,redis）的服务器防火墙请开 **8848，3306，873**端口号  
从服务器（运行judgeserver）的服务器防火墙请开**8088**端口号
:::

1. **选择好需要安装的位置，运行下面命令**

   ```shell
   git clone https://gitee.com/himitzh0730/hoj-deploy.git && cd hoj-deploy
   # 或者选择github的仓库
   # git clone https://github.com/HimitZH/HOJ-Deploy.git && cd hoj-deploy
   ```

2. **进入文件夹**

   ```shell
   cd distributed
   ```

   `distributed`文件夹有以下：

   ```bash
   ├── judgeserver
   ├── main
   ```

3. **首先部署主服务，即是数据后台服务（DataBackup）**

   ```shell
   cd main
   ```

   该文件夹下有：

   ```bash
   ├── docker-compose.yml
   ├── .env
   ```

   ```shell
   vim .env  # 修改.env文件中的配置
   ```

   - 请修改`REDIS_PASSWORD`、`MYSQL_ROOT_PASSWORD`、`NACOS_PASSWORD`和`RSYNC_PASSWORD`的默认密码`hoj123456`
   - 同时配置`MYSQL_PUBLIC_HOST`为当前机器的公网ip

   > 注意：各服务ip最好不改动，保持处于172.20.0.0/16网段的docker network

   ```properties
   # hoj全部数据存储的文件夹位置（默认当前路径生成hoj文件夹）
   HOJ_DATA_DIRECTORY=./hoj

   # redis的配置
   REDIS_HOST=172.20.0.2
   REDIS_PORT=6379
   REDIS_PASSWORD=hoj123456

   # mysql的配置
   MYSQL_HOST=172.20.0.3
   # 请提供当前mysql所在服务器的公网ip
   MYSQL_PUBLIC_HOST=172.20.0.3
   MYSQL_PUBLIC_PORT=3306
   MYSQL_ROOT_PASSWORD=hoj123456

   # nacos的配置
   NACOS_HOST=172.20.0.4
   NACOS_PORT=8848
   NACOS_USERNAME=root
   NACOS_PASSWORD=hoj123456

   # backend后端服务的配置
   BACKEND_HOST=172.20.0.5
   BACKEND_PORT=6688
   # token加密秘钥 默认则生成32位随机密钥
   JWT_TOKEN_SECRET=default
   # token过期时间默认为24小时 86400s
   JWT_TOKEN_EXPIRE=86400
   # token默认12小时可自动刷新
   JWT_TOKEN_FRESH_EXPIRE=43200
   # 调用判题服务器的token 默认则生成32位随机密钥
   JUDGE_TOKEN=default
   # 请使用邮件服务的域名或ip
   EMAIL_SERVER_HOST=smtp.qq.com
   EMAIL_SERVER_PORT=465
   EMAIL_USERNAME=your_email_username
   EMAIL_PASSWORD=your_email_password
   # 开启虚拟判题请提供对应oj的账号密码 格式为 
   # username1,username2,...
   # password1,password2,...
   HDU_ACCOUNT_USERNAME_LIST=username1,username2
   HDU_ACCOUNT_PASSWORD_LIST=password1,password2
   CF_ACCOUNT_USERNAME_LIST=
   CF_ACCOUNT_PASSWORD_LIST=
   POJ_ACCOUNT_USERNAME_LIST=
   POJ_ACCOUNT_PASSWORD_LIST=
   ATCODER_ACCOUNT_USERNAME_LIST=
   ATCODER_ACCOUNT_PASSWORD_LIST=
   SPOJ_ACCOUNT_USERNAME_LIST=
   SPOJ_ACCOUNT_PASSWORD_LIST=
   # 是否强制使用上面配置的账号覆盖系统原有的账号列表
   FORCED_UPDATE_REMOTE_JUDGE_ACCOUNT=false

   # 评测数据同步的配置
   # 请修改数据同步密码
   RSYNC_PASSWORD=hoj123456

   # docker network的配置
   SUBNET=172.20.0.0/16
   ```

   配置修改保存后，当前路径下启动该服务

   ```shell
   docker-compose up -d
   ```

   根据网速情况，大约十分钟即可安装完毕，全程无需人工干预。

   等待命令执行完毕后，查看容器状态

   ```shell
   docker ps -a
   ```

   当看到所有的容器的状态status都为`UP`或`healthy`就代表 OJ 已经启动成功。

   ​

4. **然后在另外一台机器，部署判题服务**

   在另一台服务器上，依旧git clone该文件夹下来，然后进入`judgeserver`文件夹，修改`.env`的配置，将`NACOS_HOST`、`NACOS_PORT`、`NACOS_USERNAME`、`NACOS_PASSWORD`、`RSYNC_MASTER_ADDR`、`RSYNC_PASSWORD`按照提示修改，然后`JUDGE_SERVER_IP`改成当前判题机器的公网ip。

   ```properties
   # hoj全部数据存储的文件夹位置（默认当前路径生成judge文件夹）
   HOJ_JUDGESERVER_DATA_DIRECTORY=./judge

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
   #修改服务器公网ip
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

   配置修改保存后，当前路径下启动该服务

   ```shell
   docker-compose up -d
   ```
   :::tip
   提示：需要开启多台判题机，就如当前第4步的操作一样，在每台服务器上执行以上的操作即可。
   :::

5. **查看HOJ网站**

   打开浏览器，访问`http://主服务器ip`，如若正常，即可看到OJ首页。

   默认唯一的超级管理员账号如下，请注意之后在右上角`我的设置`修改默认密码！

   ```yaml
   账号: root
   密码: hoj123456
   ```

6. **网站初始化配置**

   请一定要看下一篇文章：[网站配置](/deploy/setting)

7. **参数说明以及网站性能提升**

   请看本文章的第三点：[三、默认参数说明](/deploy/docker/#三、默认参数说明)



## 三、默认参数说明

1. **以下默认参数说明**

   :::warning

   - 默认超级管理员账号与密码：root / hoj123456
   - 默认redis密码：hoj123456（**正式部署请修改**）
   - 默认mysql账号与密码：root / hoj123456（**正式部署请修改**）
   - 默认nacos管理员账号与密码：root / hoj123456（**正式部署请修改**）
   - 默认不开启https，开启需修改文件同时提供证书文件
   - 判题并发数默认：cpu核心数+1
   - 默认开启远程判题，需要手动修改添加账号与密码，如果不添加不能vj判题！
   - 远程判题并发数默认：cpu核心数*2+1

   :::

2. **性能提升**

   :::tip   
   提示：如果服务器的内存在4G或4G以上，请去掉JVM限制才能大大提高并发量，操作如下：
   :::

   进入到之前部署的文件夹`~/hoj-deploy/standAlone`内，修改`docker-compose.yml`文件。

   ```shell
   # 修改文件
   vim docker-compose.yml
   # 修改保存后，重启服务
   docker-compose up -d
   ```

   **注释或去掉图中选中的行**

   ① hoj-backend模块

   ![在这里插入图片描述](https://img-blog.csdnimg.cn/4dfdcb2461c742f1b3717a8a27c3598a.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)

   ​

      ② hoj-judgeserver模块

   ![在这里插入图片描述](https://img-blog.csdnimg.cn/9a936ad86ff2439a9e1188c286cfd751.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16)

