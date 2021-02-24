##   1. Compile

#### Json Request Body

```json

        {
            "cmd": [{
                "args": ["/usr/bin/g++", "a.cc", "-o", "a"],
                "env": ["PATH=/usr/bin:/bin"],
                "files": [{
                    "content": ""
                }, {
                    "name": "stdout",
                    "max": 10240
                }, {
                    "name": "stderr",
                    "max": 10240
                }],
                "cpuLimit": 10000000000,
                "memoryLimit": 104857600,
                "procLimit": 50,
                "copyIn": {
                    "a.cc": {
                        "content": "#include <iostream>\nusing namespace std;\nint main() {\nint a, b;\ncin >> a >> b;\ncout << a + b << endl;\n}"
                    }
                },
                "copyOut": ["stdout", "stderr"],
                "copyOutCached": ["a.cc", "a"],
                "copyOutDir": "1"
            }]
        }

```

#### Json Response Data

```
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
                "a": "5LWIZAA45JHX4Y4Z",
                "a.cc": "NOHPGGDTYQUFRSLJ"
            }
        }
    ]
```

## 2. Test Case

#### Json Request Body

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

#### Json Response Data

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

## 3. SPJ Test Case

#### Json Request Body

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

#### Json Response Data

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