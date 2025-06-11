<template>
  <OrdersBase
    page-title="全部订单"
    :status-filter="''"
    :show-status-filter="true"
    :show-status-column="true"
  />
</template>

<script setup>
import OrdersBase from './OrdersBase.vue'
</script>
