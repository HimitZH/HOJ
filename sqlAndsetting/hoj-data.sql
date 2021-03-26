/*
SQLyog Professional v12.09 (64 bit)
MySQL - 5.7.29-0ubuntu0.18.04.1 : Database - hoj
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`hoj` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `hoj`;

/*Data for the table `auth` */

insert  into `auth`(`id`,`name`,`permission`,`status`,`gmt_create`,`gmt_modified`) values (4,'superadmin','all',0,'2020-10-25 00:17:17','2020-10-25 00:17:17'),(5,'admin','all',0,'2020-10-25 00:17:22','2020-10-25 00:17:22'),(6,'common','select,update',0,'2020-10-25 00:17:33','2020-10-25 00:17:33'),(7,NULL,NULL,0,'2020-10-25 00:17:49','2020-10-25 00:17:49');

/*Data for the table `language` */

insert  into `language`(`id`,`content_type`,`description`,`name`,`compile_command`,`template`,`is_spj`,`oj`,`gmt_create`,`gmt_modified`) values (1,'text/x-csrc','GCC 5.4','C','/usr/bin/gcc -DONLINE_JUDGE -O2 -w -fmax-errors=3 -std=c11 {src_path} -lm -o {exe_path}','#include <stdio.h>\r\nint add(int a, int b) {\r\n    return a+b;\r\n}\r\nint main() {\r\n    printf(\"%d\", add(1, 2));    \r\n    return 0;\r\n}',1,'ME','2020-12-12 23:11:44','2021-02-18 20:05:19'),(2,'text/x-c++src','G++ 5.4','C++','/usr/bin/g++ -DONLINE_JUDGE -O2 -w -fmax-errors=3 -std=c++14 {src_path} -lm -o {exe_path}','#include <iostream>\r\nint add(int a, int b) {\r\n    return a+b;\r\n}\r\nint main() {\r\n    std::cout << add(1, 2);\r\n    return 0;\r\n}',1,'ME','2020-12-12 23:12:44','2021-02-18 20:05:20'),(3,'text/x-java','OpenJDK 1.8','Java','/usr/bin/javac {src_path} -d {exe_dir} -encoding UTF8','import java.util.Scanner;\r\npublic class Main{\r\n    public static void main(String[] args){\r\n        Scanner in=new Scanner(System.in);\r\n        int a=in.nextInt();\r\n        int b=in.nextInt();\r\n        System.out.println((a+b));\r\n    }\r\n}',0,'ME','2020-12-12 23:12:51','2021-02-18 20:05:21'),(4,'text/x-python','Python 3.6','Python3','/usr/bin/python3 -m py_compile {src_path}','a, b = map(int, input().split())\r\nprint(a + b)',0,'ME','2020-12-12 23:14:23','2021-02-18 20:05:22'),(5,'text/x-python','Python 2.7','Python2','/usr/bin/python -m py_compile {src_path}','a, b = map(int, raw_input().split())\r\nprint(a+b)',0,'ME','2021-01-26 11:11:44','2021-02-18 20:05:24'),(6,'text/x-csrc','GCC','GCC',NULL,NULL,0,'HDU','2021-02-18 21:32:34','2021-02-18 23:42:56'),(7,'text/x-c++src','G++','G++',NULL,NULL,0,'HDU','2021-02-18 21:32:58','2021-02-18 21:32:58'),(8,'text/x-c++src','C++','C++',NULL,NULL,0,'HDU','2021-02-18 21:33:11','2021-02-18 21:33:26'),(9,'text/x-csrc','C','C',NULL,NULL,0,'HDU','2021-02-18 21:33:41','2021-02-18 21:34:11'),(10,'text/x-pascal','Pascal','Pascal',NULL,NULL,0,'HDU','2021-02-18 21:34:33','2021-02-19 00:16:35'),(12,'text/x-csrc','GNU GCC C11 5.1.0','GNU GCC C11 5.1.0',NULL,NULL,0,'CF','2021-03-03 19:46:24','2021-03-03 19:46:24'),(13,'text/x-c++src','Clang++17 Diagnostics','Clang++17 Diagnostics',NULL,NULL,0,'CF','2021-03-03 19:46:24','2021-03-03 19:46:24'),(14,'text/x-c++src','GNU G++11 5.1.0','GNU G++11 5.1.0',NULL,NULL,0,'CF','2021-03-03 19:46:24','2021-03-03 19:46:24'),(15,'text/x-c++src','GNU G++14 6.4.0','GNU G++14 6.4.0',NULL,NULL,0,'CF','2021-03-03 19:46:24','2021-03-03 19:46:24'),(16,'text/x-c++src','GNU G++17 7.3.0','GNU G++17 7.3.0',NULL,NULL,0,'CF','2021-03-03 19:46:24','2021-03-03 19:46:24'),(17,'text/x-c++src','Microsoft Visual C++ 2010','Microsoft Visual C++ 2010',NULL,NULL,0,'CF','2021-03-03 19:46:24','2021-03-03 19:46:24'),(18,'text/x-c++src','Microsoft Visual C++ 2017','Microsoft Visual C++ 2017',NULL,NULL,0,'CF','2021-03-03 19:46:24','2021-03-03 19:46:24'),(19,'text/x-csharp','C# Mono 6.8','C# Mono 6.8',NULL,NULL,0,'CF','2021-03-03 19:46:24','2021-03-25 21:11:24'),(20,'text/x-d','D DMD32 v2.091.0','D DMD32 v2.091.0',NULL,NULL,0,'CF','2021-03-03 19:46:24','2021-03-25 21:11:15'),(21,'text/x-go','Go 1.15.6','Go 1.15.6',NULL,NULL,0,'CF','2021-03-03 19:46:24','2021-03-25 21:11:07'),(22,'text/x-haskell','Haskell GHC 8.10.1','Haskell GHC 8.10.1',NULL,NULL,0,'CF','2021-03-03 19:46:24','2021-03-25 21:10:58'),(23,'text/x-java','Java 1.8.0_241','Java 1.8.0_241',NULL,NULL,0,'CF','2021-03-03 19:46:24','2021-03-25 21:10:52'),(24,'text/x-java','Kotlin 1.4.0','Kotlin 1.4.0',NULL,NULL,0,'CF','2021-03-03 19:46:24','2021-03-25 21:10:45'),(25,'text/x-ocaml','OCaml 4.02.1','OCaml 4.02.1',NULL,NULL,0,'CF','2021-03-03 19:46:24','2021-03-03 19:46:24'),(26,'text/x-pascal','Delphi 7','Delphi 7',NULL,NULL,0,'CF','2021-03-03 19:46:24','2021-03-03 19:46:24'),(27,'text/x-pascal','Free Pascal 3.0.2','Free Pascal 3.0.2',NULL,NULL,0,'CF','2021-03-03 19:46:24','2021-03-03 19:46:24'),(28,'text/x-pascal','PascalABC.NET 3.4.2','PascalABC.NET 3.4.2',NULL,NULL,0,'CF','2021-03-03 19:46:24','2021-03-03 19:46:24'),(29,'text/x-perl','Perl 5.20.1','Perl 5.20.1',NULL,NULL,0,'CF','2021-03-03 19:46:24','2021-03-03 19:46:24'),(30,'text/x-php','PHP 7.2.13','PHP 7.2.13',NULL,NULL,0,'CF','2021-03-03 19:46:24','2021-03-03 19:46:24'),(31,'text/x-python','Python 2.7.18','Python 2.7.18',NULL,NULL,0,'CF','2021-03-03 19:46:24','2021-03-25 21:10:11'),(32,'text/x-python','Python 3.9.1','Python 3.9.1',NULL,NULL,0,'CF','2021-03-03 19:46:24','2021-03-25 21:10:09'),(33,'text/x-python','PyPy 2.7 (7.3.0)','PyPy 2.7 (7.3.0)',NULL,NULL,0,'CF','2021-03-03 19:46:24','2021-03-25 21:10:08'),(34,'text/x-python','PyPy 3.7 (7.3.0)','PyPy 3.7 (7.3.0)',NULL,NULL,0,'CF','2021-03-03 19:46:24','2021-03-25 21:10:06'),(35,'text/x-ruby','Ruby 3.0.0','Ruby 3.0.0',NULL,NULL,0,'CF','2021-03-03 19:46:24','2021-03-25 21:10:04'),(36,'text/x-rustsrc','Rust 1.49.0','Rust 1.49.0',NULL,NULL,0,'CF','2021-03-03 19:46:24','2021-03-25 21:10:02'),(37,'text/x-scala','Scala 2.12.8','Scala 2.12.8',NULL,NULL,0,'CF','2021-03-03 19:46:24','2021-03-03 19:46:24'),(38,'text/javascript','JavaScript V8 4.8.0','JavaScript V8 4.8.0',NULL,NULL,0,'CF','2021-03-03 19:46:24','2021-03-03 19:46:24'),(39,'text/javascript','Node.js 12.6.3','Node.js 12.6.3',NULL,NULL,0,'CF','2021-03-03 19:46:24','2021-03-25 21:09:56'),(40,'text/x-csharp','C# 8, .NET Core 3.1','C# 8, .NET Core 3.1',NULL,NULL,0,'CF','2021-03-25 21:17:39','2021-03-25 21:17:50'),(41,'text/x-java','Java 11.0.6','Java 11.0.6',NULL,NULL,0,'CF','2021-03-25 21:20:03','2021-03-25 21:20:08');

/*Data for the table `role` */

insert  into `role`(`id`,`role`,`description`,`status`,`gmt_create`,`gmt_modified`) values (00000000000000001000,'root','超级管理员',0,'2020-10-25 00:16:30','2020-10-25 00:16:30'),(00000000000000001001,'admin','管理员',0,'2020-10-25 00:16:41','2020-10-25 00:16:41'),(00000000000000001002,'user','用户',0,'2020-10-25 00:16:52','2020-10-25 00:16:52');

/*Data for the table `role_auth` */

insert  into `role_auth`(`id`,`auth_id`,`role_id`,`gmt_create`,`gmt_modified`) values (1,4,1000,'2020-10-25 00:18:17','2020-10-25 00:18:17'),(2,5,1001,'2020-10-25 00:18:38','2020-10-25 00:18:38'),(3,6,1002,'2020-10-25 00:18:48','2020-10-25 00:18:48');

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
