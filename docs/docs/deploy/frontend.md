# 前端部署

1. [下载本项目](https://gitee.com/himitzh0730/hoj/tree/master/hoj-vue)，git clone或者download zip

2. 前提是本地有vue-cli4与npm，请自行百度下载

4. 然后在当前hoj-vue文件夹的src路径运行打包命令

   ```powershell
   npm run build
   ```

5. 打包成功会在src同文件夹内有个dist文件夹，复制里面的html和css等静态文件

5. 在云服务器上创建文件夹

   ```shell
   mkdir -p /hoj/www/html
   ```

   然后将这些静态文件复制到里面即可

6. 配置nginx，在安装好nginx后，修改nginx.conf配置

   ```shell
   sudo vi /etc/nginx/nginx.conf
   ```

7. 将下面的内容复制进去

   **注意：没有域名使用IP+端口号也一样**

   ```json
   server{
       listen 80;  # 监听访问的端口号
       server_name www.hcode.top;  # 此处填写你的域名或IP
       root /hoj/www/html;   # 此处填写你的网页根目录
       location /api{
                 proxy_set_header X-Real-IP $remote_addr;
                 proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
                 proxy_set_header   Host                 $http_host;
                 proxy_set_header   X-Forwarded-Proto    $scheme;
                 proxy_pass http://localhost:6688; # 填写你的后端地址和端口
        }
        location ~ .*\.(js|json|css)$ {
                gzip on;
                gzip_static on; # gzip_static是nginx对于静态文件的处理模块，该模块可以读取预先压缩的gz文件，这样可以减少每次请求进行gzip压缩的CPU资源消耗。
                gzip_min_length 1k;
                gzip_http_version 1.1;
                gzip_comp_level 9;
                gzip_types  text/css application/javascript application/json;
                root /hoj/www/html; # 此处填写你的网页根目录
         }
         location / {  # 路由重定向以适应Vue中的路由
                 index index.html;
                 try_files $uri $uri/ /index.html;
         }
   }
   ```

8. 修改后保存，然后重启或者热重载nginx，不出意外应该可用访问前端页面了。

   ```shell
   sudo systemctl restart nginx 
   或
   sudo nginx -s reload
   ```