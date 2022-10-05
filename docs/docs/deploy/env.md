# 1. 环境配置

## 环境说明
:::tip
- 后端：需要在Linux系统下部署运行，建议使用ubuntu18.04，其它版本的Linux系统也可，同时需要**Docker**辅助部署
- 前端：Linux系统下，需要nginx进行反向代理
- 判题服务：由于判题沙盒有多操作系统版本，Linux系统或Windows都可，但是在本HOJ镜像中**只能**使用**Ubuntu16.04**以上或者**CentOS8**以上。
- 数据同步：需要运行判题服务和后端服务的服务器有rsync即可
- **尽量不要使用突发性能或共享型的云服务器实例，有可能造成评测时间计量不准确。**
  :::

## Linux环境搭建

> HOJ使用的Ubuntu18.04版本，单机部署建议2核4G以上内存

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



