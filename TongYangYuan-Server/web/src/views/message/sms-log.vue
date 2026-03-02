<template>
  <div class="mod-demo__syssmslog">
    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-input v-model="state.dataForm.smsCode" :placeholder="$t('sms.smsCode')" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-input v-model="state.dataForm.mobile" :placeholder="$t('sms.mobile')" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-select v-model="state.dataForm.status" :placeholder="$t('sms.status')" clearable>
          <el-option :label="$t('sms.status1')" :value="1"></el-option>
          <el-option :label="$t('sms.status0')" :value="0"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="state.getDataList()">{{ $t("query") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button v-if="state.hasPermission('sys:smslog:all')" type="danger" @click="state.deleteHandle()">{{ $t("deleteBatch") }}</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="state.dataListLoading" :data="state.dataList" border @selection-change="state.dataListSelectionChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="smsCode" :label="$t('sms.smsCode')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="mobile" :label="$t('sms.mobile')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="params1" :label="$t('sms.params1')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="params2" :label="$t('sms.params2')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="params3" :label="$t('sms.params3')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="params4" :label="$t('sms.params4')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="status" :label="$t('sms.status')" header-align="center" align="center">
        <template v-slot="scope">
          <el-tag v-if="scope.row.status === 1" size="small">{{ $t("sms.status1") }}</el-tag>
          <el-tag v-else size="small" type="danger">{{ $t("sms.status0") }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createDate" :label="$t('sms.createDate')" header-align="center" align="center" width="170"></el-table-column>
      <el-table-column :label="$t('handle')" fixed="right" header-align="center" align="center" width="150">
        <template v-slot="scope">
          <el-button v-if="state.hasPermission('sys:smslog:all')" type="primary" link @click="state.deleteHandle(scope.row.id)">{{ $t("delete") }}</el-button>
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
  getDataListURL: "/message/smslog/page",
  getDataListIsPage: true,
  deleteURL: "/message/smslog",
  deleteIsBatch: true,
  dataForm: {
    mobile: "",
    status: "",
    smsCode: ""
  }
});

const state = reactive({ ...useView(view), ...toRefs(view) });
</script>
