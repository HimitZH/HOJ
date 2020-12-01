<template>
  <div>
    <el-card>
      <span slot="header" class="panel-title home-title">Delete Test Case
        <el-popover placement="right" trigger="hover">
          这些测试用例不属于任何问题，您可以安全地清理它们。
          <i slot="reference" class="el-icon-question"></i>
        </el-popover>
      </span>
      <vxe-table :data="data" stripe auto-resize>
        <vxe-table-column
          min-width="150"
          title="Last Modified">
          <template v-slot="{row}">
            {{row.create_time | timestampFormat }}
          </template>
        </vxe-table-column>
        <vxe-table-column
          min-width="80"
          field="id"
          title="Test Case ID">
        </vxe-table-column>
        <vxe-table-column
          title="Option"
          field="right"
          min-width="200">
          <template v-slot="{row}">
            <el-button  icon="el-icon-delete-solid" size="mini" @click.native="deleteTestCase(row.id)" type="danger">
            </el-button>
          </template>
        </vxe-table-column>
      </vxe-table>

      <div class="panel-options" v-show="data.length > 0">
        <el-button type="warning" size="small"
                   :loading="loading"
                   icon="el-icon-fa-trash"
                   @click="deleteTestCase()">Delete All
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script>
  import api from '@/common/api'
  import moment from 'moment'

  export default {
    name: 'prune-test-case',
    data () {
      return {
        data: [],
        loading: false
      }
    },
    mounted () {
      // this.init()
    },
    methods: {
      init () {
        api.getInvalidTestCaseList().then(resp => {
          this.data = resp.data.data
        }, () => {
        })
      },
      deleteTestCase (id) {
        if (!id) {
          this.loading = true
        }
        api.pruneTestCase(id).then(resp => {
          this.loading = false
          this.init()
        })
      }
    },
    filters: {
      timestampFormat (value) {
        return moment.unix(value).format('YYYY-MM-DD  HH:mm:ss')
      }
    }
  }
</script>

<style>

</style>
