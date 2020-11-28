<template>
  <div class="view">
    <el-card>
      <div slot="header">
        <span class="panel-title home-title">Users</span>
        <div class="filter-row">
            <span>
            <el-button type="danger" icon="el-icon-delete-solid"
              @click="deleteUsers" size="small">Delete
            </el-button>
            </span>
            <span>
            <vxe-input v-model="keyword" placeholder="Enter keyword" type="search" size="medium" @search-click="filterByKeyword"></vxe-input>
            </span>
        </div>
      </div>
      <vxe-table stripe auto-resize :data="userList"  
      ref="xTable"
      :loading="loadingTable"
      :checkbox-config="{labelField: 'id', highlight: true, range: true}"
      @checkbox-change="handleSelectionChange"
      @checkbox-all="handlechangeAll"
      >
        <vxe-table-column type="checkbox" width="60"></vxe-table-column>
        <vxe-table-column field="uid" title="ID" width="140"></vxe-table-column>
        <vxe-table-column field="username" title="Username" min-width="140"></vxe-table-column>
        <vxe-table-column field="realname" title="Real Name" min-width="140"></vxe-table-column>
        <vxe-table-column field="email" title="Email" min-width="150"></vxe-table-column>
        <vxe-table-column field="gmtCreate" title="Create Time" min-width="150">
          <template v-slot="{ row }">
            {{row.gmtCreate | localtime }}
          </template>
        </vxe-table-column>
        <vxe-table-column field="role" title="User Type" min-width="100">
          <template v-slot="{ row }">
            {{row.role|parseRole}}
          </template>
        </vxe-table-column>
        <vxe-table-column field="status" title="Status" min-width="100">
          <template v-slot="{ row }">
          <el-tag effect="dark" color="#19be6b" v-if="row.status==0">正常</el-tag>
          <el-tag effect="dark" color="#ed3f14" v-else>禁用</el-tag>
          </template>
        </vxe-table-column>
        <vxe-table-column  title="Option" min-width="150">
          <template v-slot="{ row }">
            <el-tooltip class="item" effect="dark" content="编辑用户" placement="top">
                <el-button icon="el-icon-edit-outline" size="mini" @click.native="openUserDialog(row)" type="primary">
                </el-button>
            </el-tooltip>
            <el-tooltip class="item" effect="dark" content="删除用户" placement="top">
                <el-button  icon="el-icon-delete-solid" size="mini" @click.native="deleteUsers([row.uid])" type="danger">
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
          :total="total">
        </el-pagination>
      </div>
    </el-card>

    <!-- 导入csv用户数据 -->
    <el-card style="margin-top:20px">
      <div slot="header"><span class="panel-title home-title">Import Users</span></div>
      <p>1. 用户数据导入仅支持csv格式的用户数据。</p>
      <p>2. 共三列数据: 用户名，密码，邮箱，任一列不能为空，否则该行数据可能导入失败。</p>
      <p>3. 第一行不必写(“用户名”，“密码”，“邮箱”)这三个列名。</p>
      <p>4. 请导入保存为UTF-8编码的文件，否则中文可能会乱码。</p>
      <el-upload v-if="!uploadUsers.length"
                 action=""
                 :show-file-list="false"
                 accept=".csv"
                 :before-upload="handleUsersCSV">
        <el-button size="small" icon="el-icon-upload" type="primary">Choose File</el-button>
      </el-upload>
      <template v-else>

        <vxe-table :data="uploadUsersPage" stripe auto-resize>
          <vxe-table-column title="Username" field="username" min-width="150">
            <template v-slot="{row}">
              {{row[0]}}
            </template>
          </vxe-table-column>
          <vxe-table-column title="Password" field="password" min-width="150">
            <template v-slot="{row}">
              {{row[1]}}
            </template>
          </vxe-table-column>
          <vxe-table-column title="Email" field="email" min-width="150">
            <template v-slot="{row}">
              {{row[2]}}
            </template>
          </vxe-table-column>
        </vxe-table>

        <div class="panel-options">
          <el-button type="primary" size="small"
                     icon="el-icon-upload"
                     @click="handleUsersUpload">Upload All
          </el-button>
          <el-button type="danger" size="small"
                     icon="el-icon-delete"
                     @click="handleResetData">Clear All
          </el-button>
          <el-pagination
            class="page"
            layout="prev, pager, next"
            :page-size="uploadUsersPageSize"
            :current-page.sync="uploadUsersCurrentPage"
            :total="uploadUsers.length">
          </el-pagination>
        </div>
      </template>
    </el-card>

    <!--生成用户数据-->
    <el-card style="margin-top:20px">
      <div slot="header">
        <span class="panel-title home-title">Generate Users</span>
      </div>
      <el-form :model="formGenerateUser" ref="formGenerateUser" :rules="formGenerateRules">
        <el-row :gutter="10">
          <el-col :md="5" :xs="24">
            <el-form-item label="Prefix" prop="prefix">
              <el-input v-model="formGenerateUser.prefix" placeholder="Prefix"></el-input>
            </el-form-item>
          </el-col>
          <el-col :md="5" :xs="24">
            <el-form-item label="Suffix" prop="suffix">
              <el-input v-model="formGenerateUser.suffix" placeholder="Suffix"></el-input>
            </el-form-item>
          </el-col>
          <el-col :md="5" :xs="24">
            <el-form-item label="Start Number" prop="number_from">
              <el-input-number v-model="formGenerateUser.number_from" style="width: 100%"></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :md="5" :xs="24">
            <el-form-item label="End Number" prop="number_to" >
              <el-input-number v-model="formGenerateUser.number_to" style="width: 100%"></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :md="4" :xs="24">
            <el-form-item label="Password Length" prop="password_length" >
              <el-input v-model.number="formGenerateUser.password_length"
                        placeholder="Password Length"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item>
          <el-button type="primary" @click="generateUser" icon="fa fa-users" :loading="loadingGenerate" size="small"> Generate & Export
          </el-button>
          <span class="userPreview" v-if="formGenerateUser.number_from <= formGenerateUser.number_to">
            The usernames will be {{formGenerateUser.prefix + formGenerateUser.number_from + formGenerateUser.suffix}},
            <span v-if="formGenerateUser.number_from + 1 < formGenerateUser.number_to">
              {{formGenerateUser.prefix + (formGenerateUser.number_from + 1) + formGenerateUser.suffix + '...'}}
            </span>
            <span v-if="formGenerateUser.number_from + 1 <= formGenerateUser.number_to">
              {{formGenerateUser.prefix + formGenerateUser.number_to + formGenerateUser.suffix}}
            </span>
          </span>
        </el-form-item>
      </el-form>
    </el-card>

    <!--编辑用户的对话框-->
    <el-dialog title="User Info" :visible.sync="showUserDialog" width="350px">
      <el-form :model="selectUser" label-width="100px" label-position="left">
        <el-row :gutter="10">
          <el-col :span="24">
            <el-form-item label="Username" required>
              <el-input v-model="selectUser.username"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="Real Name" required>
              <el-input v-model="selectUser.realname"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="Email" required>
              <el-input v-model="selectUser.email"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="Password" required>
              <el-input v-model="selectUser.password"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="User Type">
              <el-select v-model="selectUser.role">
                <el-option label="用户" :value="1002"></el-option>
                <el-option label="管理员" :value="1001"></el-option>
                <el-option label="超级管理员" :value="1000"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="Status">
              <el-switch
              :active-value="0"
              :inactive-value="1"
              v-model="selectUser.status">
              </el-switch>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button type="danger"  @click.native="showUserDialog = false">Cancel</el-button>
        <el-button type="primary"  @click.native="saveUser">Save</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
  import papa from 'papaparse' // csv插件
  import api from '@/common/api'
  import utils from '@/common/utils'
  import myMessage from '@/common/message'
  export default {
    name: 'user',
    data () {
      const CheckTogtFrom=(rule, value, callback) => {
      if (value < this.formGenerateUser.number_from) {
        callback(new Error("用户结束编号不能小于起始编号"));
          }
          callback();
      }

      const CheckPwdLength=(rule, value, callback) => {
      if (value<6||value>25) {
        callback(new Error("密码长度请选择6~25字符长度"));
          }
          callback();
      }
      return {
        // 一页显示的用户数
        pageSize: 10,
        // 用户总数
        total: 0,
        // 数据库查询的用户列表
        userList: [
          {
            uid:"1001",username:'Himit_ZH',realname:"黄志浩",email:"372347736@qq.com",password:"123456",
            gmtCreate:'2020-11-11 11:11:11',role:1000,status:0
          },
          {
            uid:"1002",username:'Himit_ZH',realname:"黄志浩",email:"372347736@qq.com",password:"123456",
            gmtCreate:'2020-11-11 11:11:11',role:1001,status:0
          }
        ],
        uploadUsers: [],
        uploadUsersPage: [],
        uploadUsersCurrentPage: 1,
        uploadUsersPageSize: 15,
        // 搜索关键字
        keyword: '',
        // 是否显示用户对话框
        showUserDialog: false,

        // 当前用户model
        selectUser: {},
        loadingTable: false,
        loadingGenerate: false,
        // 当前页码
        currentPage: 0,
        selectedUsers: [],
        formGenerateUser: {
          prefix: '',
          suffix: '',
          number_from: 0,
          number_to: 10,
          password_length: 8
        },
        formGenerateRules: {
          number_from: [
            { required: true, message: '编号起始不能为空', trigger: 'blur' },
          ],
          number_to:[
            { required: true, message: '最大编号不能为空', trigger: 'blur' },
            {validator:CheckTogtFrom, trigger: "blur"},
          ],
          password_length:[
            { required: true, message: '密码长度不能为空', trigger: 'blur' },
            { type: 'number', message: '密码长度必须为数字类型', trigger: "blur"},
            {validator:CheckPwdLength, trigger: "blur"}
          ]
        }
      }
    },
    mounted () {
      // this.getUserList(1)
    },
    methods: {
      // 切换页码回调
      currentChange (page) {
        this.currentPage = page
        this.getUserList(page)
      },
      // 提交修改用户的信息
      saveUser () {
        api.editUser(this.selectUser).then(res => {
          // 更新列表
          this.getUserList(this.currentPage)
        }).then(() => {
          this.showUserDialog = false
        }).catch(() => {
        })
      },
      filterByKeyword(){
        console.log("ssssssss")
      },
      // 打开用户对话框
      openUserDialog (row) {
        this.showUserDialog = true
        this.selectUser = Object.assign({}, row); // 深克隆 防止影响原来的表格
      },
      // 获取用户列表
      getUserList (page) {
        this.loadingTable = true
        api.getUserList((page - 1) * this.pageSize, this.pageSize, this.keyword).then(res => {
          this.loadingTable = false
          this.total = res.data.data.total
          this.userList = res.data.data.results
        }, res => {
          this.loadingTable = false
        })
      },
      deleteUsers (ids) {
        this.$confirm('你确定要删除该用户？可能会关联删除该用户创建的公告，题目，比赛等。', '确认', {
          type: 'warning'
        }).then(() => {
          api.deleteUsers(ids.join(',')).then(res => {
            this.getUserList(this.currentPage)
          }).catch(() => {
            this.getUserList(this.currentPage)
          })
        }, () => {
        })
      },

      // 用户表部分勾选 改变选中的内容
      handleSelectionChange ({records }) {
        this.selectedUsers = records 
      },
      // 一键全部选中，改变选中的内容列表
      handlechangeAll () {
        this.selectedUsers = this.$refs.xTable.getCheckboxRecords();
      },
      generateUser () {
        this.$refs['formGenerateUser'].validate((valid) => {
          if (!valid) {
            myMessage.error('请在生成用户名规则中选择或输入正确的值')
            return;
          }
          this.loadingGenerate = true
          let data = Object.assign({}, this.formGenerateUser)
          api.generateUser(data).then(res => {
            this.loadingGenerate = false
            let url = '/admin/generate_user?file_id=' + res.data.data.file_id
            utils.downloadFile(url).then(() => {
              this.$alert('所有用户创建成功，用户表已下载到您的电脑里了。', '提醒')
            })
            this.getUserList(1)
          }).catch(() => {
            this.loadingGenerate = false
          })
        })
      },
      handleUsersCSV (file) {
        papa.parse(file, {
          complete: (results) => {
            let data = results.data.filter(user => {
              return user[0] && user[1] && user[2]
            })
            let delta = results.data.length - data.length
            if (delta > 0) {
              myMessage.warning(delta + '行用户数据被过滤，原因是可能为空行或某个列值为空')
            }
            this.uploadUsersCurrentPage = 1
            this.uploadUsers = data
            this.uploadUsersPage = data.slice(0, this.uploadUsersPageSize)
          },
          error: (error) => {
            myMessage.error(error)
          }
        })
      },
      handleUsersUpload () {
        api.importUsers(this.uploadUsers).then(res => {
          this.getUserList(1)
          this.handleResetData()
        }).catch(() => {
        })
      },
      handleResetData () {
        this.uploadUsers = []
      }
    },
    computed: {
      selectedUserIDs () {
        let ids = []
        for (let user of this.selectedUsers) {
          ids.push(user.id)
        }
        return ids
      }
    },
    watch: {
      'keyword' () {
        this.currentChange(1)
      },
      'uploadUsersCurrentPage' (page) {
        this.uploadUsersPage = this.uploadUsers.slice((page - 1) * this.uploadUsersPageSize, page * this.uploadUsersPageSize)
      }
    }
  }
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
  .notification  p {
      margin: 0;
      text-align: left;
  }
  .filter-row{
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
