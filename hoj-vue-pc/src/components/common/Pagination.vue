<template>
  <div class="page">
    <el-pagination
          background 
          :total="total"
          :page-size="pageSize"
          @current-change="onChange"
          @size-change="onPageSizeChange"
          :show-sizer="showSizer"
          layout="prev, pager, next"
          :page-sizes="[20, 30, 50, 100, 200]"
          :current-page="current"></el-pagination>
  </div>
</template>

<script>
  export default {
    name: 'pagination',
    props: {
      total: {
        required: true,
        type: Number
      },
      pageSize: {
        required: false,
        type: Number
      },
      showSizer: {
        required: false,
        type: Boolean,
        default: false
      },
      current: {
        required: false,
        type: Number
      }
    },
    methods: {
      onChange (page) {
        if (page < 1) {
          page = 1
        }
        this.$emit('update:current', page)
        this.$emit('on-change', page)
      },
      onPageSizeChange (pageSize) {
        this.$emit('update:pageSize', pageSize)
        this.$emit('on-page-size-change', pageSize)
      }
    }
  }
</script>

<style scoped >
  .page {
    margin: 20px;
    float: right;
  }
</style>
