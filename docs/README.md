### 1. 后端部署

**linux下安装下载docker，自行百度**

#### 1.1 安装MySQL

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
   mkdir -p /data/mysql/conf
   //mysql数据文件路径 
   mkdir –p /data/mysql/data
   //日志文件路径
   mkdir -p /data/mysql/logs 
   ```

4. 启动mysql

   ```shell
   docker run -p 3306:3306 --name mysql -d \
   --restart=always \
   --network hoj-network \
   --ip 172.18.0.4 \
   --restart="always" \
   -v /data/mysql/conf.d:/etc/mysql/conf.d \
   -v /data/mysql/logs:/logs \
   -v /data/mysql/data:/data \
   -e MYSQL_ROOT_PASSWORD="123456" \
   mysql:5.7 
   ```

5. 启动成功后 使用docker ps 可查看 如果正常则进行数据库创建操作

6. 创建名字叫hoj的数据库，执行脚本在sqlAndSetting文件夹里面或者 [hoj.sql](https://gitee.com/himitzh0730/hoj/blob/master/sqlAndsetting/hoj.sql)、[hoj-data.sql](https://gitee.com/himitzh0730/hoj/blob/master/sqlAndsetting/hoj-data.sql)

7. 创建名字叫nacos的数据库，执行脚本在sqlAndSetting文件夹里面或者 [nacos.sql](https://gitee.com/himitzh0730/hoj/blob/master/sqlAndsetting/nacos-mysql.sql)

#### 1.2  注册中心与配置中心naco

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

   ```shell
   docker run -d \
   -e JVM_XMS=128m \
   -e JVM_XMX=128m \
   -e JVM_XMN=64m \
   -e MODE=standalone \
   -e SPRING_DATASOURCE_PLATFORM=mysql \
   -e MYSQL_SERVICE_HOST="数据库所在服务器ip或使用容器ip（172.18.0.4）" \
   -e MYSQL_SERVICE_PORT=3306 \
   -e MYSQL_SERVICE_USER=root \
   -e MYSQL_SERVICE_PASSWORD="数据库密码" \
   -e MYSQL_SERVICE_DB_NAME=nacos \
   -p 8848:8848 \
   --network hoj-network \
   --ip 172.18.0.3 \
   --name nacos \
   --restart=always \
   nacos/nacos-server
   ```

3. 查看自定义网络中各容器ip

   ```shell
   //查看网络
   docker network ls
   //查看网络容器
   docker network inspect hoj-network
   ```

4. 连上nacos，将后端服务需要的配置添加进去

   ```she
   http://ip:8848/nacos/index.html
   nacos/nacos(用户名和密码)
   ```

   **登陆后，点击添加**

   ![nacos1]( https://cdn.jsdelivr.net/gh/HimitZH/CDN/images/nacos1.jpg)

   **依次添加后台服务的配置文件和判题服务的配置文件**

   ![nacos2]( https://cdn.jsdelivr.net/gh/HimitZH/CDN/images/nacos2.jpg)

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
       host: 172.18.0.2 #your_redis_host 如果是公用容器网络 请使用网络ip 例如1.3的172.18.0.2
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

6. 添加好后点击发布，再次添加hoj-judge-server-prod.yml，流程一样

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

#### 1.3 Redis部署

依旧使用docker部署 ,**mypassword是redis的密码，请使用上面配置文件中redis的password，必须一致！**

```shell
//查询目前可用的reids镜像 
docker search redis

//选择拉取官网的镜像
docker pull redis

//查看本地是否有redis镜像 
docker images

//运行redis并设置密码 一般运行
docker run -d --name redis -p 6379:6379 redis --appendonly yes --requirepass "mypassword" --restart="always" --network hoj-network

