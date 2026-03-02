<template>
  <div class="mod-message__mail-log">
    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-input v-model="state.dataForm.templateId" :placeholder="$t('mail.templateId')" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-input v-model="state.dataForm.mailTo" :placeholder="$t('mail.mailTo')" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-select v-model="state.dataForm.status" :placeholder="$t('mail.status')" clearable>
          <el-option :label="$t('mail.status1')" :value="1"></el-option>
          <el-option :label="$t('mail.status0')" :value="0"></el-option>
        </el-select>
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
      <el-table-column prop="templateId" :label="$t('mail.templateId')" sortable="custom" header-align="center" align="center"></el-table-column>
      <el-table-column prop="mailFrom" :label="$t('mail.mailFrom')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="mailTo" :label="$t('mail.mailTo')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="mailCc" :label="$t('mail.mailCc')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="subject" :label="$t('mail.subject')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="status" :label="$t('mail.status')" sortable="custom" header-align="center" align="center">
        <template v-slot="scope">
          <el-tag v-if="scope.row.status === 1" size="small">{{ $t("mail.status1") }}</el-tag>
          <el-tag v-else size="small" type="danger">{{ $t("mail.status0") }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createDate" :label="$t('mail.createDate')" sortable="custom" header-align="center" align="center" width="180"></el-table-column>
      <el-table-column :label="$t('handle')" fixed="right" header-align="center" align="center" width="150">
        <template v-slot="scope">
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

const view = reactive({
  getDataListURL: "/message/maillog/page",
  getDataListIsPage: true,
  deleteURL: "/message/maillog",
  deleteIsBatch: true,
  dataForm: {
    templateId: "",
    mailTo: "",
    status: null
  }
});

const state = reactive({ ...useView(view), ...toRefs(view) });
</script>
