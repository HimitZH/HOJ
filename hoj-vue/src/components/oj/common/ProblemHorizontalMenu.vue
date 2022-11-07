<template>
  <el-card
    shadow="always"
    id="problem-footer"
  >
    <div class="dataNav">
      <button
        class="dataNavPrev"
        @click="navPrev"
      ><i class="el-icon-arrow-left"></i></button>
      <ul
        class="dataNavList"
        id="problem-footer-list"
      >
        <span
          v-for="(item, index) in navList"
          :key="index"
        >
          <el-tooltip
            effect="dark"
            placement="top"
          >
            <div slot="content">
              <div style="text-align:center">
                {{item.problemId+'. '+item.title}}
              </div>
              <template v-if="item.status != null">
                <div style="text-align:center">
                  {{JUDGE_STATUS[item.status].name}}
                  <template v-if="item.score != null">
                    ({{ item.score}} pts)
                  </template>
                </div>
              </template>
            </div>
            <li
              class="dataNavListItem"
              :class="pid == item.pid?'is-active':''"
              @click="goProblem(item.problemId)"
              :style="{transform:'translateX(-'+move+'px)'}"
            >
              <template v-if="item.status == 0">
                <i
                  class="el-icon-check"
                  :style="getIconColor(item.status)"
                ></i>
              </template>
              <template v-else-if="item.status == -5">
                <i
                  class="fa fa-question"
                  :style="getIconColor(item.status)"
                ></i>
              </template>
              <template v-else-if="item.status == -2">
                <i
                  class="el-icon-minus"
                  :style="getIconColor(item.status)"
                ></i>
              </template>
              <template v-else-if="item.status != null">
                <i
                  class="el-icon-close"
                  :style="getIconColor(item.status)"
                ></i>
              </template>
              {{item.problemId}}
            </li>
          </el-tooltip>
        </span>
      </ul>
      <button
        class="dataNavNext"
        @click="navNext"
      ><i class="el-icon-arrow-right"></i></button>
    </div>
  </el-card>

</template>

<script>
import { JUDGE_STATUS } from "@/common/constants";
import api from "@/common/api";
export default {
  props: {
    pid: {
      type: Number,
      required: true,
    },
    tid: {
      type: Number,
      default: null,
    },
    cid: {
      type: Number,
      default: null,
    },
    gid: {
      type: Number,
      default: null,
    },
  },
  data() {
    return {
      navList: [],
      move: 0,
      dataNavListRealWidth: 0,
      dataNavListViewWidth: 0,
      moveLen: 100,
      JUDGE_STATUS: {},
    };
  },
  created() {
    this.JUDGE_STATUS = Object.assign({}, JUDGE_STATUS);
  },
  mounted() {
    this.getFullScreenProblemList();
  },
  methods: {
    getFullScreenProblemList() {
      api.getFullScreenProblemList(this.tid, this.cid).then(
        (res) => {
          this.navList = res.data.data;
          this.$nextTick(() => {
            this.calcMoveLen();
          });
        },
        (err) => {}
      );
    },
    calcMoveLen() {
      this.dataNavListRealWidth = document.getElementById(
        "problem-footer-list"
      ).scrollWidth;
      this.dataNavListViewWidth = document.getElementById(
        "problem-footer-list"
      ).offsetWidth;
      this.moveLen = this.dataNavListViewWidth / 2;
      let num = Math.floor((this.dataNavListViewWidth - 70) / 100);
      let index = this.getCurrentProblemInListIndex();
      if (index != -1 && index > num) {
        this.move = this.moveLen + (index - num) * 100;
      }
      window.addEventListener("resize", () => this.recalcMoveLen());
    },
    recalcMoveLen() {
      try {
        this.dataNavListViewWidth = document.getElementById(
          "problem-footer-list"
        ).offsetWidth;
        if (
          this.move >
          this.dataNavListRealWidth - this.dataNavListViewWidth - 100
        ) {
          this.move = this.dataNavListRealWidth - this.dataNavListViewWidth;
        }
      } catch (e) {}
    },
    navPrev() {
      if (this.move > this.moveLen) {
        this.move = this.move - this.moveLen;
      } else {
        this.move = 0;
      }
    },
    navNext() {
      if (
        this.move <
        this.dataNavListRealWidth - this.dataNavListViewWidth - this.moveLen
      ) {
        this.move = this.move + this.moveLen;
      } else {
        this.move = this.dataNavListRealWidth - this.dataNavListViewWidth;
      }
    },
    getCurrentProblemInListIndex() {
      var len = this.navList.length;
      for (var i = 0; i < len; i++) {
        if (this.navList[i].pid == this.pid) {
          return i;
        }
      }
      return -1;
    },
    getIconColor(status) {
      if (status == null || status == undefined) {
        return "";
      }
      return (
        "font-weight: 600;font-size: 16px;color:" +
        this.JUDGE_STATUS[status].rgb
      );
    },
    goProblem(problemId) {
      this.$router.push({
        name: this.$route.name,
        params: {
          contestID: this.cid,
          problemID: problemId,
          trainingID: this.tid,
          groupID: this.gid,
        },
      });
    },
  },
  watch: {
    $route() {
      this.getFullScreenProblemList();
    },
    pid(){
      // 避免pid传递过慢，导致当前题目移动居中失败，需要监听pid变化再次判断
      let num = Math.floor((this.dataNavListViewWidth - 70) / 100);
      let index = this.getCurrentProblemInListIndex();
      if (index != -1 && index > num) {
        this.move = this.moveLen + (index - num) * 100;
      }
    }
  },
};
</script>

<style scoped>
ul {
  margin: 0;
}
@media screen and (min-width: 1050px) {
  #problem-footer {
    margin: 0 -1%;
  }
}
/deep/.el-card__body {
  padding: 5px 0px !important;
}
.dataNav {
  display: flex;
  overflow: hidden;
  font-weight: bolder;
}
.dataNav .dataNavList {
  display: flex;
  overflow: hidden;
}
.dataNav .dataNavList .dataNavListItem {
  display: inline-block;
  height: 35px;
  width: 100px;
  min-width: 100px;
  margin: auto 0;
  border-radius: 5px;
  font-size: 13px;
  text-align: center;
  color: #7e8690;
  line-height: 35px;
  transition: transform 0.2s;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: middle;
}
.dataNavListItem:hover,
.dataNavListItem.is-active {
  color: #2e95fb !important;
  background: linear-gradient(2100deg, #f2f7fc 0%, #fefefe 100%) !important;
  border-bottom: 2px solid #2e95fb !important;
  outline: 0 !important;
  transition: all 0.2s ease;
  cursor: pointer;
}
.dataNav .dataNavNext {
  margin-left: auto;
}
.dataNav .dataNavPrev,
.dataNav .dataNavNext {
  display: inline-block;
  width: 35px;
  min-width: 35px;
  height: 35px;
  margin: auto 0;
  border: none;
  border-radius: 5px;
  text-align: center;
  line-height: 35px;
  background-color: hsl(211, 20%, 97%);
}
.dataNav .dataNavPrev:focus,
.dataNav .dataNavNext:focus {
  outline: none;
}
.dataNav .dataNavPrev:hover,
.dataNav .dataNavNext:hover {
  color: #c7cede;
  background-color: #2e95fb;
  cursor: pointer;
}
</style>