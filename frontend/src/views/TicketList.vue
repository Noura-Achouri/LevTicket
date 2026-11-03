<template>
  <div class="ticket-list-page">
    <h2>{{ $t('Active Auctions') }}</h2>
    <el-row :gutter="16">
      <el-col :span="8" v-for="ticket in tickets" :key="ticket.id">
        <el-card @click="goToAuction(ticket.id)" style="cursor: pointer; margin-bottom: 16px;">
          <h3>{{ ticket.eventName }}</h3>
          <p>{{ ticket.categoryName }}</p>
          <p>{{ $t('Current bid') }}: ${{ ticket.currentBid }}</p>
          <el-tag type="success" v-if="ticket.status === 'active'">{{ $t('Active') }}</el-tag>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getTickets } from '@/api/tickets'

const router = useRouter()
const tickets = ref([])

function goToAuction(id) {
  router.push(`/auction/${id}`)
}

onMounted(async () => {
  const res = await getTickets({})
  tickets.value = res.data.tickets
})
</script>