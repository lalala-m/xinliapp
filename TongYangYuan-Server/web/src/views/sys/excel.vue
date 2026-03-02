<template>
  <div class="mod-demo__excel">
    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-input v-model="state.dataForm.realName" :placeholder="$t('excel.realName')" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-input v-model="state.dataForm.userIdentity" :placeholder="$t('excel.identity')" clearable></el-input>
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
      <el-form-item>
        <el-button type="warning" @click="importHandle()">{{ $t("excel.import") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="info" @click="state.exportHandle()">{{ $t("export") }}</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="state.dataListLoading" :data="state.dataList" border @selection-change="state.dataListSelectionChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="realName" :label="$t('excel.realName')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="userIdentity" :label="$t('excel.identity')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="address" :label="$t('excel.address')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="joinDate" :label="$t('excel.joinDate')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="className" :label="$t('excel.className')" header-align="center" align="center"></el-table-column>
      <el-table-column :label="$t('handle')" fixed="right" header-align="center" align="center" width="150">
        <template v-slot="scope">
          <el-button type="primary" link @click="addOrUpdateHandle(scope.row.id)">{{ $t("update") }}</el-button>
          <el-button type="primary" link @click="state.deleteHandle(scope.row.id)">{{ $t("delete") }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"> </el-pagination>
    <import ref="importRef" @refreshDataList="state.getDataList"></import>
    <!-- 弹窗, 新增 / 修改 -->
    <add-or-update ref="addOrUpdateRef" @refreshDataList="state.getDataList"></add-or-update>
  </div>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { reactive, ref, toRefs } from "vue";
import Import from "./excel-import.vue";
import AddOrUpdate from "./excel-add-or-update.vue";

const view = reactive({
  getDataListURL: "/sys/excel/page",
  getDataListIsPage: true,
  exportURL: "/sys/excel/export",
  deleteURL: "/sys/excel",
  deleteIsBatch: true,
  dataForm: {
    realName: "",
    userIdentity: ""
  }
});

const state = reactive({ ...useView(view), ...toRefs(view) });

const importRef = ref();
const importHandle = () => {
  importRef.value.init();
};

const addOrUpdateRef = ref();
const addOrUpdateHandle = (id?: number) => {
  addOrUpdateRef.value.init(id);
};
</script>
