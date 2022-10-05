<template>
  <el-row>
    <el-card>
      <section>
        <span class="find-training">{{ $t('m.Search_Training') }}</span>
        <vxe-input
          v-model="query.keyword"
          :placeholder="$t('m.Enter_keyword')"
          type="search"
          size="medium"
          style="width:230px"
          @keyup.enter.native="filterByChange"
          @search-click="filterByChange"
        ></vxe-input>
      </section>
      <section>
        <b class="training-category">{{ $t('m.Training_Auth') }}</b>
        <div>
          <el-tag
            size="medium"
            class="category-item"
            :effect="query.auth ? 'plain' : 'dark'"
            @click="filterByAuthType(null)"
            >{{ $t('m.All') }}</el-tag
          >
          <el-tag
            size="medium"
            class="category-item"
            v-for="(key, index) in TRAINING_TYPE"
            :type="key.color"
            :effect="query.auth == key.name ? 'dark' : 'plain'"
            :key="index"
            @click="filterByAuthType(key.name)"
            >{{ $t('m.Training_' + key.name) }}</el-tag
          >
        </div>
      </section>
      <section>
        <b class="training-category">{{ $t('m.Training_Category') }}</b>
        <div>
          <el-tag
            size="medium"
            class="category-item"
            :style="getCategoryBlockColor(null)"
            @click="filterByCategory(null)"
            >{{ $t('m.All') }}</el-tag
          >
          <el-tag
            size="medium"
            class="category-item"
            v-for="(category, index) in categoryList"
            :style="getCategoryBlockColor(category)"
            :key="index"
            @click="filterByCategory(category.id)"
            >{{ category.name }}</el-tag
          >
        </div>
      </section>
    </el-card>

    <el-card style="margin-top:2em">
      <vxe-table
        border="inner"
        stripe
        ref="trainingList"
        auto-resize
        :data="trainingList"
        :loading="loading"
        style="font-size: 14px !important;font-weight: 450 !important;"
      >
        <vxe-table-column
          field="rank"
          :title="$t('m.Number')"
          min-width="60"
          show-overflow
        >
        </vxe-table-column>
        <vxe-table-column
          field="title"
          :title="$t('m.Title')"
          min-width="200"
          align="center"
        >
          <template v-slot="{ row }"
            ><el-link type="primary" @click="toTraining(row.id)">{{
              row.title
            }}</el-link>
          </template>
        </vxe-table-column>

        <vxe-table-column
          field="auth"
          :title="$t('m.Auth')"
          min-width="100"
          align="center"
        >
          <template v-slot="{ row }">
            <el-tag :type="TRAINING_TYPE[row.auth]['color']" effect="dark">
              {{ $t('m.Training_' + row.auth) }}
            </el-tag>
          </template>
        </vxe-table-column>
        <vxe-table-column
          field="categoryName"
          :title="$t('m.Category')"
          min-width="130"
          align="center"
        >
          <template v-slot="{ row }">
            <el-tag
              size="medium"
              class="category-item"
              :style="
                'background-color: #fff;color: ' +
                  row.categoryColor +
                  ';border-color: ' +
                  row.categoryColor +
                  ';'
              "
              :key="index"
              >{{ row.categoryName }}</el-tag
            >
          </template>
        </vxe-table-column>

        <vxe-table-column 
          field="acCount" 
          :title="$t('m.Progress')" 
          min-width="120"
          align="center">
          <template v-slot="{ row }">
            <span>
              <el-tooltip
                effect="dark"
                :content="row.acCount + '/' + row.problemCount"
                placement="top"
              >
                <el-progress
                  :text-inside="true"
                  :stroke-width="20"
                  :percentage="getPassingRate(row.acCount, row.problemCount)"
                ></el-progress>
              </el-tooltip>
            </span>
          </template>
        </vxe-table-column>

        <vxe-table-column
          field="problemCount"
          :title="$t('m.Problem_Number')"
          min-width="70"
          align="center"
        >
        </vxe-table-column>
        <vxe-table-column
          field="author"
          :title="$t('m.Author')"
          min-width="130"
          align="center"
          show-overflow
        >
          <template v-slot="{ row }"
            ><el-link type="info" @click="goUserHome(row.author)">{{
              row.author
            }}</el-link>
          </template>
        </vxe-table-column>
        <vxe-table-column
          field="gmtModified"
          :title="$t('m.Recent_Update')"
          min-width="96"
          align="center"
          show-overflow
        >
          <template v-slot="{ row }">
            <span>
                <el-tooltip
                  :content="row.gmtModified | localtime"
                  placement="top"
                >
                  <span>{{ row.gmtModified | fromNow }}</span>
                </el-tooltip>
              </span>
          </template>
        </vxe-table-column>
      </vxe-table>
    </el-card>
    <Pagination
      :total="total"
      :pageSize="limit"
      @on-change="filterByPage"
      :current.sync="currentPage"
    ></Pagination>
  </el-row>
