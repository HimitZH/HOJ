<template>
  <div>
    <h1>User Home Page</h1>
    <p>Username: {{ $route.query.username }}</p>
    <p>UID: {{ $route.query.uid }}</p>
    <router-link to="/">Go Home</router-link>
  </div>
</template>
<script>
export default {
  name: 'UserHomeView'
}
</script>
