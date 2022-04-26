# 自定义题目难度

:::tip

由于题目的难度是由前端代码决定**显示文本与背景颜色**的，所以想要修改或增删难度需要自定义前端，那么首先得知道如何**自定义前端**，[请点击查看](/use/update-fe/)

:::

接着，找到`/hoj-vue/src/common/constants.js`的文件，修改里面的难度常量代码`PROBLEM_LEVEL`如下，修改完后，请自行build前端项目生成dist的静态文件夹，上传到服务器后，修改挂载，重启hoj-frontend容器即可，重启完后，浏览器可能有缓存，多刷新即可！！！

```javascript
export const PROBLEM_LEVEL={
  '0':{
    name:{
      'zh-CN':'简单', // 中文文本显示
      'en-US':'Easy', // 英文文本显示
    },
    color:'#19be6b'  // 背景颜色
  },
  '1':{
    name:{
      'zh-CN':'中等',
      'en-US':'Mid',
    },
    color:'#2d8cf0'
  },
  '2':{
    name:{
      'zh-CN':'困难',
      'en-US':'Hard',
    },
    color:'#ed3f14'
  }
}

```

:::warning

注意：每个OI题目的得分计算公式为：(总得分×0.1+难度×2)×(通过测试点数÷总测试点数)，所以上面代码中的数字会影响OI题目得分，请尽量合理使用正整数！！！

:::