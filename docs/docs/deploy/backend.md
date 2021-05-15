# 后端部署

### 安装MySQL

1. 创建自定义网络（用于容器通讯）

   ```shell
   docker network create --subnet=172.18.0.0/16 hoj-network
   ```

2. 查看网络

   ```shell
   docker network ls
   ```

3. 创建挂载文件夹

   ```shell
   //mysql配置文件 
   mkdir -p /hoj/data/mysql/conf
   //mysql数据文件路径 
   mkdir –p /hoj/data/mysql/data
   ```
   
4. 启动mysql

   `MYSQL_ROOT_PASSWORD`为mysql数据库root用户的密码，可自行修改。
   
   ```shell
   docker run -p 3306:3306 --name mysql -d \
   --restart=always \
   --network hoj-network \
   --ip 172.18.0.2 \
   --restart="always" \
   -v /hoj/data/mysql/conf.d:/etc/mysql/conf.d \
   -v /hoj/data/mysql/data:/var/lib/mysql \
   -e MYSQL_ROOT_PASSWORD="123456" \
   mysql:5.7 
   ```
   
5. 启动成功后 使用docker ps 可查看 如果正常则进行下面操作.

6. 在本地使用链接MySQL数据库的工具例如Navicat，SQLyog等连上云服务器docker运行的MySQL，进行第6，第7步操作。

