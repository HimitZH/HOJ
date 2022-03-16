# 单体部署③——Redis部署

## docker部署

```shell
docker run -d --name redis -p 6379:6379 \
-v $PWD/hoj/data/redis/data:/data \
--restart="always" \
redis \
--requirepass "redis_password" 
```

## 常规部署

请自行百度。。