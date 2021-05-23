# 特殊判题

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

