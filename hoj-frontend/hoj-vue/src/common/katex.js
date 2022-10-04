import 'katex'
import renderMathInElement from 'katex/contrib/auto-render/auto-render'
import 'katex/dist/katex.min.css'

function _ () {
}

const defaultOptions = {
  errorCallback: _,
  throwOnError: false,
  delimiters: [
    {left: '$', right: '$', display: false},
    {left: '$$', right: '$$', display: true},
    {left: '\\[', right: '\\]', display: true},
    {left: '\\(', right: '\\)', display: false}
  ],
  ignoredTags:["script", "noscript", "style", "textarea", "code", "option",],
}

function render (el, binding) {
  let options = {}
  if (binding.value) {
    options = binding.value.options || {}
  }
  Object.assign(options, defaultOptions)
  renderMathInElement(el, options)
}

export default {
  install: function (Vue, options) {
    Vue.directive('katex', {
      bind: render,
      componentUpdated: render
    })
  }
}
