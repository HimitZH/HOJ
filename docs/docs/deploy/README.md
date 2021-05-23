# 环境配置

## 环境说明

- 后端：需要在Linux系统下部署运行，建议使用ubuntu18.04，其它版本的Linux系统也可，同时需要**Docker**辅助部署
- 前端：Linux系统下，需要nginx进行反向代理
- 判题服务：由于判题沙盒有多操作系统版本，Linux系统或Windows都可，强烈建议Linux系统（Ubuntu）
- 数据同步：需要运行判题服务和后端服务的服务器有rsync即可

## Linux环境搭建

> 请先准备一台 CPU: 1核 内存: 2G 硬盘: 30G的云服务器，推荐Ubuntu16.04以上的操作系统，
>
> HOJ使用的Ubuntu18.04版本，单机部署建议2G以上内存

### 安装docker

1. 安装需要的包

   ```shell
   sudo apt-get update
   ```

2. 安装依赖包

   ```shell
   sudo apt-get install \
      apt-transport-https \
      ca-certificates \
      curl \
      gnupg-agent \
      software-properties-common
   ```

3. 添加 Docker 的官方 GPG 密钥

   ```shell
   curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
   ```

4. 设置远程仓库

   ```shell
   sudo add-apt-repository \
      "deb [arch=amd64] https://download.docker.com/linux/ubuntu \
     $(lsb_release -cs) \
     stable"
   ```

5. 安装 Docker-CE

   ```shell
   sudo apt-get update
   sudo apt-get install docker-ce docker-ce-cli containerd.io
   ```

6. 验证是否成功

   ```shell
   sudo docker run hello-world
   ```

### 安装docker-compose

1. 下载

   ```shell
   sudo curl -L https://get.daocloud.io/docker/compose/releases/download/1.25.5/docker-compose-`uname -s`-`uname -m` -o /usr/local/bin/docker-compose
   ```

2. 授权

   ```shell
   sudo chmod +x /usr/local/bin/docker-compose
   ```

## Windows 环境

Windows 下的安装仅供体验，勿在生产环境使用。如有必要，请使用虚拟机安装 Linux 并将 OJ 安装在其中。

以下教程仅适用于 Win10 x64 下的 `PowerShell`

1. 安装 Windows 的 Docker 工具
2. 右击右下角 Docker 图标，选择 Settings 进行设置
3. 选择 `Shared Drives` 菜单，之后勾选你想安装 OJ 的盘符位置（例如勾选D盘），点击 `Apply`
4. 输入 Windows 的账号密码进行文件共享
5. 安装 `Python`、`pip`、`git`、`docker-compose`，安装方法自行搜索。



## 单个部署

单个部署的顺序是：redis，mysql，rsync，frontend >> nacos >> backend，judgeserver



## docker-compose开始部署

1. 选择好需要安装的位置，运行下面命令

   ```shell
   git clone https://gitee.com/himitzh0730/hoj-deploy.git && cd hoj-deploy
   ```

2. 单机部署（建议服务器内存2G以上）

   > 注意：以下操作建议试用，配置大部分是默认的，实际运行请修改`docker-compose.yml`文件的配置

   ```shell
   cd standAlone && docker-compose up -d
   ```

   根据网速情况，大约十到二十分钟即可安装完毕，全程无需人工干预。

   等待命令执行完毕后，查看容器状态

   ```shell
   docker ps -a
   ```

   当看到所有的容器的状态status都为`UP`或`healthy`就代表 OJ 已经启动成功。

   > 更多自定义配置请查看**/standAlone/.env**的文件，或者/src下各组件的详情说明

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

  <u>注意：网站的注册及用户账号相关操作需要邮件系统，所以请在系统配置中配置自己的邮件服务。</u>

3. 分布式部署（默认开启rsync数据同步）

   - 主服务启动，默认不提供判题服务，请修改该启动文件配置

     ```shell
     cd distributed/main
     vim .env # 请根据文件内注释提示修改
     ```

     配置修改保存后，在`docker-compose.yml`当前路径下启动该服务

     ```shell
     docker-compose up -d
     ```

   - 判题服务启动，请修改该启动文件配置

     ```shell
     cd distributed/judgeserver
     vim .env # 请根据文件内注释提示修改
     ```

     配置修改保存后，在`docker-compose.yml`当前路径下启动该服务

     ```shell
     docker-compose up -d
     ```

   两个服务都启动完成，在浏览器输入主服务ip或域名进行访问，登录root账号到后台查看服务状态以及到`http://ip/admin/conf`修改服务配置!


> 如果需要开启https

- 单机：

  提供server.crt和server.key证书与密钥文件放置`/standAlone`目录下，与`docker-compose.yml`和`.env`文件放置同一位置，然后修改`docker-compose.yml`中的hoj-frontend的配置

- 分布式：提供server.crt和server.key证书与密钥文件放置`/distributed/main目录下，与`docker-compose.yml`和`.env`文件放置同一位置，然后修改`docker-compose.yml`中的hoj-frontend的配置

```yaml
hoj-frontend:
    image: registry.cn-shenzhen.aliyuncs.com/hcode/hoj_frontend
    container_name: hoj-frontend
    restart: always
    # 开启https，请提供证书
    volumes:
      - ./server.crt:/etc/nginx/etc/crt/server.crt
      - ./server.key:/etc/nginx/etc/crt/server.key
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
