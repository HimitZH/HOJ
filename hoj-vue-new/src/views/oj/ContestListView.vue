<template>
  <div>
    <h1>Contest List Page</h1>
    <p>Type: {{ $route.query.type }}</p>
    <router-link to="/">Go Home</router-link>
  </div>
</template>
<script>
export default {
  name: 'ContestListView'
}
</script>
