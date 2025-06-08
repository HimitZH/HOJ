<template>
  <div>
    <h1>Contest Detail Page</h1>
    <p>Contest ID: {{ $route.params.contestID }}</p>
    <router-link to="/">Go Home</router-link>
  </div>
</template>
<script>
export default {
  name: 'ContestDetailView'
}
</script>
