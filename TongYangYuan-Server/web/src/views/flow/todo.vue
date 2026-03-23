<template>
  <div class="mod__process">
    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-input v-model="state.dataForm.definitionName" :placeholder="$t('process.name')"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="state.getDataList()">{{ $t("query") }}</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="state.dataListLoading" :data="state.dataList" border @selection-change="state.dataListSelectionChangeHandle" @sort-change="state.dataListSortChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="processDefinitionName" :label="$t('process.processDefinitionName')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="taskName" :label="$t('process.taskName')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="startUserName" :label="$t('process.startUserId')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="createTime" :label="$t('process.createTime')" header-align="center" align="center"></el-table-column>
      <el-table-column :label="$t('handle')" fixed="right" header-align="center" align="center" width="100">
        <template v-slot="scope">
          <el-button type="primary" link :processInstanceId="processInstanceId" @click="taskHandle(scope.row)">{{ $t("manage") }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"> </el-pagination>
  </div>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { reactive, ref, toRefs } from "vue";
import { IObject } from "@/types/interface";

const processInstanceId = ref("");

const view = reactive({
  getDataListURL: "/flow/common/todo/page",
  getDataListIsPage: true,
  activatedIsNeed: true,
  dataForm: {
    definitionName: "",
    taskId: ""
  }
});

const state = reactive({ ...useView(view), ...toRefs(view) });

// 处理
const taskHandle = (row: IObject) => {
  state.handleFlowRoute(row);
};
</script>
