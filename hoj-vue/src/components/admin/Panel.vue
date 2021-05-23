<template>
  <div class="panel" :class="{'small': small}">
    <header>
      <div class="title">
        <template v-if="$slots.title">
          <slot name="title"></slot>
        </template>
        <template v-else>
          {{title}}
        </template>
      </div>

      <div class="header_right">
        <slot name="header"></slot>
      </div>
    </header>

    <div class="body">
      <slot></slot>
    </div>
  </div>
</template>

<script>
  export default {
    name: 'Panel',
    props: {
      title: {
        type: String,
        required: false
      },
      small: {
        type: Boolean,
        default: false
      }
    }
  }
</script>
<style scoped lang="less">
  .panel {
    margin-bottom: 20px;
    background-color: #fff;
    border: 1px solid transparent;
    border-radius: 4px;
    box-shadow: 0 1px 1px rgba(0, 0, 0, .05);
    &.small {
      max-width: 830px;
      min-width: 700px;
      margin-left: 20px;
      margin-top: 10px;
    }
    header {
      position: relative;
      z-index: 10;
      > .title {
        margin: 0;
        color: #333;
        border-color: #ddd;
        font-size: 18px;
        font-weight: 300;
        letter-spacing: 0.025em;
        height: 60px;
        line-height: 45px;
        padding: 10px 15px;
        border-bottom: 1px solid #eee;
        border-top-left-radius: 3px;
        border-top-right-radius: 3px;
      }
      > .header_right {
        position: absolute;
        top: 50%;
        right: 20px;
        transform: translate(0, -50%);
      }
    }
    .body {
      padding: 15px;
    }
  }
</style>
<style lang="less">
  .panel-options {
    background-color: transparent;
    position: relative;
    height: 50px;
    button {
      margin-top: 18px;
      margin-right: 10px;
    }
    > .page {
      position: absolute;
      right:20px;
      top: 20px;
    }
  }
</style>
