<template>
  <div class="mod-sys__menu">
    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-button v-if="state.hasPermission('sys:menu:save')" type="primary" @click="addOrUpdateHandle()">{{ $t("add") }}</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="state.dataListLoading" :data="state.dataList" row-key="id" border style="width: 100%">
      <el-table-column prop="name" :label="$t('menu.name')" header-align="center" min-width="150"></el-table-column>
      <el-table-column prop="icon" :label="$t('menu.icon')" header-align="center" align="center">
        <template v-slot="scope">
          <svg class="iconfont" aria-hidden="true">
            <use :xlink:href="`#${scope.row.icon}`"></use>
          </svg>
        </template>
      </el-table-column>
      <el-table-column prop="menuType" :label="$t('menu.type')" header-align="center" align="center">
        <template v-slot="scope">
          <el-tag v-if="scope.row.menuType === 0">{{ $t("menu.type0") }}</el-tag>
          <el-tag v-else type="info">{{ $t("menu.type1") }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="openStyle" :label="$t('menu.openStyle')" header-align="center" align="center">
        <template v-slot="scope">
          <span v-if="scope.row.menuType !== 0"></span>
          <el-tag v-else-if="scope.row.openStyle === 1">{{ $t("menu.openStyle1") }}</el-tag>
          <el-tag v-else type="info">{{ $t("menu.openStyle0") }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="menuHide" :label="$t('menu.menuHide')" header-align="center" align="center">
        <template v-slot="scope">
          <span v-if="scope.row.menuType !== 0"></span>
          <el-tag v-else-if="scope.row.menuHide === 0">{{ $t("menu.menuHide0") }}</el-tag>
          <el-tag v-else type="info">{{ $t("menu.menuHide1") }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sort" :label="$t('menu.sort')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="url" :label="$t('menu.url')" header-align="center" align="center" width="150"></el-table-column>
      <el-table-column prop="permissions" :label="$t('menu.permissions')" header-align="center" align="center" width="150"></el-table-column>
      <el-table-column :label="$t('handle')" fixed="right" header-align="center" align="center" width="170">
        <template v-slot="scope">
          <el-button v-if="state.hasPermission('sys:menu:save') && scope.row.menuType === 0" type="primary" link @click="addHandle(scope.row)">{{ $t("add") }}</el-button>
          <el-button v-if="state.hasPermission('sys:menu:update')" type="primary" link @click="addOrUpdateHandle(scope.row.id)">{{ $t("update") }}</el-button>
          <el-button v-if="state.hasPermission('sys:menu:delete')" type="primary" link @click="state.deleteHandle(scope.row.id)">{{ $t("delete") }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 弹窗, 新增 / 修改 -->
    <add-or-update ref="addOrUpdateRef" @refreshDataList="state.getDataList"></add-or-update>
  </div>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { reactive, ref, toRefs } from "vue";
import AddOrUpdate from "./menu-add-or-update.vue";

const view = reactive({
  getDataListURL: "/sys/menu/list",
  deleteURL: "/sys/menu"
});

const state = reactive({ ...useView(view), ...toRefs(view) });

const addOrUpdateRef = ref();
const addOrUpdateHandle = (id?: number) => {
  addOrUpdateRef.value.init(id);
};

const addHandle = (row: any) => {
  addOrUpdateRef.value.init2(row);
};
</script>
