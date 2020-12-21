<template>
  <div class="view">
    <el-card>
      <div slot="header">
        <span class="panel-title home-title">
          {{title}}
        </span>
      </div>
      <el-form label-position="top">
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="Contest Title" required>
              <el-input v-model="contest.title" placeholder="Enter the Contest Title"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="Contest Description" required>
              <Simditor v-model="contest.description"></Simditor>
            </el-form-item>
          </el-col>
          <el-col :md="8" :xs="24">
            <el-form-item label="Contest Start Time" required>
              <el-date-picker
                v-model="contest.startTime"
                @change="changeDuration"
                type="datetime"
                placeholder="Enter the Contest Start Time">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :md="8" :xs="24">
            <el-form-item label="Contest End Time" required>
              <el-date-picker
                v-model="contest.endTime"
                @change="changeDuration"
                type="datetime"
                placeholder="Enter the Contest End Time">
              </el-date-picker>
            </el-form-item>
          </el-col>

          <el-col :md="8" :xs="24">
            <el-form-item label="Contest Duration" required>
              <el-input v-model="durationText" disabled>
              </el-input>
            </el-form-item>
          </el-col>
           <el-col :md="8" :xs="24">
            <el-form-item label="Contest Type">
              <el-radio class="radio" v-model="contest.type" :label="0" :disabled="disableRuleType">ACM</el-radio>
              <el-radio class="radio" v-model="contest.type" :label="1" :disabled="disableRuleType">OI</el-radio>
            </el-form-item>
          </el-col>
          
           <el-col :md="8" :xs="24" v-if="contest.sealRank">
            <el-form-item label="Real Time Rank">
              <el-switch
                v-model="contest.sealRank"
                active-color="#13ce66"
                inactive-color="#ff4949">
              </el-switch>
            </el-form-item>
          </el-col>

          <el-col :md="16" :xs="24" v-else>
            <el-form-item label="Seal Rank">
              <el-switch
                v-model="contest.sealRank"
                active-color="#13ce66"
                inactive-color="">
              </el-switch>
            </el-form-item>
          </el-col>

          <el-col :md="8" :xs="24">
            <el-form-item label="Seal Rank Time" :required="contest.sealRank" v-show="contest.sealRank">
              <el-select v-model="seal_rank_time" >
                <el-option label="比赛结束前半小时" :value="0" :disabled="contest.duration<1800"></el-option>
                <el-option label="比赛结束前一小时" :value="1" :disabled="contest.duration<3600"></el-option>
                <el-option label="比赛全程时间" :value="2"></el-option>
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :md="8" :xs="24">
            <el-form-item label="Contest Auth" required>
              <el-select v-model="contest.auth" >
                <el-option label="公开赛" :value="0"></el-option>
                <el-option label="私有赛" :value="1"></el-option>
                <el-option label="保护赛" :value="2"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :md="8" :xs="24">
            <el-form-item label="Contest Password" v-show="contest.auth!=0" :required="contest.auth!=0">
              <el-input v-model="contest.pwd" placeholder="Contest Password"></el-input>
            </el-form-item>
          </el-col>
          <!-- <el-col :span="24">
            <el-form-item label="Allowed IP Ranges">
              <div v-for="(range, index) in contest.allowed_ip_ranges" :key="index">
                <el-row :gutter="20" style="margin-bottom: 15px">
                  <el-col :span="8">
                    <el-input v-model="range.value" placeholder="CIDR Network"></el-input>
                  </el-col>
                  <el-col :span="10">
                    <el-button  icon="el-icon-plus" @click="addIPRange" type="primary"></el-button>
                    <el-button  icon="el-icon-delete-solid" @click.native="removeIPRange(range)" type="danger"></el-button>
                  </el-col>
                </el-row>
              </div>
            </el-form-item>
          </el-col> -->
        </el-row>
      </el-form>
       <el-button type="primary"  @click.native="saveContest">Save</el-button>
    </el-card>
  </div>
</template>

