# 如何备份

### 1. 单体部署

请到`~/hoj-deploy/standAlone`的目录下，即是与`docker-compose.yml`的文件同个目录下，该目录下有个叫`hoj`的文件夹，里面的文件夹介绍如下：

```html
hoj
├── file   		# 存储了上传的图片、上传的临时题目数据、markdown引用的文件等文件
├── judge  		# 存储了每个提交题目的评测过程产生的数据
├── log    		# 存储了hoj-backend项目的运行日志
├── testcase    # 存储了题目的评测数据
└── data        
    ├── mysql
    │   ├── data # 存储了MySQL数据库的数据
    ├── redis
    │   ├── data # 存储了redis产生的快照数据
```

那么，如果需要备份，只需将该`hoj`文件夹复制一份即可，在新的机器上重新部署新的hoj的时候，将该文件夹放置与`docker-compose.yml`一个目录下，使用`docker-compose up -d`即可启动恢复原来的数据。

:::tip

注意：在新机器上启用备份的数据的操作顺序如下：

1. 先将`hoj`文件夹先复制到`~/hoj-deploy/standAlone`目录里面（保证该目录无`hoj`文件夹，干净！）
2. 然后修改`.env`文件的配置，主要是`Redis`,`Nacos`,`MySQL`等的密码配置，与原先备份`hoj`文件夹时的老机器的配置一致！
3. 最后再使用`docker-compose up -d`启动！

:::



### 2. 分布式部署

- 主服务器（运行hoj-backend的服务器）

  请到`~/hoj-deploy/distributed/main`的目录下，即是与`docker-compose.yml`的文件同个目录下，该目录下有个叫`hoj`的文件夹，里面的文件夹介绍如下：

  ```html
  hoj
  ├── file   		# 存储了上传的图片、上传的临时题目数据、markdown引用的文件等文件
  ├── log    		# 存储了hoj-backend项目的运行日志
  ├── testcase    # 存储了题目的评测数据
  └── data        
      ├── mysql
      │   ├── data # 存储了MySQL数据库的数据
      ├── redis
      │   ├── data # 存储了redis产生的快照数据
  ```

- 判题服务器（运行hoj-judgeserver的服务器）

  请到`~/hoj-deploy/distributed/judgeserver`的目录下，即是与`docker-compose.yml`的文件同个目录下，该目录下有个叫`judge`的文件夹，里面的文件夹介绍如下：

  ```html
  judge
  ├── run  		# 存储了每个提交题目的评测过程产生的数据
  ├── log    		# 存储了hoj-judgeserver项目的运行日志
  ├── test_case   # 存储了题目的评测数据(每100s从主服务器同步)
  ├── spj         # 存储了SPJ的代码
  ```

那么，主要要备份的还是**主服务器**的数据，只需将该`hoj`文件夹复制一份即可，在新的机器上重新部署新的hoj的时候，将该文件夹放置与`docker-compose.yml`一个目录下，使用`docker-compose up -d`即可启动恢复原来的数据。

:::tip

注意：在新机器上启用备份的数据的操作顺序如下：

1. 先将`hoj`文件夹先复制到`~/hoj-deploy/distributed/main`目录里面（保证该目录无`hoj`文件夹，干净！）
2. 然后修改`.env`文件的配置，主要是`Redis`,`Nacos`,`MySQL`等的密码配置，与原先备份`hoj`文件夹时的老机器的配置一致！
3. 最后再使用`docker-compose up -d`启动！

:::

