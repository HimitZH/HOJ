<template>
  <div class="bg-white shadow-md rounded-lg p-4">
    <div class="flex justify-between items-center mb-4">
      <h3 class="text-xl font-semibold">
        <i class="fas fa-trophy mr-2"></i>{{ $t('m.Recent_Contest') }}
      </h3>
      <!-- Add refresh button if needed -->
    </div>
    <div v-if="loading" class="text-center">
      <p>Loading contests...</p>
    </div>
    <div v-else-if="contests.length === 0">
      <p>{{ $t('m.No_recent_contests') }}</p>
    </div>
    <div v-else class="space-y-4">
      <div v-for="contest in contests" :key="contest.id"
           :class="['p-4 border rounded-lg shadow-sm hover:shadow-md transition-shadow', contestStatusClass(contest.status)]">
        <div class="flex justify-between items-start">
          <a @click="goContest(contest.id)" class="text-lg font-semibold text-blue-600 hover:text-blue-800 hover:underline cursor-pointer break-all">
            {{ contest.title }}
          </a>
          <span :style="{ backgroundColor: getContestStatus(contest.status).color, color: 'white' }" class="px-2 py-1 text-xs font-bold rounded">
            <i class="far fa-circle mr-1"></i>{{ $t('m.' + getContestStatus(contest.status).name) }}
          </span>
        </div>
        <div class="mt-2 text-sm text-gray-600">
          <div class="flex items-center space-x-2 mb-1">
             <!-- Contest Type (ACM/OI) -->
             <button @click="goContestList(contest.type)"
                     :class="['px-2 py-0.5 text-xs rounded-full text-white', contest.type === 0 ? 'bg-blue-500 hover:bg-blue-600' : 'bg-yellow-500 hover:bg-yellow-600']">
                 <i class="fas fa-trophy mr-1"></i>{{ parseContestProblemType(contest.type) }}
             </button>
             <!-- Contest Auth (Public/Private/Protected) -->
             <span :class="['px-2 py-0.5 text-xs rounded-full border', getContestAuth(contest.auth).borderColor, getContestAuth(contest.auth).textColor, getContestAuth(contest.auth).bgColor]">
                 {{ $t('m.' + getContestAuth(contest.auth).name) }}
             </span>

          </div>
          <ul class="flex flex-wrap gap-2 items-center">
            <li class="flex items-center">
              <button class="px-2 py-1 text-xs bg-blue-100 text-blue-700 rounded-full hover:bg-blue-200">
                 <i class="far fa-calendar-alt mr-1"></i>{{ formatContestTime(contest.startTime) }}
              </button>
            </li>
            <li class="flex items-center">
               <button class="px-2 py-1 text-xs bg-green-100 text-green-700 rounded-full hover:bg-green-200">
                 <i class="far fa-clock mr-1"></i>{{ getContestDuration(contest.startTime, contest.endTime) }}
               </button>
            </li>
            <li v-if="contest.count != null" class="flex items-center">
              <button class="px-2 py-1 text-xs bg-gray-100 text-gray-700 rounded-full hover:bg-gray-200">
                 <i class="fas fa-user-friends mr-1 text-blue-500"></i>x{{ contest.count }}
              </button>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/common/api';
import { CONTEST_STATUS_REVERSE, CONTEST_TYPE_REVERSE } from '@/common/constants';
import { getDuration, formatTime, parseContestType as parseContestProblemTypeUtil } from '@/common/utils'; // Renamed to avoid conflict
import { mapGetters } from 'vuex';

export default {
  name: 'RecentContests',
  data() {
    return {
      loading: false,
      contests: [],
    };
  },
  computed: {
     ...mapGetters(['isAuthenticated', 'websiteConfig']), // Assuming websiteConfig might be used for something
  },
  mounted() {
    this.fetchRecentContests();
  },
  methods: {
    fetchRecentContests() {
      this.loading = true;
      api.getRecentContests().then(
        (res) => {
          this.contests = res.data.data || [];
          this.loading = false;
        },
        (err) => {
          console.error('Failed to load recent contests:', err);
          this.contests = [];
          this.loading = false;
        }
      );
    },
    getContestStatus(status) {
      return CONTEST_STATUS_REVERSE[status] || { name: 'Error', color: '#000' };
    },
    contestStatusClass(status) {
     if (status === 0) return 'border-green-500 bg-green-50'; // Running
     if (status === -1) return 'border-yellow-500 bg-yellow-50'; // Scheduled
     return 'border-gray-300'; // Ended or other
    },
    getContestAuth(authVal) {
       // CONTEST_TYPE_REVERSE in original seems to be for auth (public, private, protected)
       const auth = CONTEST_TYPE_REVERSE[authVal] || { name: 'Unknown', tips: 'Unknown' };
       // Map Element UI colors to Tailwind - this is an approximation
       let borderColor = 'border-gray-400';
       let textColor = 'text-gray-700';
       let bgColor = 'bg-gray-100';
       if (auth.name === 'Public') {
         borderColor = 'border-green-500'; textColor = 'text-green-700'; bgColor = 'bg-green-50';
       } else if (auth.name === 'Private') {
         borderColor = 'border-red-500'; textColor = 'text-red-700'; bgColor = 'bg-red-50';
       } else if (auth.name === 'Protected') {
         borderColor = 'border-yellow-500'; textColor = 'text-yellow-700'; bgColor = 'bg-yellow-50';
       }
       return { name: auth.name, tips: auth.tips, borderColor, textColor, bgColor };
    },
    parseContestProblemType(type) {
      // This is for contest type like ACM/OI, not auth type
      return parseContestProblemTypeUtil(type);
    },
    getContestDuration(startTime, endTime) {
      return getDuration(startTime, endTime);
    },
    formatContestTime(time) {
      return formatTime(time, 'MM-DD HH:mm');
    },
    goContest(contestID) {
      if (!this.isAuthenticated) {
         // In Vue 3, $i18n might be globally available or injected.
         // For now, let's assume a simple alert or a global message handler.
         alert(this.$i18n.t('m.Please_login_first')); // Replace with proper message later
         this.$store.dispatch('changeModalStatus', { visible: true, mode: 'Login' }); // Assuming this action exists
      } else {
         this.$router.push({ name: 'ContestDetails', params: { contestID: contestID } });
      }
    },
    goContestList(type) {
      // type here refers to contest problem type (ACM/OI)
      this.$router.push({ name: 'ContestList', query: { type: type } });
    }
  },
};
</script>

<style scoped>
 /* Add FontAwesome if not already globally available */
/* @import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css'); */
</style>
