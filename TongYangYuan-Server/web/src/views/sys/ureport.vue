<template>
  <div class="mod-ureport__model">
    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-input v-model="state.dataForm.fileName" :placeholder="$t('fileName')" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="state.getDataList()">{{ $t("query") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="danger" @click="state.deleteHandle()">{{ $t("deleteBatch") }}</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="state.dataListLoading" :data="state.dataList" border @selection-change="state.dataListSelectionChangeHandle" @sort-change="state.dataListSortChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="fileName" :label="$t('fileName')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="createDate" :label="$t('createDate')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="updateDate" :label="$t('updateDate')" header-align="center" align="center"></el-table-column>
      <el-table-column :label="$t('handle')" fixed="right" header-align="center" align="center" width="220">
        <template v-slot="scope">
          <el-button type="primary" link @click="onDesigner(scope.row)">{{ $t("design") }}</el-button>
          <el-button type="primary" link @click="onPreview(scope.row)">{{ $t("preview") }}</el-button>
          <el-button type="primary" link @click="onUrl(scope.row)">{{ $t("menu.url") }}</el-button>
          <el-button type="primary" link @click="state.deleteHandle(scope.row.id)">{{ $t("delete") }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"> </el-pagination>
  </div>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { reactive, toRefs } from "vue";
import app from "@/constants/app";
import { IObject } from "@/types/interface";
import { registerDynamicToRouterAndNext } from "@/router";
import { ElMessageBox } from "element-plus";

const view = reactive({
  getDataListURL: "/sys/ureport2/page",
  getDataListIsPage: true,
  deleteURL: "/sys/ureport2",
  deleteIsBatch: true,
  dataForm: {
    fileName: ""
  }
});

const state = reactive({ ...useView(view), ...toRefs(view) });

const onPreview = (row: IObject) => {
  registerDynamicToRouterAndNext({
    path: "/iframe",
    query: {
      url: `${app.api}/sys/ureport/preview?_u=renren-${row.fileName}`,
      _mt: "preview-" + row.fileName
    }
  });
};
const onDesigner = (row: IObject) => {
  registerDynamicToRouterAndNext({
    path: "/iframe",
    query: {
      url: `${app.api}/sys/ureport/designer?_u=renren-${row.fileName}`,
      _mt: "designer-" + row.fileName
    }
  });
};

const onUrl = (row: IObject) => {
  ElMessageBox.alert("{{ApiUrl}}/sys/ureport/preview?_u=renren-" + row.fileName, "菜单路由", {
    confirmButtonText: "确定"
  });
};
</script>
