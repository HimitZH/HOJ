
import $ from 'jquery'
import myMessage from '@/common/message';
export const addCodeBtn = _ => {
	//markdown代码存放在pre code 标签对中
  $('pre code').each(function () {
    let lines = $(this).text().split('\n').length - 1
    //添加有序列表
    let $numbering = $('<ol/>').addClass('pre-numbering')
    //添加复制按钮，此处使用的是element-ui icon 图标
    let $copy = $('<i title="copy"> COPY</i>').addClass('el-icon-document-copy code-copy')
    $(this)
      .parent()
      .append($numbering)
      .append($copy)
    for (let i = 0; i <= lines; i++) {
      $numbering.append($('<li/>'))
    }
  })
  //监听复制按钮点击事件
  $('pre i.code-copy').click(e => {
    let text = $(e.target).siblings('code').text()
    let element = $('<textarea>' + text + '</textarea>')
    $('body').append(element)
    element[0].select()
    document.execCommand('Copy')
    element.remove()
    //这里是自定义的消息通知组件
    myMessage.success('success')
  })
}

