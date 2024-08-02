# hoj-vue-pc

## 工具

node: v16 (太高或者太低都不好使)
使用nvm管理node版本

### install nvm
```
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.3/install.sh | bash
```

### install node version 16
```
nvm install 16
```

### install yarn
```
curl -sS https://dl.yarnpkg.com/debian/pubkey.gpg | sudo apt-key add -
echo "deb http://dl.yarnpkg.com/debian/ stable main" | sudo tee /etc/apt/sources.list.d/yarn.list

sudo apt update
sudo apt install yarn
```

## Development Environment Requirements

We recommend using Node.js version 14.21.3.

If you wish to not disrupt your existing development environment, you can download precompiled binaries of Node.js and temporarily modify your environment variables to use this version.

[Download Precompiled Binaries](https://nodejs.org/en/download/prebuilt-binaries)

## Project setup
```
yarn
```

### Compiles and hot-reloads for development
```
yarn serve
```

### Compiles and minifies for production
```
yarn build
```

### Customize configuration
See [Configuration Reference](https://cli.vuejs.org/config/).
