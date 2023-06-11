import api from '@/common/api'
import time from '@/common/time';
import { CONTEST_STATUS,CONTEST_STATUS_REVERSE,CONTEST_TYPE_REVERSE,RULE_TYPE, buildContestRankConcernedKey} from '@/common/constants'
import { mapState, mapGetters, mapActions } from 'vuex';
import moment from 'moment';
import storage from '@/common/storage';
export default {
  methods: {
    init(){
    this.contestID = this.$route.params.contestID;
    this.CONTEST_TYPE_REVERSE = Object.assign({}, CONTEST_TYPE_REVERSE);
    this.CONTEST_STATUS = Object.assign({}, CONTEST_STATUS);
    this.CONTEST_STATUS_REVERSE = Object.assign({}, CONTEST_STATUS_REVERSE);
    this.RULE_TYPE = Object.assign({}, RULE_TYPE);
    let key = buildContestRankConcernedKey(this.contestID);
    this.concernedList = storage.get(key) || [];
    this.loading.info = true;
    this.$store.dispatch('getScoreBoardContestInfo').then((res) => {
        this.getContestOutsideScoreboard();
        if (!this.contestEnded) {
          this.autoRefresh = true;
          this.handleAutoRefresh(true);
        }
        this.changeDomTitle({ title: res.data.data.title });
        let data = res.data.data.contest;
        let endTime = moment(data.endTime);
        this.loading.info = false;
        // 如果当前时间还是在比赛结束前的时间，需要计算倒计时，同时开启获取比赛公告的定时器
        if (endTime.isAfter(moment(data.now))) {
          // 实时更新时间
          this.timer = setInterval(() => {
            this.$store.commit('nowAdd1s');
          }, 1000);
        }
      },(err)=>{
        this.loading.info = false;
      });
    },
    getContestOutsideScoreboard () {
      let data = {
        cid: this.$route.params.contestID,
        forceRefresh: this.forceUpdate ? true: false,
        removeStar: !this.showStarUser,
        concernedList:this.concernedList,
        currentPage: this.page,
        limit: this.limit,
        keyword: this.keyword == null? null: this.keyword.trim(),
        containsEnd: this.isContainsAfterContestJudge
      }
      this.loading.rank = true;
      api.getContestOutsideScoreboard(data).then(res => {
        this.applyToTable(res.data.data.records);
        this.total = res.data.data.total
        this.loading.rank = false;
      },(err)=>{
        this.loading.rank = false;
        if(this.refreshFunc){
          this.autoRefresh = false;
          clearInterval(this.refreshFunc)
        }
      })
    },
    handleAutoRefresh (status) {
      if (status == true) {
        this.refreshFunc = setInterval(() => {
          this.getContestOutsideScoreboard()
        }, 30000)
      } else {
        clearInterval(this.refreshFunc)
      }
    },
    ...mapActions(['changeDomTitle']),
    formatTooltip(val) {
      if (this.contest.status == -1) {
        // 还未开始
        return '00:00:00';
      } else if (this.contest.status == 0) {
        return time.secondFormat(this.BeginToNowDuration); // 格式化时间
      } else {
        return time.secondFormat(this.contest.duration);
      }
    },
    getProblemCount(num){
      return num == undefined? 0 : num
    },
    updateConcernedList(uid,isConcerned){
      if(isConcerned){
        this.concernedList.push(uid);
      }else{
        var index = this.concernedList.indexOf(uid);
        if (index > -1) {
        this.concernedList.splice(index, 1);
        }
      }
      let key = buildContestRankConcernedKey(this.contestID);
      storage.set(key, this.concernedList);
      this.getContestOutsideScoreboard();
    },
    toGroupContestList(gid){
      this.$router.push({
        name: 'GroupContestList',
        params: {
          groupID: gid,
        },
      })
    },
    getRankShowName(rankShowName, username){
      let finalShowName = rankShowName;
      if(rankShowName == null || rankShowName == '' || rankShowName.trim().length == 0){
        finalShowName = username;
      }
      return finalShowName;
    }
  },
  computed: {
    ...mapState({
      contest: (state) => state.contest.contest,
      now: (state) => state.contest.now,
      contestProblems: state => state.contest.contestProblems
    }),
    ...mapGetters([
      'countdown',
      'BeginToNowDuration',
      'isContestAdmin',
      'userInfo',
      'isShowContestSetting'
    ]),
    forceUpdate: {
      get () {
        return this.$store.state.contest.forceUpdate
      },
      set (value) {
        this.$store.commit('changeRankForceUpdate', {value: value})
      }
    },
    showStarUser:{
      get () {
        return !this.$store.state.contest.removeStar
      },
      set (value) {
        this.$store.commit('changeRankRemoveStar', {value: !value})
      }
    },
    concernedList:{
      get () {
        return this.$store.state.contest.concernedList
      },
      set (value) {
        this.$store.commit('changeConcernedList', {value: value})
      }
    },
    progressValue: {
      get: function() {
        return this.$store.getters.progressValue;
      },
      set: function() {},
    },
    timeStep() {
      // 时间段平分滑条长度
      return 100 / this.contest.duration;
    },
    countdownColor() {
      if (this.contest.status) {
        return 'color:' + CONTEST_STATUS_REVERSE[this.contest.status].color;
      }
    },
    contestEnded() {
      return this.contest.status == CONTEST_STATUS.ENDED;
    },
    isContainsAfterContestJudge:{
      get () {
        return this.$store.state.contest.isContainsAfterContestJudge;
      },
      set (value) {
        this.$store.commit('changeContainsAfterContestJudge', {value: value})
      }
    },
  },
  beforeDestroy () {
    clearInterval(this.refreshFunc)
    clearInterval(this.timer);
    this.$store.commit('clearContest');
  }
}
