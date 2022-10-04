<template>
  <el-card>
      <div slot="header">
        <span class="title">{{OJ}} {{$t('m.Account_Config')}}</span>
      </div>
      <el-row 
        v-for="(value,index) in usernameListTmp" 
        :key="index" 
        :gutter="15"
        class="mg-top">
        <el-col :xs="24" :md="10">
          <el-input
            v-model="usernameListTmp[index]"
            size="small"
            clearable>
            <template slot="prepend">{{$t('m.Account')}}{{index+1}}</template>
          </el-input>
        </el-col>
        <el-col :xs="24" :md="10">
          <el-input
            v-model="passwordListTmp[index]"
            size="small"
            show-password>
            <template slot="prepend">{{$t('m.Password')}}{{index+1}}</template>
          </el-input>
        </el-col>
        <el-col :xs="24" :md="4" class="t-center">
          <el-button 
            type="danger" 
            icon="el-icon-delete" 
            circle 
            size="small"
            @click="deleteAccount(index)">
          </el-button>
        </el-col>

      </el-row>
      <el-button 
        type="warning" 
        round 
        size="small"
        class="mg-top"
        @click="addAccount"
        icon="el-icon-plus">{{ $t('m.Add_Account') }}
      </el-button>
      <el-button
        type="primary"
        :loading="loading"
        style="margin-top:15px"
        @click.native="saveSwitchConfig"
        size="small"
        >
        <i class="fa fa-save"> {{ $t('m.Save') }}</i>
      </el-button>
  </el-card>
</template>

<script>
export default {
  props: {
    usernameList:{
      default:[],
      type: Array
    },
    passwordList:{
      default:[],
      type: Array
    },
    OJ:{
      type: String
    },
    loading:{
      type: Boolean,
      default: false
    }
  },
  data() {
      return {
        usernameListTmp: [],
        passwordListTmp: [],
      }
  },
  mounted(){
    this.usernameListTmp = this.usernameList;
    this.passwordListTmp = this.passwordList;
  },
  methods:{
      deleteAccount(index){
        this.usernameListTmp.splice(index, 1);
        this.$emit('update:usernameList', this.usernameListTmp);
        this.passwordListTmp.splice(index, 1);
        this.$emit('update:passwordList', this.passwordListTmp);
      },
      addAccount(){
        this.usernameListTmp.push("");
        this.$emit('update:usernameList', this.usernameListTmp);
        this.passwordListTmp.push("");
        this.$emit('update:passwordList', this.passwordListTmp);
      },
      saveSwitchConfig(){
        this.$emit('saveSwitchConfig');
      }
  },
  watch: {
    usernameList(val) {
      if (this.usernameListTmp !== val) {
        this.usernameListTmp = val;
      }
    },
    passwordList(val) {
      if (this.passwordListTmp !== val) {
        this.passwordListTmp = val;
      }
    },
  }
}
</script>

<style scoped>
.title{
  font-size: 18px;
  font-weight: bolder;
}
.mg-top{
  margin-top: 15px;
}
@media screen and (max-width: 992px) {
  .t-center{
    text-align: center;
    margin-top: 10px;
  }
}
</style>