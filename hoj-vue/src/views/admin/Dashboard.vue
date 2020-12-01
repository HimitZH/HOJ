<template>
<div>
  <el-row :gutter="20">
    <el-col :xs="24" :md="10" :lg="8">
      <el-card class="admin-info">
        <div slot="header">
          <el-row :gutter="10">
            <el-col :span="10">
              <img class="avatar" :src="userInfo.avatar" />
            </el-col>
            <el-col :span="14">
              <span class="panel-title admin-info-name">{{
                userInfo.username
              }}</span>
              <p>{{ userInfo.admin_type }}</p>
            </el-col>
          </el-row>
        </div>
        <div class="last-info">
          <p class="last-info-title home-title">Last Login</p>
          <el-form label-width="80px" class="last-info-body">
            <el-form-item label="Time:">
              <span>{{ session.last_activity | localtime }}</span>
            </el-form-item>
            <el-form-item label="IP:">
              <span>{{ session.ip }}</span>
            </el-form-item>
            <el-form-item label="OS:">
              <span>{{ os }}</span>
            </el-form-item>
            <el-form-item label="Browser:">
              <span>{{ browser }}</span>
            </el-form-item>
          </el-form>
        </div>
      </el-card>
    </el-col>

    <!-- <el-col :md="14" :lg="16" v-if="isSuperAdmin"> -->
    <el-col :xs="24" :md="14" :lg="16">
      <div class="info-container">
        <info-card
          color="#909399"
          icon="fa fa-users"
          message="Total Users"
          iconSize="30px"
          class="info-item"
          :value="infoData.user_count"
        ></info-card>
        <info-card
          color="#67C23A"
          icon="fa fa-list"
          message="Today Submissions"
          class="info-item"
          :value="infoData.today_submission_count"
        ></info-card>
        <info-card
          color="#409EFF"
          icon="fa fa-trophy"
          message="Recent Contests"
          class="info-item"
          :value="infoData.recent_contest_count"
        ></info-card>
      </div>
      <!-- <el-card title="System_Overview" v-if="isSuperAdmin"> -->
      <el-card>
          <div slot="header">
            <span class="panel-title home-title">System Overview</span>
          </div>
          <p>Judge Server: {{ infoData.judge_server_count }}</p>
          <p>Https Status:
            <el-tag :type="https ? 'success' : 'danger'" size="small">
              {{ https ? "Enabled" : "Disabled" }}
            </el-tag>
          </p>
      </el-card>
    </el-col>
  </el-row>

   <el-card style="margin-top:10px">
        <div slot="header">
          <span class="panel-title home-title">Judger Service</span>
        </div>
        <vxe-table stripe auto-resize :data="judgeInfo" align="center">
          <vxe-table-column type="seq" width="50"></vxe-table-column>
          <vxe-table-column
            field="serviceId"
            title="Name"
            min-width="150"
          ></vxe-table-column>
          <vxe-table-column
            field="host"
            title="Host"
            min-width="80"
          ></vxe-table-column>
          <vxe-table-column
            field="port"
            title="Port"
            min-width="80"
          ></vxe-table-column>

           <vxe-table-column
           min-width="80"
          field="cpu_core"
          title="CPU Core">
        </vxe-table-column>

        <vxe-table-column
          min-width="100"
          field="cpu_usage"
          title="CPU Usage">
          <template v-slot="{row}">{{ row.cpu_usage }}%</template>
        </vxe-table-column>

        <vxe-table-column
          min-width="110"
          field="memory_usage"
          title="Memory Usage">
          <template v-slot="{row}">{{row.memory_usage }}%</template>
        </vxe-table-column>

          <vxe-table-column field="secure" title="Secure" min-width="80">
            <template v-slot="{ row }">
             <el-tooltip content="是否触发保护阈值" placement="top">
               <el-tag effect="dark" color="#ed3f14" v-if="row.secure">True</el-tag>
              <el-tag effect="dark" color="#2d8cf0" v-else>False</el-tag>
             </el-tooltip>
            </template>
          </vxe-table-column>
          <vxe-table-column field="healthy" title="Healthy" min-width="100">
            <template v-slot="{ row }">
              <el-tag effect="dark" color="#19be6b" v-if="row.healthy"
                >healthy</el-tag
              >
              <el-tag effect="dark" color="#f90" v-else>unhealthy</el-tag>
            </template>
          </vxe-table-column>
        </vxe-table>
      </el-card>
      </div>
