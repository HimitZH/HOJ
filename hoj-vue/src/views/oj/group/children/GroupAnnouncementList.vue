<template>
  <el-card>
    <div class="filter-row">
      <el-row>
        <el-col :md="3" :xs="5">
          <span class="title">{{ $t('m.Group_Announcement') }}</span>
        </el-col>
        <el-col :md="18" :xs="19">
          <el-button
            type="primary"
            size="small"
            @click="handleCreatePage"
            icon="el-icon-plus"
            >{{ $t('m.Create') }}</el-button
          >
        </el-col>
      </el-row>
    </div>
    <AnnouncementList ref="announcementList"> </AnnouncementList>
  </el-card>
</template>

<script>
import AnnouncementList from '@/components/oj/group/AnnouncementList.vue';
export default {
  name: 'GroupAnnouncementList',
  components: {
    AnnouncementList,
  },
  data() {
    return {};
  },
  mounted() {
    this.init();
  },
  methods: {
    init() {},
    handleCreatePage() {
      this.$refs.announcementList.openAnnouncementDialog(null);
    },
  },
  computed: {},
};
</script>

<style scoped>
.title {
  font-size: 20px;
  vertical-align: middle;
  float: left;
}
.filter-row {
  margin-bottom: 5px;
  text-align: center;
}
@media screen and (max-width: 768px) {
  .filter-row span {
    margin-left: 5px;
    margin-right: 5px;
  }
}
@media screen and (min-width: 768px) {
  .filter-row span {
    margin-left: 10px;
    margin-right: 10px;
  }
}
</style>
