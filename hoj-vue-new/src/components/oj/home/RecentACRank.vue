<template>
  <div class="bg-white shadow-md rounded-lg p-4">
    <div class="flex justify-between items-center mb-4">
      <h3 class="text-xl font-semibold">
        <i class="fas fa-medal mr-2"></i>{{ $t('m.Recent_7_Days_AC_Rank') }}
      </h3>
      <button @click="fetchRankData" class="text-blue-500 hover:text-blue-700" :disabled="loading">
        <i :class="['fas fa-sync-alt', loading ? 'animate-spin' : '']"></i>
        {{ $t('m.Refresh') }}
      </button>
    </div>
    <div v-if="loading && rankData.length === 0" class="text-center">
      <p>Loading rank...</p>
    </div>
    <div v-else-if="rankData.length === 0 && !loading" class="text-center">
      <p>{{ $t('m.No_rank_data_available') }}</p>
    </div>
    <div v-else class="overflow-x-auto max-h-96"> {/* max-h-96 for scrollable table */}
      <table class="min-w-full divide-y divide-gray-200">
        <thead class="bg-gray-50 sticky top-0">
          <tr>
            <th scope="col" class="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider w-16">#</th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              {{ $t('m.Username') }}
            </th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-20">
              {{ $t('m.AC') }}
            </th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
          <tr v-for="(user, index) in rankData" :key="user.username" class="hover:bg-gray-100">
            <td class="px-4 py-4 whitespace-nowrap text-sm text-center">
              <span :class="getRankTagClass(index)" class="text-white text-xs font-bold px-2 py-1 rounded-full">
                {{ index + 1 }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
              <div class="flex items-center">
                <Avatar :username="user.username" :src="user.avatar" :size="25" class="mr-2"/>
                <a @click="goUserHome(user.username, user.uid)" class="text-blue-600 hover:text-blue-800 hover:underline cursor-pointer">
                  {{ user.username }}
                </a>
                <span v-if="user.titleName"
                      :style="{ backgroundColor: user.titleColor, color: 'white' }"
                      class="ml-2 px-1.5 py-0.5 text-xs font-semibold rounded">
                  {{ user.titleName }}
                </span>
              </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ user.ac }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
import api from '@/common/api';
import Avatar from '@/components/oj/common/Avatar.vue'; // Import the Avatar component

export default {
  name: 'RecentACRank',
  components: {
    Avatar,
  },
  data() {
    return {
      loading: false,
      rankData: [],
    };
  },
  mounted() {
    this.fetchRankData();
  },
  methods: {
    fetchRankData() {
      this.loading = true;
      api.getRecent7ACRank().then(
        (res) => {
          this.rankData = res.data.data || [];
          this.loading = false;
        },
        (err) => {
          console.error('Failed to load recent AC rank:', err);
          this.rankData = [];
          this.loading = false;
        }
      );
    },
    getRankTagClass(index) {
      if (index === 0) return 'bg-red-600';    // Gold/First
      if (index === 1) return 'bg-yellow-500'; // Silver/Second
      if (index === 2) return 'bg-orange-500'; // Bronze/Third
      return 'bg-gray-500';                  // Others
    },
    goUserHome(username, uid) {
      this.$router.push({
        name: 'UserHome', // Make sure this route is defined
        query: { username, uid },
      });
    },
  },
};
</script>

<style scoped>
 /* Add FontAwesome if not already globally available */
/* @import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css'); */
</style>
