<template>
  <div class="marquee-wrap" :class="'marquee-wrap_' + id">
    <div class="scroll" :class="'scroll_' + id">
      <p class="marquee" :class="'marquee_' + id">{{ text }}</p>
      <p class="copy" :class="'copy_' + id"></p>
    </div>
    <p class="getWidth" :class="'getWidth_' + id">{{ text }}</p>
  </div>
</template>

<script>
export default {
  name: 'Marquee',
  props: ['val', 'id'],
  data() {
    return {
      timer: null,
      text: '',
    };
  },
  created() {},
  mounted() {
    let timer = setTimeout(() => {
      this.move();
      clearTimeout(timer);
    }, 1000);
    for (let item of this.val) {
      this.text += item;
    }
  },
  methods: {
    move() {
      let maxWidth = document.querySelector('.marquee-wrap_' + this.id)
        .clientWidth;
      let width = document.querySelector('.getWidth_' + this.id).scrollWidth;
      if (width <= maxWidth) return;
      let scroll = document.querySelector('.scroll_' + this.id);
      let copy = document.querySelector('.copy_' + this.id);
      copy.innerText = this.text;
      let distance = 0;
      this.timer = setInterval(() => {
        distance -= 1;
        if (-distance >= width) {
          distance = 16;
        }
        scroll.style.transform = 'translateX(' + distance + 'px)';
      }, 20);
    },
  },
  beforeDestroy() {
    clearInterval(this.timer);
  },
};
</script>

<style scoped>
.marquee-wrap {
  width: 100%;
  overflow: hidden;
  position: relative;
}
.marquee {
  margin-right: 16px;
}
p {
  margin: 0;
  word-break: keep-all;
  white-space: nowrap;
}
.scroll {
  display: flex;
}
.getWidth {
  word-break: keep-all;
  white-space: nowrap;
  position: absolute;
  opacity: 0;
  top: 0;
}
</style>
