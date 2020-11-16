<template>
  <span>
    <slot>{{content}}</slot>
  </span>
</template>
<script>
export default {
  name: "CountDown",
  data() {
    return {
      timer: null,
      date: null,
      savedtime: 0, //时间
      hour: null,
      min: null,
      sec: null,
      content: this.endText //显示
    };
  },
  props: {
    // 倒计时时间 (分钟)
    endTime: {
      type: Number,
      default: ""
    },
    endText: {
      type: String,
      default: "0:00:00"
    }
  },
  mounted() {
    // 时间换成毫秒传递
    this.timeStart(this.endTime * 60000);
  },
  methods: {
    // 起始时间
    timeStart(endTime) {
      this.date = new Date();
      var date1 = new Date().getTime(); // 获取当前时间戳
      // 当前时间戳+3600s（一小时，其他时间通过计算时间戳进行相应加减），重新设置 Date 对象
      this.date.setTime(date1 + endTime);
      this.date = this.date.getTime();
      // 传递结束时的时间戳
      this.countdowm(this.date);
    },
    // 继续倒计时
    timeresume() {
      this.timeStart(this.savedtime);
    },
    // 暂停时间
    timepause() {
      clearInterval(this.timer);
      this.savedtime =
        this.hour * 60 * 60 * 1000 + this.min * 60 * 1000 + this.sec * 1000;
    },
    // 开始倒计时
    countdowm(timestamp) {
      let self = this;
      self.timer = setInterval(function() {
        let nowTime = new Date();
        let endTime = new Date(timestamp * 1);
        let t = endTime.getTime() - nowTime.getTime();
          // 判断剩余时间是否 >0
        if (t > 0) {
          self.hour = Math.floor((t / 3600000) % 24);
          self.min = Math.floor((t / 60000) % 60);
          self.sec = Math.floor((t / 1000) % 60);
          self.$emit("callBack", 1 + self.min + self.hour * 60); // 每减少一分钟父页面滑块的值就减 1
          let min = self.min < 10 ? "0" + self.min : self.min;
          let sec = self.sec < 10 ? "0" + self.sec : self.sec;
          let format = `${self.hour}:${min}:${sec}`;
          self.content = format;
        } else {
          // 倒计时结束
          self.$emit("callBack", 0);
          clearInterval(self.timer);
          self.content = "0:00:00";
        }
      }, 1000);
    }
  }
};
</script>