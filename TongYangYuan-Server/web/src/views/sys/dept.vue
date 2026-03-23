<template>
  <div class="mod-sys__dept">
    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-button v-if="state.hasPermission('sys:dept:save')" type="primary" @click="addOrUpdateHandle()">{{ $t("add") }}</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="state.dataListLoading" :data="state.dataList" row-key="id" border style="width: 100%">
      <el-table-column prop="name" :label="$t('dept.name')" header-align="center" min-width="150"></el-table-column>
      <el-table-column prop="parentName" :label="$t('dept.parentName')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="sort" :label="$t('dept.sort')" header-align="center" align="center" width="80"></el-table-column>
      <el-table-column :label="$t('handle')" fixed="right" header-align="center" align="center" width="150">
        <template v-slot="scope">
          <el-button v-if="state.hasPermission('sys:dept:update')" type="primary" link @click="addOrUpdateHandle(scope.row.id)">{{ $t("update") }}</el-button>
          <el-button v-if="state.hasPermission('sys:dept:delete')" type="primary" link @click="state.deleteHandle(scope.row.id)">{{ $t("delete") }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 弹窗, 新增 / 修改 -->
    <add-or-update ref="addOrUpdateRef" @refreshDataList="state.getDataList"></add-or-update>
  </div>
</template>

<script lang="ts" setup>
import { reactive, ref, toRefs } from "vue";
import AddOrUpdate from "./dept-add-or-update.vue";
import useView from "@/hooks/useView";

const view = reactive({
  getDataListURL: "/sys/dept/list",
  deleteURL: "/sys/dept"
});

const state = reactive({ ...useView(view), ...toRefs(view) });

const addOrUpdateRef = ref();
const addOrUpdateHandle = (id?: number) => {
  addOrUpdateRef.value.init(id);
};
</script>
