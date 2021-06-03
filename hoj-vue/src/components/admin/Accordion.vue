<template>
  <div class="accordion">
    <header>
      <span class="title">{{ title }}</span>
      <span class="header_right">
        <slot name="header"></slot>
      </span>
    </header>
    <div class="body" v-show="isOpen">
      <slot></slot>
    </div>
    <footer @click="isOpen = !isOpen">
      <i
        :class="{ rotate: !isOpen }"
        class="el-icon-caret-top"
        style="color:#2d8cf0"
      ></i>
    </footer>
  </div>
</template>

<script>
export default {
  name: 'Accordion',
  props: {
    title: {
      type: String,
      required: true,
    },
    isOpen: {
      type: Boolean,
      required: true,
      default: true,
    },
  },
};
</script>

<style scoped>
.tit .accordion {
  border: 1px solid #eaeefb;
}
.accordion header {
  position: relative;
}
.title {
  font-size: 14px;
  margin: 0 0 0 10px;
  line-height: 50px;
}
.header_right {
  float: right;
}

.body {
  background-color: #f9fafc;
  border-top: 1px solid #eaeefb;
  clear: both;
  overflow: hidden;
  padding: 15px 10px;
}
footer {
  border-top: 1px solid #eaeefb;
  height: 36px;
  box-sizing: border-box;
  background-color: #fff;
  border-bottom-left-radius: 4px;
  border-bottom-right-radius: 4px;
  text-align: center;
  margin-top: -1px;
  color: #d3dce6;
  cursor: pointer;
  transition: 0.2s;
}
footer:hover {
  background-color: #ebeef5;
}
.rotate {
  transform: rotate(180deg);
}
</style>
