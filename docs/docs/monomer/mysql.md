# 单体部署①——MySQL部署

首先 先下载[hoj-deploy](https://gitee.com/himitzh0730/hoj-deploy/tree/master)

```shell
git clone https://gitee.com/himitzh0730/hoj-deploy.git && cd hoj-deploy/src/mysql
```

## 前言

当前文件夹为打包`hoj-mysql`镜像的相关文件，只需将这些文件复制到同一个文件夹内，之后执行以下命令进行打包成镜像。

```shell
docker build -t hoj-mysql .
```



docker run启动

```shell
docker run -d --name hoj-mysql \
-v $PWD/hoj/data/mysql/data:/var/lib/mysql \
-e MYSQL_ROOT_PASSWORD="hoj123456" \
-e TZ="Asia/Shanghai" \
-e NACOS_USERNAME=root \
-e NACOS_PASSWORD=hoj123456 \
-p 3306:3306 \
--restart="always" \
hoj-mysql
#registry.cn-shenzhen.aliyuncs.com/hcode/hoj_database
```

docker-compose 启动

```shell
version: "3"
services:
  hoj-mysql:
    #image: registry.cn-shenzhen.aliyuncs.com/hcode/hoj_database
    image: hoj-mysql
    container_name: hoj-mysql
    restart: always
    volumes:
      - ./hoj/data/mysql/data:/var/lib/mysql
    environment:
      - MYSQL_ROOT_PASSWORD=hoj123456 # root账号的密码
      - TZ=Asia/Shanghai
      - NACOS_USERNAME=root # 需要初始化nacos数据库的管理员账号
      - NACOS_PASSWORD=hoj123456 # nacos管理员账号的密码
    ports:
      - "3306:3306"
#  如果有自定义网络可以类似添加如下
#    networks:
#      hoj-network:
#        ipv4_address: 172.20.0.3
```



## 文件介绍

### 1. bcrypt

此文件为go打包的可执行文件，作用是生成经过bcrypt加密的对应**nacos管理员账号的密码**，然后生成对应的插入sql语句，代码内容如下：

```go
package main

import (
	"flag"
	"fmt"
	"golang.org/x/crypto/bcrypt"
	"os"
)

// 加密密码
func HashAndSalt(pwd []byte) string {

	hash, err := bcrypt.GenerateFromPassword(pwd, bcrypt.MinCost)
	if err != nil {

	}
	return string(hash)
}

func main() {
	var username string
	var password string
	var filePath string

	flag.StringVar(&username,"username", "nacos", "nacos登录账号")
	flag.StringVar(&password,"password", "nacos", "nacos登录密码")
	flag.StringVar(&filePath,"filepath", "./nacos-data.sql", "sql脚本的文件夹路径")

	//解析命令行参数
	flag.Parse()

	bcrtpyPassword := HashAndSalt([]byte(password))

	sql := "use `nacos`;\nINSERT INTO users (username, password, enabled) VALUES ('%s', '%s', TRUE);\nINSERT INTO roles (username, role) VALUES ('%s', 'ROLE_ADMIN');"
	formatSql := fmt.Sprintf(sql, username, bcrtpyPassword, username)

	fileObj, err := os.OpenFile(filePath, os.O_APPEND|os.O_CREATE|os.O_WRONLY, 0111)

	if err != nil {
		fmt.Println("err:" + err.Error())
		return
	}
	defer fileObj.Close()
	fileObj.WriteString(formatSql)
}
```

### 2. hoj.sql

此文件为hoj数据库的生成脚本及相关表数据的初始化

### 3. nacos.sql

此文件为nacos数据库的生成脚本及相关表数据的初始化

### 4. run.sh

此文件为shell脚本，用于执行sql脚本文件的执行，生成hoj，nacos数据库及插入相关数据

```shell
#!/bin/bash

$WORK_PATH/bcrypt --username=$NACOS_USERNAME --password=$NACOS_PASSWORD --filepath=$WORK_PATH/$FILE_2;

sleep 2;

mysql -uroot -p$MYSQL_ROOT_PASSWORD << EOF
system echo '================Start create database hoj====================';
source $WORK_PATH/$FILE_0;
system echo '================Start create database nacos==================';
source $WORK_PATH/$FILE_1;
system echo '================Start insert user into nacos=================';
source $WORK_PATH/$FILE_2;
system echo '=====================Everything is ok!=======================';
EOF
```

### 5. Dockerfile

```dockerfile
FROM mysql:8

#定义工作目录
ENV WORK_PATH /usr/local/work

#定义会被容器自动执行的目录
ENV AUTO_RUN_DIR /docker-entrypoint-initdb.d

#定义sql文件名
ENV FILE_0 hoj.sql
ENV FILE_1 nacos.sql
ENV FILE_2 nacos-data.sql

#定义shell文件名
ENV INSTALL_DATA_SHELL run.sh

#定义生成nacos-data.sql的文件名
ENV NACOS_DATA_SHELL bcrypt

ENV NACOS_USERNAME=${NACOS_USERNAME}

ENV NACOS_PASSWORD=${NACOS_PASSWORD}

COPY ./$FILE_0 $WORK_PATH/
COPY ./$FILE_1 $WORK_PATH/
COPY ./$FILE_3 $WORK_PATH/

COPY ./$INSTALL_DATA_SHELL $AUTO_RUN_DIR/
COPY ./bcrypt  $WORK_PATH/

COPY ./mysql.cnf /etc/mysql/conf.d/

RUN chmod a+x $WORK_PATH/bcrypt

RUN echo '' > $WORK_PATH/$FILE_2

RUN chmod +777 $WORK_PATH/$FILE_2

#给执行文件增加可执行权限

RUN chmod a+x $AUTO_RUN_DIR/$INSTALL_DATA_SHELL
```