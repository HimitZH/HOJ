<template>
  <div>
    <el-card>
      <div slot="header">
        <span class="panel-title home-title">{{ $t('m.Admin_Tag') }}</span>
        <div class="filter">
          <span>
            <el-button
              type="primary"
              size="small"
              @click="openTagDialog('add', null)"
              icon="el-icon-plus"
              >{{ $t('m.Add_Tag') }}
            </el-button>
          </span>
          <span>
            <el-button
              type="warning"
              size="small"
              @click="openTagClassificationDialog('add', null)"
              icon="el-icon-plus"
              >{{ $t('m.Add_Tag_Classification') }}
            </el-button>
          </span>
          <span>
            <el-select
              v-model="tagOj"
              @change="init"
              size="small"
              style="width: 150px;margin-top: 10px;"
            >
              <el-option :label="$t('m.My_OJ')" :value="'ME'"></el-option>
              <el-option
                :label="remoteOj.name"
                :key="index"
                :value="remoteOj.key"
                v-for="(remoteOj, index) in REMOTE_OJ"
              ></el-option>
            </el-select>
          </span>
        </div>
      </div>
      <h3 style="margin: -5px;">{{ $t('m.Tag_Tips') }}</h3>
    </el-card>
    <div v-loading = "getTagListLoading">
      <el-row :gutter="20">
          <el-col v-for="(tagsAndClassification,index)  in tagsAndClassificationList"  
            :key="index" :md="8" :xs="24">
            <el-card style="margin-top:15px">
              <el-collapse v-model="activeTagClassificationIdList">
                  <el-collapse-item :name="tagsAndClassification.classification == null?-1:tagsAndClassification.classification.id">
                      <template slot="title">
                        <span>{{ tagsAndClassification.classification!=null? 
                          tagsAndClassification.classification.name:$t('m.Unclassified')
                        }}
                        </span>
                        <span style="margin-left:10px;"
                          v-if="tagsAndClassification.classification!=null">
                          <el-button type="primary" 
                            icon="el-icon-edit" 
                            circle
                            size="mini"
                            @click.stop="openTagClassificationDialog('update',tagsAndClassification.classification)"
                          ></el-button>
                        </span>
                        <span style="margin-left:10px;"
                          v-if="tagsAndClassification.classification!=null">
                          <el-button type="danger" 
                            icon="el-icon-delete" 
                            circle
                            size="mini"
                            @click.stop="deleteTagClassification(tagsAndClassification.classification)"
                          ></el-button>
                        </span>
                      </template>
                      <el-button
                        class="button-new-tag"
                        size="small"
                        @click="openTagDialog('add', null,tagsAndClassification.classification)"
                        >+ {{ $t('m.Add_Tag') }}</el-button
                      >
                      <el-tag
                        :key="index"
                        v-for="(tag, index) in tagsAndClassification.tagList"
                        closable
                        :color="tag.color ? tag.color : '#409eff'"
                        effect="dark"
                        :disable-transitions="false"
                        @close="deleteTag(tag)"
                        @click="openTagDialog('update', tag)"
                        class="tag"
                      >
                        {{ tag.name }}
                      </el-tag>
                  </el-collapse-item>
              </el-collapse>
            </el-card>
          </el-col>
      </el-row>
    </div>

    <el-dialog
      :title="$t('m.' + upsertTagTitle)"
      width="350px"
      :visible.sync="addTagDialogVisible"
      :close-on-click-modal="false"
    >
      <el-form>
        <el-form-item :label="$t('m.Tag_Name')" required>
          <el-input v-model="tag.name" size="small"></el-input>
        </el-form-item>
        <el-form-item :label="$t('m.Tag_Color')" required>
          <el-color-picker v-model="tag.color"></el-color-picker>
        </el-form-item>

        <el-form-item :label="$t('m.Tag_Attribution')" required="">
          <el-select v-model="tag.oj" size="small" style="width: 150px;">
            <el-option :label="$t('m.My_OJ')" :value="'ME'"></el-option>
            <el-option
              :label="remoteOj.name"
              :key="index"
              :value="remoteOj.key"
              v-for="(remoteOj, index) in REMOTE_OJ"
            ></el-option>
          </el-select>
        </el-form-item>

        <el-form-item :label="$t('m.Tag_Classification')" required="">
          <el-select v-model="tag.tcid" size="small" style="width: 150px;">
            <el-option
              :label="classification.name"
              :key="index"
              :value="classification.id"
              v-for="(classification, index) in chooseTagClassificationList"
            ></el-option>
            <el-option :label="$t('m.Unclassified')" :value="null"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item style="text-align:center">
          <el-button
            type="primary"
            @click="upsertTag"
            :loading="upsertTagLoading"
            >{{ $t('m.' + upsertTagBtn) }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-dialog>

     <el-dialog
      :title="$t('m.' + upsertTagClassificationTitle)"
      width="350px"
      :visible.sync="addTagClassificationDialogVisible"
      :close-on-click-modal="false"
    >
      <el-form>
        <el-form-item :label="$t('m.Tag_Classification_Name')" required>
          <el-input v-model="tagClassification.name" size="small"></el-input>
        </el-form-item>
        <el-form-item :label="$t('m.Tag_Classification_Attribution')" required="">
          <el-select v-model="tagClassification.oj" size="small" style="width: 150px;" :disabled="true">
            <el-option :label="$t('m.My_OJ')" :value="'ME'"></el-option>
            <el-option
              :label="remoteOj.name"
              :key="index"
              :value="remoteOj.key"
              v-for="(remoteOj, index) in REMOTE_OJ"
            ></el-option>
          </el-select>
        </el-form-item>

        <el-form-item :label="$t('m.Tag_Classification_Rank')">
          <el-input-number
              v-model="tagClassification.rank"
              :min="0"
              :max="2147483647"
            ></el-input-number>
        </el-form-item>

        <el-form-item style="text-align:center">
          <el-button
            type="primary"
            @click="upsertTagClassification"
            :loading="upsertTagClassificationLoading"
            >{{ $t('m.' + upsertTagBtn) }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>
<script>
import myMessage from '@/common/message';
import api from '@/common/api';
import { REMOTE_OJ } from '@/common/constants';
export default {
  data() {
    return {
      tagOj: 'ME',
      REMOTE_OJ: {},
      getTagListLoading: false,
      tagsAndClassificationList: [],
      tagClassificationList :[],
      chooseTagClassificationList:[],
      addTagDialogVisible: false,
      upsertTagTitle: 'Add_Tag',
      upsertTagBtn: 'To_Add',
      upsertTagLoading: false,
      tag: {
        id: null,
        name: null,
        color: null,
        oj: 'ME',
        tcid: null,
      },
      tagClassification:{
        id:null,
        name:null,
        rank:0,
        oj:'ME'
      },
      addTagClassificationDialogVisible: false,
      upsertTagClassificationTitle: 'Add_Tag_Classification',
      upsertTagClassificationLoading:false,
      activeTagClassificationIdList:[-1]
    };
  },
  mounted() {
    this.REMOTE_OJ = Object.assign({}, REMOTE_OJ);
    this.init();
  },
  methods: {
    init(){
      this.getTagClassification();
      this.getProblemTagsAndClassification();
    },
    getProblemTagsAndClassification() {
      this.getTagListLoading = true;
      api.getProblemTagsAndClassification(this.tagOj).then(
        (res) => {
          this.tagsAndClassificationList = res.data.data;
          this.getTagListLoading = false;
        },
        (err) => {
          this.getTagListLoading = false;
        }
      );
    },

    deleteTag(tag) {
      this.$confirm(this.$i18n.t('m.Delete_Tag_Tips'), 'Tips', {
        type: 'warning',
      }).then(
        () => {
          api
            .admin_deleteTag(tag.id)
            .then((res) => {
              myMessage.success(this.$i18n.t('m.Delete_successfully'));
              this.getProblemTagsAndClassification();
            })
            .catch(() => {});
        },
        () => {}
      );
    },
    openTagDialog(action, tag, classification = null) {
      if (action == 'add') {
        this.upsertTagTitle = 'Add_Tag';
        this.upsertTagBtn = 'To_Add';
        this.tag = {
          id: null,
          name: null,
          color: null,
          oj: this.tagOj,
          tcid: classification == null? null: classification.id
        };
      } else {
        this.upsertTagTitle = 'Update_Tag';
        this.upsertTagBtn = 'To_Update';
        this.tag = Object.assign({}, tag);
      }
      this.addTagDialogVisible = true;
    },

     upsertTag() {
      if (this.tag.id) {
        this.upsertTagLoading = true;
        api.admin_updateTag(this.tag).then(
          (res) => {
            this.upsertTagLoading = false;
            myMessage.success(this.$i18n.t('m.Update_Successfully'));
            this.addTagDialogVisible = false;
            this.getProblemTagsAndClassification();
          },
          (err) => {
            this.upsertTagLoading = false;
          }
        );
      } else {
        this.upsertTagLoading = true;
        api.admin_addTag(this.tag).then(
          (res) => {
            this.upsertTagLoading = false;
            myMessage.success(this.$i18n.t('m.Add_Successfully'));
            this.addTagDialogVisible = false;
            this.getProblemTagsAndClassification();
          },
          (err) => {
            this.upsertTagLoading = false;
          }
        );
      }
    },

    getTagClassification(){
      api.admin_getTagClassification(this.tagOj).then((res)=>{
        this.tagClassificationList = res.data.data;
        this.chooseTagClassificationList = this.tagClassificationList.filter(c=>c.oj == this.tag.oj);
      })
    },

    upsertTagClassification() {
      if (this.tagClassification.id) {
        this.upsertTagClassificationLoading = true;
        api.admin_updateTagClassification(this.tagClassification).then(
          (res) => {
            this.upsertTagClassificationLoading = false;
            myMessage.success(this.$i18n.t('m.Update_Successfully'));
            this.addTagClassificationDialogVisible = false;
            this.getProblemTagsAndClassification();
          },
          (err) => {
            this.upsertTagClassificationLoading = false;
          }
        );
      } else {
        this.upsertTagClassificationLoading = true;
        api.admin_addTagClassification(this.tagClassification).then(
          (res) => {
            this.upsertTagClassificationLoading = false;
            myMessage.success(this.$i18n.t('m.Add_Successfully'));
            this.tagsAndClassificationList.unshift(
              {
                classification :res.data.data,
                tagList:[]
              });
            this.chooseTagClassificationList.push(res.data.data);
            this.addTagClassificationDialogVisible = false;
          },
          (err) => {
            this.upsertTagClassificationLoading = false;
          }
        );
      }
    },

    deleteTagClassification(tagClassification) {
      this.$confirm(this.$i18n.t('m.Delete_Tag_Classification_Tips'), 'Tips', {
        type: 'warning',
      }).then(
        () => {
          api
            .admin_deleteTagClassification(tagClassification.id)
            .then((res) => {
              myMessage.success(this.$i18n.t('m.Delete_successfully'));
              this.getProblemTagsAndClassification();
            })
            .catch(() => {});
        },
        () => {}
      );
    },

    openTagClassificationDialog(action, tagClassification) {
      if (action == 'add') {
        this.upsertTagClassificationTitle = 'Add_Tag_Classification';
        this.upsertTagBtn = 'To_Add';
        this.tagClassification = {
          id: null,
          name: null,
          rank:0,
          oj: this.tagOj,
        };
      } else {
        this.upsertTagClassificationTitle = 'Update_Tag_Classification';
        this.upsertTagBtn= 'To_Update';
        this.tagClassification = Object.assign({}, tagClassification);
      }
      this.addTagClassificationDialogVisible = true;
    },

  },
  watch:{
    'tag.oj'(newVal){
      this.chooseTagClassificationList = this.tagClassificationList.filter(c=>c.oj == newVal);
      this.tag.tcid = null;
    }
  }
};
</script>
<style scoped>
.filter {
  margin-top: 10px;
}
.filter span {
  margin-right: 10px;
}
.el-tag {
  margin-left: 10px;
  margin-top: 10px;
}
.tag {
  cursor: pointer;
}
.button-new-tag {
  margin-left: 10px;
  height: 32px;
  line-height: 30px;
  padding-top: 0;
  padding-bottom: 0;
  margin-top: 10px;
}

/deep/.el-collapse-item__header{
  font-weight: bolder !important;
  height:40px !important;
  line-height: 40px !important;
  font-size: 15px !important;
}
/deep/.el-collapse-item__content {
  padding-bottom: 10px !important;
}
</style>
