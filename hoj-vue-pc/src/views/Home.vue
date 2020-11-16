<template>
  <div>
  <el-card class="contest">
  <div slot="header" class="clearfix title">
    <el-link @click="goContest" :underline="false">比赛标题</el-link>
  </div>
    <el-carousel indicator-position="outside" height="200px" :interval='interval'>
    <el-carousel-item v-for="(contest,index) in contests" :key="index">
      <div class="contest-info">
            <div class="contest-tags">
              <el-button type="primary" round size="mini"><i class="fa fa-calendar"></i>
                {{contest.beginTime | localtime }}
              </el-button>
              <el-button type="success" round size="mini"><i class="fa fa-clock-o"></i>
                {{contest.duration}}
              </el-button>
              <el-button type="warning" round size="mini"><i class="fa fa-trophy"></i>
                {{contest.type}}
              </el-button>
            </div>
            <div class="contest-description">
              <blockquote v-html="contest.description"></blockquote>
            </div>
        </div>
    </el-carousel-item>
  </el-carousel>
  </el-card>
  <el-row>
    <el-col :md="14" :sm="24">
      <el-card class="box-card">
      <div slot="header" class="clearfix">
        <h2>Other OnlineJudge Contest</h2>
      </div>
       <el-table
          :data="tableData"
          height="400"
          border
          style="width: 100%;border-radius: 8px;">
          <el-table-column
            prop="oj"
            label="OJ"
            width="100">
          </el-table-column>
          <el-table-column
            prop="title"
            label="Title"
          >
          </el-table-column>
          <el-table-column
            prop="beginTime"
            width="160"
            label="Begin time">
          </el-table-column>
          <el-table-column
            prop="endTime"
            width="160"
            label="End time">
          </el-table-column>
      </el-table>
      </el-card>
    </el-col>
    <el-col :md="10" :sm="24">
      <div class="block">
        <el-timeline>
          <el-timeline-item timestamp="2018/4/12" placement="top" icon="el-icon-more" type="primary">
            <el-card>
              <h4>更新 Github 模板</h4>
              <p>王小虎 提交于 2018/4/12 20:46</p>
            </el-card>
          </el-timeline-item>
          <el-timeline-item timestamp="2018/4/3" placement="top">
            <el-card>
              <h4>更新 Github 模板</h4>
              <p>王小虎 提交于 2018/4/3 20:46</p>
            </el-card>
          </el-timeline-item>
          <el-timeline-item timestamp="2018/4/2" placement="top">
            <el-card>
              <h4>更新 Github 模板</h4>
              <p>王小虎 提交于 2018/4/2 20:46</p>
            </el-card>
          </el-timeline-item>
        </el-timeline>
        </div>
      </el-col>
  </el-row>
  </div>
</template>

<script>
import time from "@/common/time";
export default {
    name:"home",
    data() {
      return {
        interval:6000,
        tableData: [{
          oj: 'Codeforces',
          title: 'Codeforces Round #680 (Div. 1, based on VK Cup 2020-2021 - Final)',
          beginTime: '2020-11-08T05:00:00Z',
          endTime:'2020-11-08T08:00:00Z',
        },],
        contests: [
          {
            beginTime:'2020-11-08T05:00:00Z',
            duration:'5hours',
            type:"ACM",
            description:'<h1>描述</h1>',
          }
        ],
      }
    },
    methods:{
      goContest(){

      }
    },
    filters: {
    localtime(value) {
      return time.utcToLocal(value);
    },
  },
  }
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

.el-col{
  margin-top: 25px;
}
.contest{
  text-align: center;
}
.clearfix:before,
  .clearfix:after {
    display: table;
    content: "";
  }
  .clearfix:after {
    clear: both
  }

.contest .title .el-link {
  font-size: 25px;
  font-weight: 500;
  color: #444;
}
.clearfix h2{
  color: #409EFF;
}
.el-link.el-link--default:hover {
    color: #409EFF;
    transition: all 0.28s ease;
}
.contest  .content-info {
  padding: 0 70px 40px 70px;
}
.contest .contest-description{
  margin-top: 25px;
}
</style>
