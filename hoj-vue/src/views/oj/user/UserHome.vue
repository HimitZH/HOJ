<template>
  <div class="container">
    <div class="avatar-container">
      <avatar :username="profile.username" :inline="true" :size="130" color="#FFF" :src="profile.avatar"></avatar>
    </div>
    <el-card class="box-card">
      <div >
        <p style="margin-top: 50px">
          <span  class="emphasis"><i class="fa fa-user-circle-o" aria-hidden="true"></i> {{profile.username}}</span>
        </p>
         <p >
          <span ><i class="fa fa-graduation-cap" aria-hidden="true"></i> {{profile.school?profile.school:'暂未设置'}}</span>
        </p>
        <p class="mood"><i class="fa fa-pencil-square-o" aria-hidden="true"></i>
          {{profile.signature?profile.signature:'暂无个性签名'}}
        </p>

        <hr id="split"/>

        <el-row :gutter="12">
            <el-col :md="6" :sm="24">
                <el-card shadow="always" class="submission">
                    <p><i class="fa fa-th" aria-hidden="true"></i> Submissions</p>
                      <p class="data-number">{{profile.total}}</p>
                </el-card>
            </el-col>
            <el-col :md="6" :sm="24">
                <el-card shadow="always" class="solved">
                    <p><i class="fa fa-check-circle" aria-hidden="true"></i> Solved</p>
                      <p class="data-number">{{profile.solvedList.length}}</p>
                </el-card>
            </el-col>
            <el-col :md="6" :sm="24">
                <el-card shadow="always" class="score">
                    <p><i class="fa fa-star" aria-hidden="true"></i> Score</p>
                      <p class="data-number">{{profile.score}}</p>
                </el-card>
            </el-col>
            <el-col :md="6" :sm="24">
                <el-card shadow="always" class="rating">
                    <p><i class="fa fa-user-secret" aria-hidden="true"></i> Rating</p>
                      <p class="data-number">{{profile.rating?profile.rating:'--'}}</p>
                </el-card>
            </el-col>
        </el-row>
    
        
        <div id="problems">
          <div v-if="profile.solvedList.length">List of AC problems
            <el-button type="primary" icon="el-icon-refresh" circle size="mini" @click="freshProblemDisplayID"></el-button>
          </div>
          <p v-else>暂无数据</p>
          <div class="btns">
            <div class="problem-btn" v-for="problemID of profile.solvedList" :key="problemID">
              <el-button type="success" @click="goProblem(problemID)">{{problemID}}</el-button>
            </div>
          </div>
        </div>
        
      </div>
    </el-card>
  </div>
</template>
<script>
  import { mapActions } from 'vuex'
  import time from '@/common/time'
  import api from '@/common/api'
  import myMessage from '@/common/message'
  import Avatar from 'vue-avatar'
  export default {
    components: {
      Avatar
    },
    data () {
      return {
        profile: {
          username:'暂无数据',
          avatar:'',
          school:'暂无数据',
          signature:'暂无数据',
          total:0,
          rating:0,
          score:0,
          solvedList:[],
        },
      }
    },
    mounted () {
      this.init()
    },
    methods: {
      ...mapActions(['changeDomTitle']),
      init () {
        this.uid = this.$route.query.uid
        api.getUserInfo(this.uid).then(res => {
          this.changeDomTitle({title: res.data.username})
          this.profile = res.data.data
        })
      },
      goProblem (problemID) {
        this.$router.push({name: 'problemDetails', params: {problemID: problemID}})
      },
      freshProblemDisplayID () {
        this.init()
        myMessage.success('更新成功！')
      }
    },
    watch:{
       $route(newVal, oldVal) {
         if(newVal!==oldVal){
           this.init();
         }
       }
    }
    
}
</script>

<style scoped>
.submission{
    background: skyblue;
    color: #FFF;
    font-size: 14px;
}
.solved{
    background: #67C23A;
    color: #FFF;
    font-size: 14px;
}
.score{
    background:#e6a23c;
    color: #FFF;
    font-size: 14px;
}
.rating{
    background:#dd6161;
    color: #FFF;
    font-size: 14px;
}
.mood{
    font-style: italic;
    font-size: 15px;
}
.data-number{
  font-size: 20px;
  font-weight: 600;
}

.container  p {
  margin-top: 8px;
  margin-bottom: 8px;
}

@media screen and (max-width: 1080px) {
 .container {
    position: relative;
    width: 100%;
    margin-top: 110px;
    text-align: center;
  }
  .container .avatar-container {
    position: absolute;
    left: 50%;
    transform: translate(-50%);
    z-index: 1;
    margin-top: -90px;
    }   
}

@media screen and (min-width: 1080px) {
    .container {
    position: relative;
    width: 75%;
    margin-top: 160px;
    text-align: center;
  }
  .container .avatar-container {
    position: absolute;
    left: 50%;
    transform: translate(-50%);
    z-index: 1;
    margin-top: -8%;
    }   
}
 .container .avatar {
    width: 140px;
    height: 140px;
    border-radius: 50%;
    box-shadow: 0 1px 1px 0;
}

.container .emphasis {
      font-size: 20px;
      font-weight: 600;
}
#problems {
  margin-top: 40px;
  padding-left: 30px;
  padding-right: 30px;
  font-size: 18px;
}
.btns {
    margin-top: 15px;
}
.problem-btn {
    display: inline-block;
    margin: 5px;
}

</style>
