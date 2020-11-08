<template>
<div>
  <NavBar></NavBar>
  <div id="app">
      <transition name="fadeInUp" mode="out-in">
        <router-view></router-view>
      </transition>
  </div>
</div>
</template>

<script>
import NavBar from '@/components/common/NavBar'
export default {
  name: 'app',
  components: {
    NavBar
  },
}
</script>

<style>
 * {
    -webkit-box-sizing: border-box;
    -moz-box-sizing: border-box;
    box-sizing: border-box;
  }

  a {
    text-decoration: none;
    background-color: transparent;
    color: #495060;
    transition: all 0.28s ease;
  }
  a:hover{
     color: #2196f3;
  }

@media screen and (min-width: 1200px) {
  #app {
    margin-top: 80px;
    padding: 0 4%;
  }
}
@media screen and (max-width: 1200px) {
  #app {
    margin-top: 210px;
    padding: 0 4%;
  }
}
@media screen and (max-width: 400px) {
  #app {
    margin-top: 270px;
    padding: 0 4%;
  }
}

  .footer {
    margin-top: 20px;
    margin-bottom: 10px;
    text-align: center;
    font-size: small;
  }

  .fadeInUp-enter-active {
    animation: fadeInUp .8s;
  }
body{
  background-color: #eee;
}

</style>