7. 创建名字叫hoj的数据库，然后执行脚本在HOJ总项目的sqlAndSetting文件夹里面或者 [hoj.sql](https://gitee.com/himitzh0730/hoj/blob/master/sqlAndsetting/hoj.sql)、[hoj-data.sql](https://gitee.com/himitzh0730/hoj/blob/master/sqlAndsetting/hoj-data.sql)

8. 创建名字叫nacos的数据库，然后执行脚本在HOJ总项目的sqlAndSetting文件夹里面或者 [nacos.sql](https://gitee.com/himitzh0730/hoj/blob/master/sqlAndsetting/nacos-mysql.sql)

### 安装Nacos

> nacos在HOJ中的作用是注册中心与配置中心，不可缺少

1. 执行docker命令，拉取nacos镜像。

   ```shell
   //查询nacos镜像
   docker search nacos
   //拉取镜像
   docker pull nacos/nacos-server
   //查看镜像
   docker images
   ```

2. 启动nacos

   注意：`MYSQL_SERVICE_PASSWORD`为数据库密码，如果第一步部署MySQL有自定义密码，请修改与之对应。

   ```shell
   docker run -d \
   -e JVM_XMS=384m \
   -e JVM_XMX=384m \
   -e JVM_XMN=192m \
   -e MODE=standalone \
   -e SPRING_DATASOURCE_PLATFORM=mysql \
   -e MYSQL_SERVICE_HOST="172.18.0.2" \
   -e MYSQL_SERVICE_PORT=3306 \
   -e MYSQL_SERVICE_USER=root \
   -e MYSQL_SERVICE_PASSWORD="123456" \
   -e MYSQL_SERVICE_DB_NAME=nacos \
   -p 8848:8848 \
   --network hoj-network \
   --ip 172.18.0.3 \
   --name nacos \
   --restart=always \
   nacos/nacos-server
   ```

3. 查看是否启动成功

   ```shell
   # 查看当前docker运行的容器
   docker ps
   # 查看日志
   docker logs nacos
   ```

4. 连上nacos，将后端服务需要的配置添加进去

   `ip`为服务器ip，请自行修改，登录账号密码初始都为 nacos

   ```she
   访问 http://ip:8848/nacos/index.html
   nacos/nacos(用户名和密码)
   ```

   **登陆后，点击添加**

   ![nacos2]( https://cdn.jsdelivr.net/gh/HimitZH/CDN/images/nacos2.jpg)

   **依次添加后台服务的配置文件和判题服务的配置文件**

   ![nacos1]( https://cdn.jsdelivr.net/gh/HimitZH/CDN/images/nacos1.jpg)

5. hoj-data-backup-prod.yml的配置如下，请自行修改

   ```yaml
   hoj:
     jwt:
       # 加密秘钥
       secret: zsc-acm-hoj
       # token有效时长，3*3600*24，单位秒
       expire: 259200
       # 2*3600*24s内还有请求，可进行刷新
       checkRefreshExpire: 172800
       header: token
     judge:
       # 调用判题服务器的token
       token: zsc-acm-hoj-judge-server
     db: # mysql数据库服务配置
       host: 172.18.0.4  #如果是公用容器网络 请使用网络ip 例如1.1的172.18.0.4
       port: 3306
       name: hoj # 默认hoj
       username: root 
       password: 123456 # your_mysql_password
     mail: # 邮箱服务配置
       ssl: true
       username: your_email_username
       password: your_email_password
       host: your_email_host
       port: your_email_port
       background-img: https://cdn.jsdelivr.net/gh/HimitZH/CDN/images/HCODE.png # 邮箱系统发送邮件模板的背景图片地址
     redis: # redis服务配置
       host: 172.18.0.3 # your_redis_host 如果是公用容器网络 请使用网络ip
       port: 6371
       password: your_redis_password
     web-config:
       base-url: http://www.hcode.top                  # 后端服务地址
       name: zsc-acm-hoj                               # 后端服务地址
       short-name: hoj                                 # oj简写
       register: true
       footer:  # 网站页面底部footer配置
         record:                                      
           name: 浙ICP备20009096号-1                   # 网站备案
           url: http://www.hcode.top                    # 网站域名
         project: # 项目                               
           name: HOJ                                   # 项目名字
           url: https://gitee.com/himitzh0730/hoj      # 项目地址
     hdu:
       account:
         username: hdu账号1用户名,hdu账号2用户名,...
         password: hdu账号1密码,hdu账号2密码,...
     cf:
       account:
         username: cf账号1用户名,cf账号2用户名,...
         password: cf账号1密码,cf账号2密码,...
   ```

6. 添加好后点击发布，然后再次添加hoj-judge-server-prod.yml，流程一样

   **注意：判题服务默认不是跟后端服务部署在同一云服务器的，所以以下配置，请使用mysql和redis的公网IP。**

   ```yaml
   hoj:
     judge:
       db:
         username: your_mysql_username
         password: your_mysql_password
         host: your_mysql_host
         port: your_mysql_port
         name: your_mysql_database_name # 数据库名字默认hoj
       # 调用判题服务器的token，与数据服务后台必须一致！
       token: zsc-acm-hoj-judge-server
     redis:
       host: your_redis_host
       port: your_redis_port
       password: your_redis_password
   ```

### 安装Redis

1. 依旧使用docker部署 ,**mypassword是redis的密码，请使用上面配置文件中redis的password，必须一致！**

2. 查询可用redis镜像

   ```shell
   docker search redis、
   ```

3. 选择拉取官网的镜像

   ```shell
   docker pull redis
   ```

4. 创建本地目录来挂载redis的数据

   ```shell
   mkdir -p /hoj/data/redis/data
   ```

5. 启动Redis，`mypassword`请与上面nacos导入的配置文件的redis密码一致

   ```shell
   docker run -d --name redis -p 6379:6379 
   -v /hoj/data/redis/data:/data \
   --restart="always" \
   --network hoj-network \
   --ip 172.18.0.3 \
   redis \
   --requirepass "mypassword" 
   ```

6. 查看是否启动成功

   ```shell
   docker ps
   ```

### 部署后台服务

> 注意：如果想把nacos与该后台服务分别部署不同服务器，就可以修改本HOJ项目路径`/hoj-springboot/DataBackup/src/main/resources/bootstrap.yml`的相关配置
>
> ```yaml
> hoj-backstage:
>   port: 6688
>   nacos-url: 172.18.0.3:8848  # nacos地址,如果使用了docker network 可用使用network的ip 否则请使用服务器ip
> ```

1. 依旧是需要下载本HOJ项目的hoj-springboot文件夹，然后进入该文件夹。

2. 使用cmd打开当前JudgeServer文件夹路径，然后使用mvn命令进行打包成jar包，也可以用IDEA自带的辅助工具一键package

   ```powershell
   mvn clean package -Dmaven.test.skip=true
   ```

3. 在云服务器上创建对应的文件夹进行存储，同时创建数据挂载目录

   - 该目录存放源代码打包的jar包与Dockfile文件

     ```shell
     mkdir -p /hoj/server
     ```

   - 该目录存放网站上传的文件，例如头像文件，一些zip压缩包等

     ```shell
     mkdir -p /hoj/file
     ```

   - 该目录存放题目的评测数据

     ```shell
     mkdir -p /hoj/testcase
     ```

4. 然后将第2步打包的jar包上传到该目录下(/hoj/server)，然后在当前路径(/hoj/server)同时创建名叫`Dockerfile`的文件，

   ```shell
   sudo vi Dockerfile
   ```

5. 将下面内容复制进去

   ```dockerfile
   FROM java:8
     
   COPY *.jar /app.jar
   
   CMD ["--server.port=6688"]
   
   EXPOSE 6688
   
   ENV TZ=Asia/Shanghai
   
   RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
   
   ENTRYPOINT ["java","-Xmx512m","-Xms512m","-Xmn256m","-Djava.security.egd=file:/dev/./urandom","-jar", "/app.jar"]
   ```

6. 保存退出后，使用命令打包成docker镜像

   ```shell
   // 使用dockerfile打包成镜像
   docker build -t hoj .
   // 查看hoj镜像是否存在
   docker images
   ```

7. 启动容器

   ```shell
   docker run -d -p 6688:6688 \
   -v /hoj/file:/hoj/file \
   -v /hoj/testcase:/hoj/testcase \
   --name hoj \
   --network hoj-network \
   --restart=always \
   hoj
   ```

8. 查看是否成功

   ```shell
   docker ps
   ```