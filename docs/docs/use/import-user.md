## 导入用户

![在这里插入图片描述](https://img-blog.csdnimg.cn/338bf9db257844a1b32a4ff54c6528f6.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBASGltaXRfWkg=,size_20,color_FFFFFF,t_70,g_se,x_16)



要求如下:

1. 用户数据导入仅支持csv格式的用户数据。

2. 共七列数据：用户名和密码不能为空，邮箱、真实姓名、性别、昵称和学校可选填，否则该行数据可能导入失败。

3. 第一行不必写(“用户名”，“密码”，“邮箱”，"真实姓名"，“性别”，“昵称”，“学校”)这四个列名

4. 性别为男请使用“male”或“0”，女请使用“female”或“1”，不填默认为“secrecy”。

5. 请导入保存为UTF-8编码的文件，否则中文可能会乱码。

