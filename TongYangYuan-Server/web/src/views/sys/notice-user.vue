<template>
  <div class="mod-demo__sysnoticeuser">
    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <ren-select v-model="state.dataForm.noticeType" dict-type="notice_type" :placeholder="$t('notice.type')"></ren-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="state.getDataList()">{{ $t("query") }}</el-button>
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
      <el-table-column prop="readStatus" :label="$t('notice.readStatus')" header-align="center" align="center" width="130">
        <template v-slot="scope">
          <el-tag v-if="scope.row.readStatus === 0" type="danger">{{ $t("notice.readStatus0") }}</el-tag>
          <el-tag v-else type="success">{{ $t("notice.readStatus1") }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('handle')" fixed="right" header-align="center" align="center" width="150">
        <template v-slot="scope">
          <el-button type="primary" link @click="viewHandle(scope.row)">{{ $t("notice.view") }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"> </el-pagination>
  </div>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { reactive, toRefs } from "vue";
import { IObject } from "@/types/interface";
import baseService from "@/service/baseService";
import { registerDynamicToRouterAndNext } from "@/router";
import { useI18n } from "vue-i18n";
const { t } = useI18n();

const view = reactive({
  getDataListURL: "/sys/notice/mynotice/page",
  getDataListIsPage: true,
  dataForm: {
    noticeType: ""
  }
});

const state = reactive({ ...useView(view), ...toRefs(view) });

const viewHandle = (row: IObject) => {
  // 路由参数
  const routeParams = {
    path: "/sys/notice-user-view",
    query: {
      id: row.id,
      _mt: t("notice.view2")
    }
  };

  // 如果未读，则标记为已读
  if (row.readStatus === 0) {
    updateReadStatus(row.id);
  }

  // 动态路由
  registerDynamicToRouterAndNext(routeParams);
};

const updateReadStatus = (noticeId: string) => {
  baseService.put("/sys/notice/mynotice/read/" + noticeId).then(() => {
    //
  });
};
</script>
