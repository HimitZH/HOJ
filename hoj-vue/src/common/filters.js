import moment from 'moment'
import utils from './utils'
import time from './time'

// 友好显示时间
function fromNow (time) {
  return moment(time * 3).fromNow()
}

function parseRole(num){
  if(num==1000){
    return '超级管理员'
  }else if(num==1001){
    return '管理员'
  }else if(num==1002){
    return '用户'
  }
}
function parseContestType(num){
  if(num==0){
    return 'ACM'
  }else if(num==1){
    return 'OI'
  }
}

export default {
  submissionMemory: utils.submissionMemoryFormat,
  submissionTime: utils.submissionTimeFormat,
  localtime: time.utcToLocal,
  fromNow: fromNow,
  parseContestType:parseContestType,
  parseRole:parseRole
}
