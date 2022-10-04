<template>
  <div class="page">
    <el-pagination
      background
      :small="isMobile"
      :total="total"
      :pager-count="5"
      :page-size="pageSize"
      @current-change="onChange"
      @size-change="onPageSizeChange"
      :layout="layout"
      :page-sizes="pageSizes"
      :current-page="current"
      :hide-on-single-page="total == 0"
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
    pageSizes: {
      required: false,
      type: Array,
      default: [10, 15, 30, 50, 100],
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
    isMobile: {
      type: Boolean,
      default: false,
    },
  },
  created() {
    let screenWidth = window.screen.width;
    if (screenWidth < 768) {
      this.isMobile = true;
    } else {
      this.isMobile = false;
    }
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
  margin-right: 0px;
  float: right;
}
.el-pagination {
  padding-right: 0px !important;
}
/deep/.el-pagination__sizes {
  margin: 0px !important;
}
/deep/.el-pagination .el-select .el-input {
  margin-right: 0px !important;
}
</style>
