import Vue from 'vue'
import storage from '@/common/storage'
import { STORAGE_KEY } from '@/common/constants'
import myMessage from '@/common/message'
import { isArguments } from 'xe-utils/methods'

function submissionMemoryFormat (memory) {
  if (memory === undefined) return '--'
  // 1048576 = 1024 * 1024
  let t = parseInt(memory) / 1048576
  return String(t.toFixed(0)) + 'MB'
}

function submissionTimeFormat (time) {
  if (time === undefined) return '--'
  return time + 'ms'
}

function getACRate (acCount, totalCount) {
  let rate = totalCount === 0 ? 0.00 : (acCount / totalCount * 100).toFixed(2)
  return String(rate) + '%'
}

// 去掉值为空的项，返回object
function filterEmptyValue (object) {
  let query = {}
  Object.keys(object).forEach(key => {
    if (object[key] || object[key] === 0 || object[key] === false) {
      query[key] = object[key]
    }
  })
  return query
}

// 按指定字符数截断添加换行，非英文字符按指定字符的半数截断
function breakLongWords (value, length = 16) {
  let re
  if (escape(value).indexOf('%u') === -1) {
    // 没有中文
    re = new RegExp('(.{' + length + '})', 'g')
  } else {
    // 中文字符
    re = new RegExp('(.{' + (parseInt(length / 2) + 1) + '})', 'g')
  }
  return value.replace(re, '$1\n')
}

function downloadFile (url) {
  return new Promise((resolve, reject) => {
    Vue.prototype.$axios.get(url, {responseType: 'blob'}).then(resp => {
      let headers = resp.headers
      if (headers['content-type'].indexOf('json') !== -1) {
        let fr = new window.FileReader()
        if (resp.data.error) {
          myMessage.error(resp.data.error)
        } else {
          myMessage.error('Invalid file format')
        }
        fr.onload = (event) => {
          let data = JSON.parse(event.target.result)
          if (data.error) {
            myMessage.error(data.data)
          } else {
            myMessage.error('Invalid file format')
          }
        }
        let b = new window.Blob([resp.data], {type: 'application/json'})
        fr.readAsText(b)
        return
      }
      let link = document.createElement('a')
      link.href = window.URL.createObjectURL(new window.Blob([resp.data], {type: headers['content-type']}))
      link.download = (headers['content-disposition'] || '').split('filename=')[1]
      document.body.appendChild(link)
      console.log(link)
      link.click()
      link.remove()
      resolve()
    }).catch((error) => {
      reject(error)
    })
  })
}

function getLanguages () {
  return new Promise((resolve, reject) => {
    let languages = storage.get(STORAGE_KEY.languages)
    if (languages) {
      resolve(languages)
    }else{
      let langs = [
      {
        content_type: "text/x-csrc",
        description: "GCC 5.4",
        name: "C",
        compile_command: "/usr/bin/gcc -DONLINE_JUDGE -O2 -w -fmax-errors=3 -std=c11 {src_path} -lm -o {exe_path}",
        template: "#include <stdio.h>\nint add(int a, int b) {\n    return a+b;\n}\nint main() {\n    printf(\"%d\", add(1, 2));\n    return 0;\n}"
      },
      {
        content_type: "text/x-c++src",
        description: "G++ 5.4",
        name: "C++",
        compile_command: "/usr/bin/g++ -DONLINE_JUDGE -O2 -w -fmax-errors=3 -std=c++14 {src_path} -lm -o {exe_path}",
        template: "#include <iostream>\nint add(int a, int b) {\n    return a+b;\n}\nint main() {\n    std::cout << add(1, 2);\n    return 0;\n}",
      },  
      { content_type: "text/x-java",
        description: "OpenJDK 1.8",
        name: "Java",
        compile_command: "/usr/bin/javac {src_path} -d {exe_dir} -encoding UTF8",
        template: "import java.util.Scanner;\npublic class Main{\n    public static void main(String[] args){\n        Scanner in=new Scanner(System.in);\n        int a=in.nextInt();\n        int b=in.nextInt();\n        System.out.println((a+b));\n    }\n}"
      },
      {
        content_type: "text/x-python",
        description: "Python 3.7",
        name: "Python3",
        template: "a, b = map(int, input().split())\nprint(a + b)",
        compile_command: "/usr/bin/python3 -m py_compile {src_path}"
      }
      ];
      storage.set(STORAGE_KEY.languages,langs);
      resolve(langs);
    }
  })
}

function stringToExamples(value){
  let reg = "<input>(.+?)</input><output>(.+?)</output>";
  let re = RegExp(reg,"g");
  let objList = []
  let tmp;
  while(tmp=re.exec(value)){
    objList.push({input:tmp[1],output:tmp[2]})
  }
  return objList
}

function examplesToString(objList){
  let result=""
  for(let obj of objList){
    result+= "<input>"+obj.input+"</input><output>"+obj.output+"</output>"
  }
  return result
}



export default {
  submissionMemoryFormat: submissionMemoryFormat,
  submissionTimeFormat: submissionTimeFormat,
  getACRate: getACRate,
  filterEmptyValue: filterEmptyValue,
  breakLongWords: breakLongWords,
  downloadFile: downloadFile,
  getLanguages:getLanguages,
  stringToExamples:stringToExamples,
  examplesToString:examplesToString
}
