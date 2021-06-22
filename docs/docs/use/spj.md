# 特殊判题

## 什么是特殊判题？

特殊判题（Special Judge）是指OJ将使用一个特定的程序来判断提交的程序的输出是不是正确的，而不是单纯地看提交的程序的输出是否和标准输出一模一样。

## 使用场景

一般使用Special Judge都是因为题目的答案不唯一，更具体一点说的话一般是两种情况：

- 题目最终要求输出一个解决方案，而且这个解决方案可能不唯一。
- 题目最终要求输出一个浮点数，而且会告诉只要答案和标准答案相差不超过某个较小的数就可以，比如0.01。这种情况保留3位小数、4位小数等等都是可以的，而且多保留几位小数也没什么坏处。

## 例子：

在创建题目的适合，选择开启特殊判题，编写特殊判题程序，然后编译通过便可。

**注意：用户的输出数据以stdin流输入道spj程序里面，也就是可以直接用scanf或cin等获取用户的输出数据，具体形式跟用户程序的输入数据获取一样。**

> 后台对题目使用特殊判题时，请参考以下程序例子 判断精度

```cpp
#include<iostream>
#include<cstdio>
#define AC 100
#define WA 101
#define ERROR 102
using namespace std;


void close_file(FILE *f){
    if(f != NULL){
        fclose(f);
    }
}

int main(int argc, char *args[]){
    /**
    input:输入
    output:样例的输出
    user_output:用户的输出
    **/
    FILE *input = fopen(args[1], "r");
    FILE *output = fopen(args[2], "r");
    
    double std_out;
	fscanf(output, "%lf", &std_out);
	close_file(output);
    close_file(input);
    
    double user_output;//读入用户输出 
    cin>>user_output;
	if (fabs(user_output - std_out)<=1e-6)
		return AC;
	else 
		return WA;
}
```

