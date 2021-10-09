# 题目管理

## 一、HOJ题目

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210530214011773.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70)

#### 1. 导出题目

点击选择需要的题目，便可以批量导出成一个zip压缩包，分别对应一个json格式的题目数据，一个对应名字的文件夹存放评测数据文件，具体的文件结构如下：

```
+-- problem_1000.json
+-- problem_1000
|   +-- 1.in
|   +-- 1.out
|   +-- ....
+-- problem_1001.json
+-- problem_1001
|   +-- 1.in
|   +-- 1.out
|   +-- ....
```

#### 2. 导入题目

选择需要导入的题目数据zip压缩包，注意**不要多一层文件夹进行压缩**，**请保证题目json文件的名字与其对应的存放评测数据的文件夹名字一致**，具体文件格式如下：

```
+-- problem_1000.json
+-- problem_1000
|   +-- 1.in
|   +-- 1.out
|   +-- ....
+-- problem_1001.json
+-- problem_1001
|   +-- 1.in
|   +-- 1.out
|   +-- ....
```

#### 3. 题目的json文件格式

请严格按照以下格式，才可以正常导入。

```json
{
    // 题目支持的语言如下，可增加与减少
    "languages": ["C", "C++", "Java", "Python3", "Python2",  "Golang", "C#"], 
    "samples": [
        {
            "output": "1.in", 
            "input": "1.out",
            //"score": 10  // 如果是io题目需要给测试点加得分
        },
        {
            "output": "2.in", 
            "input": "2.out",
            //"score": 10  // 如果是io题目需要给测试点加得分
        }
    ], 
    "tags": ["测试题","测试"], // 题目标签，一般不超过三个 
    "problem": {
        "auth": 1, // 1 公开赛
        "author": "admin", // 题目上传的作者，请使用用户名
        "isRemote": false, // 均为非VJ题目，不用修改
        "problemId": "HOJ-1010", // 题目的展示id
        "description": "", // 题目的描述，支持markdown语法
        "source": "", // 题目来源
        "title": "", // 题目标题
        "type": 0,  // 0为ACM题目，1为OI题目
        "timeLimit": 1000, // 时间限制 单位是ms
        "memoryLimit": 256, // 空间限制 单位是mb
        "input": "", // 题目的输入描述
        "output": "", // 题目的输出描述
        "difficulty": 0, // 题目难度，1为简单，2为中等，3为困难
        "examples": "", // 题目的题面样例，格式为<input>输入</input><output>输出</output><input>输入</input><output>输出</output>
        "ioScore": 100, // OI题目总得分，与测试点总分一致
        "codeShare": true, // 该题目是否允许用户共享其提交的代码 
        "hint": "", // 题目提示
        "isRemoveEndBlank": true, // 评测数据的输出是否自动去掉行末空格
        "openCaseResult": true,  // 是否允许用户看到各个评测点的结果
       	// "spjLanguage:"C" // 特殊判题的程序代码语言
        // "spjCode":"" // 特殊判题的代码
    }, 
     "isSpj": false, // 是否为特殊判题
    "codeTemplates": [
        {
            "code": "", // 模板代码
            "language": "C" // 模板代码语言
        }, 
        {
            "code": "", // 模板代码
            "language": "C++"// 模板代码语言
        }
    ]
}
```

## 二、导入QDUOJ或FPS格式的题目

1. 请严格按照青岛oj的后台导出的压缩文件来上传。
2. 请使用标准的FPS格式的题目数据文件(.xml)

![在这里插入图片描述](https://img-blog.csdnimg.cn/57c6518fb9fe426088c064d85dd110d3.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16)



## 三、导入其它OJ题目

导入HDU、Codeforces题目，只需提供该题目的题号便可一键导入

**管理员进入后台，点击题目列表**

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210523223454472.png#pic_center)

然后添加上方的添加按钮

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210523222914722.png)



**在弹出窗中选择OJ名字及题号，便可导入**



![在这里插入图片描述](https://img-blog.csdnimg.cn/20210523223042100.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzg1MzA5Nw==,size_16,color_FFFFFF,t_70)

**注意：**

- HDU的题号一般是 1000以上的数字
- Codeforces的题号是1000A、1000B、这种数字加大写英文字母的格式