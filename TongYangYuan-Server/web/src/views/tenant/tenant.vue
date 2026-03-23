<template>
  <div class="mod-tenant__tenant">
    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-input v-model="state.dataForm.tenantName" :placeholder="$t('tenant.tenantName')" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="state.getDataList()">{{ $t("query") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="addOrUpdateHandle()">{{ $t("add") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="danger" @click="state.deleteHandle()">{{ $t("deleteBatch") }}</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="state.dataListLoading" :data="state.dataList" border @selection-change="state.dataListSelectionChangeHandle" @sort-change="state.dataListSortChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="username" :label="$t('tenant.username')" sortable="custom" header-align="center" align="center"></el-table-column>
      <el-table-column prop="tenantName" :label="$t('tenant.tenantName')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="tenantMode" label="租户模式" header-align="center" align="center">
        <template v-slot="scope">
          <el-tag v-if="scope.row.tenantMode === 0">字段模式</el-tag>
          <el-tag v-else type="success">数据源模式</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" :label="$t('tenant.status')" sortable="custom" header-align="center" align="center">
        <template v-slot="scope">
          <el-tag v-if="scope.row.status === 0" size="small" type="danger">{{ $t("tenant.status0") }}</el-tag>
          <el-tag v-else size="small" type="success">{{ $t("tenant.status1") }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createDate" :label="$t('tenant.createDate')" sortable="custom" header-align="center" align="center" width="180"></el-table-column>
      <el-table-column :label="$t('handle')" fixed="right" header-align="center" align="center" width="150">
        <template v-slot="scope">
          <el-button type="primary" link @click="addOrUpdateHandle(scope.row.id)" :disabled="scope.row.id === '10000'">
            {{ $t("update") }}
          </el-button>
          <el-button type="primary" link @click="state.deleteHandle(scope.row.id)" :disabled="scope.row.id === '10000'">
            {{ $t("delete") }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"></el-pagination>
    <!-- 弹窗, 新增 / 修改 -->
    <add-or-update ref="addOrUpdateRef" @refreshDataList="state.getDataList"></add-or-update>
  </div>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { reactive, ref, toRefs } from "vue";
import AddOrUpdate from "./tenant-add-or-update.vue";

const view = reactive({
  getDataListURL: "/sys/tenant/page",
  getDataListIsPage: true,
  deleteURL: "/sys/tenant",
  deleteIsBatch: true,
  exportURL: "/sys/tenant/export",
  addOrUpdate: ref(),
  dataForm: {
    tenantName: ""
  }
});

const state = reactive({ ...useView(view), ...toRefs(view) });

const addOrUpdateRef = ref();
const addOrUpdateHandle = (id?: number) => {
  addOrUpdateRef.value.init(id);
};
</script>
