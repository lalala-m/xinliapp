<template>
  <el-dialog v-model="state.visibleSelect" class="rr-dialog" :title="$t('tenant.switch')" :close-on-click-modal="false" :close-on-press-escape="false" :append-to-body="true">
    <div>
      <el-form :model="state.dataForm" :inline="true" @keyup.enter="state.getDataList()">
        <el-form-item>
          <el-input v-model="state.dataForm.tenantName" :placeholder="$t('tenant.tenantName')"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button @click="state.getDataList()">{{ $t("query") }}</el-button>
        </el-form-item>
      </el-form>
      <el-table v-loading="state.dataListLoading" :data="state.dataList" highlight-current-row border @current-change="currentChangeHandle" style="width: 100%">
        <el-table-column prop="id" :label="$t('tenant.tenantCode')" header-align="center" align="center"></el-table-column>
        <el-table-column prop="tenantName" :label="$t('tenant.tenantName')" header-align="center" align="center"></el-table-column>
        <el-table-column prop="status" :label="$t('tenant.status')" header-align="center" align="center">
          <template v-slot="scope">
            <el-tag v-if="scope.row.status === 0" type="danger">{{ $t("tenant.status0") }}</el-tag>
            <el-tag v-else type="success">{{ $t("tenant.status1") }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"></el-pagination>
    </div>
    <template v-slot:footer>
      <el-button @click="cancelHandle()">{{ $t("cancel") }}</el-button>
      <el-button type="primary" @click="commitHandle()">{{ $t("confirm") }}</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { nextTick, reactive, ref, toRefs } from "vue";
import { IObject } from "@/types/interface";
import { setCache } from "@/utils/cache";
import { CacheTenantCode } from "@/constants/cacheKey";
import { ElMessage } from "element-plus";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const view = reactive({
  visibleSelect: false,
  currentRow: null as IObject | null,
  getDataListURL: "/sys/tenant/page",
  getDataListIsPage: true,
  dataForm: {
    tenantName: ""
  }
});

const state = reactive({ ...useView(view), ...toRefs(view) });

const init = () => {
  state.visibleSelect = true;
  nextTick(() => {
    state.getDataList();
  });
};

const cancelHandle = () => {
  state.visibleSelect = false;
};

const currentChangeHandle = (val: IObject) => {
  state.currentRow = val;
};

const commitHandle = () => {
  if (!state.currentRow) {
    ElMessage.warning(t("tenant.select"));
  } else {
    setCache(CacheTenantCode, state.currentRow.id);
    window.location.reload();
  }
};

defineExpose({
  init
});
</script>

<style lang="less">
.el-table th {
  background-color: #ffffff;
}
</style>
