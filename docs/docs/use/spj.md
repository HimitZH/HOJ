# 特殊判题

## 什么是特殊判题？

特殊判题（Special Judge）是指OJ将使用一个特定的程序来判断提交的程序的输出是不是正确的，而不是单纯地看提交的程序的输出是否和标准输出一模一样。

## 使用场景

一般使用Special Judge都是因为题目的答案不唯一，更具体一点说的话一般是两种情况：

- 题目最终要求输出一个解决方案，而且这个解决方案可能不唯一。
- 题目最终要求输出一个浮点数，而且会告诉只要答案和标准答案相差不超过某个较小的数就可以，比如0.01。这种情况保留3位小数、4位小数等等都是可以的，而且多保留几位小数也没什么坏处。

## 例子：

在创建题目的适合，选择开启特殊判题，编写特殊判题程序，然后编译通过便可。

> 后台对题目使用特殊判题时，请参考以下程序例子

```cpp
#include<iostream>
#include<cstdio>
#define AC 100
#define WA 101
#define ERROR 102
using namespace std;


int spj(int user_output, FILE *output);

void close_file(FILE *f){
    if(f != NULL){
        fclose(f);
    }
}

int main(int argc, char *args[]){
    FILE *output;
    int result;
    if(argc != 2){
        return ERROR;
    }
    int user_output;
    cin>>user_output;
    cout<<user_output<<endl;
    output = fopen(args[1], "r");
	
    result = spj(user_output, output);
    printf("result: %d\n", result);
    
    close_file(output);
    return result;
}

int spj(int user_output, FILE *output){
    /*
      parameter: 
        - output，标程输出文件的指针
        - user_output，用户输出数据
      return: 
        - 如果用户答案正确，返回AC
        - 如果用户答案错误返回WA
        - 如果主动捕获到自己的错误，如内存分配失败，返回ERROR
      */
      int std_out;
      while(fscanf(output, "%d", &std_out) != EOF){
          if(user_output+1 != std_out){
             cout<<user_output<<endl<<std_out;
              return WA;
          }
      }
      return AC;
}
```

