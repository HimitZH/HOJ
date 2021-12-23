# 如何更新

## 一、无二次开发的更新
:::warning
2021.09.21之后部署hoj的请看下面操作  
:::
请在对应的docker-compose.yml当前文件夹下执行`docker-compose pull`拉取最新镜像，然后重新`docker-compose up -d`即可。

:::warning
2021.09.21之前部署hoj的请看下面操作
:::

### 1、修改MySQL8.0默认的密码加密方式

（1）进行hoj-mysql容器

```shell
docker exec -it hoj-mysql bash
```

  (2) 输入对应的mysql密码，进入mysql数据库

  注意：-p 后面跟着数据库密码例如hoj123456

```shell
mysql -uroot -p数据库密码
```

（3）成功进入后，执行以下命令

```shell
mysql> use mysql;

mysql> grant all PRIVILEGES on *.* to root@'%' WITH GRANT OPTION;

 
mysql> ALTER user 'root'@'%' IDENTIFIED BY '数据库密码' PASSWORD EXPIRE NEVER;

 
mysql> ALTER user 'root'@'%' IDENTIFIED WITH mysql_native_password BY '数据库密码';

 
mysql> FLUSH PRIVILEGES;
```

（4） 两次exit 退出mysql和容器

### 2、 添加hoj-mysql-checker模块

（1）可以选择拉取仓库最新的docker-compose.yml文件（跟部署操作一样,但是会覆盖之前设置的参数）或者访问：

https://gitee.com/himitzh0730/hoj-deploy/blob/master/standAlone/docker-compose.yml

（2）或者编辑docker-compose.yml文件，手动添加新模块

```yaml
  hoj-mysql-checker:
    image: registry.cn-shenzhen.aliyuncs.com/hcode/hoj_database_checker
    container_name: hoj-mysql-checker
    depends_on:
      - hoj-mysql
    links:
      - hoj-mysql:mysql
    environment:
      - MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD:-hoj123456}
    networks:
      hoj-network:
        ipv4_address: 172.20.0.8
```

(3)  保存后重启容器即可

```shell
docker-compose down

docker-compose pull

docker-compose up -d
```

**注意**：此次修改成功后，以后更新，都请在对应的docker-compose.yml当前文件夹下执行`docker-compose pull`拉取最新镜像，然后重新`docker-compose up -d`即可。

## 二、自定义前端的更新

>  附加：如何自定义前端请看这里 => [自定义前端文档](/use/update-fe.html)

（1）首先到`./hoj/hoj-vue`文件夹中，拉取[hoj-vue](https://gitee.com/himitzh0730/hoj/tree/master/hoj-vue)仓库最新的代码，请注意解决出现的冲突。

```shell
git pull
```

或者重新直接download成zip包，然后重新自定义修改前端

当然，如果想查看对比主仓库更新的内容，可以用以下命令一步步合并

```bash
git remote -v                 # 查看主仓库的远程仓库
git fetch origin master:temp  # 将最新的主仓库代码拉到本地一个temp的分支
git diff temp                 # 比较现在本地代码与最新temp分支的区别
git merge temp                # 合并temp分支到本地的master分支
git branch -d temp            # 删除temp这个临时分支
```

（2）接着，重新用npm打包，在`./hoj/hoj-vue/dist`文件夹会生成静态的前端文件，放到原来指定的位置即可

```shell
npm run build
```

（3）其它模块的更新，都请在对应的docker-compose.yml当前文件夹下执行`docker-compose pull`拉取最新镜像，然后重新`docker-compose up -d`即可。