<template>
  <OrdersBase
    page-title="打印中订单"
    :status-filter="2"
    :show-status-filter="false"
    :show-status-column="false"
  />
</template>

<script setup>
import OrdersBase from './OrdersBase.vue'
</script>
