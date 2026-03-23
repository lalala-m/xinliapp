<template>
  <div class="mod-sys__log-error">
    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-input v-model="state.dataForm.module" :placeholder="$t('logError.module')" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="state.getDataList()">{{ $t("query") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="info" @click="state.exportHandle()">{{ $t("export") }}</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="state.dataListLoading" :data="state.dataList" show-overflow-tooltip border @sort-change="state.dataListSortChangeHandle" style="width: 100%">
      <el-table-column prop="module" :label="$t('logError.module')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="requestUri" :label="$t('logError.requestUri')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="requestMethod" :label="$t('logError.requestMethod')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="requestParams" :label="$t('logError.requestParams')" header-align="center" align="center" width="150"></el-table-column>
      <el-table-column prop="ip" :label="$t('logError.ip')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="userAgent" :label="$t('logError.userAgent')" header-align="center" align="center" width="150"></el-table-column>
      <el-table-column prop="createDate" :label="$t('logError.createDate')" sortable="custom" header-align="center" align="center" width="180"></el-table-column>
      <el-table-column :label="$t('handle')" fixed="right" header-align="center" align="center" width="150">
        <template v-slot="scope">
          <el-button type="primary" link @click="infoHandle(scope.row.errorInfo)">{{ $t("logError.errorInfo") }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-drawer v-model="drawer" :title="$t('logError.errorInfo')" :size="1000">
      <ren-code-editor v-model="drawerContent" language="java" :height="600"></ren-code-editor>
    </el-drawer>
    <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"> </el-pagination>
  </div>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { reactive, ref, toRefs } from "vue";
import { useI18n } from "vue-i18n";
import RenCodeEditor from "@/components/monaco-editor/index.vue";

const { t } = useI18n();

const view = reactive({
  getDataListURL: "/sys/log/error/page",
  getDataListIsPage: true,
  exportURL: "/sys/log/error/export",
  dataForm: {
    module: ""
  }
});

const state = reactive({ ...useView(view), ...toRefs(view) });

const drawer = ref(false);
const drawerContent = ref("");
const infoHandle = (info: string) => {
  drawerContent.value = info;
  drawer.value = true;
};
</script>

<style lang="less" scoped>
.mod-sys__log-error {
  &-view-info {
    width: 80%;
  }
}
</style>
