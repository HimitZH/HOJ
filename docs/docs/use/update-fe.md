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

```yaml
- ./logo.png:/usr/share/nginx/html/assets/img/logo.a0924d7d.png
- ./backstage.png:/usr/share/nginx/html/assets/img/backstage.8bce8c6e.png
- ./favicon.ico:/usr/share/nginx/html/favicon.ico
```

`logo.png`为前台导航栏左边的logo，`backstage.png`为后台侧边导航栏上方的logo，`favicon.ico`为小图标

**图片文件需放在与docker-compose.yml同个目录下，或者提供绝对路径也可。**

