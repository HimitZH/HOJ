<template>
  <div class="container">
    <div class="avatar-container">
      <avatar
        :username="profile.username"
        :inline="true"
        :size="130"
        color="#FFF"
        :src="profile.avatar"
      ></avatar>
    </div>
    <el-card class="box-card">
      <div class="recent-login">
        <el-tooltip
          :content="profile.recentLoginTime | localtime"
          placement="top"
        >
          <el-tag type="success" effect="plain" size="medium">
            <i class="fa fa-circle">
              {{ $t('m.Recent_login_time')
              }}{{ profile.recentLoginTime | fromNow }}</i
            >
          </el-tag>
        </el-tooltip>
      </div>
      <div class="user-info">
        <p>
          <span class="emphasis"
            ><i class="fa fa-user-circle-o" aria-hidden="true"></i>
            {{ profile.username }}</span
          >
          <span class="gender-male male" v-if="profile.gender == 'male'">
            <i class="fa fa-mars"></i>
          </span>
          <span
            class="gender-male female"
            v-else-if="profile.gender == 'female'"
          >
            <i class="fa fa-venus"></i>
          </span>
        </p>
        <p v-if="profile.titleName">
          <span>
            <el-tag effect="dark" size="small" :color="profile.titleColor">
              {{ profile.titleName }}
            </el-tag>
          </span>
        </p>
        <p v-if="profile.nickname">
          <span>
            <el-tag
              effect="plain"
              size="small"
              :type="nicknameColor(profile.nickname)"
            >
              {{ profile.nickname }}
            </el-tag>
          </span>
        </p>
        <span class="default-info" v-if="profile.school"
          ><i class="fa fa-graduation-cap" aria-hidden="true"></i>
          {{ profile.school }}</span
        >
        <span id="icons">
          <a
            :href="profile.github"
            v-if="profile.github"
            class="icon"
            target="_blank"
          >
            <i class="fa fa-github"> {{ $t('m.Github') }}</i>
          </a>
          <a
            :href="profile.blog"
            v-if="profile.blog"
            class="icon"
            target="_blank"
          >
            <i class="fa fa-share-alt-square"> {{ $t('m.Blog') }}</i>
          </a>
        </span>
        <hr id="split" />
        <el-row :gutter="12">
          <el-col :md="6" :sm="24">
            <el-card shadow="always" class="submission">
              <p>
                <i class="fa fa-th" aria-hidden="true"></i>
                {{ $t('m.UserHome_Submissions') }}
              </p>
              <p class="data-number">{{ profile.total }}</p>
            </el-card>
          </el-col>
          <el-col :md="6" :sm="24">
            <el-card shadow="always" class="solved">
              <p>
                <i class="fa fa-check-circle" aria-hidden="true"></i>
                {{ $t('m.UserHome_Solved') }}
              </p>
              <p class="data-number">{{ profile.solvedList.length }}</p>
            </el-card>
          </el-col>
          <el-col :md="6" :sm="24">
            <el-card shadow="always" class="score">
              <p>
                <i class="fa fa-star" aria-hidden="true"></i>
                {{ $t('m.UserHome_Score') }}
              </p>
              <p class="data-number">{{ getSumScore(profile.scoreList) }}</p>
            </el-card>
          </el-col>
          <el-col :md="6" :sm="24">
            <el-card shadow="always" class="rating">
              <p>
                <i class="fa fa-user-secret" aria-hidden="true"></i>
                {{ $t('m.UserHome_Rating') }}
              </p>
              <p class="data-number">
                {{ profile.rating ? profile.rating : '--' }}
              </p>
            </el-card>
          </el-col>
        </el-row>
        <el-tabs type="card" style="margin-top:1rem;">
          <el-tab-pane :label="$t('m.Personal_Profile')">
            <div class="signature-body">
              <div
                class="markdown-body"
                v-dompurify-html="contentHtml"
                v-highlight
                v-if="contentHtml"
              ></div>
              <div class="markdown-body" v-else>
                <p>{{ $t('m.Not_set_yet') }}</p>
              </div>
            </div>
          </el-tab-pane>
          <el-tab-pane :label="$t('m.UserHome_Solved_Problems')"
            ><div id="problems">
              <div v-if="profile.solvedList.length">
                {{ $t('m.List_Solved_Problems') }}
                <el-button
                  type="primary"
                  icon="el-icon-refresh"
                  circle
                  size="mini"
                  @click="freshProblemDisplayID"
                ></el-button>
              </div>
              <p v-else>{{ $t('m.UserHome_Not_Data') }}</p>
              <div class="btns">
                <div
                  class="problem-btn"
                  v-for="problemID of profile.solvedList"
                  :key="problemID"
                >
                  <el-button type="success" @click="goProblem(problemID)">{{
                    problemID
                  }}</el-button>
                </div>
              </div>
            </div></el-tab-pane
          >
        </el-tabs>
      </div>
    </el-card>
  </div>
