<template>
  <div class="mod__running">
    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-input v-model="state.dataForm.id" :placeholder="$t('running.id')"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="state.getDataList()">{{ $t("query") }}</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="state.dataListLoading" :data="state.dataList" border style="width: 100%">
      <el-table-column prop="processInstanceId" :label="$t('running.id')" header-align="center" align="center" width="320"></el-table-column>
      <el-table-column prop="processDefinitionName" :label="$t('running.processDefinitionName')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="startUserName" :label="$t('process.startUserId')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="suspended" :label="$t('running.suspended')" header-align="center" align="center">
        <template v-slot="scope">
          <el-tag v-if="scope.row.suspended" size="small">{{ $t("running.suspended1") }}</el-tag>
          <el-tag v-else size="small" type="danger">{{ $t("running.suspended0") }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('handle')" fixed="right" header-align="center" align="center" width="150">
        <template v-slot="scope">
          <el-button type="primary" link @click="state.deleteHandle(scope.row.processInstanceId)">{{ $t("delete") }}</el-button>
          <el-button type="primary" link @click="showDetail(scope.row)">{{ $t("process.detail") }}</el-button>
        </template>
      </el-table-column>
      <el-table-column prop="startTime" :label="$t('process.createTime')" header-align="center" align="center"></el-table-column>
    </el-table>
    <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"> </el-pagination>
  </div>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { reactive, toRefs } from "vue";
import { IObject } from "@/types/interface";

const view = reactive({
  getDataListURL: "/flow/running/page",
  getDataListIsPage: true,
  deleteURL: "/flow/running",
  dataForm: {
    id: ""
  }
});

const state = reactive({ ...useView(view), ...toRefs(view) });

const showDetail = (row: IObject) => {
  state.flowDetailRoute(row);
};
</script>
