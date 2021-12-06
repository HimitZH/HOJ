# 开启HTTPS

- 单机部署：

  提供server.crt和server.key证书与密钥文件放置`/standAlone`目录下，与`docker-compose.yml`和`.env`文件放置同一位置，然后修改`docker-compose.yml`中的hoj-frontend的配置

- 分布式部署：

  提供server.crt和server.key证书与密钥文件放置`/distributed/main`目录下，与`docker-compose.yml`和`.env`文件放置同一位置，然后修改`docker-compose.yml`中的hoj-frontend的配置

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

