# 1. 环境配置

## 服务器配置

注意：购买服务器时，**请尽量不要使用突发性能实例或共享型实例，这可能会导致评测时间计量不准确。**

表内系数：**n >= 1**

| 推荐程度        | CPU  | 内存   | 硬盘    | 建议                 |
| ----------- | ---- | ---- | ----- | ------------------ |
| **最低**服务器配置 | 1核   | 2G   | n*20G | 只推荐测试用，内存不足...     |
| **推荐**服务器配置 | n*2核 | n*4G | n*20G | 推测大约n*(100~200)人使用 |

:::info

注意：如果要举办**100**人以上的比赛，请适当增加判题机，评测并发速度快，分担评测压力。

建议单个判题机选择服务器的配置为**1核2G**以上，带宽**1M**以上即可，后续需要增加的具体请看：[多个判题机](/deploy/multi-judgeserver/)

:::

## 系统选择

| 推荐程度     | 系统                | 架构    | 说明                                       |
| -------- | ----------------- | ----- | ---------------------------------------- |
| **优先**   | Ubuntu 16.04 及以上  | amd64 | HOJ是基于Ubuntu 18.04构建的，强烈建议使用             |
| **其次**   | Centos 8          | amd64 | 以前喜欢用Centos 7的用户建议先升级8                   |
| **最低**   | Centos 7          | amd64 | 请先看文档：[Centos 7下部署遇到的问题](/deploy/faq/#一、部署在centos7系统中无法正常评测-如何解决) |
| **未测试过** | 其他 x64 Linux 发行版  | ?     | 未知，系统支持cgroup一般可以                        |
| **不建议**  | Window、Mac Os ... | ?     | 只建议运行除判题机（hoj-judgeserver）外的服务           |

## Linux环境搭建

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



