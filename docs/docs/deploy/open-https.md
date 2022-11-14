# 开启HTTPS

:::tip

注意：需要使用你所购买域名的**Ngnix**对应的证书文件

:::

- 单机部署：

  提供`server.crt`和`server.key`证书与密钥文件放置`/standAlone`目录下，与`docker-compose.yml`和`.env`文件放置同一位置，然后修改`docker-compose.yml`中的hoj-frontend的配置。

- 分布式部署：

  提供`server.crt`和`server.key`证书与密钥文件放置`/distributed/main`目录下，与`docker-compose.yml`和`.env`文件放置同一位置，然后修改`docker-compose.yml`中的hoj-frontend的配置。



主要修改`volumes`的挂载，取消掉原来的注释，将`server.crt`和`server.key`文件映射覆盖容器中原有的文件，然后修改`SERVER_NAME`为你的域名，格式例如`baidu.com`，`USE_HTTPS`改为true，如下面所示。

```yaml
hoj-frontend:
    image: registry.cn-shenzhen.aliyuncs.com/hcode/hoj_frontend
    container_name: hoj-frontend
    restart: always
    # 开启https，请提供证书
    volumes:
      - ./server.crt:/etc/nginx/etc/crt/server.crt
      - ./server.key:/etc/nginx/etc/crt/server.key
    # 修改前端logo
    # - ./logo.a0924d7d.png:/usr/share/nginx/html/assets/img/logo.a0924d7d.png
    # - ./backstage.8bce8c6e.png:/usr/share/nginx/html/assets/img/backstage.8bce8c6e.png
    environment:
      - SERVER_NAME=localhost  # 提供你的域名！！！！例如baidu.com
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

修改完成后，使用`docker-compose up -d`重启`hoj-frontend`容器即可。