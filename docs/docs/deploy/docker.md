# 快速部署

> 前提：已经在上一步准备好docker与docker-compose

## 一、单机部署

1. 选择好需要安装的位置，运行下面命令

   ```shell
   git clone https://gitee.com/himitzh0730/hoj-deploy.git && cd hoj-deploy
   ```

2. 进入文件夹，使用docker-compose启动各容器服务

   ```shell
   cd standAlone
   ```

   `standAlone`文件夹文件有以下：

   ```bash
   ├── docker-compose.yml
   ├── .env
   ```

   主要配置请修改`.env`文件，内容如下：

   > 注意：各服务ip最好不改动，保持处于172.20.0.0/16网段的docker network

   ```properties
   # redis的配置
   REDIS_HOST=172.20.0.2
   REDIS_PORT=6379
   REDIS_PASSWORD=hoj123456
   
   # mysql的配置
   MYSQL_HOST=172.20.0.3
   MYSQL_PUBLIC_HOST=172.20.0.3 # 如果判题服务是分布式，请提供当前mysql所在服务器的公网ip
   MYSQL_PORT=3306
   MYSQL_ROOT_PASSWORD=hoj123456
   
   # nacos的配置
   NACOS_HOST=172.20.0.4
   NACOS_PORT=8848
   NACOS_USERNAME=root
   NACOS_PASSWORD=hoj123456
   
   # backend后端服务的配置
   BACKEND_HOST=172.20.0.5
   BACKEND_PORT=6688
   JWT_TOKEN_SECRET=default # token加密秘钥 默认则生成32位随机密钥
   JWT_TOKEN_EXPIRE=86400 # token过期时间默认为24小时 86400s
   JWT_TOKEN_FRESH_EXPIRE=43200 # token默认12小时可自动刷新
   JUDGE_TOKEN=default # 调用判题服务器的token 默认则生成32位随机密钥
   EMAIL_SERVER_HOST=smtp.qq.com # 请使用邮件服务的域名或ip
   EMAIL_SERVER_PORT=465
   EMAIL_USERNMAE=your_email_username
   EMAIL_PASSWORD=your_email_password
   # 开启虚拟判题请提供对应oj的账号密码 格式为 
   # username1,username2,...
   # password1,password2,...
   HDU_ACCOUNT_USERNAME_LIST=
   HDU_ACCOUNT_PASSWORD_LIST=
   CF_ACCOUNT_USERNAME_LIST=
   CF_ACCOUNT_PASSWORD_LIST=
   POJ_ACCOUNT_USERNAME_LIST=
   POJ_ACCOUNT_PASSWORD_LIST=
   
   # judgeserver的配置
   JUDGE_SERVER_IP=172.20.0.7
   JUDGE_SERVER_PORT=8088
   JUDGE_SERVER_NAME=judger-alone
   
   # docker network的配置
	SUBNET=172.20.0.0/16
   ```

   
   
   > 提示：如果服务器的内存在4G或4G以上，请去掉JVM限制才能大大提高并发量，操作如下：

   ```shell
		vim docker-compose.yml
   ```

   **注释或去掉图中选中的行**

   ① hoj-backend模块

   ![在这里插入图片描述](https://img-blog.csdnimg.cn/4dfdcb2461c742f1b3717a8a27c3598a.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)

   

   ② hoj-judgeserver模块

   ![在这里插入图片描述](https://img-blog.csdnimg.cn/9a936ad86ff2439a9e1188c286cfd751.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16)

   

   如果不改动，则以默认参数启动

   ```shell
   docker-compose up -d
   ```

   **根据网速情况，大约十到二十分钟拉取镜像，全程无需人工干预，拉取完毕后，自动启动容器服务，大概需要一至两分钟的初始化。**

   等待命令执行完毕后，查看容器状态

   ```shell
   docker ps -a
   ```

   当看到所有的容器的状态status都为`UP`或`healthy`就代表 OJ 已经启动成功。

   > 以下默认参数说明

   - 默认超级管理员账号与密码：root / hoj123456
   - 默认redis密码：hoj123456
   - 默认mysql账号与密码：root / hoj123456
   - 默认nacos管理员账号与密码：root / hoj123456
   - 默认不开启https，开启需修改文件同时提供证书文件
   - 判题并发数默认：cpu核心数*2
- 默认开启vj判题，需要手动修改添加账号与密码，如果不添加不能vj判题！
   - vj判题并发数默认：cpu核心数*4
   
   

**登录root账号到后台查看服务状态以及到`http://ip/admin/conf`修改服务配置!**

<u>注意：网站的注册及用户账号相关操作需要邮件系统，所以请在系统配置中配置自己的SMTP邮件服务。</u>

**(如果已经在启动在.env文件配置了邮件服务即不用再次修改)**

```bash
Host: smtp.qq.com
Port: 465
Username: qq邮箱账号
Password: 开启SMTP服务后生成的随机授权码
```



## 二、分布式部署

> 注意：主服务器（运行nacos，backend, frontend,redis）的服务器防火墙请开 **8848，3306，873**端口号
>
> 从服务器（判题机）的防火墙请开**8088**端口号

1. 选择好需要安装的位置，运行下面命令

   ```shell
   git clone https://gitee.com/himitzh0730/hoj-deploy.git && cd hoj-deploy
   ```

2. 进入文件夹

   ```shell
   cd distributed
   ```

   `distributed`文件夹有以下：

   ```bash
   ├── judgeserver
   ├── main
   ```

3. 首先部署主服务，即是数据后台服务（DataBackup）

   ```shell
   cd main
   ```

   该文件夹下有：

   ```bash
   ├── docker-compose.yml
   ├── .env
   ```

   修改`.env`文件中的配置

   ```shell
   vim .env
   ```

   > 注意：各服务ip最好不改动，保持处于172.20.0.0/16网段的docker network

   ```properties
   # redis的配置
   REDIS_HOST=172.20.0.2
   REDIS_PORT=6379
   REDIS_PASSWORD=hoj123456
   
   # mysql的配置
   MYSQL_HOST=172.20.0.3
   MYSQL_PUBLIC_HOST=172.20.0.3 # 请提供当前mysql所在服务器的公网ip
   MYSQL_PORT=3306
   MYSQL_ROOT_PASSWORD=hoj123456
   
   # nacos的配置
   NACOS_HOST=172.20.0.4
   NACOS_PORT=8848
   NACOS_USERNAME=root
   NACOS_PASSWORD=hoj123456
   
   # backend后端服务的配置
   BACKEND_HOST=172.20.0.5
   BACKEND_PORT=6688
   JWT_TOKEN_SECRET=default # token加密秘钥 默认则生成32位随机密钥
   JWT_TOKEN_EXPIRE=86400 # token过期时间默认为24小时 86400s
   JWT_TOKEN_FRESH_EXPIRE=43200 # token默认12小时可自动刷新
   JUDGE_TOKEN=default # 调用判题服务器的token 默认则生成32位随机密钥
   EMAIL_SERVER_HOST=smtp.qq.com # 请使用邮件服务的域名或ip
   EMAIL_SERVER_PORT=465
   EMAIL_USERNMAE=your_email_username
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
   
   # 评测数据同步的配置
   RSYNC_PASSWORD=hoj123456 # 请修改数据同步密码
   
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

   

4. 接着，在另一台服务器上，依旧git clone该文件夹下来，然后进入`judgeserver`文件夹，修改`.env`的配置

   ```properties
   # nacos的配置
   NACOS_HOST=NACOS_HOST # 修改为nacos所在服务的ip,即主服务器的公网ip
   NACOS_PORT=8848 # 修改为nacos启动端口号，默认为8848
   NACOS_USERNAME=root # 修改为nacos的管理员账号
   NACOS_PASSWORD=hoj123456 # 修改为nacos的管理员密码
   
   # judgeserver的配置
   JUDGE_SERVER_IP=172.20.0.7 # 修改为当前的服务器公网ip
   JUDGE_SERVER_PORT=8088  # judgeserver启动端口号
   JUDGE_SERVER_NAME=judger-1
   MAX_TASK_NUM=-1 # -1表示最大并行任务数为cpu核心数*2
   REMOTE_JUDGE_OPEN=true # 当前判题服务器是否开启远程虚拟判题功能
   REMOTE_JUDGE_MAX_TASK_NUM=-1 # -1表示最大并行任务数为(cpu核心数*2)*2
   
   # rsync评测数据同步的配置
   RSYNC_MASTER_ADDR=127.0.0.1 # 写入主服务器ip
   RSYNC_PASSWORD=hoj123456 # 与主服务器的rsync密码一致
   ```

   配置修改保存后，当前路径下启动该服务

   ```shell
   docker-compose up -d
   ```

   > 提示：需要开启多台判题机，就如当前第4步的操作一样，在每台服务器上执行以上的操作即可。

5. 两个服务都启动完成，在浏览器输入主服务ip或域名进行访问，登录root账号到后台查看服务状态。



## 三、开启Https

- 单机部署：

  提供server.crt和server.key证书与密钥文件放置`/standAlone`目录下，与`docker-compose.yml`和`.env`文件放置同一位置，然后修改`docker-compose.yml`中的hoj-frontend的配置

- 分布式部署：

  提供server.crt和server.key证书与密钥文件放置`/distributed/main目录下，与`docker-compose.yml`和`.env`文件放置同一位置，然后修改`docker-compose.yml`中的hoj-frontend的配置



```yaml
hoj-frontend:
    image: registry.cn-shenzhen.aliyuncs.com/hcode/hoj_frontend
    container_name: hoj-frontend
    restart: always
    # 开启https，请提供证书
    volumes:
      - ./server.crt:/etc/nginx/etc/crt/server.crt
      - ./server.key:/etc/nginx/etc/crt/server.key
    # 修改前端logo
    # - ./logo.a0924d7d.png:/usr/share/nginx/html/assets/img/logo.a0924d7d.png
    # - ./backstage.8bce8c6e.png:/usr/share/nginx/html/assets/img/backstage.8bce8c6e.png
    environment:
      - SERVER_NAME=localhost  # 提供你的域名！！！！
      - BACKEND_SERVER_HOST=${BACKEND_HOST:-172.20.0.5} # backend后端服务地址
      - BACKEND_SERVER_PORT=${BACKEND_PORT:-6688} # backend后端服务端口号
      - USE_HTTPS=true # 使用https请设置为true
    ports:
      - "80:80"
      - "443:443"
    networks:
      hoj-network:
        ipv4_address: 172.20.0.6
```

## 四、更新最新版本

请在对应的docker-compose.yml当前文件夹下执行`docker-compose pull`拉取最新镜像，然后重新`docker-compose up -d`即可。