</template>


<script>
import { mapGetters } from "vuex";
import browserDetector from "browser-detect";
import InfoCard from "@/components/admin/infoCard.vue";
import api from "@/common/api";

export default {
  name: "dashboard",
  components: {
    InfoCard,
  },
  data() {
    return {
      infoData: {
        user_count: 0,
        recent_contest_count: 0,
        today_submission_count: 0,
        judge_server_count: 0,
        env: {},
      },
      judgeInfo: [
        {
          serviceId: "Judger-server",
          host: "192.0.0.1",
          port: "8080",
          memory_usage:40,
          cpu_usage:60,
          cpu_core:2,
          secure: false,
          healthy: true,
        },
        {
          serviceId: "Judger-server",
          host: "192.0.0.2",
          port: "8080",
          memory_usage:40,
          cpu_usage:60,
          cpu_core:2,
          secure: true,
          healthy: false,
        },
        {
          serviceId: "Judger-server",
          host: "192.0.0.3",
          port: "8080",
          memory_usage:40,
          cpu_usage:60,
          cpu_core:2,
          secure: true,
          healthy: true,
        },
      ],
      activeNames: [1],
      session: {},
      userInfo: {
        username: "Himit_ZH",
        admin_type: "管理员",
        avatar: "https://cdn.jsdelivr.net/gh/HimitZH/CDN/images/HCODE.png",
      },
    };
  },
  mounted() {
    // this.refreshJudgeServerList()
    // this.intervalId = setInterval(() => {
    //     this.refreshJudgeServerList()
    // }, 5000)

    api.getDashboardInfo().then(
      (resp) => {
        this.infoData = resp.data.data;
      },
      () => {}
    );
    
    api.getSessions().then(
      (resp) => {
        this.parseSession(resp.data.data);
      },
      () => {}
    );
  },
  methods: {
    parseSession(sessions) {
      let session = sessions[0];
      if (sessions.length > 1) {
        session = sessions
          .filter((s) => !s.current_session)
          .sort((a, b) => {
            return a.last_activity < b.last_activity;
          })[0];
      }
      this.session = session;
    },
    refreshJudgeServerList () {
        api.getJudgeServer().then(res => {
          this.servers = res.data.data.servers
        })
    },
  },
  computed: {
    ...mapGetters(["profile", "userInfo", "isSuperAdmin"]),
    cdn() {
      return this.infoData.env.STATIC_CDN_HOST;
    },
    https() {
      return document.URL.slice(0, 5) === "https";
    },
    browser() {
      let b = browserDetector(this.session.user_agent);
      if (b.name && b.version) {
        return b.name + " " + b.version;
      } else {
        return "Unknown";
      }
    },
    os() {
      let b = browserDetector(this.session.user_agent);
      return b.os ? b.os : "Unknown";
    },
  },
  //  beforeRouteLeave (to, from, next) {
  //     clearInterval(this.intervalId)
  //     next()
  //   }
};
</script>

<style scoped>
.admin-info {
  margin-bottom: 20px;
}
.admin-info-name {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 10px;
  color: #409eff;
}
.admin-info .avatar {
  max-width: 100%;
  max-width: 100px;
  max-height: 100px;
}
.admin-info .last-info-title {
  font-size: 16px;
}
.el-form-item {
  margin-bottom: 5px;
}

.info-container {
  display: flex;
  justify-content: flex-start;
  flex-wrap: wrap;
}
.info-container .info-item {
  flex: 1 0 auto;
  min-width: 200px;
  margin-bottom: 10px;
}
/deep/ .el-tag--dark {
   border-color: #fff; 
}
/deep/.el-card__header {
  padding-bottom: 0;
}

</style>
