
- JDK 21）
- Maven 3.8+
- Node.js 14+ 与 npm

## 准备环境变量（.env）
在 `hoj-springboot/` 目录下准备 `.env`（该文件不提交到Github），最少包含：

```dotenv
# Redis
REDIS_HOST=100.97.103.105
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password

# MySQL（注意端口通常是 3306）
MYSQL_HOST=100.97.103.105
MYSQL_PORT=3306
MYSQL_PUBLIC_HOST=100.97.103.105
MYSQL_PUBLIC_PORT=3306
MYSQL_USERNAME=root
MYSQL_PASSWORD=your_mysql_password
MYSQL_DB=hoj

# 等价/兼容映射（可选）
HOJ_DB_USERNAME=${MYSQL_USERNAME}
HOJ_DB_PASSWORD=${MYSQL_PASSWORD}
HOJ_DB_PUBLIC_HOST=${MYSQL_PUBLIC_HOST}
HOJ_DB_PUBLIC_PORT=${MYSQL_PUBLIC_PORT}
HOJ_DB_NAME=${MYSQL_DB}

# Nacos（本地一般禁用）
NACOS_ENABLED=false
# 如需启用：NACOS_HOST=...  NACOS_PORT=...  NACOS_USERNAME=...  NACOS_PASSWORD=...

# JWT/判题配置（可选，但推荐设置）
HOJ_JWT_SECRET=change_me_secure_secret
HOJ_JWT_EXPIRE=86400
HOJ_JWT_CHECK_REFRESH_EXPIRE=43200
HOJ_JUDGE_TOKEN=change_me_judge_token
```

## 模块运行

### 后端 DataBackup（端口 6688）
在 `hoj-springboot/DataBackup` 目录：
```bash
set -a && source .env && set +a && export NACOS_ENABLED=false && mvn -s ../../.mvn/settings.xml spring-boot:run -Dspring-boot.run.profiles=dev
```

### 判题服务 JudgeServer
在 `hoj-springboot/JudgeServer` 目录：
```bash
set -a && source ../.env && set +a
export NACOS_ENABLED=false
mvn -s ../../.mvn/settings.xml spring-boot:run -Dspring-boot.run.profiles=dev
```

## 日志位置
- DataBackup：`hoj-springboot/DataBackup/hoj_backend/application.log`
- JudgeServer：`hoj-springboot/JudgeServer/hoj_backend/application.log`（若存在）
