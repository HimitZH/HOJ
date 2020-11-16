<template>
  <el-row type="flex" justify="space-around">
    <el-col :span="22" id="status">
      <el-alert :type="status.type" show-icon :closable="false" effect="dark" :class="getbackgroudColor(submission.data[0].status)"
        style="padding: 18px;">
        <template slot="title">
          <span class="title">{{ status.statusName }}</span>
        </template>
        <template slot>
          <div v-if="isCE"  class="content">
            <pre>{{ submission.data[0].err_info }}</pre>
          </div>
          <div v-else  class="content">
            <span class="span-row">Time: {{ submission.data[0].time }}</span>
            <span class="span-row">Memory: {{ submission.data[0].memory }}</span>
            <span class="span-row">Language: {{ submission.data[0].language }}</span>
            <span class="span-row">Author: {{ submission.data[0].author }}</span>
          </div>
        </template>
      </el-alert>
    </el-col>

    <el-col v-if="submission.data && !isCE" :span="22">
      <vxe-table align="center" :data="submission.data" stripe  style="padding-top: 13px;">
        <vxe-table-column field="sid" title="ID" width="213"></vxe-table-column>
        <vxe-table-column
          field="stime"
          title="Submit time"
          width="213"
        ></vxe-table-column>
        <vxe-table-column field="pid" title="Problem ID" width="213">
          <template v-slot="{ row }">
            <a
              :href="getProblemUri(row.pid)"
              style="color: rgb(87, 163, 243)"
              >{{ row.pid }}</a
            >
          </template>
        </vxe-table-column>
        <vxe-table-column field="status" title="Status" width="213">
          <template v-slot="{ row }">
            <span :class="getStatusColor(row.status)">{{
              JUDGE_STATUS[row.status].name
            }}</span>
          </template>
        </vxe-table-column>
        <vxe-table-column
          field="time"
          title="Time"
          width="213"
        ></vxe-table-column>
        <vxe-table-column
          field="memory"
          title="Memory"
          width="213"
        ></vxe-table-column>
      </vxe-table>
    </el-col>

    <el-col :span="22">
      <Highlight
        :code="submission.code"
        :language="submission.language"
        :border-color="status.color"
      ></Highlight>
    </el-col>
    <el-col v-if="submission.can_unshare" :span="22">
      <div id="share-btn">
        <el-button type="primary" icon="el-icon-document-copy" size="large" @click="doCopy">Copy</el-button>
        <el-button
          v-if="submission.shared"
          type="warning"
          size="large"
          icon="el-icon-circle-close"
          @click="shareSubmission(false)"
        >
          Unshared
        </el-button>
        <el-button
          v-else
          type="primary"
          size="large"
          icon="el-icon-share"
          @click="shareSubmission(true)"
        >
          Shared
        </el-button>
      </div>
    </el-col>
  </el-row>
</template>

<script>
import api from "@/common/api";
import { JUDGE_STATUS } from "@/common/constants";
import utils from "@/common/utils";
import Highlight from "@/components/common/Highlight";
import mMessage from '@/common/message'

export default {
  name: "submissionDetails",
  components: {
    Highlight,
  },
  data() {
    return {
      submission: {
        code:
          "import java.util.Scanner;\n\
          public class Main{\n\
              public static void main(String[] args){\n\
                  Scanner in=new Scanner(System.in);\n\
                  int a=in.nextInt();\n\
                  int b=in.nextInt();\n\
                  System.out.println((a+b));  \n\
              }\n\
          }",
        data: [{
          sid: 1000,
          stime: "2020-08-08 16:00:00",
          pid: "1001",
          status: 0,
          time: "4ms",
          memory: "3MB",
          language:'Java',
          author: "Himit_ZH",
          err_info:'CE错误',
        }],
        can_unshare:true,
        shared:true
      },
      isConcat: false,
      loading: false,
      JUDGE_STATUS:'',
    };
  },
  mounted() {
    this.getSubmission();
    this.JUDGE_STATUS = Object.assign({}, JUDGE_STATUS);
  },
  methods: {
    doCopy() {
      this.$copyText(this.submission.code).then(function (e) {
        mMessage.success('Code copied successfully');
      }, function (e) {
        mMessage.success('Code copy failed');
      })
    },
    getProblemUri(pid) {
      return "/problem/" + pid;
    },
    getStatusColor(status) {
      return "el-tag el-tag--medium status-" + JUDGE_STATUS[status].color;
    },
    getbackgroudColor(status){
      return "status-" + JUDGE_STATUS[status].color;
    },
    getSubmission() {
      this.loading = true;
      api.getSubmission(this.$route.params.id).then(
        (res) => {
          this.loading = false;
          let data = res.data.data;
          if (data.info && data.info.data && !this.isConcat) {
            // score exist means the submission is OI problem submission
            if (data.info.data[0].score !== undefined) {
              this.isConcat = true;
              const scoreColumn = {
                title: this.$i18n.t("m.Score"),
                align: "center",
                key: "score",
              };
              this.columns.push(scoreColumn);
              this.loadingTable = false;
            }
            if (this.isAdminRole) {
              this.isConcat = true;
              const adminColumn = [
                {
                  title: this.$i18n.t("m.Real_Time"),
                  align: "center",
                  render: (h, params) => {
                    return h(
                      "span",
                      utils.submissionTimeFormat(params.row.real_time)
                    );
                  },
                },
                {
                  title: this.$i18n.t("m.Signal"),
                  align: "center",
                  key: "signal",
                },
              ];
              this.columns = this.columns.concat(adminColumn);
            }
          }
          this.submission = data;
        },
        () => {
          this.loading = false;
        }
      );
    },
    shareSubmission(shared) {
      let data = { id: this.submission.data[0].sid, shared: shared };
      api.updateSubmission(data).then(
        (res) => {
          this.getSubmission();
          this.$success(this.$i18n.t("m.Succeeded"));
        },
        () => {}
      );
    },
  },
  computed: {
    status() {
      return {
        type: JUDGE_STATUS[this.submission.data[0].status].type,
        statusName: JUDGE_STATUS[this.submission.data[0].status].name,
        color: JUDGE_STATUS[this.submission.data[0].status].rgb,
      };
    },
    isCE() {
      return this.submission.data[0].status === -2;
    },
    isAdminRole() {
      return this.$store.getters.isAdminRole;
    },
  },
};
</script>

<style scoped>
#status .title {
  font-size: 20px;
}
#status .content {
  margin-top: 10px;
  font-size: 14px;
}
#status .content span {
  margin-right: 10px;
}
#status .span-row{
  display:block;
   float:left
}
#status .content pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  word-break: break-all;
}

#share-btn {
  float: right;
  margin-top: 5px;
  margin-right: 10px;
}
.el-row--flex {
  flex-wrap: wrap;
}
pre {
  border: none;
  background: none;
}
</style>
