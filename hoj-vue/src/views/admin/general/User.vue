<template>
  <div class="view">
    <el-card>
      <div slot="header">
        <span class="panel-title home-title">{{ $t('m.General_User') }}</span>
        <div class="filter-row">
          <span>
            <el-button
              type="danger"
              icon="el-icon-delete-solid"
              @click="deleteUsers(null)"
              size="small"
              >{{ $t('m.Delete') }}
            </el-button>
          </span>
          <span>
            <vxe-input
              v-model="keyword"
              :placeholder="$t('m.Enter_keyword')"
              type="search"
              size="medium"
              @search-click="filterByKeyword"
              @keyup.enter.native="filterByKeyword"
            ></vxe-input>
          </span>
          <span>
            <el-switch
              v-model="onlyAdmin"
              :active-text="$t('m.OnlyAdmin')"
              :width="40"
              @change="filterByAdmin"
              :inactive-text="$t('m.All')"
            >
            </el-switch>
          </span>
        </div>
      </div>
      <vxe-table
        stripe
        auto-resize
        :data="userList"
        ref="xTable"
        :loading="loadingTable"
        :checkbox-config="{ labelField: 'id', highlight: true, range: true, checkMethod: checCheckboxkMethod }"
        @checkbox-change="handleSelectionChange"
        @checkbox-all="handlechangeAll"
      >
        <vxe-table-column type="checkbox" width="60"></vxe-table-column>
        <vxe-table-column
          field="username"
          :title="$t('m.User')"
          min-width="200"
          show-overflow
        >
          <template v-slot="{ row }">
            <span>{{ row.username }}</span>
            <span style="margin-left:2px">
              <el-tag
                effect="dark"
                size="small"
                v-if="row.titleName"
                :color="row.titleColor"
              >
                {{ row.titleName }}
              </el-tag>
            </span>
          </template>
        </vxe-table-column>
        <vxe-table-column
          field="realname"
          :title="$t('m.RealName')"
          min-width="140"
          show-overflow
        ></vxe-table-column>
        <vxe-table-column
          field="email"
          :title="$t('m.Email')"
          min-width="150"
          show-overflow
        ></vxe-table-column>
        <vxe-table-column
          field="gmtCreate"
          :title="$t('m.Created_Time')"
          min-width="150"
        >
          <template v-slot="{ row }">
            {{ row.gmtCreate | localtime }}
          </template>
        </vxe-table-column>
        <vxe-table-column
          field="role"
          :title="$t('m.User_Type')"
          min-width="100"
        >
          <template v-slot="{ row }">
            {{ getRole(row.roles) | parseRole }}
          </template>
        </vxe-table-column>
        <vxe-table-column
          field="status"
          :title="$t('m.Status')"
          min-width="100"
        >
          <template v-slot="{ row }">
            <el-tag effect="dark" color="#19be6b" v-if="row.status == 0">{{
              $t('m.Normal')
            }}</el-tag>
            <el-tag effect="dark" color="#ed3f14" v-else>{{
              $t('m.Disable')
            }}</el-tag>
          </template>
        </vxe-table-column>
        <vxe-table-column :title="$t('m.Option')" min-width="150">
          <template v-slot="{ row }">
            <el-tooltip
              effect="dark"
              :content="$t('m.Edit_User')"
              placement="top"
            >
              <el-button
                icon="el-icon-edit-outline"
                size="mini"
                @click.native="openUserDialog(row)"
                type="primary"
              >
              </el-button>
            </el-tooltip>
            <el-tooltip
              effect="dark"
              :content="$t('m.Delete_User')"
              placement="top"
            >
              <el-button
                icon="el-icon-delete-solid"
                size="mini"
                @click.native="deleteUsers([row.uid])"
                type="danger"
              >
              </el-button>
            </el-tooltip>
          </template>
        </vxe-table-column>
      </vxe-table>
      <div class="panel-options">
        <el-pagination
          class="page"
          layout="prev, pager, next, sizes"
          @current-change="currentChange"
          :page-size="pageSize"
          :total="total"
          @size-change="onPageSizeChange"
          :page-sizes="[10, 30, 50, 100]"
        >
        </el-pagination>
      </div>
    </el-card>

    <!-- 导入csv用户数据 -->
    <el-card style="margin-top:20px">
      <div slot="header">
        <span class="panel-title home-title">{{ $t('m.Import_User') }}</span>
      </div>
      <p>1. {{ $t('m.Import_User_Tips1') }}</p>
      <p>2. {{ $t('m.Import_User_Tips2') }}</p>
      <p>3. {{ $t('m.Import_User_Tips3') }}</p>
      <p>4. {{ $t('m.Import_User_Tips4') }}</p>
      <p>5. {{ $t('m.Import_User_Tips5') }}</p>
      <el-upload
        v-if="!uploadUsers.length"
        action=""
        :show-file-list="false"
        accept=".csv"
        :before-upload="handleUsersCSV"
      >
        <el-button size="small" icon="el-icon-folder-opened" type="primary">{{
          $t('m.Choose_File')
        }}</el-button>
      </el-upload>
      <template v-else>
        <vxe-table :data="uploadUsersPage" stripe auto-resize>
          <vxe-table-column
            :title="$t('m.Username')"
            field="username"
            min-width="96"
            show-overflow
          >
            <template v-slot="{ row }">
              {{ row[0] }}
            </template>
          </vxe-table-column>
          <vxe-table-column
            :title="$t('m.Password')"
            field="password"
            min-width="130"
            show-overflow
          >
            <template v-slot="{ row }">
              {{ row[1] }}
            </template>
          </vxe-table-column>
          <vxe-table-column
            :title="$t('m.Email')"
            field="email"
            min-width="120"
            show-overflow
          >
            <template v-slot="{ row }">
              {{ row[2] }}
            </template>
          </vxe-table-column>
          <vxe-table-column
            :title="$t('m.RealName')"
            field="realname"
            min-width="150"
            show-overflow
          >
            <template v-slot="{ row }">
              {{ row[3] }}
            </template>
          </vxe-table-column>
          <vxe-table-column
            :title="$t('m.Gender')"
            field="gender"
            min-width="60"
            show-overflow
          >
            <template v-slot="{ row }">
              {{ row[4] }}
            </template>
          </vxe-table-column>
          <vxe-table-column
            :title="$t('m.Nickname')"
            field="nickname"
            min-width="100"
            show-overflow
          >
            <template v-slot="{ row }">
              {{ row[5] }}
            </template>
          </vxe-table-column>
          <vxe-table-column
            :title="$t('m.School')"
            field="school"
            min-width="100"
            show-overflow
          >
            <template v-slot="{ row }">
              {{ row[6] }}
            </template>
          </vxe-table-column>
        </vxe-table>

        <div class="panel-options">
          <el-button
            type="primary"
            size="small"
            icon="el-icon-upload"
            @click="handleUsersUpload"
            >{{ $t('m.Upload_All') }}
          </el-button>
          <el-button
            type="danger"
            size="small"
            icon="el-icon-delete"
            @click="handleResetData"
            >{{ $t('m.Clear_All') }}
          </el-button>
          <el-pagination
            class="page"
            layout="prev, pager, next"
            :page-size="uploadUsersPageSize"
            :current-page.sync="uploadUsersCurrentPage"
            :total="uploadUsers.length"
          >
          </el-pagination>
        </div>
      </template>
    </el-card>

    <!--生成用户数据-->
    <el-card style="margin-top:20px">
      <div slot="header">
        <span class="panel-title home-title">{{ $t('m.Generate_User') }}</span>
      </div>
      <el-form
        :model="formGenerateUser"
        ref="formGenerateUser"
        :rules="formGenerateRules"
      >
        <el-row :gutter="10">
          <el-col :md="5" :xs="24">
            <el-form-item :label="$t('m.Prefix')" prop="prefix">
              <el-input
                v-model="formGenerateUser.prefix"
                placeholder="Prefix"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :md="5" :xs="24">
            <el-form-item :label="$t('m.Suffix')" prop="suffix">
              <el-input
                v-model="formGenerateUser.suffix"
                placeholder="Suffix"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :md="5" :xs="24">
            <el-form-item :label="$t('m.Start_Number')" prop="number_from">
              <el-input-number
                v-model="formGenerateUser.number_from"
                style="width: 100%"
              ></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :md="5" :xs="24">
            <el-form-item :label="$t('m.End_Number')" prop="number_to">
              <el-input-number
                v-model="formGenerateUser.number_to"
                style="width: 100%"
              ></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :md="4" :xs="24">
            <el-form-item
              :label="$t('m.Password_Length')"
              prop="password_length"
            >
              <el-input
                v-model.number="formGenerateUser.password_length"
                :placeholder="$t('m.Password_Length')"
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item>
          <el-button
            type="primary"
            @click="generateUser"
            icon="fa fa-users"
            :loading="loadingGenerate"
            size="small"
          >
            {{ $t('m.Generate_and_Export') }}
          </el-button>
          <span
            class="userPreview"
            v-if="formGenerateUser.number_from <= formGenerateUser.number_to"
          >
            {{ $t('m.The_usernames_will_be') }}
            {{
              formGenerateUser.prefix +
                formGenerateUser.number_from +
                formGenerateUser.suffix
            }},
            <span
              v-if="
                formGenerateUser.number_from + 1 < formGenerateUser.number_to
              "
            >
              {{
                formGenerateUser.prefix +
                  (formGenerateUser.number_from + 1) +
                  formGenerateUser.suffix +
                  '...'
              }}
            </span>
            <span
              v-if="
                formGenerateUser.number_from + 1 <= formGenerateUser.number_to
              "
            >
              {{
                formGenerateUser.prefix +
                  formGenerateUser.number_to +
                  formGenerateUser.suffix
              }}
            </span>
          </span>
        </el-form-item>
      </el-form>
    </el-card>

    <!--编辑用户的对话框-->
    <el-dialog
      :title="$t('m.User')"
      :visible.sync="showUserDialog"
      width="350px"
    >
      <el-form
        :model="selectUser"
        label-width="100px"
        label-position="left"
        :rules="updateUserRules"
        ref="updateUser"
      >
        <el-row :gutter="10">
          <el-col :span="24">
            <el-form-item :label="$t('m.Username')" required prop="username">
              <el-input v-model="selectUser.username" size="small"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="$t('m.RealName')" prop="realname">
              <el-input v-model="selectUser.realname" size="small"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="$t('m.Email')" prop="email">
              <el-input v-model="selectUser.email" size="small"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="$t('m.Set_New_PWD')">
              <el-switch
                :active-value="true"
                :inactive-value="false"
                v-model="selectUser.setNewPwd"
              >
              </el-switch>
            </el-form-item>
          </el-col>
          <el-col :span="24" v-if="selectUser.setNewPwd == 1">
            <el-form-item
              :label="$t('m.General_New_Password')"
              required
              prop="password"
            >
              <el-input
                v-model="selectUser.password"
                :placeholder="$t('m.General_New_Password')"
                size="small"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="$t('m.User_Type')">
              <el-select v-model="selectUser.type" size="small">
                <el-option
                  label="超级管理员"
                  :value="1000"
                  :key="1000"
                ></el-option>
                <el-option
                  label="题目管理员"
                  :value="1008"
                  :key="1008"
                ></el-option>
                <el-option
                  label="普通管理员"
                  :value="1001"
                  :key="1001"
                ></el-option>
                <el-option
                  label="用户(默认)"
                  :value="1002"
                  :key="1002"
                ></el-option>
                <el-option
                  label="用户(禁止提交)"
                  :value="1003"
                  :key="1003"
                ></el-option>
                <el-option
                  label="用户(禁止发讨论)"
                  :value="1004"
                  :key="1004"
                ></el-option>
                <el-option
                  label="用户(禁言)"
                  :value="1005"
                  :key="1005"
                ></el-option>
                <el-option
                  label="用户(禁止提交&禁止发讨论)"
                  :value="1006"
                  :key="1006"
                ></el-option>
                <el-option
                  label="用户(禁止提交&禁言)"
                  :value="1007"
                  :key="1007"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="$t('m.Title_Name')">
              <el-input v-model="selectUser.titleName" size="small"></el-input>
            </el-form-item>
            <el-form-item :label="$t('m.Title_Color')">
              <el-color-picker
                v-model="selectUser.titleColor"
              ></el-color-picker>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="$t('m.Status')">
              <el-switch
                :active-value="0"
                :inactive-value="1"
                :active-text="$t('m.Normal')"
                :inactive-text="$t('m.Disable')"
                v-model="selectUser.status"
              >
              </el-switch>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button type="danger" @click.native="showUserDialog = false">{{
          $t('m.Cancel')
        }}</el-button>
        <el-button type="primary" @click.native="saveUser">{{
          $t('m.OK')
        }}</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import papa from 'papaparse'; // csv插件
