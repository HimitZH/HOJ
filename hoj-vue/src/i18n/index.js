import Vue from 'vue'
import VueI18n from 'vue-i18n'
import elenUS from 'element-ui/lib/locale/lang/en'
import elzhCN from 'element-ui/lib/locale/lang/zh-CN'
import elzhTW from 'element-ui/lib/locale/lang/zh-TW'
import eljaJP from 'element-ui/lib/locale/lang/ja'
import elkoKR from 'element-ui/lib/locale/lang/ko'
import vxeEnUS from 'vxe-table/lib/locale/lang/en-US'
import vxeZhCN from 'vxe-table/lib/locale/lang/zh-CN'
import vxeZhTW from 'vxe-table/lib/locale/lang/zh-TW'
import vxeJaJP from 'vxe-table/lib/locale/lang/ja-JP'
import vxeKoKR from 'vxe-table/lib/locale/lang/en-US'
import storage from '@/common/storage'
Vue.use(VueI18n)

const languages = [
  {value: 'en-US', label: 'English', el: elenUS, vxe: {...vxeEnUS}},
  {value: 'zh-CN', label: '简体中文',  el: elzhCN, vxe: {...vxeZhCN}},
  {value: 'zh-TW', label: '正體中文',  el: elzhTW, vxe: {...vxeZhTW}},
  {value: 'ja-JP', label: '日本語',  el: eljaJP, vxe: {...vxeJaJP}},
  {value: 'ko-KR', label: '한국어',  el: elkoKR, vxe: {...vxeKoKR}},
]
const messages = {}

// combine admin and oj
for (let lang of languages) {
  let locale = lang.value
  let m = require(`./oj/${locale}`).m
  Object.assign(m, require(`./admin/${locale}`).m)
  let ui = Object.assign(lang.vxe, lang.el)
  messages[locale] = Object.assign({m: m}, ui);
}


// load language packages
export default new VueI18n({
  locale: storage.get('Web_Language') || 'zh-CN',
  messages: messages
})

function getLangLabelByValue(value){
  for (let lang of languages) {
    if(lang.value == value){
      return lang.label;
    }
  }
  return "unknown"
}

export {languages, getLangLabelByValue}
