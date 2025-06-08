import { createI18n } from 'vue-i18n'

const i18n = createI18n({
  locale: 'en', // set locale
  fallbackLocale: 'en', // set fallback locale
  messages: { //_TDO: load messages from files
    en: { message: 'hello' },
    cn: { message: '你好' }
  }
})

export default i18n
