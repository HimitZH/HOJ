<template>
<el-card class="box-card">
    <el-collapse v-model="activeName" accordion>
  <el-collapse-item name="Account">
    <template slot="title">
     <i class="fa fa-gear"> Account Setting</i>
    </template>
    <component :is="Account"></component>
  </el-collapse-item>
  <el-collapse-item name="UserInfo">
    <template slot="title">
      <i class="fa fa-gear"> UserInfo Setting</i>
    </template>
    <component :is="UserInfo"></component>
  </el-collapse-item>
</el-collapse>
</el-card>
</template>
<script>
import Account from "@/components/setting/Account";
import UserInfo from "@/components/setting/UserInfo"
export default {
  components:{
    Account,
    UserInfo
  },
  data(){
      return{
          Account:'Account',
          UserInfo:'UserInfo',
          activeName:'Account'
      }
  }
}
</script>
<style scoped>
@media screen and (min-width: 1200px) {
  .box-card{
  margin-left: 10%;
  margin-right: 10%;
  }
}
@media screen and (max-width: 1200px) {
  .box-card{
    margin-top: 30%;
  }
}
/deep/ .el-collapse-item__header{
  border-radius: 4px;
  font-size: 18px;
  color: skyblue;
}
</style>