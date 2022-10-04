import moment from 'moment'
import utils from './utils'
import time from './time'
import {PROBLEM_LEVEL} from './constants'

// 友好显示时间
function fromNow (time) {
  return moment(time).fromNow()
}

function parseRole(num){
  if(num==1000){
    return '超级管理员'
  }else if(num==1001){
    return '普通管理员'
  }else if(num==1002){
    return '用户(默认)'
  }else if(num==1003){
    return '用户(禁止提交)'
  }else if(num==1004){
    return '用户(禁止发讨论)'
  }else if(num==1005){
    return '用户(禁言)'
  }else if(num==1006){
    return '用户(禁止提交&禁止发讨论)'
  }else if(num==1007){
    return '用户(禁止提交&禁言)'
  }else if(num==1008){
    return '题目管理员'
  }
}
function parseContestType(num){
  if(num==0){
    return 'ACM'
  }else if(num==1){
    return 'OI'
  }
}

function parseProblemLevel(num){
  return PROBLEM_LEVEL[num].name;
}

export default {
  submissionMemory: utils.submissionMemoryFormat,
  submissionTime: utils.submissionTimeFormat,
  localtime: time.utcToLocal,
  fromNow: fromNow,
  parseContestType:parseContestType,
  parseRole:parseRole,
  parseProblemLevel:parseProblemLevel,
}
