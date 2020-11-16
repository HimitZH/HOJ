<template>
    
<div class="item">
      <div class="open_con">
        <el-button
          icon="el-icon-switch-button"
          circle
          :plain="plain"
          type="primary"
          @click="OPens"
        ></el-button>
      </div>
      <div class="slider">
        <el-slider v-model="value" :format-tooltip="formatTooltip"></el-slider>
      </div>
    </div>
</template>
<script>
import countDown from "./count"; //引入倒计时组件
export default {
  name: "Caeds",
  components: { countDown },
  data() {
    return {
      value: 0,
      plain: true, // 开关按钮 true 是关闭，false是打开
      stop: 0,
      time: "0:00:00"
    };
  },
  mounted() {},
  methods: {
    formatTooltip(val) {
      if (val < 60) {
        if (val < 10) {
          this.time = "0:0" + val + ":00";
          return "0:0" + val + ":00";
        } else {
          this.time = "0:" + val + ":00";
          return "0:" + val + ":00";
        }
      } else {
        if (val < 70) {
          this.time = "1:0" + (val - 60) + ":00";
          return "1:0" + (val - 60) + ":00";
        } else {
          this.time = "1:" + (val - 60) + ":00";
          return "1:" + (val - 60) + ":00";
        }
      }
    },
    // 
    callBack(val) {
      console.log(val);
      this.value = val;
      // 倒计时结束 关闭按钮
      if (val == 0) {
        this.stop = 0;
        this.plain = true;
      }
    },
    OPens() {
        if (this.value != 0) {
          if (!this.plain && this.stop == 1) {
            this.stop = 2;
            console.log("stop");
            this.$refs.countdown.timepause();
          }
          if (this.plain && this.stop == 2) {
            this.stop = 1;
            console.log("open");
            this.$refs.countdown.timeresume();
          }
          if (this.stop == 0) {
            this.stop = 1;
          }
        }
        this.plain = !this.plain;
    },
  }
};
</script>