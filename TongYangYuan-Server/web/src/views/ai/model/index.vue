<template>
  <div>
    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <ren-select v-model="state.dataForm.platform" dict-type="ai_platform" placeholder="所属平台"></ren-select>
      </el-form-item>
      <el-form-item>
        <el-input v-model="state.dataForm.name" placeholder="模型名称"></el-input>
      </el-form-item>
      <el-form-item>
        <el-input v-model="state.dataForm.model" placeholder="模型标识"></el-input>
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
      <el-table-column prop="platform" label="所属平台" header-align="center" align="center">
        <template v-slot="scope">
          {{ state.getDictLabel("ai_platform", scope.row.platform) }}
        </template>
      </el-table-column>
      <el-table-column prop="name" label="模型名称" header-align="center" align="center"></el-table-column>
      <el-table-column prop="model" label="模型标识" header-align="center" align="center"></el-table-column>
      <el-table-column prop="status" label="状态" header-align="center" align="center">
        <template v-slot="scope">
          {{ state.getDictLabel("enable_disable", scope.row.status) }}
        </template>
      </el-table-column>
      <el-table-column prop="createDate" label="创建时间" header-align="center" align="center"></el-table-column>
      <el-table-column label="操作" fixed="right" header-align="center" align="center" width="150">
        <template v-slot="scope">
          <el-button type="primary" link @click="addOrUpdateHandle(scope.row.id)">{{ $t("update") }}</el-button>
          <el-button type="primary" link @click="state.deleteHandle(scope.row.id)">{{ $t("delete") }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"> </el-pagination>
    <!-- 弹窗, 新增 / 修改 -->
    <add-or-update :key="addKey" ref="addOrUpdateRef" @refreshDataList="state.getDataList"></add-or-update>
  </div>
</template>

<script setup lang="ts">
import useView from "@/hooks/useView";
import { reactive, nextTick, ref, toRefs } from "vue";
import AddOrUpdate from "./add-or-update.vue";

const view = reactive({
  getDataListURL: "/sys/ai/model/page",
  getDataListIsPage: true,
  deleteURL: "/sys/ai/model",
  deleteIsBatch: true,
  dataForm: {
    platform: "",
    name: "",
    model: ""
  }
});

const state = reactive({ ...useView(view), ...toRefs(view) });

const addOrUpdateRef = ref();
const addKey = ref(0);
const addOrUpdateHandle = (id?: number) => {
  addKey.value++;
  nextTick(() => {
    addOrUpdateRef.value.init(id);
  });
};
</script>
