<template>
  <div class="mod-pay__alipaynotifylog">
    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-input v-model="state.dataForm.outTradeNo" :placeholder="$t('order.outTradeNo')" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-input v-model="state.dataForm.notifyId" :placeholder="$t('order.notifyId')" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-input v-model="state.dataForm.tradeStatus" :placeholder="$t('order.tradeStatus')" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="state.getDataList()">{{ $t("query") }}</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="state.dataListLoading" show-overflow-tooltip :data="state.dataList" border @selection-change="state.dataListSelectionChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="outTradeNo" :label="$t('order.orderId')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="totalAmount" :label="$t('order.totalAmount')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="buyerPayAmount" :label="$t('order.buyerPayAmount')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="notifyId" :label="$t('order.notifyId')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="tradeNo" :label="$t('order.tradeNo')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="tradeStatus" :label="$t('order.tradeStatus')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="createDate" :label="$t('createDate')" header-align="center" align="center"></el-table-column>
    </el-table>
    <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"> </el-pagination>
  </div>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { reactive, toRefs } from "vue";

const view = reactive({
  getDataListURL: "/sys/pay/alipayNotifyLog/page",
  getDataListIsPage: true,
  deleteIsBatch: true,
  dataForm: {
    outTradeNo: "",
    notifyId: "",
    tradeStatus: ""
  }
});

const state = reactive({ ...useView(view), ...toRefs(view) });
</script>
