<template>
  <div class="page">
    <el-pagination
      background
      :total="total"
      :page-size="pageSize"
      @current-change="onChange"
      @size-change="onPageSizeChange"
      :layout="layout"
      :page-sizes="[10, 15, 30, 50, 100]"
      :current-page="current"
    ></el-pagination>
  </div>
</template>

<script>
export default {
  name: 'pagination',
  props: {
    total: {
      required: true,
      type: Number,
    },
    pageSize: {
      required: false,
      type: Number,
    },
    showSizer: {
      required: false,
      type: Boolean,
      default: false,
    },
    current: {
      required: false,
      type: Number,
    },
    layout: {
      require: false,
      type: String,
      default: 'prev, pager, next',
    },
  },
  methods: {
    onChange(page) {
      if (page < 1) {
        page = 1;
      }
      this.$emit('update:current', page);
      this.$emit('on-change', page);
    },
    onPageSizeChange(pageSize) {
      this.$emit('update:pageSize', pageSize);
      this.$emit('on-page-size-change', pageSize);
    },
  },
};
</script>

<style scoped>
.page {
  margin: 20px;
  float: right;
}
</style>
