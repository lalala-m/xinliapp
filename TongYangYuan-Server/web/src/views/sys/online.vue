<template>
  <div class="mod-sys__post">
    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-input v-model="state.dataForm.username" :placeholder="$t('online.username')" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="state.getDataList()">{{ $t("query") }}</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="state.dataListLoading" :data="state.dataList" border @selection-change="state.dataListSelectionChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="userId" :label="$t('online.userId')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="username" :label="$t('online.username')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="realName" :label="$t('online.realName')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="loginDate" :label="$t('online.loginDate')" sortable="custom" header-align="center" align="center"></el-table-column>
      <el-table-column prop="expireDate" :label="$t('online.expireDate')" sortable="custom" header-align="center" align="center"></el-table-column>
      <el-table-column :label="$t('handle')" fixed="right" header-align="center" align="center" width="150">
        <template v-slot="scope">
          <el-button type="primary" link @click="logoutHandle(scope.row.userId)">{{ $t("online.kickOut") }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"> </el-pagination>
  </div>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { reactive, toRefs } from "vue";
import baseService from "@/service/baseService";
import { ElMessage, ElMessageBox } from "element-plus";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const view = reactive({
  getDataListURL: "/sys/online/page",
  getDataListIsPage: true,
  deleteIsBatch: false,
  dataForm: {
    username: ""
  }
});

const state = reactive({ ...useView(view), ...toRefs(view) });

const logoutHandle = (id: string) => {
  ElMessageBox.confirm(t("prompt.info", { handle: t("online.kickOut") }), t("prompt.title"), {
    confirmButtonText: t("confirm"),
    cancelButtonText: t("cancel"),
    type: "warning"
  })
    .then(() => {
      baseService.post("/sys/online/logout?id=" + id).then(() => {
        ElMessage.success({
          message: t("prompt.success"),
          duration: 500,
          onClose: () => {
            state.query();
          }
        });
      });
    })
    .catch(() => {
      //
    });
};
</script>
