# 自定义前端


## 一、完全自定义前端

直接下载 [hoj-vue](https://gitee.com/himitzh0730/hoj/tree/master/hoj-vue)

修改后，使用`npm run build`，生成一个dist文件夹，结构如下：

```
dist
├── index.html
├── favicon.ico
└── assets
    ├── css
    │   ├── ....
    ├── fonts
    │   ├── ....
    ├── img
    │   ├── ....
    ├── js
    │   ├── ....

....
....
```

将 `dist` 文件夹复制到服务器上某个目录下，比如 `/hoj/www/html/dist`，然后修改 `docker-compose.yml`，在 `hoj-frontend` 模块中的 `volumes` 中增加一行 `- /hoj/www/html/dist:/usr/share/nginx/html` （冒号前面的请修改为实际的路径），然后 `docker-compose up -d` 即可。


## 二、仅修改图标
:::tip
如果仅仅只是修改前台logo、管理后台logo和favicon.ico，那么操作如下：
:::

修改 `docker-compose.yml`，在 `hoj-frontend` 模块中的 `volumes` 中添加如下：

注意：冒号前面的是你主机实际存储图片文件的路径，主要是确定是否需要修改这个；冒号后面的是映射的docker容器内的文件路径，**不要修改**！！！

例如：`/usr/share/nginx/html/assets/img/logo.a0924d7d.png`这个不要修改，主要修改`./logo.png`，确定图片文件的路径即可！

```yaml
- ./logo.png:/usr/share/nginx/html/assets/img/logo.a0924d7d.png
- ./backstage.png:/usr/share/nginx/html/assets/img/backstage.8bce8c6e.png
- ./favicon.ico:/usr/share/nginx/html/favicon.ico
```

`logo.png`为前台导航栏左边的logo，`backstage.png`为后台侧边导航栏上方的logo，`favicon.ico`为小图标

**图片文件需放在与docker-compose.yml同个目录下，或者提供绝对路径也可。**

修改保存完成，使用`docker-compose up -d`重启`hoj-frontend`即可，前端浏览器有缓存，可以刷新浏览器或者换别的浏览器进行查看！

