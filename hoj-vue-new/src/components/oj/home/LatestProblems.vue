<template>
  <div class="bg-white shadow-md rounded-lg p-4">
    <div class="flex justify-between items-center mb-4">
      <h3 class="text-xl font-semibold">
        <i class="fas fa-list-alt mr-2"></i>{{ $t('m.Latest_Problem') }}
      </h3>
      <button @click="fetchRecentProblems" class="text-blue-500 hover:text-blue-700" :disabled="loading">
        <i :class="['fas fa-sync-alt', loading ? 'animate-spin' : '']"></i>
        {{ $t('m.Refresh') }}
      </button>
    </div>
    <div v-if="loading && problems.length === 0" class="text-center">
      <p>Loading problems...</p>
    </div>
    <div v-else-if="problems.length === 0 && !loading" class="text-center">
      <p>{{ $t('m.No_problems_available') }}</p>
    </div>
    <div v-else class="overflow-x-auto">
      <table class="min-w-full divide-y divide-gray-200">
        <thead class="bg-gray-50">
          <tr>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              {{ $t('m.Problem_ID') }}
            </th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              {{ $t('m.Title') }}
            </th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              {{ $t('m.Recent_Update') }}
            </th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
          <tr v-for="problem in problems" :key="problem.problemId" @click="goProblem(problem.problemId)" class="hover:bg-gray-100 cursor-pointer">
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{{ problem.problemId }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ problem.title }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
              <el-tooltip :content="formatTime(problem.gmtModified)" placement="top">
                 <span>{{ fromNow(problem.gmtModified) }}</span>
               </el-tooltip>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
import api from '@/common/api';
// Assuming time formatting functions will be utility functions.
// For fromNow, libraries like 'date-fns' (formatDistanceToNow) or a custom utility.
// For localtime, a simple Date().toLocaleString() or a library.
// The original Home.vue used filters: row.gmtModified | localtime and row.gmtModified | fromNow
// We will create simple utility functions for now.

const formatTime = (timeStr) => {
  if (!timeStr) return 'N/A';
  return new Date(timeStr).toLocaleString();
};

const fromNow = (timeStr) => {
  if (!timeStr) return 'N/A';
  const date = new Date(timeStr);
  const seconds = Math.floor((new Date() - date) / 1000);
  let interval = seconds / 31536000;
  if (interval > 1) return Math.floor(interval) + " years ago";
  interval = seconds / 2592000;
  if (interval > 1) return Math.floor(interval) + " months ago";
  interval = seconds / 86400;
  if (interval > 1) return Math.floor(interval) + " days ago";
  interval = seconds / 3600;
  if (interval > 1) return Math.floor(interval) + " hours ago";
  interval = seconds / 60;
  if (interval > 1) return Math.floor(interval) + " minutes ago";
  return Math.floor(seconds) + " seconds ago";
};

export default {
  name: 'LatestProblems',
  data() {
    return {
      loading: false,
      problems: [],
    };
  },
  mounted() {
    this.fetchRecentProblems();
  },
  methods: {
    fetchRecentProblems() {
      this.loading = true;
      api.getRecentUpdatedProblemList().then(
        (res) => {
          this.problems = res.data.data || [];
          this.loading = false;
        },
        (err) => {
          console.error('Failed to load recent problems:', err);
          this.problems = [];
          this.loading = false;
        }
      );
    },
    goProblem(problemID) {
      // Assuming a router instance is available via this.$router
      // And a route named 'ProblemDetails' exists
      this.$router.push({
        name: 'ProblemDetails', // Make sure this route is defined
        params: { problemID: problemID },
      });
    },
    formatTime, // Make utility available in template
    fromNow,     // Make utility available in template
  },
};
</script>

<style scoped>
/* Add FontAwesome if not already globally available */
/* @import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css'); */
/* Minimal styling, Tailwind handles most. Add custom styles if needed. */
</style>
