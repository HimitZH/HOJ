<template>
  <div class="view">
    <el-card>
      <div slot="header">
        <span class="panel-title home-title">User</span>
        <div class="filter-row">
          <span>
            <el-button
              type="danger"
              icon="el-icon-delete-solid"
              @click="deleteUsers(null)"
              size="small"
              >Delete
            </el-button>
          </span>
          <span>
            <vxe-input
              v-model="keyword"
              placeholder="Enter keyword"
              type="search"
              size="medium"
              @search-click="filterByKeyword"
              @keyup.enter.native="filterByKeyword"
            ></vxe-input>
          </span>
        </div>
      </div>
      <vxe-table
        stripe
        auto-resize
        :data="userList"
        ref="xTable"
        :loading="loadingTable"
        :checkbox-config="{ labelField: 'id', highlight: true, range: true }"
        @checkbox-change="handleSelectionChange"
        @checkbox-all="handlechangeAll"
      >
        <vxe-table-column type="checkbox" width="60"></vxe-table-column>
        <vxe-table-column
          field="uid"
          title="UUID"
          width="130"
        ></vxe-table-column>
        <vxe-table-column
          field="username"
          title="Username"
          min-width="140"
        ></vxe-table-column>
        <vxe-table-column
          field="realname"
          title="Real Name"
          min-width="140"
        ></vxe-table-column>
        <vxe-table-column
          field="email"
          title="Email"
          min-width="150"
        ></vxe-table-column>
        <vxe-table-column field="gmtCreate" title="Create Time" min-width="150">
          <template v-slot="{ row }">
            {{ row.gmtCreate | localtime }}
          </template>
        </vxe-table-column>
        <vxe-table-column field="role" title="User Type" min-width="100">
          <template v-slot="{ row }">
            {{ getRole(row.roles) | parseRole }}
          </template>
        </vxe-table-column>
        <vxe-table-column field="status" title="Status" min-width="100">
          <template v-slot="{ row }">
            <el-tag effect="dark" color="#19be6b" v-if="row.status == 0"
              >正常</el-tag
            >
            <el-tag effect="dark" color="#ed3f14" v-else>禁用</el-tag>
          </template>
        </vxe-table-column>
        <vxe-table-column title="Option" min-width="150">
          <template v-slot="{ row }">
            <el-tooltip effect="dark" content="编辑用户" placement="top">
              <el-button
                icon="el-icon-edit-outline"
                size="mini"
                @click.native="openUserDialog(row)"
                type="primary"
              >
              </el-button>
            </el-tooltip>
            <el-tooltip effect="dark" content="删除用户" placement="top">
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
          layout="prev, pager, next"
          @current-change="currentChange"
          :page-size="pageSize"
          :total="total"
        >
        </el-pagination>
      </div>
    </el-card>

    <!-- 导入csv用户数据 -->
    <el-card style="margin-top:20px">
      <div slot="header">
        <span class="panel-title home-title">Import Users</span>
      </div>
      <p>1. 用户数据导入仅支持csv格式的用户数据。</p>
      <p>
        2. 共三列数据:
        用户名，密码，邮箱，任一列不能为空，否则该行数据可能导入失败。
      </p>
      <p>3. 第一行不必写(“用户名”，“密码”，“邮箱”)这三个列名。</p>
      <p>4. 请导入保存为UTF-8编码的文件，否则中文可能会乱码。</p>
      <el-upload
        v-if="!uploadUsers.length"
        action=""
        :show-file-list="false"
        accept=".csv"
        :before-upload="handleUsersCSV"
      >
        <el-button size="small" icon="el-icon-folder-opened" type="primary"
          >Choose File</el-button
        >
      </el-upload>
      <template v-else>
        <vxe-table :data="uploadUsersPage" stripe auto-resize>
          <vxe-table-column title="Username" field="username" min-width="150">
            <template v-slot="{ row }">
              {{ row[0] }}
            </template>
          </vxe-table-column>
          <vxe-table-column title="Password" field="password" min-width="150">
            <template v-slot="{ row }">
              {{ row[1] }}
            </template>
          </vxe-table-column>
          <vxe-table-column title="Email" field="email" min-width="150">
            <template v-slot="{ row }">
              {{ row[2] }}
            </template>
          </vxe-table-column>
        </vxe-table>

        <div class="panel-options">
          <el-button
            type="primary"
            size="small"
            icon="el-icon-upload"
            @click="handleUsersUpload"
            >Upload All
          </el-button>
          <el-button
            type="danger"
            size="small"
            icon="el-icon-delete"
            @click="handleResetData"
            >Clear All
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
        <span class="panel-title home-title">Generate Users</span>
      </div>
      <el-form
        :model="formGenerateUser"
        ref="formGenerateUser"
        :rules="formGenerateRules"
      >
        <el-row :gutter="10">
          <el-col :md="5" :xs="24">
            <el-form-item label="Prefix" prop="prefix">
              <el-input
                v-model="formGenerateUser.prefix"
                placeholder="Prefix"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :md="5" :xs="24">
            <el-form-item label="Suffix" prop="suffix">
              <el-input
                v-model="formGenerateUser.suffix"
                placeholder="Suffix"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :md="5" :xs="24">
            <el-form-item label="Start Number" prop="number_from">
              <el-input-number
                v-model="formGenerateUser.number_from"
                style="width: 100%"
              ></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :md="5" :xs="24">
            <el-form-item label="End Number" prop="number_to">
              <el-input-number
                v-model="formGenerateUser.number_to"
                style="width: 100%"
              ></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :md="4" :xs="24">
            <el-form-item label="Password Length" prop="password_length">
              <el-input
                v-model.number="formGenerateUser.password_length"
                placeholder="Password Length"
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
            Generate & Export
          </el-button>
          <span
            class="userPreview"
            v-if="formGenerateUser.number_from <= formGenerateUser.number_to"
          >
            The usernames will be
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
    <el-dialog title="User Info" :visible.sync="showUserDialog" width="350px">
      <el-form
        :model="selectUser"
        label-width="100px"
        label-position="left"
        :rules="updateUserRules"
        ref="updateUser"
      >
        <el-row :gutter="10">
          <el-col :span="24">
            <el-form-item label="Username" required prop="username">
              <el-input v-model="selectUser.username"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="Real Name" prop="realname">
              <el-input v-model="selectUser.realname"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="Email" prop="email">
              <el-input v-model="selectUser.email"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="Set NewPwd">
              <el-switch
                :active-value="true"
                :inactive-value="false"
                v-model="selectUser.setNewPwd"
              >
              </el-switch>
            </el-form-item>
          </el-col>
          <el-col :span="24" v-if="selectUser.setNewPwd == 1">
            <el-form-item label="New Pwd" required prop="password">
              <el-input
                v-model="selectUser.password"
                placeholder="Enter the new password"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="User Type">
              <el-select v-model="selectUser.type">
                <el-option
                  label="超级管理员"
                  :value="1000"
                  :key="1000"
                ></el-option>
                <el-option label="管理员" :value="1001" :key="1001"></el-option>
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
            <el-form-item label="Status">
              <el-switch
                :active-value="0"
                :inactive-value="1"
                v-model="selectUser.status"
              >
              </el-switch>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button type="danger" @click.native="showUserDialog = false"
          >Cancel</el-button
        >
        <el-button type="primary" @click.native="saveUser">Save</el-button>
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
        callback(new Error('用户结束编号不能小于起始编号'));
      }
      callback();
    };
    const CheckPwdLength = (rule, value, callback) => {
      if (value < 6 || value > 25) {
        callback(new Error('密码长度请选择6~25字符长度'));
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
            callback(new Error('用户名已存在'));
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
            callback(new Error('邮箱已存在'));
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
      },
      updateUserRules: {
        username: [
          { required: true, message: 'Username is required', trigger: 'blur' },
          {
            validator: CheckUsernameNotExist,
            trigger: 'blur',
            message: 'The username already exists',
          },
          {
            max: 255,
            message: 'The longest length of a username is 255',
            trigger: 'blur',
          },
        ],
        realname: [
          {
            max: 255,
            message: 'The longest length of a username is 255',
            trigger: 'blur',
          },
        ],
        email: [
          {
            type: 'email',
            message: 'The email format is incorrect',
            trigger: 'blur',
          },
          {
            validator: CheckEmailNotExist,
            message: 'The email already exists',
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
          { required: true, message: '编号起始不能为空', trigger: 'blur' },
        ],
        number_to: [
          { required: true, message: '最大编号不能为空', trigger: 'blur' },
          { validator: CheckTogtFrom, trigger: 'blur' },
        ],
        password_length: [
          { required: true, message: '密码长度不能为空', trigger: 'blur' },
          {
            type: 'number',
            message: '密码长度必须为数字类型',
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
    // 提交修改用户的信息
    saveUser() {
      this.$refs['updateUser'].validate((valid) => {
        if (valid) {
          api
            .admin_editUser(this.selectUser)
            .then((res) => {
              // 更新列表
              myMessage.success(res.data.msg);
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
      this.keyword = '';
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
    },
    // 获取用户列表
    getUserList(page) {
      this.loadingTable = true;
      api.admin_getUserList(page, this.pageSize, this.keyword).then(
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
        this.$confirm(
          '你确定要删除该用户？可能会关联删除该用户创建的公告，题目，比赛等。',
          '确认',
          {
            type: 'warning',
          }
        ).then(
          () => {
            api
              .admin_deleteUsers(ids)
              .then((res) => {
                myMessage.success(res.data.msg);
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
        myMessage.warning('勾选的用户不能为空！');
      }
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
          myMessage.error('请在生成用户名规则中选择或输入正确的值');
          return;
        }
        this.loadingGenerate = true;
        let data = Object.assign({}, this.formGenerateUser);
        api
          .admin_generateUser(data)
          .then((res) => {
            this.loadingGenerate = false;
            myMessage.success(res.data.msg);
            let url = '/file/generate-user-excel?key=' + res.data.data.key;
            utils.downloadFile(url).then(() => {
              this.$alert(
                '所有指定格式用户创建成功，用户表已成功下载到您的电脑里了！',
                '提醒'
              );
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
            return user[0] && user[1] && user[2];
          });
          let delta = results.data.length - data.length;
          if (delta > 0) {
            myMessage.warning(
              delta + '行用户数据被过滤，原因是可能为空行或某个列值为空'
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
          myMessage.success(res.data.msg);
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
