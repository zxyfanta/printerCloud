<template>
  <OrdersBase
    page-title="已支付订单"
    :status-filter="1"
    :show-status-filter="false"
    :show-status-column="false"
  />
</template>

<script setup>
import OrdersBase from './OrdersBase.vue'
</script>
