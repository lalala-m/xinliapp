<template>
  <div class="mod-sys__process">
    <el-table v-loading="state.dataListLoading" :data="state.dataList" border @selection-change="state.dataListSelectionChangeHandle" @sort-change="state.dataListSortChangeHandle" style="width: 100%">
      <el-table-column prop="activityName" :label="$t('process.taskName')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="assigneeName" :label="$t('process.assignee')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="startTime" :label="$t('task.startTime')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="endTime" :label="$t('task.endTime')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="comment" :label="$t('process.comment')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="durationInSeconds" :label="$t('task.durationInSeconds')" header-align="center" align="center" width="180"></el-table-column>
    </el-table>
    <el-pagination
      v-if="state.dataForm.pid === '0'"
      :current-page="state.page"
      :page-sizes="[10, 20, 50, 100]"
      :page-size="state.limit"
      :total="state.total"
      small="true"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="state.pageSizeChangeHandle"
      @current-change="state.pageCurrentChangeHandle"
    >
    </el-pagination>
  </div>
</template>
<style scoped>
.image {
  width: 60%;
  display: block;
  margin: 0 auto 30px auto;
}
</style>

<script lang="ts" setup>
import qs from "qs";
import useView from "@/hooks/useView";
import { onMounted, reactive, toRefs } from "vue";
import { useRoute } from "vue-router";
import { getToken } from "@/utils/cache";
import app from "@/constants/app";

const route = useRoute();
const view = reactive({
  getDataListURL: "/flow/common/historic/list",
  getDataListIsPage: false,
  createdIsNeed: false,
  dataForm: {
    processInstanceId: "",
    pid: ""
  }
});

const state = reactive({ ...useView(view), ...toRefs(view) });

onMounted(() => {
  init();
});

const init = () => {
  state.dataForm.processInstanceId = route.query.processInstanceId as string;
  getDiagramImage();
  state.getDataList();
};

const getDiagramImage = () => {
  const params = qs.stringify({
    access_token: getToken(),
    processInstanceId: state.dataForm.processInstanceId
  });
  return `${app.api}/flow/common/diagram/image?${params}`;
};
</script>
