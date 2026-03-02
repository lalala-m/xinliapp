<template>
  <div>
    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-input v-model="state.dataForm.outTradeNo" :placeholder="$t('order.outTradeNo')" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="state.getDataList()">{{ $t("query") }}</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="state.dataListLoading" :data="state.dataList" show-overflow-tooltip border @selection-change="state.dataListSelectionChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="outTradeNo" :label="$t('order.orderId')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="total" label="订单金额" header-align="center" align="center">
        <template v-slot="scope"> {{ scope.row.total / 100 }} <el-tag>元</el-tag> </template>
      </el-table-column>
      <el-table-column prop="tradeState" label="交易状态" header-align="center" align="center"></el-table-column>
      <el-table-column prop="transactionId" label="微信订单号" header-align="center" align="center"></el-table-column>
      <el-table-column prop="tradeType" label="交易类型" header-align="center" align="center"></el-table-column>
      <el-table-column prop="createDate" :label="$t('createDate')" header-align="center" align="center"></el-table-column>
    </el-table>
    <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"> </el-pagination>
  </div>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { reactive, toRefs } from "vue";

const view = reactive({
  getDataListURL: "/sys/pay/wechatNotifyLog/page",
  getDataListIsPage: true,
  deleteIsBatch: true,
  dataForm: {
    outTradeNo: ""
  }
});

const state = reactive({ ...useView(view), ...toRefs(view) });
</script>
