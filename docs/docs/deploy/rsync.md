# 评测数据同步

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
