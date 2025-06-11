<template>
  <OrdersBase
    page-title="已取消订单"
    :status-filter="4"
    :show-status-filter="false"
    :show-status-column="false"
  />
</template>

<script setup>
import OrdersBase from './OrdersBase.vue'
</script>