<script>
  import api from '@/common/api'
  import Simditor from '@/components/admin/Simditor.vue'
  import time from '@/common/time'
  import moment from 'moment'
  import { mapGetters } from "vuex";
  import myMessage from '@/common/message'
  export default {
    name: 'CreateContest',
    components: {
      Simditor
    },
    data () {
      return {
        title: 'Create Contest',
        disableRuleType: false,
        durationText:'', // 比赛时长文本表示
        seal_rank_time:0, // 当开启封榜模式，即实时榜单关闭时，可选择前半小时，前一小时，全程封榜,默认半小时1800s
        contest: {
          title: '',
          description: '',
          startTime: '',
          endTime: '',
          duration:0,
          type: 0,
          password: '',
          sealRank: true,
          sealRankTime:'',//封榜时间
          auth: 0,
          // allowed_ip_ranges: [{
          //   value: ''
          // }]
        }
      }
    },
    mounted () {
      if (this.$route.name === 'admin-edit-contest') {
        this.title = 'Edit Contest'
        this.disableRuleType = true
        this.getContestByCid();
      }
    },
    watch: {
      $route() {
          if (this.$route.name === 'admin-edit-contest') {
            this.title = 'Edit Contest'
            this.disableRuleType = true
            this.getContestByCid();
          }else{
            this.title = 'Create Contest'
            this.disableRuleType = false
            this.contest = []
          }
        }
    },
    computed: {
    ...mapGetters(["userInfo"]),
    },
    methods: {
      getContestByCid(){
         api.admin_getContest(this.$route.params.contestId).then(res => {
          let data = res.data.data
          // let ranges = []
          // for (let v of data.allowed_ip_ranges) {
          //   ranges.push({value: v})
          // }
          // if (ranges.length === 0) {
          //   ranges.push({value: ''})
          // }
          // data.allowed_ip_ranges = ranges
          this.contest = data
          this.changeDuration()
          // 封榜时间转换
          let halfHour =  moment(this.contest.endTime).subtract(1800,'seconds').toString()
          let oneHour =  moment(this.contest.endTime).subtract(3600,'seconds').toString()
          let allHour =  moment(this.contest.startTime).toString()
          let sealRankTime = moment(this.contest.sealRankTime).toString()
          switch(sealRankTime){
            case halfHour:
              this.seal_rank_time = 0;
              break;
            case oneHour:
              this.seal_rank_time = 1;
              break;
            case allHour:
              this.seal_rank_time = 2;
              break;
          }
        }).catch(() => {
        })
      },

      saveContest () {
        let funcName = this.$route.name === 'admin-edit-contest' ? 'admin_editContest' : 'admin_createContest'

        switch(this.seal_rank_time){
          case 0: // 结束前半小时
            this.contest.sealRankTime = moment(this.contest.endTime).subtract(1800,'seconds');
            break;
          case 1: // 结束前一小时
            this.contest.sealRankTime = moment(this.contest.endTime).subtract(3600,'seconds');
            break;
          case 2: // 全程
            this.contest.sealRankTime = moment(this.contest.startTime);
        }
        let data = Object.assign({}, this.contest)
        // let ranges = []
        // for (let v of data.allowed_ip_ranges) {
        //   if (v.value !== '') {
        //     ranges.push(v.value)
        //   }
        // }
        // data.allowed_ip_ranges = ranges
        if(funcName === 'admin_createContest'){
          data['uid'] = this.userInfo.uid
          data['author'] = this.userInfo.username
        }
        
        api[funcName](data).then((res) => {
          myMessage.success(res.data.msg)
          this.$router.push({name: 'admin-contest-list', query: {refresh: 'true'}})
        }).catch(() => {
        })
      },
      changeDuration(){
        let start = this.contest.startTime;
        let end = this.contest.endTime
        let durationMS = time.durationMs(start,end);
        if(durationMS<0){
          this.durationText = '比赛起始时间不应该晚于结束时间！'
          this.contest.duration = 0;
          return;
        }
        if(start!=''&&end!=''){
            this.durationText = time.duration(start,end);
            this.contest.duration = durationMS;
        }
      },
      // addIPRange () {
      //   this.contest.allowed_ip_ranges.push({value: ''})
      // },
    //   removeIPRange (range) {
    //     let index = this.contest.allowed_ip_ranges.indexOf(range)
    //     if (index !== -1) {
    //       this.contest.allowed_ip_ranges.splice(index, 1)
    //     }
    //   }
    },
  }
</script>
