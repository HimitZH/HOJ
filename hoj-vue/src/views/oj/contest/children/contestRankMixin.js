import api from '@/common/api'
import { mapGetters, mapState } from 'vuex'
import { CONTEST_STATUS } from '@/common/constants'

export default {
  methods: {
    getContestRankData (page = 1, refresh = false) {
      if (this.showChart && !refresh) {
        this.$refs.chart.showLoading({maskColor: 'rgba(250, 250, 250, 0.8)'})
      }
      let params = {
        currentPage:page,
        limit: this.limit,
        cid: this.$route.params.contestID,
        forceRefresh: this.forceUpdate ? true: false,
        removeStar: !this.showStarUser
      }
      api.getContestRank(params).then(res => {
        if (this.showChart && !refresh) {
          this.$refs.chart.hideLoading()
        }
        this.total = res.data.data.total
        if (page === 1) {
          this.applyToChart(res.data.data.records.slice(0, 10))
        }
        this.applyToTable(res.data.data.records)
      })
    },
    handleAutoRefresh (status) {
      if (status == true) {
        this.refreshFunc = setInterval(() => {
          this.getContestRankData(this.page, true)
        }, 10000)
      } else {
        clearInterval(this.refreshFunc)
      }
    }
  },
  computed: {
    ...mapGetters(['isContestAdmin']),
    ...mapState({
      'contest': state => state.contest.contest,
      'contestProblems': state => state.contest.contestProblems
    }),
    showChart: {
      get () {
        return this.$store.state.contest.itemVisible.chart
      },
      set (value) {
        this.$store.commit('changeContestItemVisible', {chart: value})
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
    showTable: {
      get () {
        return this.$store.state.contest.itemVisible.table
      },
      set (value) {
        this.$store.commit('changeContestItemVisible', {table: value})
      }
    },
    forceUpdate: {
      get () {
        return this.$store.state.contest.forceUpdate
      },
      set (value) {
        this.$store.commit('changeRankForceUpdate', {value: value})
      }
    },
    refreshDisabled () {
      return this.contest.status == CONTEST_STATUS.ENDED
    }
  },
  beforeDestroy () {
    clearInterval(this.refreshFunc)
  }
}
