<template>
  <div class="mod-sys__rjdcustomerinfo">
    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-button @click="state.getDataList()">{{ $t("query") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="info" @click="state.exportHandle()">{{ $t("export") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button v-if="state.hasPermission('sys:rjdcustomerinfo:save')" type="primary" @click="addOrUpdateHandle()">{{ $t("add") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button v-if="state.hasPermission('sys:rjdcustomerinfo:delete')" type="danger" @click="state.deleteHandle()">{{ $t("deleteBatch") }}</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="state.dataListLoading" :data="state.dataList" border @selection-change="state.dataListSelectionChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="customerId" label="客户ID（与原系统一致）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="customerName" label="客户名称" header-align="center" align="center"></el-table-column>
      <el-table-column prop="customerCode" label="客户编码" header-align="center" align="center"></el-table-column>
      <el-table-column prop="contactPerson" label="对接人" header-align="center" align="center"></el-table-column>
      <el-table-column prop="contactPhone" label="联系电话" header-align="center" align="center"></el-table-column>
      <el-table-column prop="customerType" label="客户类型（如终端客户、经销商等）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="tenantCode" label="租户编码" header-align="center" align="center"></el-table-column>
      <el-table-column prop="status" label="状态 0：停用 1：正常" header-align="center" align="center"></el-table-column>
      <el-table-column prop="syncTime" label="从原系统同步的时间" header-align="center" align="center"></el-table-column>
      <el-table-column prop="creator" label="创建者（关联sys_user.id）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="createDate" label="创建时间" header-align="center" align="center"></el-table-column>
      <el-table-column prop="updater" label="更新者（关联sys_user.id）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="updateDate" label="更新时间" header-align="center" align="center"></el-table-column>
      <el-table-column prop="delFlag" label="删除标识  0：未删除    1：删除" header-align="center" align="center"></el-table-column>
      <el-table-column :label="$t('handle')" fixed="right" header-align="center" align="center" width="150">
        <template v-slot="scope">
          <el-button v-if="state.hasPermission('sys:rjdcustomerinfo:update')" type="primary" link @click="addOrUpdateHandle(scope.row.id)">{{ $t("update") }}</el-button>
          <el-button v-if="state.hasPermission('sys:rjdcustomerinfo:delete')" type="primary" link @click="state.deleteHandle(scope.row.id)">{{ $t("delete") }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"> </el-pagination>
    <!-- 弹窗, 新增 / 修改 -->
    <add-or-update :key="addKey" ref="addOrUpdateRef" @refreshDataList="state.getDataList"></add-or-update>
  </div>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { nextTick, reactive, ref, toRefs, watch } from "vue";
import AddOrUpdate from "./rjdcustomerinfo-add-or-update.vue";

const view = reactive({
  getDataListURL: "/sys/rjdcustomerinfo/page",
  getDataListIsPage: true,
  exportURL: "/sys/rjdcustomerinfo/export",
  deleteURL: "/sys/rjdcustomerinfo",
  deleteIsBatch: true,
  dataForm: {}
});

const state = reactive({ ...useView(view), ...toRefs(view) });

const addKey = ref(0);
const addOrUpdateRef = ref();
const addOrUpdateHandle = (customerId?: number) => {
  addKey.value++;
  nextTick(() => {
    addOrUpdateRef.value.init(customerId);
  });
};
</script>
