<template>
  <div>
    <el-card class="contest">
      <div slot="header" class="clearfix title">
        <el-link @click="goContest" :underline="false">比赛标题</el-link>
      </div>
      <el-carousel
        indicator-position="outside"
        height="200px"
        :interval="interval"
      >
        <el-carousel-item v-for="(contest, index) in contests" :key="index">
          <div class="contest-info">
            <div class="contest-tags">
              <el-button type="primary" round size="mini"
                ><i class="fa fa-calendar"></i>
                {{ contest.beginTime | localtime }}
              </el-button>
              <el-button type="success" round size="mini"
                ><i class="fa fa-clock-o"></i>
                {{ contest.duration }}
              </el-button>
              <el-button type="warning" round size="mini"
                ><i class="fa fa-trophy"></i>
                {{ contest.type }}
              </el-button>
            </div>
            <div class="contest-description">
              <blockquote v-html="contest.description"></blockquote>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </el-card>
    <el-row :gutter="20">
      <el-col :md="12" :sm="24">
        <Announcements></Announcements>
      </el-col>
      <el-col :md="12" :sm="24">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span class="panel-title home-title"
              >Other OnlineJudge Contest</span
            >
          </div>
          <vxe-table
            border="inner"
            stripe
            auto-resize
            :data="otherContests">
            <vxe-table-column field="oj" title="OJ" min-width="100"></vxe-table-column>
            <vxe-table-column field="title" title="Title" min-width="200"></vxe-table-column>
            <vxe-table-column field="beginTime" title="Begin Time" min-width="150">
              <template v-slot="{ row }" >
                <span>{{row.beginTime| localtime}}</span>
              </template>
            </vxe-table-column>
            <vxe-table-column field="endTime" title="End Time" min-width="150">
              <template v-slot="{ row }" >
                <span>{{row.endTime| localtime}}</span>
              </template>
            </vxe-table-column>
          </vxe-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import time from "@/common/time";
import Announcements from "@/components/common/Announcements.vue";
export default {
  name: "home",
  components: {
    Announcements,
  },
  data() {
    return {
      interval: 6000,
      otherContests: [
        {
          oj: "Codeforces",
          title:
            "Codeforces Round #680 (Div. 1, based on VK Cup 2020-2021 - Final)",
          beginTime: "2020-11-08T05:00:00Z",
          endTime: "2020-11-08T08:00:00Z",
        },
      ],
      contests: [
        {
          beginTime: "2020-11-08T05:00:00Z",
          duration: "5hours",
          type: "ACM",
          description: "<h1>描述</h1>",
        },
      ],
    };
  },
  methods: {
    goContest() {},
  },
  filters: {
    localtime(value) {
      return time.utcToLocal(value);
    },
  },
};
</script>
<style scoped>
.el-carousel__item h3 {
  color: #475669;
  font-size: 14px;
  opacity: 0.75;
  line-height: 200px;
  margin: 0;
}
.el-carousel__item:nth-child(n) {
  background-color: #fff;
}

.el-col {
  margin-top: 25px;
}
.contest {
  text-align: center;
}
.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}
.clearfix:after {
  clear: both;
}

.contest .title .el-link {
  font-size: 25px;
  font-weight: 500;
  color: #444;
}
.clearfix h2 {
  color: #409eff;
}
.el-link.el-link--default:hover {
  color: #409eff;
  transition: all 0.28s ease;
}
.contest .content-info {
  padding: 0 70px 40px 70px;
}
.contest .contest-description {
  margin-top: 25px;
}
</style>
