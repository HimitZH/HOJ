<template>
  <comment :cid="$route.params.contestID"></comment>
</template>
<script>
export default {
  name: 'ContestComment',
  components: {
    comment,
  },
};
</script>
