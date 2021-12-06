<template>
  <el-row>
    <el-card shadow class="training-header">
      <div slot="header">
        <span class="panel-title">{{ training.title }}</span>
      </div>
      <el-tag :type="TRAINING_TYPE[training.auth]['color']" effect="dark">
        {{ training.auth }}
      </el-tag>
    </el-card>

    <el-tabs @tab-click="tabClick" v-model="route_name" class="card-top">
      <el-tab-pane name="TrainingDetails" lazy>
        <span slot="label"><i class="el-icon-s-home"></i>&nbsp;训练简介</span>
        <el-row :gutter="30">
          <el-col :sm="24" :md="8">
            <el-card
              v-if="passwordFormVisible"
              class="password-form-card card-top"
              style="text-align:center"
            >
              <div slot="header">
                <span class="panel-title" style="color: #e6a23c;"
                  ><i class="el-icon-warning">
                    {{ $t('m.Password_Required') }}</i
                  ></span
                >
              </div>
              <h3>
                {{ $t('m.To_Enter_Need_Password') }}
              </h3>
              <el-form>
                <el-input
                  v-model="trainingPassword"
                  type="password"
                  :placeholder="$t('m.Enter_the_contest_password')"
                  @keydown.enter.native="checkPassword"
                  style="width:70%"
                />
                <el-button
                  type="primary"
                  @click="checkPassword"
                  style="margin:5px"
                  >{{ $t('m.OK') }}</el-button
                >
              </el-form>
            </el-card>
            <el-card class="card-top">
              <div class="info-rows">
                <div>
                  <span>
                    <span>训练编号</span>
                  </span>
                  <span>
                    <span>{{ training.rank }}</span>
                  </span>
                </div>
                <div>
                  <span>
                    <span>训练类型</span>
                  </span>
                  <span>
                    <span
                      ><el-tag
                        size="medium"
                        class="category-item"
                        :style="
                          'color: #fff;background-color: ' +
                            training.categoryName +
                            ';background-color: ' +
                            training.categoryColor
                        "
                        >{{ training.categoryName }}</el-tag
                      ></span
                    >
                  </span>
                </div>
                <div>
                  <span>
                    <span>总题数</span>
                  </span>
                  <span>
                    <span>{{ training.problemCount }}</span>
                  </span>
                </div>
                <div>
                  <span>
                    <span>作者</span>
                  </span>
                  <span>
                    <span
                      ><el-link type="info">{{
                        training.author
                      }}</el-link></span
                    >
                  </span>
                </div>
                <div>
                  <span>
                    <span>最近更新</span>
                  </span>
                  <span>
                    <span>1011-11-11 11:11:11</span>
                  </span>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :sm="24" :md="16">
            <el-card class="card-top">
              <div slot="header">
                <span class="panel-title">训练简介</span>
              </div>
              <div
                v-html="descriptionHtml"
                v-highlight
                class="markdown-body"
              ></div>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <el-tab-pane
        name="ContestProblemList"
        lazy
        :disabled="contestMenuDisabled"
      >
        <span slot="label"
          ><i class="fa fa-list" aria-hidden="true"></i>&nbsp;题目列表</span
        >
        <transition name="el-zoom-in-bottom">
          <router-view v-if="route_name === 'ContestProblemList'"></router-view>
        </transition>
      </el-tab-pane>

      <el-tab-pane
        name="ContestSubmissionList"
        lazy
        :disabled="contestMenuDisabled"
      >
        <span slot="label"><i class="el-icon-menu"></i>&nbsp;提交列表</span>
        <transition name="el-zoom-in-bottom">
          <router-view
            v-if="route_name === 'ContestSubmissionList'"
          ></router-view>
        </transition>
      </el-tab-pane>

      <el-tab-pane name="ContestRank" lazy :disabled="contestMenuDisabled">
        <span slot="label"
          ><i class="fa fa-bar-chart" aria-hidden="true"></i
          >&nbsp;记录榜单</span
        >
        <transition name="el-zoom-in-bottom">
          <router-view v-if="route_name === 'TrainingRank'"></router-view>
        </transition>
      </el-tab-pane>
    </el-tabs>
  </el-row>
</template>

<script>
import { TRAINING_TYPE } from '@/common/constants';
export default {
  data() {
    return {
      training: {
        title: '【进阶】一个个进阶者才需要参加的训练',
        description: '测试。。。。。。。。。。。。。。。。。',
        categoryName: '大佬训练',
        categoryColor: '#409eff',
        rank: 1000,
        problemCount: 10,
        auth: 'Private',
        author: 'Himit_ZH',
      },
      route_name: 'TrainingDetails',
      TRAINING_TYPE: {},
      passwordFormVisible: true,
      trainingPassword: '',
    };
  },
  mounted() {
    this.TRAINING_TYPE = Object.assign({}, TRAINING_TYPE);
  },
  computed: {
    descriptionHtml() {
      if (this.training.description) {
        return this.$markDown.render(this.training.description);
      }
    },
  },
};
</script>

<style scoped>
.card-top {
  margin-top: 15px;
}
.training-header {
  text-align: center;
}

.info-rows > * {
  margin-bottom: var(--info-row-margin-bottom, 1em);
  display: flex;
  align-items: center;
  font-size: 16px;
  line-height: 1.5;
  color: rgba(0, 0, 0, 0.75);
}
.info-rows > * > *:first-child {
  flex: 1 0 auto;
  text-align: left;
}
.info-rows > :last-child {
  margin-bottom: 0;
}
/deep/ .el-card__header {
  border-bottom: 0px;
  padding-bottom: 0px;
}
/deep/.el-tabs__nav-wrap {
  background: #fff;
  border-radius: 3px;
}
/deep/.el-tabs--top .el-tabs__item.is-top:nth-child(2) {
  padding-left: 20px;
}
/deep/.el-tabs__header {
  margin-bottom: 0 !important;
}
</style>
