<template>
  <div class="mod-sys__log-operation">
    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-input v-model="state.dataForm.module" :placeholder="$t('logOperation.module')" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-select v-model="state.dataForm.status" :placeholder="$t('logOperation.status')" clearable>
          <el-option :label="$t('logOperation.status0')" :value="0"></el-option>
          <el-option :label="$t('logOperation.status1')" :value="1"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="state.getDataList()">{{ $t("query") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="info" @click="state.exportHandle()">{{ $t("export") }}</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="state.dataListLoading" :data="state.dataList" show-overflow-tooltip border @sort-change="state.dataListSortChangeHandle" style="width: 100%">
      <el-table-column prop="creatorName" :label="$t('logOperation.creatorName')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="operation" :label="$t('logOperation.operation')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="module" :label="$t('logOperation.module')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="requestUri" :label="$t('logOperation.requestUri')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="requestMethod" :label="$t('logOperation.requestMethod')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="requestParams" :label="$t('logOperation.requestParams')" header-align="center" align="center" width="150"></el-table-column>
      <el-table-column prop="requestTime" :label="$t('logOperation.requestTime')" sortable="custom" header-align="center" align="center">
        <template v-slot="scope">
          {{ `${scope.row.requestTime}ms` }}
        </template>
      </el-table-column>
      <el-table-column prop="status" :label="$t('logOperation.status')" sortable="custom" header-align="center" align="center">
        <template v-slot="scope">
          <el-tag v-if="scope.row.status === 0" size="small" type="danger">{{ $t("logOperation.status0") }}</el-tag>
          <el-tag v-else size="small" type="success">{{ $t("logOperation.status1") }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="ip" :label="$t('logOperation.ip')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="userAgent" :label="$t('logOperation.userAgent')" header-align="center" align="center" width="150"></el-table-column>
      <el-table-column prop="createDate" :label="$t('logOperation.createDate')" sortable="custom" header-align="center" align="center" width="180"></el-table-column>
    </el-table>
    <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"> </el-pagination>
  </div>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { reactive, toRefs } from "vue";

const view = reactive({
  getDataListURL: "/sys/log/operation/page",
  getDataListIsPage: true,
  exportURL: "/sys/log/operation/export",
  dataForm: {
    module: "",
    status: ""
  }
});

const state = reactive({ ...useView(view), ...toRefs(view) });
</script>
