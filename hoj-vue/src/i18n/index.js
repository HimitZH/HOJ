import Vue from 'vue'
import store from "@/store"
import VueI18n from 'vue-i18n'
import elenUS from 'element-ui/lib/locale/lang/en'
import elzhCN from 'element-ui/lib/locale/lang/zh-CN'

Vue.use(VueI18n)

const languages = [
  {value: 'en-US', label: 'English', el: elenUS},
  {value: 'zh-CN', label: '简体中文',  el: elzhCN},
]
const messages = {}

// combine admin and oj
for (let lang of languages) {
  let locale = lang.value
  let m = require(`./oj/${locale}`).m
  // Object.assign(m, require(`./admin/${locale}`).m)
  messages[locale] = Object.assign({m: m}, lang.el);
}


// load language packages
export default new VueI18n({
  locale: store.getters.webLanguage,
  messages: messages
})

export {languages}
