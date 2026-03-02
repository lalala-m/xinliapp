<template>
  <div class="mod-message__mail-template">
    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-input v-model="state.dataForm.name" :placeholder="$t('mail.name')" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="state.getDataList()">{{ $t("query") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="addOrUpdateHandle()">{{ $t("add") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="configHandle()">{{ $t("mail.config") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="danger" @click="state.deleteHandle()">{{ $t("deleteBatch") }}</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="state.dataListLoading" :data="state.dataList" border @selection-change="state.dataListSelectionChangeHandle" @sort-change="state.dataListSortChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="name" :label="$t('mail.name')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="subject" :label="$t('mail.subject')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="createDate" :label="$t('mail.createDate')" sortable="custom" header-align="center" align="center" width="180"></el-table-column>
      <el-table-column :label="$t('handle')" fixed="right" header-align="center" align="center" width="180">
        <template v-slot="scope">
          <el-button type="primary" link @click="addOrUpdateHandle(scope.row.id)">{{ $t("update") }}</el-button>
          <el-button type="primary" link @click="sendHandle(scope.row.id)">{{ $t("mail.send") }}</el-button>
          <el-button type="primary" link @click="state.deleteHandle(scope.row.id)">{{ $t("delete") }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"> </el-pagination>
    <!-- 弹窗, 新增 / 修改 -->
    <add-or-update ref="addOrUpdateRef" @refreshDataList="state.getDataList"></add-or-update>
    <!-- 弹窗, 邮件配置 -->
    <config ref="configRef"></config>
    <!-- 弹窗, 发送邮件 -->
    <send :key="sendKey" ref="sendRef" @refreshDataList="state.getDataList"></send>
  </div>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { nextTick, reactive, ref, toRefs } from "vue";
import AddOrUpdate from "./mail-template-add-or-update.vue";
import Config from "./mail-template-config.vue";
import Send from "./mail-template-send.vue";

const configRef = ref();
const sendRef = ref();
const sendKey = ref(0);

const view = reactive({
  getDataListURL: "/message/mailtemplate/page",
  getDataListIsPage: true,
  deleteURL: "/message/mailtemplate",
  deleteIsBatch: true,
  dataForm: {
    name: ""
  }
});

const state = reactive({ ...useView(view), ...toRefs(view) });

// 邮件配置
const configHandle = () => {
  configRef.value.init();
};

// 发送邮件
const sendHandle = (id: string) => {
  sendKey.value++;
  nextTick(() => {
    sendRef.value.dataForm.id = id;
    sendRef.value.init();
  });
};

const addOrUpdateRef = ref();
const addOrUpdateHandle = (id?: number) => {
  addOrUpdateRef.value.init(id);
};
</script>
