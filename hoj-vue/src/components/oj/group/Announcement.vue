<template>
  <el-card shadow="never" style="border: 0">
    <div slot="header" style="text-align: left">
      <span class="home-title panel-title" >{{ title }}</span>
      <span style="float: right">
        <el-button
          v-show="listVisible"
          type="primary"
          @click="init"
          size="small"
          icon="el-icon-refresh"
          :loading="loading"
          >{{ $t('m.Refresh') }}</el-button
        >
        <el-button
          v-show="!listVisible"
          type="primary"
          icon="el-icon-back"
          @click="goBack"
          size="small"
          >{{ $t('m.Back') }}</el-button
        >
      </span>
    </div>
    <transition-group name="el-fade-in-linear">
      <div
        class="no-announcement"
        v-if="!announcementList.length"
        key="no-announcement"
      >
        <el-empty :description="$t('m.No_Announcements')"></el-empty>
      </div>
      <template v-if="listVisible">
        <ul class="announcement-container" key="list">
          <li v-for="announcement in announcementList" :key="announcement.title">
            <div class="flex-container">
              <div class="title">
                <a class="entry" @click="goAnnouncement(announcement)">
                  {{ announcement.title }}</a
                >
              </div>

              <div class="info">
                <span class="date">
                  <i class="el-icon-edit"></i>
                  {{ announcement.gmtCreate | localtime }}
                </span>
                <span class="creator">
                  <i class="el-icon-user"></i>
                  {{ announcement.username }}
                </span>
              </div>
            </div>
          </li>
        </ul>
        <Pagination
          key="page"
          :total="total"
          :page-size="limit"
          @on-change="getGroupAnnouncementList"
        >
        </Pagination>
      </template>
      <template v-else>
        <div
          v-katex
          v-highlight
          v-dompurify-html="announcement.content"
          key="content"
          class="content-container markdown-body"
        ></div>
      </template>
    </transition-group>
  </el-card>
</template>

<script>
import api from '@/common/api';
import { addCodeBtn } from '@/common/codeblock';
import Pagination from '@/components/oj/common/Pagination';
export default {
  name: 'Announcement',
  components: {
    Pagination,
  },
  props: {
    limit: {
      type: Number,
      default: 5,
    },
  },
  data() {
    return {
      total: 0,
      loading: false,
      announcementList: [],
      announcement: '',
      listVisible: true,
    };
  },
  mounted() {
    this.init();
  },
  methods: {
    init() {
      this.getGroupAnnouncementList();
    },
    getGroupAnnouncementList(page = 1) {
      this.loading = true;
      api.getGroupAnnouncementList(page, this.limit, this.$route.params.groupID).then(
        (res) => {
          this.loading = false;
          this.announcementList = res.data.data.records;
          this.total = res.data.data.total;
        },
        () => {
          this.loading = false;
        }
      );
    },
    goAnnouncement(announcement) {
      this.announcement = announcement;
      this.announcement.content = this.$markDown.render(announcement.content);
      this.listVisible = false;
      this.$nextTick((_) => {
        addCodeBtn();
      });
    },
    goBack() {
      this.listVisible = true;
      this.announcement = '';
    },
  },
  computed: {
    title() {
      if (this.listVisible) {
        return this.$i18n.t('m.Group_Announcement');
      } else {
        return this.announcement.title;
      }
    },
  },
};
</script>

<style scoped>
.announcement-container {
  margin-top: -10px;
  margin-bottom: 10px;
}
.announcement-container li {
  padding-top: 15px;
  list-style: none;
  padding-bottom: 15px;
  margin-left: 20px;
  margin-top: 10px;
  font-size: 16px;
  border: 1px solid rgba(187, 187, 187, 0.5);
  border-left: 2px solid #409eff;
}
.flex-container {
  text-align: center;
}
.flex-container .info {
  margin-top: 5px;
}

.flex-container .title .entry {
  color: #495060;
  font-style: oblique;
}
.flex-container .title a:hover {
  color: #2d8cf0;
  border-bottom: 1px solid #2d8cf0;
}
.creator {
  width: 200px;
  text-align: center;
}
.date {
  width: 200px;
  text-align: center;
  margin-right: 5px;
}

.content-container {
  padding: 0 20px 20px 20px;
}

.no-announcement {
  text-align: center;
  font-size: 16px;
}

.announcement-animate-enter-active {
  animation: fadeIn 1s;
}
ul {
  list-style-type: none;
  padding-inline-start: 0px;
}
</style>
