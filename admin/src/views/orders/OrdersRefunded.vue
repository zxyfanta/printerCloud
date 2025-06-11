<template>
  <OrdersBase
    page-title="已退款订单"
    :status-filter="5"
    :show-status-filter="false"
    :show-status-column="false"
  />
</template>

<script setup>
import OrdersBase from './OrdersBase.vue'
</script>
