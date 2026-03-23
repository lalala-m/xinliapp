<template>
  <div class="mod__process">
    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-input v-model="state.dataForm.processName" :placeholder="$t('process.name')"></el-input>
      </el-form-item>
      <el-form-item>
        <el-input v-model="state.dataForm.key" :placeholder="$t('process.key')"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="state.getDataList()">{{ $t("query") }}</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="state.dataListLoading" :data="state.dataList" border @selection-change="state.dataListSelectionChangeHandle" @sort-change="state.dataListSortChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="name" :label="$t('process.name')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="key" :label="$t('process.key')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="version" :label="$t('process.version')" header-align="center" align="center">
        <template v-slot="scope">
          <el-tag>v{{ scope.row.version }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="deploymentTime" :label="$t('process.deploymentTime')" header-align="center" align="center" width="180"></el-table-column>
      <el-table-column :label="$t('handle')" fixed="right" header-align="center" align="center" width="150">
        <template v-slot="scope">
          <el-button type="primary" link @click="createProcessInstance(scope.row)">{{ $t("process.createInstance") }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"> </el-pagination>
  </div>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { reactive, toRefs } from "vue";
import { IObject } from "@/types/interface";
import { registerDynamicToRouterAndNext } from "@/router";

const view = reactive({
  getDataListURL: "/flow/common/start/page",
  getDataListIsPage: true,
  dataForm: {
    processName: "",
    key: ""
  }
});
const state = reactive({ ...useView(view), ...toRefs(view) });

// 发起流程
const createProcessInstance = (row: IObject) => {
  const routeParams = {
    path: `/flow/task-form`,
    query: {
      processDefinitionId: row.id,
      processDefinitionKey: row.key,
      _mt: `${row.name}`
    }
  };
  registerDynamicToRouterAndNext(routeParams);
};
</script>
