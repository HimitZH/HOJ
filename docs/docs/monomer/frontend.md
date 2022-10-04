# 单体部署⑦——前端部署

## 一、常规部署

### (1). 安装nginx
:::warning
注意：apt下载太慢的话，建议换阿里云源，请自行百度or谷歌
:::
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

   在浏览器输入你的ip地址，如果出现Wellcome to nginx 那么就是配置成功

### (2). 部署

1. [下载本项目](https://gitee.com/himitzh0730/hoj/tree/master/hoj-vue)，git clone或者download zip

2. 前提是本地有vue-cli4与npm，请自行百度下载

3. 然后在当前hoj-vue文件夹的src路径运行打包命令

   ```powershell
   npm run build
   ```

4. 打包成功会在src同文件夹内有个dist文件夹，复制里面的html和css等静态文件

5. 在云服务器上创建文件夹

   ```shell
   mkdir -p /hoj/www/html
   ```

   然后将这些静态文件复制到里面即可。

   ​

   再将另外一个滚榜的前端文件夹也放到指定的文件夹，先去下载文件夹[scrollBoard](https://gitee.com/himitzh0730/hoj/tree/master/hoj-scrollBoard), 然后在服务器上创建文件夹`/hoj/www/scrollBoard`，将下载好的文件夹scrollBoard的内容复制进去。

   ```shell
   mkdir -p /hoj/www/scrollBoard
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
         location ^~ /scrollBoard{
             alias  /hoj/www/scrollBoard;
             try_files $uri $uri/ /index.html;
             index index.html index.htm;
         }
   }
   ```

8. 修改后保存，然后重启或者热重载nginx，不出意外应该可用访问前端页面了。

   ```shell
   sudo systemctl restart nginx 
   或
   sudo nginx -s reload
   ```



## 二、docker部署

:::tip
html文件夹下为hoj的vue前端打包的静态资源
:::

直接下载本项目，进入到当前文件夹执行打包命令

```shell
git clone https://gitee.com/himitzh0730/hoj-deploy.git && cd hoj-deploy/src/frontend
```

当前文件夹为打包`hoj-frontend`镜像的相关文件，将这些文件复制到同一个文件夹内，之后执行以下命令进行打包成镜像

```shell
docker build -t hoj-frontend .
```



**docker run 启动**

- Http方式

  ```shell
  docker run -d --name hoj-frontend \
  -e SERVER_NAME=localhost \
  -e BACKEND_SERVER_HOST=backend_server_host \
  -e BACKEND_SERVER_PORT=backend_server_port \
  -e USE_HTTPS=false \
  -p 80:80 \
  --restart="always" \
  hoj-frontend
  # registry.cn-shenzhen.aliyuncs.com/hcode/hoj_frontend
  ```

- Https方式

  **需将SSL证书与公钥文件（server.crt、server.kry）放置当前目录** 

  ```shell
  docker run -d --name hoj-frontend \
  -e SERVER_NAME=localhost \
  -e BACKEND_SERVER_HOST=backend_server_host \
  -e BACKEND_SERVER_PORT=backend_server_port \
  -e USE_HTTPS=true \
  -e ./server.crt:/etc/nginx/etc/crt/server.crt \
  -e ./server.key:/etc/nginx/etc/crt/server.key \
  -p 80:80 \
  -p 443:443 \
  --restart="always" \
  hoj-frontend
  # registry.cn-shenzhen.aliyuncs.com/hcode/hoj_frontend
  ```

**docker-compose 启动**

```yaml
version: "3"
services:
  hoj-frontend:
    # image: registry.cn-shenzhen.aliyuncs.com/hcode/hoj_frontend
    image: hoj-frontend
    container_name: hoj-frontend
    restart: always
    # 开启https，请提供证书
    #volumes:
    #  - ./server.crt:/etc/nginx/etc/crt/server.crt
    #  - ./server.key:/etc/nginx/etc/crt/server.key
    environment:
      - SERVER_NAME=localhost # 域名或localhost(本地)
      - BACKEND_SERVER_HOST=172.20.0.5 # backend后端服务地址
      - BACKEND_SERVER_PORT=6688 # backend后端服务端口号
      - USE_HTTPS=false
    ports:
      - "80:80"
      - "443:443"
#    networks:
#      hoj-network:
#        ipv4_address: 172.20.0.6
```

### 文件介绍

#### 1. default.conf.ssl.template

nginx的SSL配置文件模板，需要在执行 run.sh注入环境变量生成对应的nginx.conf文件

```nginx
server {
    listen 80;
    #填写绑定证书的域名
    server_name ${SERVER_NAME};
    #把http的域名请求转成https
    return 301 https://$host$request_uri;
}

server {
	listen 443 ssl;
	server_name ${SERVER_NAME};
    #证书文件名称
    ssl_certificate /etc/nginx/etc/crt/server.crt;
    #私钥文件名称
    ssl_certificate_key /etc/nginx/etc/crt/server.key;
    ssl_session_timeout 5m;
    #请按照以下协议配置
    ssl_protocols TLSv1 TLSv1.1 TLSv1.2;
    #请按照以下套件配置，配置加密套件，写法遵循 openssl 标准。
    ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:HIGH:!aNULL:!MD5:!RC4:!DHE;
    ssl_prefer_server_ciphers on;

	root /usr/share/nginx/html;
    location /api{
		proxy_pass http://${BACKEND_SERVER_HOST}:${BACKEND_SERVER_PORT}; # 填写你的后端地址和端口
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
	    proxy_set_header   Host                 $http_host;
        proxy_set_header   X-Forwarded-Proto    $scheme;
          
        client_max_body_size 128M;
    }
    location ~ .*\.(js|json|css)$ {
            gzip on;
            gzip_static on; # gzip_static是nginx对于静态文件的处理模块，该模块可以读取预先压缩的gz文件，这样可以减少每次请求进行gzip压缩的CPU资源消耗。
            gzip_min_length 1k;
            gzip_http_version 1.1;
            gzip_comp_level 9;
            gzip_types  text/css application/javascript application/json;
            root /usr/share/nginx/html;
    }
    location / {  # 路由重定向以适应Vue中的路由
            index index.html;
            try_files $uri $uri/ /index.html;
    }
  	location ^~ /scrollBoard{
        alias   /usr/share/nginx/scrollBoard;
        try_files $uri $uri/ /index.html;
        index index.html index.htm;
    }
	
}
```

#### 2. default.conf.template

nginx的配置文件模板，需要在执行 run.sh注入环境变量生成对应的nginx.conf文件

```nginx
server {
	listen 80;
	server_name ${SERVER_NAME};
	root /usr/share/nginx/html;
    location /api{
		proxy_pass http://${BACKEND_SERVER_HOST}:${BACKEND_SERVER_PORT}; # 填写你的后端地址和端口
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
	    proxy_set_header   Host                 $http_host;
        proxy_set_header   X-Forwarded-Proto    $scheme;
          
        client_max_body_size 128M;
    }
    location ~ .*\.(js|json|css)$ {
            gzip on;
            gzip_static on; # gzip_static是nginx对于静态文件的处理模块，该模块可以读取预先压缩的gz文件，这样可以减少每次请求进行gzip压缩的CPU资源消耗。
            gzip_min_length 1k;
            gzip_http_version 1.1;
            gzip_comp_level 9;
            gzip_types  text/css application/javascript application/json;
            root /usr/share/nginx/html;
    }
    location / {  # 路由重定向以适应Vue中的路由
            index index.html;
            try_files $uri $uri/ /index.html;
    }
  	location ^~ /scrollBoard{
        alias   /usr/share/nginx/scrollBoard;
        try_files $uri $uri/ /index.html;
        index index.html index.htm;
    }
}
```

#### 3. run.sh

作用是将模板conf配置文件注入对应环境变量，生成到指定文件夹

```shell
#!/usr/bin/env sh
set -eu 
if [ "$USE_HTTPS" == "true" ]; then
	envsubst '${SERVER_NAME} ${BACKEND_SERVER_HOST} ${BACKEND_SERVER_PORT}' < /etc/nginx/conf.d/default.conf.ssl.template > /etc/nginx/conf.d/default.conf
else
	envsubst '${SERVER_NAME} ${BACKEND_SERVER_HOST} ${BACKEND_SERVER_PORT}' < /etc/nginx/conf.d/default.conf.template > /etc/nginx/conf.d/default.conf
fi
rm /etc/nginx/conf.d/default.conf.template
rm /etc/nginx/conf.d/default.conf.ssl.template
exec "$@"
```

#### 4. Dockerfile

```dockerfile
FROM nginx:1.15-alpine

COPY default.conf.template /etc/nginx/conf.d/default.conf.template

COPY default.conf.ssl.template /etc/nginx/conf.d/default.conf.ssl.template

ADD html/ /usr/share/nginx/html/

ADD scrollBoard/ /usr/share/nginx/scrollBoard/

COPY ./run.sh /docker-entrypoint.sh

RUN chmod a+x /docker-entrypoint.sh

ENTRYPOINT ["/docker-entrypoint.sh"]

# 每次容器启动时执行
CMD ["nginx", "-g", "daemon off;"]

# 容器应用端口
EXPOSE 80

EXPOSE 443
```
