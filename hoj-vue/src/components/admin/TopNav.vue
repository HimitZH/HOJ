<template>
  <div class="breadcrumb">
    <el-breadcrumb separator=">">
      <el-breadcrumb-item :to="{ path: '/' }">Home page</el-breadcrumb-item>
      <el-breadcrumb-item><slot name="topNavName">PLEASE OVERIDE ME</slot></el-breadcrumb-item>
    </el-breadcrumb>
  </div>
</template>

<style scoped>
  .breadcrumb {
    margin: 10px;
    margin-right: 25px;
    margin-bottom: 20px;
    padding: 15px;
    background-color: #fff;
  }
</style>
