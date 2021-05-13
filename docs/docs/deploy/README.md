# 环境配置

## 写在前面

> 本次部署基于部署人员熟悉springboot与vue的打包，下次有空再将对应的前后端打包成镜像，使用docker一键部署就比较方便
> **但目前只能"享受"一步步部署的乐趣**

## 环境说明

- 后端：需要在Linux系统下部署运行，建议使用ubuntu18.04，其它版本的Linux系统也可，同时需要**Docker**辅助部署
- 前端：Linux系统下，需要nginx进行反向代理
- 判题服务：由于判题沙盒有多操作系统版本，Linux系统或Windows都可，强烈建议Linux系统（Ubuntu）
- 数据同步：需要运行判题服务和后端服务的服务器有rsync即可

## Linux服务器环境搭建

> 请先准备一台 CPU: 1核 内存: 2G 硬盘: 30G的云服务器，推荐Ubuntu16.04以上的操作系统，
>
> HOJ使用的Ubuntu18.04版本

### 安装nginx

> 注意：apt下载太慢的话，建议换阿里云源，请自行百度or谷歌

1. 使用apt安装

   ```shell
   sudo apt install nginx
   ```

2. 路径介绍

   - /usr/sbin/nginx：主程序
   - /etc/nginx：存放配置文件
   - /usr/share/nginx：存放静态文件
   - /var/log/nginx：存放日志

3. 启动nginx

   ```shell
   service nginx start
   ```

4. 验证是否成功

   在浏览器输入你的ip地址，如果出现Wellcome to nginx 那么就是配置成功。

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