// 有配置文件 数据挂载的运行
docker run -d --name redis -p 6379:6379 -v /hoj/redis/data:/data -v /hoj/redis/conf/redis.conf:/etc/redis/redis.conf --restart="always" --network hoj-network --ip 172.18.0.2 redis --requirepass "mypassword"
```

#### 1.4 DataBackup数据后台部署

1. 修改该路径**/hoj-springboot/DataBackup/src/main/resources/bootstrap.yml**的相关配置

   ```yaml
   hoj-backstage:
     port: 6688
     nacos-url: 172.18.0.3:8848  # nacos地址,如果使用了docker network 可用使用network的ip 否则请使用服务器ip
   ```

2. 使用cmd打开当前JudgeServer文件夹路径，然后使用mvn命令进行打包成jar包

   ```powershell
   mvn clean package -Dmaven.test.skip=true
   ```

3. 上传到服务器，**使用apt安装JDK8，请自行百度**，然后在当前文件夹同时创建名叫`Dockerfile`的文件，编写内容如下：

   ```dockerfile
   FROM java:8
     
   COPY *.jar /app.jar
   
   CMD ["--server.port=6688"]
   
   EXPOSE 6688
   
   ENV TZ=Asia/Shanghai
   
   RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
   
   ENTRYPOINT ["java","-Xmx512m","-Xms512m","-Xmn256m","-Djava.security.egd=file:/dev/./urandom","-jar", "/app.jar"]
   ```

4. 在当前文件夹使用命令打包成docker镜像

   ```shell
   // 使用dockerfile打包成镜像
   docker build -t hoj .
   // 查看hoj镜像是否存在
   docker images
   ```

5. 启动容器

   ```shell
   docker run -d -p 6688:6688 -v /hoj/file:/hoj/file -v /hoj/testcase:/hoj/testcase --name hoj --network hoj-network hoj
   ```

6. 查看是否成功

   ```shell
   docker ps
   ```

#### 1.5 JudgeServer判题服务部署

> 注意：判题服务可以部署多台云服务器，步骤一样

1. 下载本项目，git clone或者download zip

2. 修改该路径**/hoj-springboot/JudgeServer/src/main/resources/bootstrap.yml**的相关配置

   ```yaml
   hoj-judge-server:
     max-task-num: -1 # -1表示最大并行任务数为cpu核心数*2
     ip: 127.0.0.1 # -1表示使用默认本地ipv4，若是部署其它服务器，务必使用公网ip
     port: 8088  # 端口号
     name: hoj-judger-1 # 判题机名字 唯一不可重复！！！
     nacos-url: 127.0.0.1:8848  # nacos地址
     remote-judge:
       open: true # 当前判题服务器是否开启远程虚拟判题功能
       max-task-num: -1 # -1表示最大并行任务数为(cpu核心数*2)*2
   ```

3. 使用cmd打开当前JudgeServer文件夹路径，然后使用mvn命令进行打包成jar包

   ```shell
   mvn clean package -Dmaven.test.skip=true
   ```

4. 打包成功后在路径**/hoj-springboot/JudgeServer/target/** 文件夹内找到类似JudgeServer.jar的jar包，然后将该jar包与**/judger**文件夹内的Judger-SandBox文件（go打包的linux系统下可执行文件）一起上传到云服务器的同一个文件夹内，同时在该文件夹内创建一个JudgeServer.json的文件，JVM的配置可以直接配置，内容如下：

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

5. 使用apt安装JDK8，请自行百度。**下面的操作都使用root权限，才不会出错。**

6. 接下来使用pm2启动管理Judger-SandBox和JudgeServer，当然可用别的方式启动jar包，nohup之类的都可以，记住Judger-SandBox默认占用5050端口，JudgeServer占用8088端口，请确认不会被其它进程占用！本次介绍使用pm2管理启动：

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

7. 使用了第5步的就可以启动判题服务和判题安全沙盒了，操作如下：

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

8. 最后一步，下载对应编译语言的编译器，HOJ默认支持 GCC,G++,Python2,Python3,Java,Golang,C#编程语言，请自行百度下载对应的编译器。



### 2. 前端部署

1. 下载本项目，git clone或者download zip

2. 前提是本地有vue-cli4，请自行百度下载

4. 然后在当前src路径运行打包命令

   ```powershell
   npm run build
   ```

5. 打包成功会在src同文件夹内有个dist文件夹，复制里面的html和css等静态文件，上传到服务器的指定文件夹内/hoj/www/html内

6. 配置nginx，在安装好nginx后，修改nginx.conf配置

   ```shell
   sudo vi /etc/nginx/nginx.conf
   ```

7. 将下面的内容复制进去

   ```json
   server{
       listen 80;
       server_name www.hcode.top;  # 此处填写你的域名或IP
       root /hoj/www/htm;   # 此处填写你的网页根目录
       location /api{
                 proxy_set_header X-Real-IP $remote_addr;
                 proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
                 proxy_set_header   Host                 $http_host;
                 proxy_set_header   X-Forwarded-Proto    $scheme;
                 proxy_pass http://localhost:6688; # 填写你的后端地址和端口
        }
        location ~ .*\.(js|json|css)$ {
                gzip on;
                gzip_static on; # gzip_static是nginx对于静态文件的处理模块，该模块可以读取预先压缩的gz文件，这样可以减少每次请求进行gzip压缩的CPU资源消耗。
                gzip_min_length 1k;
                gzip_http_version 1.1;
                gzip_comp_level 9;
                gzip_types  text/css application/javascript application/json;
                root /hoj/www/html; # 此处填写你的网页根目录
         }
         location / {  # 路由重定向以适应Vue中的路由
                 index index.html;
                 try_files $uri $uri/ /index.html;
         }
   }
   ```

8. 修改后保存，然后重启或者热重载nginx，不出意外应该可用访问前端页面了。

   ```shell
   sudo systemctl restart nginx 
   或
   sudo nginx -s reload
   ```




### 3. 评测数据同步

1. 在主后台服务开启rsync实现服务增量同步，本HOJ使用子服务器主动拉取最新评测数据的功能（可选择主服务推的功能，但对主服务器的功耗较大）

2. 首先在主服务器运行DataBackup的服务器中配置，指令如下

   ```shell
   vim /etc/rsyncd/rsyncd.conf # 新建配置文件
   ```

   ```shell
   # 将以下内容写入的rsyncd.conf文件里面 然后保存退出
   port = 873
   uid = root
   gid = root
   use chroot = yes
   read only = yes
   log file = /hoj/log/rsyncd.log
   [testcase]
   path = /hoj/testcase/
   list = yes
   auth users = hojrsync
   secrets file = /etc/rsyncd/rsyncd.passwd
   ```

   再新建密码配置文件

   ```shell
   vim /etc/rsyncd/rsyncd.passwd
   ```

   ```shell
   # 将以下内容写入rsyncd.passwd文件里面，冒号后面的密码可用自定义，然后保存退出。
   hojrsync:123456
   ```

   修改密码配置文件的权限为600

   ```shell
   chmod 600 /etc/rsyncd/rsyncd.passwd
   ```

   然后使用命令，使用后台守护进程运行rsync

   ```shell
   rsync --daemon --config=/etc/rsyncd/rsyncd.conf
   ```

3. 之后在运行JudgeServer判题服务的服务器上使用rsync每60秒同步一次指定文件夹的评测数据（同步周期可自己改）

   新建密码配置文件，同时写入与主服务端的rsync一样的密码

   ```shell
   vim /etc/rsyncd/rsyncd.passwd
   ```

   ```shell
   123456 # 保存退出
   ```

   修改密码配置文件的权限为600

   ```shell
   chmod 600 /etc/rsyncd/rsyncd.passwd
   ```

   然后编写sh文件

   ```shell
   vim /etc/rsyncd/rsyncd_slave.sh
   ```

   注意${ip}写自己主服务器的ip

   ```shell
   while true
   do
          	rsync -avz --delete --progress --password-file=/etc/rsyncd/rsyncd.passwd hojrsync@${ip}::testcase /judge/test_case >> /judge/log/rsync_slave.log
          	sleep 60
   done
   ```

   使用 nohup后台运行即可

   ```shell
   nohup /etc/rsyncd/rsyncd_slave.sh &
   ```

   

