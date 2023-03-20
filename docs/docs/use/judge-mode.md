# 判题模式

### 一、普通判题

**普通模式是程序在线评测平台(OJ)通用的判题模式**，主要的实现逻辑步骤如下：

:::tip

1. 选手程序读取题目标准输入文件的数据

2. 判题机执行代码逻辑得到选手输出
3. 再将选手输出与题目标准输出文件的数据进行对比，最终得到判题结果

:::

### 二、特殊判题

#### 1. 什么是特殊判题？

特殊判题（Special Judge）是指OJ将使用一个特定的程序来判断提交的程序的输出是不是正确的，而不是单纯地看提交的程序的输出是否和标准输出一模一样。

#### 2. 使用场景

一般使用Special Judge都是因为题目的答案不唯一，更具体一点说的话一般是两种情况：

:::tip

- 题目最终要求输出一个解决方案，而且这个解决方案可能不唯一。
- 题目最终要求输出一个浮点数，而且会告诉只要答案和标准答案相差不超过某个较小的数就可以，比如0.01。这种情况保留3位小数、4位小数等等都是可以的，而且多保留几位小数也没什么坏处。

:::

#### 3. 支持

HOJ支持testlib.h头文件的直接使用  具体使用文档请看[https://oi-wiki.org/tools/testlib/](https://oi-wiki.org/tools/testlib/)

#### 4. 例题

在创建题目的适合，选择开启特殊判题，编写特殊判题程序，然后编译通过便可。

> 后台对题目使用特殊判题时，请参考以下程序例子 判断精度

- 使用testlib.h来进行特殊判题

```cpp
#include <iostream>
#include "testlib.h"

using namespace std;

int main(int argc, char *args[]){
    /**
    inf: 输入文件流
    ouf: 选手输出流
    ans: 标准答案流
    **/
    registerTestlibCmd(argc, args);
    double pans = ouf.readDouble();
    double jans = ans.readDouble();
    if (fabs(pans - jans)<0.01)
        quitf(_ok, "The answer is correct.");
    else
        quitf(_wa, "The answer is wrong: expected = %f, found = %f", jans, pans);
    // quitf(_pe, "The answer is presentation error."); // 格式错误
    // quitf(_fail, "The something wrong cause system error."); // 系统错误
}
```



- 读取文件进行特殊判题

```cpp
#include<iostream>
#include<cstdio>

#define PC 99  // 部分正确
#define AC 100 // 全部正确
#define PE 101 // 格式错误
#define WA 102 // 答案错误
#define ERROR 103 // 系统错误

using namespace std;

void close_file(FILE *f){
    if(f != NULL){
        fclose(f);
    }
}

int main(int argc, char *args[]){
    /**
    args[1]：标准输入文件路径 
    args[2]：选手输出文件路径
    args[3]：标准输出文件路径 
    **/
    FILE *std_input_file = fopen(args[1], "r");
    FILE *user_output_file = fopen(args[2], "r");
    FILE *std_output_file = fopen(args[3], "r");
    
    double std_out; // 标准输出
	fscanf(std_output_file, "%lf", &std_out);
    
    double user_output;// 用户输出 
    fscanf(user_output_file, "%lf", &user_output);
    
    // 关闭文件流
    close_file(std_input_file);
    close_file(user_output_file);
    close_file(std_output_file);
    
	if (fabs(user_output - std_out)<=1e-6)
		return AC;
	else 
     //  fprintf(stderr, "这里会输出错误提示");
     //  fprintf(stderr, "%s","这里会输出错误提示");
		return WA;
}
```

如果是OI题目，需要该测试点部分得分，则可参考如下

```cpp
#include<iostream>
#include<cstdio>

#define PC 99  // 部分正确
#define AC 100 // 全部正确
#define PE 101 // 格式错误
#define WA 102 // 答案错误
#define ERROR 103 // 系统错误

using namespace std;

void close_file(FILE *f){
    if(f != NULL){
        fclose(f);
    }
}

int main(int argc, char *args[]){
    /**
    args[1]：标准输入文件路径 
    args[2]：选手输出文件路径
    args[3]：标准输出文件路径 
    **/
    FILE *std_input_file = fopen(args[1], "r");
    FILE *user_output_file = fopen(args[2], "r");
    FILE *std_output_file = fopen(args[3], "r");
    
    double std_out; // 标准输出
	fscanf(std_output_file, "%lf", &std_out);
    
    double user_output;// 用户输出 
    fscanf(user_output_file, "%lf", &user_output);
    
    // 关闭文件流
    close_file(std_input_file);
    close_file(user_output_file);
    close_file(std_output_file);
  
    if(...){ // 符合部分得分
      fprintf(stdout, "%lf", 50.0); // 表示获得该测试点50.0%的分数
      fprintf(stderr, "这里会输出错误提示");
      return PC;
    }
}
```



### 三、交互判题

**交互题** 是需要选手程序与测评程序交互来完成任务的题目。一类常见的情形是，选手程序向测评程序发出询问，并得到其反馈。

交互方式主要有如两种：**STDIO 交互**和**Grader 交互**

:::tip

主要的交互逻辑：交互程序的标准输出通过交互通道写到选手程序标准输入，选手程序的标准输出通过交互通道写到交互程序的标准输入，两者需要刷新输出缓存

:::

:::warning

在 C/C++ 中，`fflush(stdout)` 和 `std::cout << std::flush` 可以实现这个操作（使用 `std::cout << std::endl` 换行时也会自动刷新缓冲区，但是 `std::cout << '\n'` 不会）

:::

#### 1. 标准交互题

**A+B问题**

*选手程序*

```cpp
#include <iostream>
#include <cstdio>
using namespace std;
int main(){
	int a,b;
	cin >> a >> b;
	cout << a + b;
	return 0;
}
```

*交互程序*（这里使用testlib来实现，但也可以自己读取文件实现）

```cpp
#include "testlib.h"
#include <iostream>

using namespace std;

int main(int argc, char* argv[])
{
    setName("Interactor A+B");
    registerInteraction(argc, argv);
    // 读取题目标准输入文件的数据
    int a = inf.readInt();
    int b = inf.readInt();
    // 往交互通道写数据，记得用endl刷新缓冲区
    cout << a << " " << b << endl;
    int ans;
    // 读取用户程序写入到交互通道的数据
    cin >> ans;
    if (a + b == ans){ // 判断结果
    	quitf(_ok, "correct");
    }else{
    	quitf(_wa,"incorrect");
    }
}
```

#### 2. 函数交互题

:::info

主要的交互逻辑：

1. 用户调用提供的库文件里面的方法执行答题逻辑，最后得出指定结果；
2. 交互测评程序根据选手调用交互库执行逻辑后得出的结果，来进行最终判断评测的结果。

:::

需要给选手的程序添加交互库，在后台的题目管理可以选择添加。

![函数交互题](/e6f17df56f26488c895944713a80e9ed.png)

**交互库**：提供写好的方法给选手调用

```cpp
#include <bits/stdc++.h>
using namespace std;

namespace interactive {
    static int n, m, cnt;
    static bool hasUsedGetN = false;
    static bool hasSubmitted = false;
    void RE() {
        puts("re");
        exit(0);
    }
    int getn() {
        if (hasUsedGetN) RE();
        cin >> n;
        hasUsedGetN = 1;
        m = rand() % n + 1;
        return n;
    }
    int query(int x) {
        if (!hasUsedGetN || hasSubmitted) RE();
        cnt++;
        if (cnt > 100000) RE();
        if (x == m)
            return (rand() % 10 <= 3);
        else
            return (rand() % 10 <= 4);
    }
    void submit(int x) {
        if (hasSubmitted) RE();
        if (x == m)
            puts("ok");
        else
            puts("wa");
        hasSubmitted = 1;
    }
} 

using interactive::getn;
using interactive::query;
using interactive::submit;
```

**用户程序**：调用库文件的方法进行答题

```cpp
#include <bits/stdc++.h>
#include "interactive.h"

int main()
{
    int n = getn();
    for(int i = 1; i <= n; i++)
    {
        for(int j = 0; j < 900; j++)
        {
            if(query(i)) arr[i]++;
        }
    }
    int min = 1000, ans = 0;
    for(int i = 1; i <= n; i++)
    {
        if(arr[i] < min)
        {
            min = arr[i];
            ans = i;
        }
    }
    submit(ans);
    return 0;
}
```

**交互测评程序**：根据选手调用交互库执行逻辑后得出的结果，来进行最终判断评测的结果

```cpp
#include "testlib.h"
#include <string>

int main(int argc, char* argv[]) {
    registerTestlibCmd(argc, argv);
    // 读取选手最终输出文件的数据来判断结果
    std::string s = ouf.readToken();
    if (s == "ok")
        quitf(_ok, "Correct");
    else
        quitf(_wa, "Wrong Answer");
}
```

