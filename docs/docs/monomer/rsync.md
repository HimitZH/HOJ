# 单体部署⑧——评测数据同步（分布式才需要）

:::tip
本镜像主要是用在于后端服务与判題服务不在同一机器，为了让题目评测数据从主服务器同步于判題服务所在机器而使用的，也就是分布式部署都需要本服务来同步评测数据，包括多台判题机。
:::

## 一、常规部署

1. 在主后台服务开启rsync实现服务增量同步，本HOJ使用子服务器主动拉取最新评测数据的功能（可选择主服务推的功能，但对主服务器的功耗较大）

2. 首先在主服务器（运行后端服务）的服务器中配置，指令如下

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

   设置开启自启动

   ```shell
   echo "/usr/bin/rsync --daemon --config=/etc/rsyncd/rsyncd.conf" >> /etc/rc.local
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
          	rsync -avz --delete --progress --password-file=/etc/rsyncd/rsyncd.passwd hojrsync@${ip}::testcase /hoj/testcase >> /hoj/log/rsync_slave.log
          	sleep 60
   done
   ```

   使用 nohup后台运行即可

   ```shell
   nohup /etc/rsyncd/rsyncd_slave.sh &
   ```



## 二、docker部署

### 前言

直接下载部署项目，进入到当前文件夹执行打包命令

```shell
git clone https://gitee.com/himitzh0730/hoj-deploy.git && cd hoj-deploy/src/rsync
```

当前文件夹为打包`hoj-rsync`镜像的相关文件，将这些文件复制到同一个文件夹内，之后执行以下命令进行打包成镜像.

```shell
docker build -t hoj-rsync .
```

**该服务用于测试用例数据在不同服务器之间的同步**

docker run启动

- 主服务器（Backend所在服务器）

  ```shell
  docker run -d --name hoj-rsync \
  -v ./hoj/testcase:/hoj/testcase:ro \
  -e RSYNC_MODE=master \
  -e RSYNC_USER=hojrsync \
  -e RSYNC_PASSWORD=hoj123456 \
  -p 873:873 \
  --restart=always \
  hoj-rsync
  # registry.cn-shenzhen.aliyuncs.com/hcode/hoj_rsync:1.0
  ```

- 从服务器（Judgeserver所在的服务器）

  ```shell
  docker run -d --name hoj-rsync \
  -v ./hoj/testcase:/hoj/testcase \
  -e RSYNC_MODE=slave \
  -e RSYNC_USER=hojrsync \
  -e RSYNC_PASSWORD=hoj123456 \
  -e RSYNC_MASTER_ADDR=master_server_ip \
  -p 873:873 \
  --restart=always \
  hoj-rsync
  # registry.cn-shenzhen.aliyuncs.com/hcode/hoj_rsync:1.0
  ```

  

docker-compose启动

- 主服务器（Backend所在服务器）

  ```yaml
  version: "3"
  services:
    hoj-rsync-master:
  #    image: registry.cn-shenzhen.aliyuncs.com/hcode/hoj_rsync:1.0
      image: hoj-rsync
      container_name: hoj-rsync-master
      volumes:
        - ./hoj/testcase:/hoj/testcase:ro
      environment:
        - RSYNC_MODE=master # 当前为slave主服务
        - RSYNC_USER=hojrsync # 请勿修改
        - RSYNC_PASSWORD=hoj123456 # 请修改数据同步密码
      ports:
        - "0.0.0.0:873:873"
  ```

- 从服务器（Judgeserver所在的服务器）

  ```yaml
  version: "3"
  services:
    hoj-rsync-slave:
  #    image: registry.cn-shenzhen.aliyuncs.com/hcode/hoj_rsync:1.0
      image: hoj-rsync
      container_name: hoj-rsync-slave
      restart: always
      volumes:
        - ./judge/test_case:/hoj/testcase
        - ./judge/log:/hoj/log
      environment:
        - RSYNC_MODE=slave # 当前为slave从服务
        - RSYNC_USER=hojrsync # 请勿修改
        - RSYNC_PASSWORD=hoj123456 # 与主服务器的rsync的密码一致
        - RSYNC_MASTER_ADDR=master_server_ip # 主服务器ip
      ports:
        - "0.0.0.0:873:873"
  ```

### 文件介绍

#### 1. rsync.conf

主服务器的rsync配置文件

```shell
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
secrets file = /hoj/rsyncd/rsyncd.passwd
```

#### 2. run.sh

根据`$RSYNC_MODE`环境变量启动不同模式的rsync服务

```bash
#!/usr/bin/bash
if [ "$RSYNC_MODE" == "master" ]; then
	echo "$RSYNC_USER:$RSYNC_PASSWORD" > /hoj/rsyncd/rsyncd_master.passwd
	chmod 600 /hoj/rsyncd/rsyncd_master.passwd
	rsync --daemon --config=/hoj/rsyncd/rsyncd.conf
else
	echo "$RSYNC_PASSWORD" > /hoj/rsyncd/rsyncd_slave.passwd
	chmod 600 /hoj/rsyncd/rsyncd_slave.passwd
	while true
	do
		rsync -avz --delete --progress --password-file=/hoj/rsyncd/rsyncd_slave.passwd $RSYNC_USER@$RSYNC_MASTER_ADDR::testcase /hoj/testcase >> /hoj/log/rsync_slave.log
		sleep 100
	done
fi
```

#### 3. Dockerfile

```dockerfile
FROM ubuntu:18.04

RUN apt-get update && apt-get -y install rsync

RUN mkdir -p /hoj/rsyncd

COPY run.sh /hoj/rsyncd/run.sh

COPY rsyncd.conf /hoj/rsyncd/rsyncd.conf

CMD /bin/bash /hoj/rsyncd/run.sh
```


