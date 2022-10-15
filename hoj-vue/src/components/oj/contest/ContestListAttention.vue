<template>
  <el-card class="contest-attention">
    <div slot="header">
      <span class="panel-title">Pay attention</span>
    </div>
    <el-row :gutter="20">
      <el-col
        :md="6"
        :xs="24"
        :key="index"
        v-for="(index,contest) in runningContestList"
      >
        <el-card class="contest-attention-item running">
          <span class="state-phase">Contest is running</span>
          <br />
          <el-link type="primary">正规比赛</el-link>
          <br />
          <span class="countdown-text">08:05:37</span>
        </el-card>
      </el-col>

      <el-col
        :md="6"
        :xs="24"
        :key="index"
        v-for="(index,contest) in scheduledContestList"
      >
        <el-card class="contest-attention-item scheduled">
          <span class="state-phase">Before contest</span>
          <br />
          <el-link type="primary">正规比赛</el-link>
          <br />
          <span class="countdown-text">08:05:37</span>
        </el-card>
      </el-col>
    </el-row>
  </el-card>
</template>
<script>
export default {
    name: "ContestListAttention",
  props: {
    runningContestList: {
      default: [1,2],
      type: Array,
    },
    scheduledContestList: {
      default: [1,2,3],
      type: Array,
    },
  },
};
</script>
<style scoped>
.contest-attention {
  margin-bottom: 20px;
}
.contest-attention-item {
  text-align: center;
  margin-bottom: 10px;
}
.contest-attention-item.running{
    border-color: rgb(25, 190, 107);
}
.contest-attention-item.scheduled{
    border-color: #f90;
}

.contest-attention-item .state-phase {
  font-size: 1.5rem;
  font-weight: 700;
}

.contest-attention-item.running .state-phase {
  color: #5eb95e;
}

.contest-attention-item.scheduled .state-phase {
  color: #f37b1d;
}

.contest-attention-item .countdown-text {
  color: #777;
}
/deep/.el-card__header {
    padding: 10px 20px;
}
/deep/.el-card__body {
    padding: 10px 20px;
}
</style>