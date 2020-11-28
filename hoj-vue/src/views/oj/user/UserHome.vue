<template>
  <div class="container">
    <div class="avatar-container">
      <img class="avatar" :src="avatar"/>
    </div>
    <el-card class="box-card">
      <div >
        <p style="margin-top: 50px">
          <span  class="emphasis"><i class="fa fa-user-circle-o" aria-hidden="true"></i> Himit_ZH</span>
        </p>
         <p>
          <span ><i class="fa fa-graduation-cap" aria-hidden="true"></i> 电子科技大学中山学院</span>
        </p>
        <p class="mood"><i class="fa fa-pencil-square-o" aria-hidden="true"></i>
          我最帅的签名
        </p>

        <hr id="split"/>

        <el-row :gutter="12">
            <el-col :md="6" :sm="24">
                <el-card shadow="always" class="submission">
                    <p><i class="fa fa-th" aria-hidden="true"></i> Submissions</p>
                      <p>111</p>
                </el-card>
            </el-col>
            <el-col :md="6" :sm="24">
                <el-card shadow="always" class="solved">
                    <p><i class="fa fa-check-circle" aria-hidden="true"></i> Solved</p>
                      <p>111</p>
                </el-card>
            </el-col>
            <el-col :md="6" :sm="24">
                <el-card shadow="always" class="score">
                    <p><i class="fa fa-star" aria-hidden="true"></i> Score</p>
                      <p>1100</p>
                </el-card>
            </el-col>
            <el-col :md="6" :sm="24">
                <el-card shadow="always" class="rating">
                    <p><i class="fa fa-user-secret" aria-hidden="true"></i> Rating</p>
                      <p>1500</p>
                </el-card>
            </el-col>
        </el-row>
    
        
        <div id="problems">
          <div v-if="problems.length">List of AC problems
            <el-button type="primary" icon="el-icon-refresh" circle size="mini"></el-button>
          </div>
          <p v-else>暂无数据</p>
          <div class="btns">
            <div class="problem-btn" v-for="problemID of problems" :key="problemID">
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

  export default {
    data () {
      return {
        username: '',
        profile: {},
        problems: [1000,1001,1002,1003,1004,1005,1006,1007,1008,1009,1010,1011,1012,1013,1014],
        avatar:require("@/assets/default.jpg")
      }
    },
    mounted () {
      this.init()
    },
    methods: {
      ...mapActions(['changeDomTitle']),
      init () {
        this.username = this.$route.query.username
        api.getUserInfo(this.username).then(res => {
          this.changeDomTitle({title: res.data.data.user.username})
          this.profile = res.data.data
          this.getSolvedProblems()
          let registerTime = time.utcToLocal(this.profile.user.create_time, 'YYYY-MM-D')
          console.log('The guy registered at ' + registerTime + '.')
        })
      },
      getSolvedProblems () {
        let ACMProblems = this.profile.acm_problems_status.problems || {}
        let OIProblems = this.profile.oi_problems_status.problems || {}
        // todo oi problems
        let ACProblems = []
        for (let problems of [ACMProblems, OIProblems]) {
          Object.keys(problems).forEach(problemID => {
            if (problems[problemID]['status'] === 0) {
              ACProblems.push(problems[problemID]['_id'])
            }
          })
        }
        ACProblems.sort()
        this.problems = ACProblems
      },
      goProblem (problemID) {
        this.$router.push({name: 'problem-details', params: {problemID: problemID}})
      },
      freshProblemDisplayID () {
        api.freshDisplayID().then(res => {
          this.$success('Update successfully')
          this.init()
        })
      }
    },
    computed: {
     
    },
    watch: {
      
      }
    
}
</script>

<style scoped>
.submission{
    background: skyblue;
    color: #FFF;
}
.solved{
    background: #67C23A;
    color: #FFF;
}
.score{
    background:#e6a23c;
    color: #FFF;
}
.rating{
    background:#dd6161;
    color: #FFF;
}
.mood{
    font-style: italic;
    font-size: 15px;
}

.container  p {
      margin-top: 8px;
      margin-bottom: 8px;
}

@media screen and (max-width: 1200px) {
 .container {
    position: relative;
    width: 100%;
    margin: 320px auto;
    text-align: center;
  }
  .container .avatar-container {
    position: absolute;
    left: 50%;
    transform: translate(-50%);
    z-index: 1;
    margin-top: -30%;
    }   
}

@media screen and (min-width: 1200px) {
    .container {
    position: relative;
    width: 75%;
    margin: 160px auto;
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

#icons {
      position: absolute;
      bottom: 20px;
      left: 50%;
      transform: translate(-50%);     
}
#icons .icon {
        padding-left: 20px;
}
</style>
