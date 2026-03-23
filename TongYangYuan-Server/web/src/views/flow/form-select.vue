<template>
  <el-dialog v-model="state.visibleSelect" width="50%" :modal="false" title="选择流程表单" :close-on-click-modal="false" :close-on-press-escape="false">
    <div class="mod-form__select">
      <el-form>
        <el-form-item>
          <el-radio-group v-model="dataForm.formType">
            <el-radio-button label="0">流程表单</el-radio-button>
            <el-radio-button label="1">自定义表单</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div v-if="dataForm.formType === '0'">
        <el-form :inline="true" :model="dataForm" @keyup.enter="state.getDataList()">
          <el-form-item>
            <el-input v-model="dataForm.name" placeholder="名称"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button @click="state.getDataList()">{{ t("query") }}</el-button>
          </el-form-item>
        </el-form>
        <el-table v-loading="state.dataListLoading" :data="state.dataList" border @selection-change="state.dataListSelectionChangeHandle" @sort-change="state.dataListSortChangeHandle" style="width: 100%" size="small">
          <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
          <el-table-column prop="id" label="id" header-align="center" align="center"></el-table-column>
          <el-table-column prop="name" label="名称" header-align="center" align="center"></el-table-column>
          <el-table-column prop="remark" label="备注" header-align="center" align="center"></el-table-column>
          <el-table-column prop="createDate" label="创建时间" header-align="center" align="center"></el-table-column>
        </el-table>
        <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"> </el-pagination>
      </div>
      <div v-else>
        <el-form-item label="表单地址：">
          <el-input v-model="dataForm.formId" placeholder="如：/src/views/flow/form/correction.vue"></el-input>
        </el-form-item>
      </div>
    </div>
    <template v-slot:footer>
      <el-button @click="cancelHandle()">{{ t("cancel") }}</el-button>
      <el-button type="primary" @click="commitHandle()">{{ t("confirm") }}</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { reactive, toRefs, nextTick, ref } from "vue";
import { ElMessage } from "element-plus";
import { useI18n } from "vue-i18n";
const { t } = useI18n();
const view = reactive({
  visibleSelect: false,
  getDataListURL: "/flow/bpmform/page",
  getDataListIsPage: true
});

const dataForm = reactive({
  name: "",
  formType: "0",
  formId: ""
});
const callback = ref();

const state = reactive({ ...useView(view), ...toRefs(view) });

const init = (callback2: (e: any) => any, metaInfo: any) => {
  if (metaInfo) {
    dataForm.formType = metaInfo.formType;
    dataForm.formId = metaInfo.formId;
    if (!dataForm.formType) {
      dataForm.formType = "0";
    }
  }

  state.visibleSelect = true;
  callback.value = callback2;
  nextTick(() => {
    state.getDataList();
  });
};

const cancelHandle = () => {
  state.visibleSelect = false;
};

const commitHandle = () => {
  if (callback.value) {
    if (dataForm.formType == "0") {
      // 流程表单
      if (state.dataListSelections?.length !== 1) {
        ElMessage.error(t("user.selecterror"));
        return;
      } else {
        const postData = { ...state.dataListSelections[0] };
        postData.type = dataForm.formType;
        callback.value(postData);
      }
    } else {
      if (!dataForm.formId) {
        ElMessage.error("表单地址不存在");
        return;
      }
      // 自定义表单
      const postData = {
        type: dataForm.formType,
        id: dataForm.formId
      };
      callback.value(postData);
    }
  }
  state.visibleSelect = false;
};

defineExpose({
  init,
  dataForm
});
</script>
