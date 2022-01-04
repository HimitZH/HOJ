# 安全沙盒的调用

> Judger-SandBox使用的是开源项目[go-judge](https://github.com/criyle/go-judge)Linux版本的可执行文件，更多调用方式请自行浏览[go-judge](https://github.com/criyle/go-judge)

HOJ用的是java来调用此沙盒，请看[JudgeServer-judge](https://gitee.com/himitzh0730/hoj/tree/master/hoj-springboot/JudgeServer/src/main/java/top/hcode/hoj/judge)下的SandboxRun.java

启动[Judger-SandBox](https://gitee.com/himitzh0730/hoj/blob/master/judger/Judger-SandBox)，默认监听5050端口

#### 验证是否启动

访问：`http://localhost:5050/version`

#### 编译

1.1 请求的url为 

> `http://localhost:5050/run`

1.2 请求方式

> POST

1.3 请求参数

> 数据格式为json，内容如下

```shell
 {
    "cmd": [
        {
            "args": [
                "/usr/bin/g++", 
                "a.cc", 
                "-o", 
                "a"
            ], 
            "env": [
                "PATH=/usr/bin:/bin"
            ], 
            "files": [
                {
                    "content": ""
                }, 
                {
                    "name": "stdout", 
                    "max": 10240
                }, 
                {
                    "name": "stderr", 
                    "max": 10240
                }
            ], 
            "cpuLimit": 10000000000, 
            "memoryLimit": 104857600, 
            "procLimit": 50, 
            "copyIn": {
                "a.cc": {
                  "content": "#include <iostream>\nusing namespace std;\nint main() {\nint a, b;\ncin >> a >> b;\ncout << a + b << endl;\n}"
                }
            }, 
            "copyOut": [
                "stdout", 
                "stderr"
            ], 
            "copyOutCached": [
                "a.cc", 
                "a"
            ], 
            "copyOutDir": "1"
        }
    ]
}
```

1.4 返回的数据为json格式

```json
  [
         {
             "status": "Accepted",
             "exitStatus": 0,
             "time": 303225231,
             "memory": 32243712,
             "runTime": 524177700,
             "files": {
                 "stderr": "",
                 "stdout": ""
             },
             "fileIds": {
                 "a": "WDQL5TNLRRVB2KAP",
                 "a.cc": "NOHPGGDTYQUFRSLJ"
             }
         }
     ]
```

#### 运行与评测

2.1 请求的url为 

> `http://localhost:5050/run`

2.2 请求方式

> POST

2.3 请求参数

> 数据格式为json，内容如下

```json
{
    "cmd": [{
        "args": ["a"],
        "env": ["PATH=/usr/bin:/bin","LANG=en_US.UTF-8","LC_ALL=en_US.UTF-8","LANGUAGE=en_US:en"],
        "files": [{
            "src": "/judge/test_case/problem_1010/1.in"
        }, {
            "name": "stdout",
            "max": 10240
        }, {
            "name": "stderr",
            "max": 10240
        }],
        "cpuLimit": 10000000000,
        "realCpuLimit":30000000000,
        "stackLimit":134217728,
        "memoryLimit": 104811111,
        "procLimit": 50,
        "copyIn": {
            "a":{"fileId":"WDQL5TNLRRVB2KAP"}
        },
        "copyOut": ["stdout", "stderr"]
    }]
}
```

2.4 返回的数据为json格式

```json
[{
  "status": "Accepted",
  "exitStatus": 0,
  "time": 3171607,
  "memory": 475136,
  "runTime": 110396333,
  "files": {
    "stderr": "",
    "stdout": "23\n"
  }
}]
```

#### 交互判题

3.1 请求的url为 

> `http://localhost:5050/run`

3.2 请求方式

> POST

3.3 请求参数

> 数据格式为json，内容如下

```json
   {
"pipeMapping": [
    {
        "in": {
            "max": 16777216,
            "index": 0,
            "fd": 1
        },
        "out": {
            "index": 1,
            "fd": 0
        }
    }
],
"cmd": [
    {
        "stackLimit": 134217728,
        "cpuLimit": 3000000000,
        "realCpuLimit": 9000000000,
        "clockLimit": 64,
        "env": [
            "LANG=en_US.UTF-8",
            "LANGUAGE=en_US:en",
            "LC_ALL=en_US.UTF-8",
            "PYTHONIOENCODING=utf-8"
        ],
        "copyOut": [
            "stderr"
        ],
        "args": [
            "/usr/bin/python3",
            "main"
        ],
        "files": [
            {
                "src": "/judge/test_case/problem_1002/5.in"
            },
            null,
            {
                "max": 16777216,
                "name": "stderr"
            }
        ],
        "memoryLimit": 536870912,
        "copyIn": {
            "main": {
                "fileId": "CGTRDEMKW5VAYN6O"
            }
        }
    },
    {
        "stackLimit": 134217728,
        "cpuLimit": 8000000000,
        "clockLimit": 24000000000,
        "env": [
            "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
            "LANG=en_US.UTF-8",
            "LANGUAGE=en_US:en",
            "LC_ALL=en_US.UTF-8"
        ],
        "copyOut": [
            "stdout",
            "stderr"
        ],
        "args": [
            "/w/spj",
            "/w/tmp"
        ],
        "files": [
            null,
            {
                "max": 16777216,
                "name": "stdout"
            },
            {
                "max": 16777216,
                "name": "stderr"
            }
        ],
        "memoryLimit": 536870912,
        "copyIn": {
            "spj": {
                "src": "/judge/spj/1002/spj"
            },
            "tmp": {
                "src": "/judge/test_case/problem_1002/5.out"
            }
        },
        "procLimit": 64
    }
]
```

3.4 返回的数据为json格式

```json
[
    {
        "status": "Accepted",
        "exitStatus": 0,
        "time": 1545123,
        "memory": 253952,
        "runTime": 4148800,
        "files": {
            "stderr": ""
        },
        "fileIds": {}
    },
    {
        "status": "Accepted",
        "exitStatus": 0,
        "time": 1501463,
        "memory": 253952,
        "runTime": 5897700,
        "files": {
            "stderr": "",
            "stdout": ""
        },
        "fileIds": {}
    }
]
```
