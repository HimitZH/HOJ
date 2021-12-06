# 单体部署④——Nacos部署

## docker部署

```shell
docker run -d \
-e JVM_XMS=384m \
-e JVM_XMX=384m \
-e JVM_XMN=192m \
-e MODE=standalone \
-e SPRING_DATASOURCE_PLATFORM=mysql \
-e MYSQL_SERVICE_HOST=mysql_host \
-e MYSQL_SERVICE_PORT=mysql_port \
-e MYSQL_SERVICE_USER=root \
-e MYSQL_SERVICE_PASSWORD="mysql_root_password" \
-e MYSQL_SERVICE_DB_NAME=nacos \
--env NACOS_AUTH_ENABLE=true \
-p 8848:8848 \
--name nacos \
--restart=always \
nacos/nacos-server:1.4.2
```

## 常规部署

请自行百度下载修改配置