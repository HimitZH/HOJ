<template>
  <div class="p-4">
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
      <!-- Left Column -->
      <div class="md:col-span-2">
        <div class="bg-white shadow-md rounded-lg p-4">
          <div class="text-center mb-4">
            <h2 class="text-2xl font-bold">
              {{ $t('m.Welcome_to') }} {{ websiteConfig.shortName }}
            </h2>
          </div>
          <!-- Carousel -->
          <div v-if="carouselImgList.length > 0" class="relative">
            <div class="overflow-hidden relative" style="height: 440px;"> <!-- Adjust height as needed -->
              <div
                v-for="(item, index) in carouselImgList"
                :key="index"
                :class="['absolute w-full h-full transition-opacity duration-1000 ease-in-out', currentIndex === index ? 'opacity-100' : 'opacity-0']"
              >
                <img :src="item.url" alt="Carousel image" class="w-full h-full object-cover"/>
              </div>
            </div>
            <button @click="prev" class="absolute top-1/2 left-2 transform -translate-y-1/2 bg-black bg-opacity-50 text-white p-2 rounded-full">‹</button>
            <button @click="next" class="absolute top-1/2 right-2 transform -translate-y-1/2 bg-black bg-opacity-50 text-white p-2 rounded-full">›</button>
            <div class="absolute bottom-2 left-1/2 transform -translate-x-1/2 flex space-x-2">
               <button
                 v-for="(item, index) in carouselImgList"
                 :key="'dot-'+index"
                 @click="goToSlide(index)"
                 :class="['w-3 h-3 rounded-full', currentIndex === index ? 'bg-white' : 'bg-gray-400']"
               ></button>
             </div>
          </div>
          <div v-else class="text-center p-8">
             <p>No images for carousel.</p>
          </div>
        </div>
        <Announcements class="mt-4" />
        <SubmissionStatistic class="mt-4" />
        <LatestProblems class="mt-4" /> <!-- Use the component -->
      </div>

      <!-- Right Column -->
      <div class="md:col-span-1">
        <RecentContests />
        <RecentACRank class="mt-4" />
        <SupportedOJ class="mt-4" /> <!-- Use the component -->
      </div>
    </div>
  </div>
</template>

<script>
import { mapState, mapGetters } from 'vuex';
import api from '@/common/api'; // Assuming api.js is copied and working
import Announcements from '@/components/oj/common/Announcements.vue'; // Import new component
import SubmissionStatistic from '@/components/oj/home/SubmissionStatistic.vue'; // Import new component
import LatestProblems from '@/components/oj/home/LatestProblems.vue'; // Import new component
import RecentContests from '@/components/oj/home/RecentContests.vue';
import RecentACRank from '@/components/oj/home/RecentACRank.vue';
import SupportedOJ from '@/components/oj/home/SupportedOJ.vue';

export default {
  name: 'Home',
  components: { // Register component
    Announcements,
    SubmissionStatistic, // Register component
    LatestProblems, // Register component
    RecentContests, // Register component
    RecentACRank, // Register
    SupportedOJ, // Register
  },
  data() {
    return {
      carouselImgList: [],
      currentIndex: 0,
      intervalId: null,
      interval: 5000, // ms
    };
  },
  computed: {
    ...mapState(['websiteConfig']), // Assuming websiteConfig is in Vuex state
    ...mapGetters(['isAuthenticated']), // Assuming isAuthenticated getter exists
  },
  methods: {
    getHomeCarousel() {
      api.getHomeCarousel().then((res) => {
        if (res.data.data != null && res.data.data.length > 0) {
          this.carouselImgList = res.data.data;
        } else {
          // Fallback images if API returns empty or null
          this.carouselImgList = [
             { url: "https://z1.ax1x.com/2023/12/09/pi20luQ.jpg" },
             { url: "https://z1.ax1x.com/2023/12/09/pi201Bj.jpg" },
          ];
        }
      }).catch(err => {
        console.error("Failed to load carousel images:", err);
        // Fallback images on API error
        this.carouselImgList = [
           { url: "https://z1.ax1x.com/2023/12/09/pi20luQ.jpg" },
           { url: "https://z1.ax1x.com/2023/12/09/pi201Bj.jpg" },
        ];
      });
    },
    next() {
      if (this.carouselImgList.length === 0) return;
      this.currentIndex = (this.currentIndex + 1) % this.carouselImgList.length;
      this.resetInterval();
    },
    prev() {
      if (this.carouselImgList.length === 0) return;
      this.currentIndex = (this.currentIndex - 1 + this.carouselImgList.length) % this.carouselImgList.length;
      this.resetInterval();
    },
    goToSlide(index) {
     this.currentIndex = index;
     this.resetInterval();
    },
    startInterval() {
     if (this.intervalId) clearInterval(this.intervalId);
     this.intervalId = setInterval(() => {
       this.next();
     }, this.interval);
    },
    resetInterval() {
     this.startInterval();
    }
  },
  mounted() {
    this.getHomeCarousel();
    this.startInterval();
    // _TODO: Fetch other data like websiteConfig if not already in store or globally available
    // For now, assuming websiteConfig is loaded into Vuex state by App.vue or similar
    // If not, we might need to dispatch an action here: this.$store.dispatch('getWebsiteConfig');
  },
  beforeUnmount() {
   if (this.intervalId) {
     clearInterval(this.intervalId);
   }
 }
};
</script>

<style scoped>
/* Scoped styles if needed, Tailwind should handle most styling */
/* Adjust carousel height for smaller screens if necessary */
 @media screen and (max-width: 768px) {
   .overflow-hidden { /* Or a more specific class for carousel height */
     height: 220px;
   }
 }
</style>
