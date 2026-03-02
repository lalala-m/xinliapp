<template>
  <div class="mod-job__schedule">
    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-input v-model="state.dataForm.beanName" :placeholder="$t('schedule.beanName')" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="state.getDataList()">{{ $t("query") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button v-if="state.hasPermission('sys:schedule:save')" type="primary" @click="addOrUpdateHandle()">{{ $t("add") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button v-if="state.hasPermission('sys:schedule:delete')" type="danger" @click="state.deleteHandle()">{{ $t("deleteBatch") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button v-if="state.hasPermission('sys:schedule:pause')" type="danger" @click="pauseHandle()">{{ $t("schedule.pauseBatch") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button v-if="state.hasPermission('sys:schedule:resume')" type="danger" @click="resumeHandle()">{{ $t("schedule.resumeBatch") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button v-if="state.hasPermission('sys:schedule:run')" type="danger" @click="runHandle()">{{ $t("schedule.runBatch") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button v-if="state.hasPermission('sys:schedule:log')" type="success" @click="logHandle()">{{ $t("schedule.log") }}</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="state.dataListLoading" :data="state.dataList" show-overflow-tooltip border @selection-change="state.dataListSelectionChangeHandle" @sort-change="state.dataListSortChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="beanName" :label="$t('schedule.beanName')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="params" :label="$t('schedule.params')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="cronExpression" :label="$t('schedule.cronExpression')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="remark" :label="$t('schedule.remark')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="status" :label="$t('schedule.status')" sortable="custom" header-align="center" align="center">
        <template v-slot="scope">
          <el-tag v-if="scope.row.status === 1" size="small">{{ $t("schedule.status1") }}</el-tag>
          <el-tag v-else size="small" type="danger">{{ $t("schedule.status0") }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('handle')" fixed="right" header-align="center" align="center" width="248">
        <template v-slot="scope">
          <el-button v-if="state.hasPermission('sys:schedule:update')" type="primary" link @click="addOrUpdateHandle(scope.row.id)">{{ $t("update") }}</el-button>
          <el-button v-if="state.hasPermission('sys:schedule:pause')" type="primary" link @click="pauseHandle(scope.row.id)">{{ $t("schedule.pause") }}</el-button>
          <el-button v-if="state.hasPermission('sys:schedule:resume')" type="primary" link @click="resumeHandle(scope.row.id)">{{ $t("schedule.resume") }}</el-button>
          <el-button v-if="state.hasPermission('sys:schedule:run')" type="primary" link @click="runHandle(scope.row.id)">{{ $t("schedule.run") }}</el-button>
          <el-button v-if="state.hasPermission('sys:schedule:delete')" type="primary" link @click="state.deleteHandle(scope.row.id)">{{ $t("delete") }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"> </el-pagination>
    <!-- 弹窗, 新增 / 修改 -->
    <add-or-update ref="addOrUpdateRef" @refreshDataList="state.getDataList"></add-or-update>
    <!-- 弹窗, 日志列表 -->
    <log ref="logRef"></log>
  </div>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { reactive, ref, toRefs } from "vue";
import baseService from "@/service/baseService";
import { IObject } from "@/types/interface";
import AddOrUpdate from "./schedule-add-or-update.vue";
import Log from "./schedule-log.vue";
import { useI18n } from "vue-i18n";
import { ElMessage, ElMessageBox } from "element-plus";
const { t } = useI18n();

const logRef = ref();

const view = reactive({
  getDataListURL: "/job/schedule/page",
  getDataListIsPage: true,
  deleteURL: "/job/schedule",
  deleteIsBatch: true,
  dataForm: {
    beanName: ""
  }
});

const state = reactive({ ...useView(view), ...toRefs(view) });

// 暂停
const pauseHandle = (id?: string) => {
  if (!id && state.dataListSelections && state.dataListSelections.length <= 0) {
    return ElMessage({
      message: t("prompt.deleteBatch"),
      type: "warning",
      duration: 500
    });
  }
  ElMessageBox.confirm(t("prompt.info", { handle: t("schedule.pause") }), t("prompt.title"), {
    confirmButtonText: t("confirm"),
    cancelButtonText: t("cancel"),
    type: "warning"
  })
    .then(() => {
      baseService.put("/job/schedule/pause", id ? [id] : state.dataListSelections && state.dataListSelections.map((item: IObject) => item.id)).then(() => {
        ElMessage.success({
          message: t("prompt.success"),
          duration: 500,
          onClose: () => {
            state.getDataList();
          }
        });
      });
    })
    .catch(() => {
      //
    });
};

// 恢复
const resumeHandle = (id?: string) => {
  if (!id && state.dataListSelections && state.dataListSelections.length <= 0) {
    return ElMessage({
      message: t("prompt.deleteBatch"),
      type: "warning",
      duration: 500
    });
  }
  ElMessageBox.confirm(t("prompt.info", { handle: t("schedule.resume") }), t("prompt.title"), {
    confirmButtonText: t("confirm"),
    cancelButtonText: t("cancel"),
    type: "warning"
  })
    .then(() => {
      baseService.put("/job/schedule/resume", id ? [id] : state.dataListSelections && state.dataListSelections.map((item: IObject) => item.id)).then(() => {
        ElMessage.success({
          message: t("prompt.success"),
          duration: 500,
          onClose: () => {
            state.getDataList();
          }
        });
      });
    })
    .catch(() => {
      //
    });
};

// 执行
const runHandle = (id?: string) => {
  if (!id && state.dataListSelections && state.dataListSelections.length <= 0) {
    return ElMessage({
      message: t("prompt.deleteBatch"),
      type: "warning",
      duration: 500
    });
  }
  ElMessageBox.confirm(t("prompt.info", { handle: t("schedule.run") }), t("prompt.title"), {
    confirmButtonText: t("confirm"),
    cancelButtonText: t("cancel"),
    type: "warning"
  })
    .then(() => {
      baseService.put("/job/schedule/run", id ? [id] : state.dataListSelections && state.dataListSelections.map((item: IObject) => item.id)).then(() => {
        ElMessage.success({
          message: t("prompt.success"),
          duration: 500,
          onClose: () => {
            state.getDataList();
          }
        });
      });
    })
    .catch(() => {
      //
    });
};

// 日志列表
const logHandle = () => {
  logRef.value.init();
};

const addOrUpdateRef = ref();
const addOrUpdateHandle = (id?: number) => {
  addOrUpdateRef.value.init(id);
};
</script>
