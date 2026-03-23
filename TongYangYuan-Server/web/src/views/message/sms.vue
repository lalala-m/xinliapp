<template>
  <div class="mod-message__sms">
    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-select v-model="state.dataForm.platform" :placeholder="$t('sms.platform')" clearable>
          <el-option :label="$t('sms.platform1')" :value="1"></el-option>
          <el-option :label="$t('sms.platform2')" :value="2"></el-option>
          <el-option :label="$t('sms.platform3')" :value="3"></el-option>
        </el-select>
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
    <el-table v-loading="state.dataListLoading" :data="state.dataList" border @selection-change="state.dataListSelectionChangeHandle" @sort-change="state.dataListSortChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="smsCode" :label="$t('sms.smsCode')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="platform" :label="$t('sms.platform')" header-align="center" align="center">
        <template v-slot="scope">
          <el-tag v-if="scope.row.platform === 1" size="small">{{ $t("sms.platform1") }}</el-tag>
          <el-tag v-else-if="scope.row.platform === 2" size="small">{{ $t("sms.platform2") }}</el-tag>
          <el-tag v-else-if="scope.row.platform === 3" size="small">{{ $t("sms.platform3") }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" header-align="center" align="center"></el-table-column>
      <el-table-column prop="createDate" :label="$t('sms.createDate')" sortable="custom" header-align="center" align="center" width="180"></el-table-column>
      <el-table-column :label="$t('handle')" fixed="right" header-align="center" align="center" width="180">
        <template v-slot="scope">
          <el-button type="primary" link @click="sendHandle(scope.row)">{{ $t("sms.send") }}</el-button>
          <el-button type="primary" link @click="addOrUpdateHandle(scope.row.id)">{{ $t("update") }}</el-button>
          <el-button type="primary" link @click="state.deleteHandle(scope.row.id)">{{ $t("delete") }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"> </el-pagination>
    <!-- 弹窗, 新增 / 修改 -->
    <add-or-update ref="addOrUpdateRef" @refreshDataList="state.getDataList"></add-or-update>
    <!-- 弹窗, 发送短信 -->
    <send ref="sendRef" @refreshDataList="state.getDataList"></send>
  </div>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { reactive, ref, toRefs } from "vue";
import AddOrUpdate from "./sms-add-or-update.vue";
import Send from "./sms-send.vue";
import { IObject } from "@/types/interface";

const sendRef = ref();

const view = reactive({
  getDataListURL: "/message/sms/page",
  getDataListIsPage: true,
  deleteURL: "/message/sms",
  deleteIsBatch: true,
  dataForm: {
    mobile: "",
    status: null,
    platform: ""
  }
});

const state = reactive({ ...useView(view), ...toRefs(view) });

// 发送短信
const sendHandle = (row: IObject) => {
  sendRef.value.dataForm.smsCode = row.smsCode;
  sendRef.value.init();
};

const addOrUpdateRef = ref();
const addOrUpdateHandle = (id?: number) => {
  addOrUpdateRef.value.init(id);
};
</script>
