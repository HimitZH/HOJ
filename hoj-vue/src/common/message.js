import Vue from 'vue'

let messageInstance = null;

const message=function(type,msg,duration=4000){
    if(messageInstance !=null){
        messageInstance.close()
    }
    messageInstance = Vue.prototype.$msg({ type: type, message: msg, zIndex: 3000 ,position:'top-center',duration:duration})
    return messageInstance
}


const error = function (msg) {
    return message('error',msg)
}

const success = function (msg) {
    return message('success',msg)
}

const info = function (msg) {
    return message('info',msg)
}

const warning = function (msg) {
    return message('warning',msg)
}
const loading = function(msg){
    return message('loading',msg)
}

const mMessage = {
    error,
    success,
    info,
    warning,
    loading,
    message
}

export default mMessage;