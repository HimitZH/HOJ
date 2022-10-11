<template>
  <div>
    <template v-if="name != null && num != undefined">
      <el-tooltip
        effect="dark"
        :content="name"
        placement="top"
      >
        <div class="num-box">
          <span :style="getRankNumColor">{{num}}</span>
        </div>
      </el-tooltip>
    </template>
    <template v-else>
      <div class="num-box">
        <span :style="getRankNumColor">{{num}}</span>
      </div>
    </template>
  </div>
</template>

<script>
export default {
  name: "RankBox",
  props: {
    color: {
      default: "#666",
      type: String,
    },
    background: {
      default: "#ededed",
      type: String,
    },
    num: {
      require: true,
      type: Number,
    },
    name: {
      require: false,
      type: String,
    },
  },
  computed: {
    getRankNumColor() {
      let style = "color:" + this.color + "; background:" + this.background +'; width:';
      if(this.num <= 99){
        style +='20px';
      }else if(this.num <= 999){
        style +='30px';
      }else if(this.num <= 9999){
        style +='40px';
      }else{
        style +='50px';
      }
      return style;
    },
  },
};
</script>

<style scoped>
.num-box {
  height: 100%;
  width: 100%;
}
.num-box span {
  background: #ededed;
  color: #666;
  display: block;
  font: 700 15px/20px Arial;
  height: 20px;
  margin: auto;
  overflow: hidden;
  text-align: center;
  width: 20px;
}
</style>