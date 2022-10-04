<template>
  <el-card :body-style="{ padding: 0, height: '100%' }" class="info-card">
    <el-row type="flex" class="info-card-container">
      <el-col
        :span="8"
        :style="{ 'background-color': color }"
        class="height-100"
      >
        <i
          :class="['info-card-icon', icon]"
          :style="{ 'font-size': iconSize }"
        ></i>
      </el-col>
      <el-col :span="16" class="info-card-text">
        <p :style="textStyle">{{ value }}</p>
        <p style="font-weight: 300">{{ message }}</p>
      </el-col>
    </el-row>
  </el-card>
</template>

<script>
export default {
  name: "inforCard",
  props: {
    value: [String, Number],
    color: String,
    message: String,
    icon: String,
    iconSize: {
      type: String,
      default: "35px",
    },
    countSize: {
      type: String,
      default: "25px",
    },
    countWeight: {
      type: Number,
      default: 700,
    },
  },
  computed: {
    textStyle() {
      return {
        "font-size": this.countSize,
        "font-weight": this.countWeight,
        color: this.color,
      };
    },
  },
};
</script>

<style  scoped>
.height-100 {
  height: 100%;
}

.info-card {
  display: inline-block;
  margin-right: 10px;
  width: 250px;
  height: 90px;
  text-align: center;
  vertical-align: middle;
}
@media screen and (max-width: 1080px) {
  .info-card {
    margin-right: 0;
  }
}
.info-card-container {
  height: 100%;
  align-items: center;
  justify-content: center;
}
.info-card-icon {
  line-height: 90px;
  color: white;
}
.info-card-text p {
  margin: 0;
}
.info-card-text .minor-text {
  font-weight: 300;
  margin-top: 2px;
  font-size: 20px;
}
</style>
