<template>
  <OrdersBase
    page-title="已完成订单"
    :status-filter="3"
    :show-status-filter="false"
    :show-status-column="false"
  />
</template>

<script setup>
import OrdersBase from './OrdersBase.vue'
</script>