import api from '@/common/api';
import utils from '@/common/utils';
import myMessage from '@/common/message';
export default {
  name: 'user',
  data() {
    const CheckTogtFrom = (rule, value, callback) => {
      if (value < this.formGenerateUser.number_from) {
        callback(
          new Error(
            this.$i18n.t(
              'm.The_end_number_cannot_be_less_than_the_start_number'
            )
          )
        );
      }
      callback();
    };
    const CheckPwdLength = (rule, value, callback) => {
      if (value < 6 || value > 25) {
        callback(
          new Error(
            this.$i18n.t(
              'm.Please_select_6_to_25_characters_for_password_length'
            )
          )
        );
      }
      callback();
    };
    const CheckUsernameNotExist = (rule, value, callback) => {
      api.checkUsernameOrEmail(value, undefined).then(
        (res) => {
          if (
            res.data.data.username === true &&
            value != this.selectUser.username
          ) {
            callback(new Error(this.$i18n.t('m.The_username_already_exists')));
          } else {
            callback();
          }
        },
        (_) => callback()
      );
    };
    const CheckEmailNotExist = (rule, value, callback) => {
      api.checkUsernameOrEmail(undefined, value).then(
        (res) => {
          if (res.data.data.email === true && value != this.selectUser.email) {
            callback(new Error(this.$i18n.t('m.The_email_already_exists')));
          } else {
            callback();
          }
        },
        (_) => callback()
      );
    };
    return {
      // 一页显示的用户数
      pageSize: 10,
      // 用户总数
      total: 0,
      // 数据库查询的用户列表
      userList: [],
      uploadUsers: [],
      uploadUsersPage: [],
      uploadUsersCurrentPage: 1,
      uploadUsersPageSize: 15,
      // 搜索关键字
      keyword: '',
      // 是否显示用户对话框
      showUserDialog: false,
      onlyAdmin: false,

      // 当前用户model
      selectUser: {
        uid: '',
        username: '',
        realname: '',
        email: '',
        password: '',
        type: 1002,
        status: 0,
        setNewPwd: false,
        titleName: '',
        titleColor: '',
      },
      updateUserRules: {
        username: [
          { required: true, message: 'Username is required', trigger: 'blur' },
          {
            validator: CheckUsernameNotExist,
            trigger: 'blur',
            message: this.$i18n.t('m.The_username_already_exists'),
          },
          {
            max: 255,
            message: this.$i18n.t('m.Username_Check_Max'),
            trigger: 'blur',
          },
        ],
        realname: [
          {
            max: 255,
            trigger: 'blur',
          },
        ],
        email: [
          {
            type: 'email',
            message: this.$i18n.t('m.Email_Check_Format'),
            trigger: 'blur',
          },
          {
            validator: CheckEmailNotExist,
            message: this.$i18n.t('m.The_email_already_exists'),
            trigger: 'blur',
          },
        ],
      },
      loadingTable: false,
      loadingGenerate: false,
      // 当前页码
      currentPage: 1,
      selectedUsers: [],
      formGenerateUser: {
        prefix: '',
        suffix: '',
        number_from: 0,
        number_to: 10,
        password_length: 6,
      },
      formGenerateRules: {
        number_from: [
          {
            required: true,
            message: this.$i18n.t('m.Start_Number_Required'),
            trigger: 'blur',
          },
        ],
        number_to: [
          {
            required: true,
            message: this.$i18n.t('m.End_Number_Required'),
            trigger: 'blur',
          },
          { validator: CheckTogtFrom, trigger: 'blur' },
        ],
        password_length: [
          {
            required: true,
            message: this.$i18n.t('m.Password_Check_Required'),
            trigger: 'blur',
          },
          {
            type: 'number',
            message: this.$i18n.t('m.Password_Length_Checked'),
            trigger: 'blur',
          },
          { validator: CheckPwdLength, trigger: 'blur' },
        ],
      },
    };
  },
  mounted() {
    this.getUserList(1);
  },
  methods: {
    // 切换页码回调
    currentChange(page) {
      this.currentPage = page;
      this.getUserList(page);
    },
    onPageSizeChange(pageSize) {
      this.pageSize = pageSize;
      this.getUserList(this.currentPage);
    },
    // 提交修改用户的信息
    saveUser() {
      this.$refs['updateUser'].validate((valid) => {
        if (valid) {
          api
            .admin_editUser(this.selectUser)
            .then((res) => {
              // 更新列表
              myMessage.success(this.$i18n.t('m.Update_Successfully'));
              this.getUserList(this.currentPage);
            })
            .then(() => {
              this.showUserDialog = false;
            })
            .catch(() => {});
        }
      });
    },
    filterByKeyword() {
      this.currentChange(1);
    },
    filterByAdmin() {
      this.currentChange(1);
    },
    getRole(roles) {
      return roles[0]['id'];
    },
    // 打开用户对话框
    openUserDialog(row) {
      this.showUserDialog = true;
      this.selectUser.uid = row.uid;
      this.selectUser.username = row.username;
      this.selectUser.realname = row.realname;
      this.selectUser.email = row.email;
      this.selectUser.setNewPwd = false;
      this.selectUser.password = '';
      this.selectUser.type = this.getRole(row.roles);
      this.selectUser.status = row.status;
      this.selectUser.titleName = row.titleName;
      this.selectUser.titleColor = row.titleColor;
    },
    // 获取用户列表
    getUserList(page) {
      this.loadingTable = true;
      api
        .admin_getUserList(page, this.pageSize, this.keyword, this.onlyAdmin)
        .then(
          (res) => {
            this.loadingTable = false;
            this.total = res.data.data.total;
            this.userList = res.data.data.records;
          },
          (res) => {
            this.loadingTable = false;
          }
        );
    },
    deleteUsers(ids) {
      if (!ids) {
        ids = this.selectedUsers;
      }
      if (ids.length > 0) {
        this.$confirm(this.$i18n.t('m.Delete_User_Tips'), 'Tips', {
          confirmButtonText: this.$i18n.t('m.OK'),
          cancelButtonText: this.$i18n.t('m.Cancel'),
          type: 'warning',
        }).then(
          () => {
            api
              .admin_deleteUsers(ids)
              .then((res) => {
                myMessage.success(this.$i18n.$t('m.Delete_successfully'));
                this.selectedUsers = [];
                this.getUserList(this.currentPage);
              })
              .catch(() => {
                this.selectedUsers = [];
                this.getUserList(this.currentPage);
              });
          },
          () => {}
        );
      } else {
        myMessage.warning(
          this.$i18n.t('m.The_number_of_users_selected_cannot_be_empty')
        );
      }
    },
    checCheckboxkMethod({ row }){
      return row.uid != this.userInfo.uid;
    },
    // 用户表部分勾选 改变选中的内容
    handleSelectionChange({ records }) {
      this.selectedUsers = [];
      for (let num = 0; num < records.length; num++) {
        this.selectedUsers.push(records[num].uid);
      }
    },
    // 一键全部选中，改变选中的内容列表
    handlechangeAll() {
      let userList = this.$refs.xTable.getCheckboxRecords();
      this.selectedUsers = [];
      for (let num = 0; num < userList.length; num++) {
        this.selectedUsers.push(userList[num].uid);
      }
    },
    generateUser() {
      this.$refs['formGenerateUser'].validate((valid) => {
        if (!valid) {
          myMessage.error(this.$i18n.t('m.Error_Please_check_your_choice'));
          return;
        }
        this.loadingGenerate = true;
        let data = Object.assign({}, this.formGenerateUser);
        api
          .admin_generateUser(data)
          .then((res) => {
            this.loadingGenerate = false;
            let url = '/api/file/generate-user-excel?key=' + res.data.data.key;
            utils.downloadFile(url).then(() => {
              this.$alert(this.$i18n.t('m.Generate_User_Success'), 'Tips');
            });
            this.getUserList(1);
          })
          .catch(() => {
            this.loadingGenerate = false;
          });
      });
    },
    handleUsersCSV(file) {
      papa.parse(file, {
        complete: (results) => {
          let data = results.data.filter((user) => {
            return user[0] && user[1];
          });
          let delta = results.data.length - data.length;
          if (delta > 0) {
            myMessage.warning(
              delta + this.$i18n.t('m.Generate_Skipped_Reason')
            );
          }
          this.uploadUsersCurrentPage = 1;
          this.uploadUsers = data;
          this.uploadUsersPage = data.slice(0, this.uploadUsersPageSize);
        },
        error: (error) => {
          myMessage.error(error);
        },
      });
    },
    handleUsersUpload() {
      api
        .admin_importUsers(this.uploadUsers)
        .then((res) => {
          this.getUserList(1);
          this.handleResetData();
          myMessage.success(this.$i18n.t('m.Upload_Users_Successfully'));
        })
        .catch(() => {});
    },
    handleResetData() {
      this.uploadUsers = [];
    },
  },
  computed: {
    selectedUserIDs() {
      let ids = [];
      for (let user of this.selectedUsers) {
        ids.push(user.id);
      }
      return ids;
    },
    userInfo() {
      return this.$store.getters.userInfo;
    },
  },
  watch: {
    uploadUsersCurrentPage(page) {
      this.uploadUsersPage = this.uploadUsers.slice(
        (page - 1) * this.uploadUsersPageSize,
        page * this.uploadUsersPageSize
      );
    },
  },
};
</script>

<style scoped>
.import-user-icon {
  color: #555555;
  margin-left: 4px;
}

.userPreview {
  padding-left: 10px;
}

/deep/ .el-tag--dark {
  border-color: #fff;
}
/deep/.el-dialog__body {
  padding-bottom: 0;
}
/deep/.el-form-item {
  margin-bottom: 10px !important;
}
.notification p {
  margin: 0;
  text-align: left;
}
.filter-row {
  margin-top: 10px;
}
@media screen and (max-width: 768px) {
  .filter-row span {
    margin-right: 5px;
  }
}
@media screen and (min-width: 768px) {
  .filter-row span {
    margin-right: 20px;
  }
}
</style>