</template>

<script>
import api from '@/common/api';
import utils from '@/common/utils';
import { TRAINING_TYPE } from '@/common/constants';
import myMessage from '@/common/message';
import { mapGetters } from 'vuex';
const Pagination = () => import('@/components/oj/common/Pagination');
export default {
  name: 'TrainingList',
  components: {
    Pagination,
  },
  data() {
    return {
      query: {
        keyword: '',
        categoryId: null,
        auth: null,
      },
      total: 0,
      currentPage: 1,
      limit: 15,
      categoryList: [],
      trainingList: [],
      TRAINING_TYPE: {},
      loading: false,
    };
  },
  created() {
    let route = this.$route.query;
    this.currentPage = parseInt(route.currentPage) || 1;
    this.TRAINING_TYPE = Object.assign({}, TRAINING_TYPE);

    if(!this.isAuthenticated){
      setTimeout(() => {
        // 将指定列设置为隐藏状态
        this.$refs.trainingList.getColumnByField('acCount').visible = false;
        this.$refs.trainingList.refreshColumn();
      }, 200);
    }

    this.getTrainingCategoryList();
  },
  mounted() {
    this.init();
  },
  methods: {
    init() {
      let route = this.$route.query;
      this.query.keyword = route.keyword || '';
      this.query.categoryId = route.categoryId || null;
      this.query.auth = route.auth || null;
      this.getTrainingList();
    },

    filterByPage(page) {
      this.currentPage = page;
      this.filterByChange();
    },

    filterByCategory(categoryId) {
      this.query.categoryId = categoryId;
      this.filterByChange();
    },

    filterByAuthType(auth) {
      this.query.auth = auth;
      this.filterByChange();
    },

    filterByChange() {
      let query = Object.assign({}, this.query);
      query.currentPage = this.currentPage;
      this.$router.push({
        path: '/training',
        query: utils.filterEmptyValue(query),
      });
    },
    getTrainingList() {
      this.loading = true;
      let query = Object.assign({}, this.query);
      api.getTrainingList(this.currentPage, this.limit, query).then(
        (res) => {
          this.trainingList = res.data.data.records;
          this.total = res.data.data.total;
          this.loading = false;
        },
        (err) => {
          this.loading = false;
        }
      );
    },
    getTrainingCategoryList() {
      api.getTrainingCategoryList().then((res) => {
        this.categoryList = res.data.data;
      });
    },

    toTraining(trainingID) {
      if (!this.isAuthenticated) {
        myMessage.warning(this.$i18n.t('m.Please_login_first'));
        this.$store.dispatch('changeModalStatus', { visible: true });
      } else {
        this.$router.push({
          name: 'TrainingDetails',
          params: { trainingID: trainingID },
        });
      }
    },
    goUserHome(username) {
      this.$router.push({
        path: '/user-home',
        query: { username },
      });
    },

    getCategoryBlockColor(category) {
      if (category == null) {
        if (!this.query.categoryId) {
          return 'color: #fff;background-color: #409EFF;background-color: #409EFF';
        } else {
          return 'background-color: #fff;color: #409EFF;border-color: #409EFF';
        }
      }

      if (category.id == this.query.categoryId) {
        return (
          'color: #fff;background-color: ' +
          category.color +
          ';background-color: ' +
          category.color +
          ';'
        );
      } else {
        return (
          'background-color: #fff;color: ' +
          category.color +
          ';border-color: ' +
          category.color +
          ';'
        );
      }
    },
    getPassingRate(ac, total) {
      if (!total) {
        return 0;
      }
      return ((ac / total) * 100).toFixed(2);
    },
  },
  computed: {
    ...mapGetters(['isAuthenticated']),
  },
  watch: {
    $route(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.init();
      }
    },
    isAuthenticated(newVal, oldVal){
      setTimeout(() => {
        // 将指定列设置为隐藏状态
        this.$refs.trainingList.getColumnByField('acCount').visible = newVal;
        this.$refs.trainingList.refreshColumn();
      }, 200);
      this.init();
    }
  },
};
</script>

<style scoped>
section {
  display: flex;
  min-height: 3em;
  margin-bottom: 1em;
  align-items: center;
}
.find-training {
  margin-right: 1em;
  white-space: nowrap;
  font-size: 1.7em;
  margin-top: 0;
  font-family: inherit;
  font-weight: bold;
  line-height: 1.2;
  color: inherit;
}
.training-category {
  margin-right: 1.5em;
  font-weight: bolder;
  white-space: nowrap;
  font-size: 16px;
  margin-top: 8px;
}
.category-item {
  margin-right: 1em;
  margin-top: 0.5em;
  font-size: 14px;
}
.category-item:hover {
  cursor: pointer;
}
</style>
