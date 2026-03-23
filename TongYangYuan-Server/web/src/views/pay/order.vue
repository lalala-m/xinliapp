<template>
  <div class="mod-pay__order">
    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-input v-model="state.dataForm.orderId" :placeholder="$t('order.orderId')" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-input v-model="state.dataForm.userId" :placeholder="$t('order.userId')" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <ren-select v-model="state.dataForm.status" dict-type="order_status" :placeholder="$t('order.status')"></ren-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="state.getDataList()">{{ $t("query") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="addOrUpdateHandle()">{{ $t("add") }}</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="state.dataListLoading" :data="state.dataList" show-overflow-tooltip border @selection-change="state.dataListSelectionChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="orderId" :label="$t('order.orderId')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="productName" :label="$t('order.productName')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="payAmount" :label="$t('order.payAmount')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="status" :label="$t('order.status')" header-align="center" align="center">
        <template v-slot="scope">
          {{ state.getDictLabel("order_status", scope.row.status) }}
        </template>
      </el-table-column>
      <el-table-column prop="payAt" :label="$t('order.payAt')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="createDate" :label="$t('order.createDate')" header-align="center" align="center"></el-table-column>
      <el-table-column :label="$t('handle')" fixed="right" header-align="center" align="center" width="230">
        <template v-slot="scope">
          <el-button v-if="scope.row.status === 0" type="primary" link @click="payHandle(scope.row.orderId)">支付宝</el-button>
          <el-button v-if="scope.row.status === 0" type="primary" link @click="weChatHandle(scope.row.orderId)">微信扫码</el-button>
          <el-button v-if="scope.row.status === 0" type="primary" link @click="state.deleteHandle(scope.row.id)">{{ $t("delete") }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"> </el-pagination>
    <!-- 弹窗, 新增 / 修改 -->
    <add-or-update ref="addOrUpdateRef" @refreshDataList="state.getDataList"></add-or-update>
    <el-dialog v-model="weChatVisible" title="微信扫码支付" :close-on-click-modal="false" :close-on-press-escape="false" :width="230">
      <qrcode-vue :value="codeUrl" :size="200" level="H" />
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { reactive, ref, toRefs } from "vue";
import AddOrUpdate from "./order-add-or-update.vue";
import app from "@/constants/app";
import QrcodeVue from "qrcode.vue";
import baseService from "@/service/baseService";

const view = reactive({
  getDataListURL: "/sys/pay/order/page",
  getDataListIsPage: true,
  deleteURL: "/sys/pay/order",
  deleteIsBatch: true,
  dataForm: {
    orderId: "",
    status: "",
    userId: ""
  }
});

const state = reactive({ ...useView(view), ...toRefs(view) });

const payHandle = (orderId: string) => {
  window.open(`${app.api}/sys/pay/alipay/webPay?orderId=` + orderId);
};

const weChatVisible = ref(false);
const codeUrl = ref("");
const weChatHandle = (orderId: string) => {
  baseService.get(`/sys/pay/wechat/nativePay?orderId=${orderId}`).then((res) => {
    codeUrl.value = res.data;
    weChatVisible.value = true;
  });
};

const addOrUpdateRef = ref();
const addOrUpdateHandle = () => {
  addOrUpdateRef.value.init();
};
</script>
