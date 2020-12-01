<template>
  <div class="accordion">
    <header>
      <span class="title">{{ title }}</span>
      <div class="header_right">
        <slot name="header"></slot>
      </div>
    </header>
    <div class="body" v-show="isOpen">
      <slot></slot>
    </div>
    <footer @click="isOpen = !isOpen">
      <i :class="{ rotate: !isOpen }" class="el-icon-caret-top"></i>
    </footer>
  </div>
</template>

<script>
export default {
  name: "Accordion",
  props: {
    title: {
      type: String,
      required: true,
    },
  },
  data() {
    return {
      isOpen: true,
    };
  },
};
</script>

<style scoped>
.tit
.accordion {
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
  background-color: #f9fafc;
}
.rotate {
  transform: rotate(180deg);
}
</style>
