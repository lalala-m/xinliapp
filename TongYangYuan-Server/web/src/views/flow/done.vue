<template>
  <div class="mod__process">
    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-input v-model="state.dataForm.processDefinitionName" :placeholder="$t('process.name')"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="state.getDataList()">{{ $t("query") }}</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="state.dataListLoading" :data="state.dataList" show-overflow-tooltip border @selection-change="state.dataListSelectionChangeHandle" @sort-change="state.dataListSortChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="processInstanceId" :label="$t('running.id')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="processDefinitionName" :label="$t('process.processDefinitionName')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="processDefinitionVersion" :label="$t('process.processDefinitionVersion')" header-align="center" align="center">
        <template v-slot="scope">
          <el-tag>v{{ scope.row.processDefinitionVersion }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="startUserName" :label="$t('process.startUserId')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="endTime" :label="$t('process.endTime')" header-align="center" align="center"></el-table-column>
      <el-table-column :label="$t('handle')" fixed="right" header-align="center" align="center" width="150">
        <template v-slot="scope">
          <el-button type="primary" link @click="showDetail(scope.row)">{{ $t("process.detail") }}</el-button>
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

const view = reactive({
  getDataListURL: "/flow/common/done/page",
  getDataListIsPage: true,
  dataForm: {
    processDefinitionName: ""
  }
});

const state = reactive({ ...useView(view), ...toRefs(view) });

const showDetail = (row: IObject) => {
  state.flowDetailRoute(row);
};
</script>
