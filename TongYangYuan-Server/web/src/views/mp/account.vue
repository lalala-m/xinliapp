<template>
  <div class="mod-mp__account">
    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-input v-model="state.dataForm.name" placeholder="名称" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-input v-model="state.dataForm.appId" placeholder="AppID" clearable></el-input>
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
    <el-table v-loading="state.dataListLoading" :data="state.dataList" border @selection-change="state.dataListSelectionChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="name" label="名称" header-align="center" align="center"></el-table-column>
      <el-table-column prop="appId" label="AppID" header-align="center" align="center"></el-table-column>
      <el-table-column prop="appSecret" label="AppSecret" header-align="center" align="center"></el-table-column>
      <el-table-column prop="token" label="Token" header-align="center" align="center"></el-table-column>
      <el-table-column prop="aesKey" label="EncodingAESKey" header-align="center" align="center"></el-table-column>
      <el-table-column prop="createDate" label="创建时间" header-align="center" align="center"></el-table-column>
      <el-table-column :label="$t('handle')" fixed="right" header-align="center" align="center" width="180">
        <template v-slot="scope">
          <el-button type="primary" link @click="addOrUpdateHandle(scope.row.id)">{{ $t("update") }}</el-button>
          <el-button type="primary" link @click="mpHandle(scope.row.appId)">接入</el-button>
          <el-button type="primary" link @click="state.deleteHandle(scope.row.id)">{{ $t("delete") }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"> </el-pagination>
    <!-- 弹窗, 新增 / 修改 -->
    <add-or-update ref="addOrUpdateRef" @refreshDataList="state.getDataList"></add-or-update>
  </div>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { reactive, ref, toRefs } from "vue";
import AddOrUpdate from "./account-add-or-update.vue";
import app from "@/constants/app";
import { ElMessageBox } from "element-plus";
import { useI18n } from "vue-i18n";
const { t } = useI18n();

const view = reactive({
  getDataListURL: "/sys/mp/account/page",
  getDataListIsPage: true,
  deleteURL: "/sys/mp/account",
  deleteIsBatch: true,
  dataForm: {
    name: "",
    appId: ""
  }
});

const state = reactive({ ...useView(view), ...toRefs(view) });

const mpHandle = (appId: any) => {
  ElMessageBox.alert(app.api + "/sys/mp/portal/" + appId, "公众号配置的URL", {
    confirmButtonText: t("confirm")
  });
};

const addOrUpdateRef = ref();
const addOrUpdateHandle = (id?: number) => {
  addOrUpdateRef.value.init(id);
};
</script>
