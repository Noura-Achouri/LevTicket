<template>
  <el-dialog :model-value="modelValue" @update:model-value="$emit('update:modelValue', $event)" :title="$t('Leave a Review')" width="400px">
    <el-form :model="form" label-position="top">
      <el-form-item :label="$t('Rating')">
        <el-rate v-model="form.starRating" />
      </el-form-item>
      <el-form-item :label="$t('Comment')">
        <el-input v-model="form.comment" type="textarea" :rows="3" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="$emit('update:modelValue', false)">{{ $t('Cancel') }}</el-button>
      <el-button type="primary" :loading="submitting" :disabled="!form.starRating" @click="submit">{{ $t('Submit') }}</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { submitReview } from '@/api/reviews'
import { ElMessage } from 'element-plus'
import { t } from '@/i18n'

const props = defineProps({
  modelValue: Boolean,
  order: Object
})
const emit = defineEmits(['update:modelValue', 'submitted'])

const form = reactive({ starRating: 5, comment: '' })
const submitting = ref(false)

async function submit() {
  if (!props.order || submitting.value) return
  submitting.value = true
  try {
    const response = await submitReview(props.order.id, props.order.sellerId, form.starRating, form.comment.trim())
    ElMessage.success(t('Review submitted'))
    form.starRating = 5
    form.comment = ''
    emit('update:modelValue', false)
    emit('submitted', response.data)
  } catch (err) {
    ElMessage.error(err.message || t('Failed to submit review'))
  } finally {
    submitting.value = false
  }
}
</script>