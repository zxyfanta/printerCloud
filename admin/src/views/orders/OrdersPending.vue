<template>
  <OrdersBase
    page-title="待支付订单"
    :status-filter="0"
    :show-status-filter="false"
    :show-status-column="false"
  />
</template>

<script setup>
import OrdersBase from './OrdersBase.vue'
</script>
