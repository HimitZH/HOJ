import Vue from 'vue'
import storage from '@/common/storage'
import { STORAGE_KEY } from '@/common/constants'

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
    Vue.prototype.$http.get(url, {responseType: 'blob'}).then(resp => {
      let headers = resp.headers
      if (headers['content-type'].indexOf('json') !== -1) {
        let fr = new window.FileReader()
        if (resp.data.error) {
          Vue.prototype.$error(resp.data.error)
        } else {
          Vue.prototype.$error('Invalid file format')
        }
        fr.onload = (event) => {
          let data = JSON.parse(event.target.result)
          if (data.error) {
            Vue.prototype.$error(data.data)
          } else {
            Vue.prototype.$error('Invalid file format')
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
      link.click()
      link.remove()
      resolve()
    }).catch(() => {})
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
      },
      {
        content_type: "text/x-c++src",
        description: "G++ 5.4",
        name: "C++",
      },  
      { content_type: "text/x-java",
        description: "OpenJDK 1.8",
        name: "Java",
      },
      {
        content_type: "text/x-python",
        description: "Python 3.7",
        name: "Python3",
      }
      ];
      storage.set(STORAGE_KEY.languages,langs);
      resolve(langs);
    }
  })
}



export default {
  submissionMemoryFormat: submissionMemoryFormat,
  submissionTimeFormat: submissionTimeFormat,
  getACRate: getACRate,
  filterEmptyValue: filterEmptyValue,
  breakLongWords: breakLongWords,
  downloadFile: downloadFile,
  getLanguages:getLanguages
}
