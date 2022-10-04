import hljs from 'highlight.js'
import 'highlight.js/styles/atom-one-light.css'

export default {
  install (Vue, options) {
    Vue.directive('highlight', {
      deep: true,
      bind: function (el, binding) {
        Array.from(el.querySelectorAll('pre code')).forEach((target) => {
          if (binding.value) {
            target.textContent = binding.value
          }
          hljs.highlightBlock(target)
        })
      },
      componentUpdated: function (el, binding) {
        Array.from(el.querySelectorAll('pre code')).forEach((target) => {
          if (binding.value) {
            target.textContent = binding.value
          }
          hljs.highlightBlock(target)
        })
      }
    })
  }
}
