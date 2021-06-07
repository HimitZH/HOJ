import moment from 'moment'
import i18n from '@/i18n'
// 全局设定语言
moment.locale(i18n.locale);


// convert utc time to localtime
function utcToLocal (utcDt, format = 'YYYY-MM-DD  HH:mm:ss') {
  return moment.utc(utcDt).local().format(format)
}

// get duration from startTime to endTime, return like 3 days, 2 hours, one year ..
function duration (startTime, endTime) {
  let start = moment(startTime)
  let end = moment(endTime)
  let duration = moment.duration(start.diff(end, 'seconds'), 'seconds')
  return duration.humanize()
}

function formatDuration(time){
  let duration = moment.duration(time)
  return duration.humanize()
}

function secondFormat (time) {
  let m = moment.duration(time, 'seconds')
  let seconds =  m.seconds()>=10?m.seconds():'0'+m.seconds();
  let hours = Math.floor(m.asHours())>=10?Math.floor(m.asHours()):'0'+Math.floor(m.asHours());
  let minutes = m.minutes()>=10?m.minutes():'0'+m.minutes();
  return hours + ':' + minutes + ':' + seconds;
}

function durationMs (startTime, endTime) {  // 计算时间段的时间戳
  let start = moment(startTime)
  let end = moment(endTime)
  return end.diff(start, 'seconds');
}
export default {
  utcToLocal: utcToLocal,
  duration: duration,
  secondFormat: secondFormat,
  durationMs:durationMs,
  formatDuration:formatDuration
}
