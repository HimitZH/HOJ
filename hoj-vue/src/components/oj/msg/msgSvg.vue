<template>
  <svg width="20" height="20">
    <circle cx="10" cy="10" r="10" style="fill: red;"></circle>
    <text x="2" dy="15" style="fill: white" v-if="total >= 10">
      {{ total > 99 ? 99 : total }}
    </text>
    <text x="6" dy="15" style="fill: white" v-else-if="total > 0">
      {{ total }}
    </text>
  </svg>
</template>

<script>
export default {
  name: 'msgSvg',
  props: {
    total: {
      required: true,
      type: Number,
    },
  },
};
</script>
