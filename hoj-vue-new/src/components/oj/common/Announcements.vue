<template>
  <div class="bg-white shadow-md rounded-lg p-4">
    <div class="flex justify-between items-center mb-4">
      <h3 class="text-xl font-semibold">
        <i class="fas fa-bullhorn mr-2"></i>{{ $t('m.Announcement') }}
      </h3>
      <button @click="init" class="text-blue-500 hover:text-blue-700" :disabled="btnLoading">
        <i :class="['fas fa-sync-alt', btnLoading ? 'animate-spin' : '']"></i>
        {{ $t('m.Refresh') }}
      </button>
    </div>
    <div v-if="btnLoading && announcements.length === 0" class="text-center">
      <p>Loading announcements...</p>
    </div>
    <div v-else-if="announcements.length === 0" class="text-center">
      <p>{{ $t('m.No_Announcements') }}</p>
    </div>
    <ul v-else class="space-y-4">
      <li v-for="announcement in announcements" :key="announcement.id" class="p-4 border border-gray-200 rounded-lg shadow-sm hover:shadow-md transition-shadow">
        <div>
          <a @click="goAnnouncement(announcement)" class="text-lg font-semibold text-blue-600 hover:text-blue-800 hover:underline cursor-pointer">
            {{ announcement.title }}
          </a>
        </div>
        <div class="text-sm text-gray-500 mt-1">
          <span class="mr-4"><i class="far fa-calendar-alt mr-1"></i>{{ formatTime(announcement.gmtCreate) }}</span>
          <span><i class="far fa-user mr-1"></i>{{ announcement.username }}</span>
        </div>
      </li>
    </ul>
    <!-- Placeholder for Pagination -->
    <div class="mt-4 text-center" v-if="total > limit">
      <p>Pagination will go here. Total: {{ total }}, Limit: {{ limit }}</p>
    </div>

    <!-- Placeholder for Announcement Detail View -->
    <div v-if="!listVisible" class="mt-4 p-4 border border-gray-200 rounded-lg">
       <button @click="goBack" class="text-blue-500 hover:text-blue-700 mb-2">
         <i class="fas fa-arrow-left mr-1"></i> {{ $t('m.Back') }}
       </button>
       <h4 class="text-2xl font-bold mb-2">{{ currentAnnouncement.title }}</h4>
       <div v-html="renderedMarkdownContent" class="prose max-w-none"></div>
    </div>
  </div>
</template>

<script>
import api from '@/common/api';
// We'll need a markdown renderer. Let's assume 'marked' for now.
// npm install marked
import { marked } from 'marked';
import DOMPurify from 'dompurify'; // npm install dompurify

export default {
  name: 'Announcements',
  props: {
    limit: {
      type: Number,
      default: 5,
    },
    // For now, we are not handling contest announcements in this iteration
    // contestID: {
    //   type: String,
    //   default: null,
    // }
  },
  data() {
    return {
      total: 0,
      btnLoading: false,
      announcements: [],
      currentAnnouncement: null,
      listVisible: true,
      renderedMarkdownContent: '',
    };
  },
  mounted() {
    this.init();
  },
  methods: {
    init() {
      // For now, only fetching general announcements
      this.getAnnouncementList();
    },
    getAnnouncementList(page = 1) {
      this.btnLoading = true;
      this.listVisible = true; // Show list when fetching
      api.getAnnouncementList(page, this.limit).then(
        (res) => {
          this.btnLoading = false;
          this.announcements = res.data.data.records;
          this.total = res.data.data.total;
        },
        (err) => {
          console.error("Failed to load announcements:", err);
          this.btnLoading = false;
          this.announcements = []; // Clear on error
          this.total = 0;
        }
      );
    },
    goAnnouncement(announcement) {
      this.currentAnnouncement = announcement;
      // Sanitize HTML before rendering
      this.renderedMarkdownContent = DOMPurify.sanitize(marked.parse(announcement.content || ''));
      this.listVisible = false;
      // _TODO: Add code highlighting if needed, similar to original's addCodeBtn()
    },
    goBack() {
      this.listVisible = true;
      this.currentAnnouncement = null;
      this.renderedMarkdownContent = '';
    },
    formatTime(timeStr) {
      // Basic time formatting, consider using a library like date-fns or moment if complex formatting is needed
      if (!timeStr) return 'N/A';
      const date = new Date(timeStr);
      return date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
    }
  },
  computed: {
    // title() { // Simplified title logic for now
    //   if (this.listVisible) {
    //     return this.$i18n.t('m.Announcement');
    //   } else if (this.currentAnnouncement) {
    //     return this.currentAnnouncement.title;
    //   }
    //   return this.$i18n.t('m.Announcement');
    // },
    // isContest() { // Simplified, not using contest announcements yet
    //   return false; // !!this.contestID;
    // },
  },
};
</script>

<style scoped>
/* Add FontAwesome if not already globally available */
/* @import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css'); */

/* Basic prose styling for markdown content, Tailwind's @tailwindcss/typography plugin is a good option for more comprehensive styling */
.prose h1 { @apply text-2xl font-bold mb-2; }
.prose h2 { @apply text-xl font-semibold mb-2; }
.prose p { @apply mb-2; }
.prose ul { @apply list-disc list-inside mb-2; }
.prose pre { @apply bg-gray-100 p-2 rounded overflow-x-auto; }
.prose code { @apply bg-gray-100 px-1 rounded; }
</style>
