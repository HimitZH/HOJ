<template>
  <div>
    <h1>Problem Detail Page</h1>
    <p>Problem ID: {{ $route.params.problemID }}</p>
    <router-link to="/">Go Home</router-link>
  </div>
</template>
<script>
export default {
  name: 'ProblemDetailView'
}
</script>
