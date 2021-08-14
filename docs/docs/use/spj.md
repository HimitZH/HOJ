# 特殊判题

## 什么是特殊判题？

特殊判题（Special Judge）是指OJ将使用一个特定的程序来判断提交的程序的输出是不是正确的，而不是单纯地看提交的程序的输出是否和标准输出一模一样。

## 使用场景

一般使用Special Judge都是因为题目的答案不唯一，更具体一点说的话一般是两种情况：

- 题目最终要求输出一个解决方案，而且这个解决方案可能不唯一。
- 题目最终要求输出一个浮点数，而且会告诉只要答案和标准答案相差不超过某个较小的数就可以，比如0.01。这种情况保留3位小数、4位小数等等都是可以的，而且多保留几位小数也没什么坏处。

## 支持

HOJ支持testlib.h头文件的直接使用  具体使用文档请看[https://oi-wiki.org/tools/testlib/](https://oi-wiki.org/tools/testlib/)

## 例子：

在创建题目的适合，选择开启特殊判题，编写特殊判题程序，然后编译通过便可。

> 后台对题目使用特殊判题时，请参考以下程序例子 判断精度

- 使用Testlib特殊判题

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
    registerTestlibCmd(argc, argv);
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

#define AC 100
#define PE 101
#define WA 102
#define ERROR 103

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
	fscanf(user_output_file, "%lf", &std_out);
    
    double user_output;// 用户输出 
    fscanf(std_output_file, "%lf", &user_output);
    
    // 关闭文件流
    close_file(std_input_file);
    close_file(user_output_file);
    close_file(std_output_file);
    
	if (fabs(user_output - std_out)<=1e-6)
		return AC;
	else 
		return WA;
}
```

