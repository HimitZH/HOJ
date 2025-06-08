<template>
  <div class="bg-white shadow-md rounded-lg p-4">
    <div class="mb-4">
      <h3 class="text-xl font-semibold">
        <i class="fas fa-cloud-upload-alt mr-2"></i>{{ $t('m.Supported_Remote_Online_Judge') }}
      </h3>
    </div>
    <div class="grid grid-cols-2 sm:grid-cols-3 gap-4">
      <a v-for="(oj, index) in remoteJudgeList" :key="index" :href="oj.url" target="_blank"
         class="block p-2 border rounded-lg hover:shadow-md transition-shadow"
         :class="oj.status ? 'border-blue-400 hover:border-blue-600' : 'border-red-400 hover:border-red-600'">
        <el-tooltip :content="oj.name" placement="top" :disabled="!oj.name">
          <img :src="oj.logo" :alt="oj.name" class="h-12 w-full object-contain">
          <!-- Fallback if image fails to load, though img alt is standard -->
          <!-- <div v-if="!oj.logo" class="text-center text-sm text-gray-500">{{ oj.name }}</div> -->
        </el-tooltip>
      </a>
    </div>
  </div>
</template>

<script>
// Import logos directly from src/assets
import hduLogo from '@/assets/hdu-logo.png';
import pojLogo from '@/assets/poj-logo.png';
import codeforcesLogo from '@/assets/codeforces-logo.png';
import gymLogo from '@/assets/gym-logo.png';
import atcoderLogo from '@/assets/atcoder-logo.png';
import spojLogo from '@/assets/spoj-logo.png';
import libreLogo from '@/assets/libre-logo.png';
// import defaultLogo from '@/assets/default.jpg'; // If you have a default/error logo

export default {
  name: 'SupportedOJ',
  data() {
    return {
      remoteJudgeList: [
        { url: 'http://acm.hdu.edu.cn', name: 'HDU', logo: hduLogo, status: true },
        { url: 'http://poj.org', name: 'POJ', logo: pojLogo, status: true },
        { url: 'https://codeforces.com', name: 'Codeforces', logo: codeforcesLogo, status: true },
        { url: 'https://codeforces.com/gyms', name: 'GYM', logo: gymLogo, status: true },
        { url: 'https://atcoder.jp', name: 'AtCoder', logo: atcoderLogo, status: true },
        { url: 'https://www.spoj.com', name: 'SPOJ', logo: spojLogo, status: true },
        { url: 'https://loj.ac/', name: 'LibreOJ', logo: libreLogo, status: true },
        // { url: 'http://erroroj.com', name: 'ErrorOJ', logo: defaultLogo, status: false },
      ]
    };
  }
  // No methods needed for getAssetUrl if importing directly
};
</script>

<style scoped>
 /* Add FontAwesome if not already globally available */
/* @import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css'); */
/* Ensure images are not overly large if object-contain doesn't constrain enough with h-12 */
img {
  max-height: 3rem; /* h-12 equivalent */
}
</style>
