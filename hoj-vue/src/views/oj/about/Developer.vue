<template>
  <div>
    <div class="intr-center">
      <paper-card type="server">
        <h1 class="text-darken">HOJ</h1>
        <h1>
          <a href="https://gitee.com/himitzh0730/hoj" target="_blank"
            >Hcode Online Judge</a
          >
        </h1>
        <p>
          {{ $t('m.Leader_BackEnd_FrontEnd_Engineer') }} / Himit_ZH
          <a href="https://github.com/HimitZH" class="icon" target="_blank"
            ><i class="fa fa-github"></i>
          </a>
        </p>
        <p>
          {{ $t('m.Group_Function_Development_Contributor') }} / 冷蕴
          <a href="https://github.com/IUaenaSong" class="icon" target="_blank"
            ><i class="fa fa-github"></i>
          </a>
        </p>
        <p class="teal-text">
          <i class="el-icon-circle-check"></i> {{ $t('m.Open_Source') }}
        </p>
      </paper-card>
    </div>
    <el-row :gutter="20">
      <el-col :xs="24" :md="12">
        <paper-card type="server">
          <h1>{{ $t('m.Distributed') }}</h1>
          <p>
            <small>{{ $t('m.Distributed_Desc') }}</small>
          </p>
          <p class="teal-text">
            <i class="el-icon-circle-check"></i> {{ $t('m.Available') }}
          </p>
        </paper-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <paper-card type="server">
          <h1>{{ $t('m.Customization') }}</h1>
          <p>
            <small>{{ $t('m.Customization_Desc') }}</small>
          </p>
          <p class="teal-text">
            <i class="el-icon-circle-check"></i> {{ $t('m.Available') }}
          </p>
        </paper-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <paper-card type="server">
          <h1>{{ $t('m.Security') }}</h1>
          <p>
            <small>{{ $t('m.Security_Desc') }}</small>
          </p>
          <p class="teal-text">
            <i class="el-icon-circle-check"></i> {{ $t('m.Available') }}
          </p>
        </paper-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <paper-card type="server">
          <h1>{{ $t('m.Diversity') }}</h1>
          <p>
            <small>{{ $t('m.Diversity_Desc') }}</small>
          </p>
          <p class="teal-text">
            <i class="el-icon-circle-check"></i> {{ $t('m.Available') }}
          </p>
        </paper-card>
      </el-col>
    </el-row>
  </div>
</template>
<script></script>
<style scoped>
.intr-center {
  text-align: center;
  max-width: 100%;
  margin: 0 auto;
}
paper-card[type='server'] {
  display: block;
  text-align: center;
  border-radius: 4px;
  transition: 0.2s ease-out 0s;
  color: rgba(0, 0, 0, 0.63);
  background-image: linear-gradient(180deg, hsla(0, 0%, 100%, 0) 30%, #fff),
    linear-gradient(70deg, #e0f1ff 32%, #fffae3);
  padding: 1rem;
  position: relative;
  border: 1px solid rgba(0, 0, 0, 0.15);
  margin-bottom: 2rem;
}
paper-card:hover {
  box-shadow: rgb(0 0 0 / 15%) 0px 0px 40px;
}
.teal-text {
  color: #009688 !important;
}
.text-darken {
  margin-top: 0;
  margin-bottom: 0.5rem;
  color: #2196f3 !important;
  font-weight: 400;
  font-size: 40px;
}
.icon {
  font-size: 1rem;
  line-height: 1.5;
  color: #212529;
}
.intr-center p {
  font-size: 1rem;
  line-height: 1.5;
}
</style>
