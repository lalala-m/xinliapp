<template>
  <div class="mod-demo__sysnotice">
    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <ren-select v-model="state.dataForm.noticeType" dict-type="notice_type" :placeholder="$t('notice.type')"></ren-select>
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
      <el-table-column prop="title" :label="$t('notice.title')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="noticeType" :label="$t('notice.type')" header-align="center" align="center" width="150">
        <template v-slot="scope">
          {{ state.getDictLabel("notice_type", scope.row.noticeType) }}
        </template>
      </el-table-column>
      <el-table-column prop="senderName" :label="$t('notice.senderName')" header-align="center" align="center" width="150"></el-table-column>
      <el-table-column prop="senderDate" :label="$t('notice.senderDate')" header-align="center" align="center" width="170"></el-table-column>
      <el-table-column prop="status" :label="$t('notice.status')" header-align="center" align="center" width="130">
        <template v-slot="scope">
          <el-tag v-if="scope.row.status === 0" type="danger">{{ $t("notice.status0") }}</el-tag>
          <el-tag v-else type="success">{{ $t("notice.status1") }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('handle')" fixed="right" header-align="center" align="center" width="150">
        <template v-slot="scope">
          <el-button v-if="scope.row.status === 0" type="primary" link @click="addOrUpdateHandle(scope.row.id)">{{ $t("update") }}</el-button>
          <el-button v-if="scope.row.status === 1" type="primary" link @click="viewHandle(scope.row)">{{ $t("notice.view") }}</el-button>
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
import AddOrUpdate from "./notice-add-or-update.vue";
import { IObject } from "@/types/interface";
import { registerDynamicToRouterAndNext } from "@/router";
import { useI18n } from "vue-i18n";
const { t } = useI18n();

const view = reactive({
  getDataListURL: "/sys/notice/page",
  getDataListIsPage: true,
  deleteURL: "/sys/notice",
  deleteIsBatch: true,
  dataForm: {
    noticeType: ""
  }
});

const state = reactive({ ...useView(view), ...toRefs(view) });

const viewHandle = (row: IObject) => {
  // 路由参数
  const routeParams = {
    path: "/sys/notice-view",
    query: {
      id: row.id,
      _mt: t("notice.view1")
    }
  };
  // 动态路由
  registerDynamicToRouterAndNext(routeParams);
};

const addOrUpdateRef = ref();
const addOrUpdateHandle = (id?: number) => {
  addOrUpdateRef.value.init(id);
};
</script>
