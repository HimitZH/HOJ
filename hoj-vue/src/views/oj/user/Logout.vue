<template>
</template>

<script>
  import api from '@/common/api'

  export default {
    mounted () {
        api.logout().then(res => {
        this.$store.dispatch('clearUserInfoAndToken')
        this.$router.replace({
          path: '/home'
        })
      })
    }
  }
</script>
