<template>
  <div class="inline-flex items-center justify-center rounded-full bg-gray-300 text-white"
       :style="{ width: size + 'px', height: size + 'px', backgroundColor: color }">
    <img v-if="src" :src="src" :alt="username" class="rounded-full w-full h-full object-cover" @error="onImageError">
    <span v-else-if="username" :style="{ fontSize: (size / 2) + 'px' }">
      {{ username.substring(0, 1).toUpperCase() }}
    </span>
  </div>
</template>

<script>
export default {
  name: 'Avatar',
  props: {
    src: String,
    username: String,
    size: {
      type: Number,
      default: 25,
    },
    color: String, // Can pass a specific color or let Tailwind handle it
  },
  methods: {
    onImageError(event) {
      // Hide image on error to show initials or default
      event.target.style.display = 'none';
    }
  }
};
</script>