</template>
<script>
import { mapActions } from 'vuex';
import api from '@/common/api';
import myMessage from '@/common/message';
import { addCodeBtn } from '@/common/codeblock';
import Avatar from 'vue-avatar';
export default {
  components: {
    Avatar,
  },
  data() {
    return {
      profile: {
        username: '',
        nickname: '',
        gender: '',
        avatar: '',
        school: '',
        signature: '',
        total: 0,
        rating: 0,
        score: 0,
        solvedList: [],
      },
    };
  },
  mounted() {
    this.init();
  },
  methods: {
    ...mapActions(['changeDomTitle']),
    init() {
      let uid = this.$route.query.uid;
      let username = this.$route.query.username;
      api.getUserInfo(uid, username).then((res) => {
        this.changeDomTitle({ title: res.data.username });
        this.profile = res.data.data;
        this.$nextTick((_) => {
          addCodeBtn();
        });
      });
    },
    goProblem(problemID) {
      this.$router.push({
        name: 'ProblemDetails',
        params: { problemID: problemID },
      });
    },
    freshProblemDisplayID() {
      this.init();
      myMessage.success(this.$i18n.t('m.Update_Successfully'));
    },
    getSumScore(scoreList) {
      if (scoreList) {
        var sum = 0;
        for (let i = 0; i < scoreList.length; i++) {
          sum += scoreList[i];
        }
        return sum;
      }
    },
    nicknameColor(nickname) {
      let typeArr = ['', 'success', 'info', 'danger', 'warning'];
      let index = nickname.length % 5;
      return typeArr[index];
    },
  },
  watch: {
    $route(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.init();
      }
    },
  },
  computed: {
    contentHtml() {
      if (this.profile.signature) {
        return this.$markDown.render(this.profile.signature);
      } else {
        return null;
      }
    },
  },
};
</script>

<style scoped>
.submission {
  background: skyblue;
  color: #fff;
  font-size: 14px;
}
.solved {
  background: #67c23a;
  color: #fff;
  font-size: 14px;
}
.score {
  background: #e6a23c;
  color: #fff;
  font-size: 14px;
}
.rating {
  background: #dd6161;
  color: #fff;
  font-size: 14px;
}
.default-info {
  font-size: 13px;
  padding-right: 5px;
}
.data-number {
  font-size: 20px;
  font-weight: 600;
}

.container p {
  margin-top: 8px;
  margin-bottom: 8px;
}

@media screen and (max-width: 1080px) {
  .container {
    position: relative;
    width: 100%;
    margin-top: 110px;
    text-align: center;
  }
  .container .avatar-container {
    position: absolute;
    left: 50%;
    transform: translate(-50%);
    z-index: 1;
    margin-top: -90px;
  }
  .container .recent-login {
    text-align: center;
    margin-top: 30px;
  }
}

@media screen and (min-width: 1080px) {
  .container {
    position: relative;
    width: 75%;
    margin-top: 160px;
    text-align: center;
  }
  .container .avatar-container {
    position: absolute;
    left: 50%;
    transform: translate(-50%);
    z-index: 1;
    margin-top: -8%;
  }
  .container .recent-login {
    position: absolute;
    right: 1rem;
    top: 0.5rem;
  }
  .container .user-info {
    margin-top: 50px;
  }
}
.container .avatar {
  width: 140px;
  height: 140px;
  border-radius: 50%;
  box-shadow: 0 1px 1px 0;
}

.container .emphasis {
  font-size: 20px;
  font-weight: 600;
}
#problems {
  margin-top: 40px;
  padding-left: 30px;
  padding-right: 30px;
  font-size: 18px;
}
.btns {
  margin-top: 15px;
}
.problem-btn {
  display: inline-block;
  margin: 5px;
}

#icons .icon {
  font-size: 13px !important;
  padding: 0 10px;
  color: #2196f3;
}
.signature-body {
  background: #fff;
  overflow: hidden;
  width: 100%;
  padding: 10px 10px;
  text-align: left;
  font-size: 14px;
  line-height: 1.6;
}
.gender-male {
  font-size: 16px;
  margin-left: 5px;
  color: white;
  border-radius: 4px;
  padding: 2px;
}
.male {
  background-color: #409eff;
}
.female {
  background-color: pink;
}
</style>
