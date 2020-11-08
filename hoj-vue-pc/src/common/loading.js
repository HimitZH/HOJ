import Element from 'element-ui';

//全局页面跳转是否启用loading
const routerLoading = true;

//全局api接口调用是否启用loading
const apiLoading = true;

//loading参数配置
const loadingConfig = {
  lock: true,
  text: 'Loading',
  spinner: 'el-icon-loading',
  background: 'rgba(0, 0, 0, 0.7)'
}



var loading = null ;
const loadingShow = () => {
  loading = Element.Loading.service(loadingConfig);
}

const loadingHide = () => {
  loading.close();
}

const loadingObj={
  loadingShow,
  loadingHide,
  routerLoading,
  apiLoading
}

export default loadingObj