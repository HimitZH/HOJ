# 单体部署⑤——后端部署

## 前言

下载本项目，进入到当前文件夹执行打包命令

```shell
git clone https://gitee.com/himitzh0730/hoj-deploy.git && cd hoj-deploy/src/backend
```

当前文件夹为打包`hoj-backend`镜像的相关文件，将这些文件复制到同一个文件夹内，**然后打包[DataBackup](https://gitee.com/himitzh0730/hoj/tree/master/hoj-springboot/DataBackup)（SpringBoot项目）成jar包也放到当前文件夹**，之后执行以下命令进行打包成镜像

```shell
docker build -t hoj-backend .
```

**项目依赖于hoj-redis，hoj-nacos，hoj-mysql等镜像成功启动，以及根据前面三个镜像的配置修改环境参数才可正常启动**

docker-compose 启动

```yaml
version: "3"
services:
  hoj-backend:
#    image: registry.cn-shenzhen.aliyuncs.com/hcode/hoj_backend
	image: hoj-backend
    container_name: hoj-backend
    restart: always
    depends_on:
      - hoj-redis
      - hoj-mysql
      - hoj-nacos
    volumes:
      - ./hoj/file:/hoj/file
      - ./hoj/testcase:/hoj/testcase
      - ./hoj/log/backend:/hoj/log/backend
    environment:
      - TZ=Asia/Shanghai
      - BACKEND_SERVER_PORT=6688 # backend服务端口号
      - NACOS_URL=172.20.0.4:8848 # hoj-nacos的url
      - NACOS_USERNAME=root # nacos的管理员账号
      - NACOS_PASSWORD=hoj123456 # nacos的管理员密码
      - JWT_TOKEN_SECRET=default # 加密秘钥 默认则生成32位随机密钥
      - JWT_TOKEN_EXPIRE=86400 # token过期时间默认为24小时 86400s
      - JWT_TOKEN_FRESH_EXPIRE=43200 # token默认12小时可自动刷新
      - JUDGE_TOKEN=default # 调用判题服务器的token 默认则生成32位随机密钥
      - MYSQL_HOST=172.20.0.3 # hoj-mysql的host
      - MYSQL_PUBLIC_HOST=172.20.0.3 # 如果判题服务是分布式，请提供当前mysql所在服务器的公网ip
      - MYSQL_PUBLIC_PORT=3306 # mysql主机端口号
      - MYSQL_PORT=3306 # mysql容器内端口号
      - MYSQL_DATABASE_NAME=hoj # 改动需要修改hoj-mysql镜像,默认为hoj
      - MYSQL_USERNAME=root 
      - MYSQL_ROOT_PASSWORD=hoj123456 # hoj-mysql的root账号密码
      - EMAIL_SERVER_HOST=smtp.qq.com # 请使用邮件服务的域名或ip
      - EMAIL_SERVER_PORT=465 # 请使用邮件服务的端口号
      - EMAIL_USERNMAE=-your_email_username # 请使用对应邮箱账号
      - EMAIL_PASSWORD=-your_email_password # 请使用对应邮箱密码
      - REDIS_HOST=172.20.0.2 # hoj-redis的host
      - REDIS_PORT=6379 # hoj-redis的port
      - REDIS_PASSWORD=hoj123456 #hoj-redis的密码
      - OPEN_REMOTE_JUDGE=true # 是否开启对hdu和codeforces的虚拟判题
      # 开启虚拟判题请提供对应oj的账号密码 格式为 
      # username1,username2,...
      # password1,password2,...
      - HDU_ACCOUNT_USERNAME_LIST=
      - HDU_ACCOUNT_PASSWORD_LIST=
      - CF_ACCOUNT_USERNAME_LIST=
      - CF_ACCOUNT_USERNAME_LIST=
      - ATCODER_ACCOUNT_USERNAME_LIST=
      - ATCODER_ACCOUNT_PASSWORD_LIST=
      - SPOJ_ACCOUNT_USERNAME_LIST=
      - SPOJ_ACCOUNT_PASSWORD_LIST=
      # 是否强制使用配置文件的remote judge账号覆盖原有系统的账号列表
      - FORCED_UPDATE_REMOTE_JUDGE_ACCOUNT=false
    ports:
      - "6688:6688"
    networks:
      hoj-network:
        ipv4_address: 172.20.0.5
        
  hoj-redis:
    image: redis:5.0.9-alpine
    container_name: hoj-redis
    restart: always
    volumes:
      - ./hoj/data/redis/data:/data
    networks:
      hoj-network:
        ipv4_address: 172.20.0.2
    ports:
      - "6379:6379"
    command: redis-server --requirepass "hoj123456" --appendonly yes
        
  hoj-mysql:
    image: registry.cn-shenzhen.aliyuncs.com/hcode/hoj_database
    container_name: hoj-mysql
    restart: always
    volumes:
      - ./hoj/data/mysql/data:/var/lib/mysql
    environment:
      - MYSQL_ROOT_PASSWORD=hoj123456
      - TZ=Asia/Shanghai
      - NACOS_USERNAME=root
      - NACOS_PASSWORD=hoj123456
    ports:
      - "3306:3306"
    networks:
      hoj-network:
        ipv4_address: 172.20.0.3
      
  hoj-nacos:
    image: nacos/nacos-server:1.4.2
    container_name: hoj-nacos
    restart: always
    depends_on: 
      - hoj-mysql
    environment:
      - JVM_XMX=384m
      - JVM_XMS=384m
      - JVM_XMN=192m
      - MODE=standalone
      - SPRING_DATASOURCE_PLATFORM=mysql
      - MYSQL_SERVICE_HOST=172.20.0.3
      - MYSQL_SERVICE_PORT=3306
      - MYSQL_SERVICE_USER=root
      - MYSQL_SERVICE_PASSWORD=hoj123456
      - MYSQL_SERVICE_DB_NAME=nacos
      - NACOS_AUTH_ENABLE=true # 开启鉴权
    networks:
      hoj-network:
        ipv4_address: 172.20.0.4

networks:
   hoj-network:
     driver: bridge
     ipam:
       config:
         - subnet: 172.20.0.0/16
```



## 文件介绍

### 1. check_nacos.sh

用于检测nacos是否启动完成，然后再执行启动backend

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
bash /run.sh
```

### 2. run.sh

启动backend的springboot jar包

```shell
#!/bin/sh

java -Djava.security.egd=file:/dev/./urandom -jar  /app.jar
```

### 3. Dockerfile

```dockerfile
FROM java:8

COPY *.jar /app.jar

COPY check_nacos.sh /check_nacos.sh

COPY run.sh /run.sh

ENV TZ=Asia/Shanghai

ENV BACKEND_SERVER_PORT=6688

VOLUME ["/hoj/file","/hoj/testcase"]

RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

CMD ["bash","/check_nacos.sh"]

EXPOSE $BACKEND_SERVER_PORT

```
