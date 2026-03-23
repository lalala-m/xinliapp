<template>
  <div class="mod-flow__form">
    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-input v-model="state.dataForm.name" placeholder="名称"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="state.getDataList()">{{ $t("query") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="addOrUpdateHandle()">{{ $t("add") }}</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="state.dataListLoading" :data="state.dataList" border @selection-change="state.dataListSelectionChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="id" label="id" header-align="center" align="center"></el-table-column>
      <el-table-column prop="name" label="名称" header-align="center" align="center"></el-table-column>
      <el-table-column prop="remark" label="备注" header-align="center" align="center"></el-table-column>
      <el-table-column prop="createDate" label="创建时间" header-align="center" align="center"></el-table-column>
      <el-table-column :label="$t('handle')" fixed="right" header-align="center" align="center" width="200">
        <template v-slot="scope">
          <el-button type="primary" link @click="addOrUpdateHandle(scope.row.id)">{{ $t("update") }}</el-button>
          <el-button type="primary" link @click="designerHandle(scope.row.id, scope.row.content)">设计</el-button>
          <el-button type="primary" link @click="detailHandle(scope.row.content)">预览</el-button>
          <el-button type="primary" link @click="state.deleteHandle(scope.row.id)">{{ $t("delete") }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"> </el-pagination>
    <!-- 弹窗, 新增 / 修改 -->
    <add-or-update :key="addKey" ref="addOrUpdateRef" @refreshDataList="state.getDataList"></add-or-update>

    <el-dialog v-model="designerVisible" title="表单设计器" fullscreen>
      <v-form-designer v-if="designerVisible" :dataForm="form.table" @save-data="saveData"></v-form-designer>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="表单预览">
      <v-form-render v-if="detailVisible" :formJson="form.table"></v-form-render>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { nextTick, reactive, ref, toRefs } from "vue";
import AddOrUpdate from "./bpmform-add-or-update.vue";
import baseService from "@/service/baseService";
import { useI18n } from "vue-i18n";
import { ElMessage } from "element-plus";
const { t } = useI18n();

const view = reactive({
  getDataListURL: "/flow/bpmform/page",
  getDataListIsPage: true,
  deleteURL: "/flow/bpmform",
  deleteIsBatch: true,
  dataForm: {
    name: ""
  }
});

const state = reactive({ ...useView(view), ...toRefs(view) });

const addKey = ref(0);
const addOrUpdateRef = ref();
const addOrUpdateHandle = (id?: number) => {
  addKey.value++;
  nextTick(() => {
    addOrUpdateRef.value.init(id);
  });
};

const form = reactive({
  table: {} as any
});

const designerVisible = ref(false);
const selectId = ref(0);
const designerHandle = (id: number, json: string) => {
  selectId.value = id;
  form.table = JSON.parse(json);

  designerVisible.value = true;
};

const detailVisible = ref(false);

const detailHandle = (json: string) => {
  form.table = JSON.parse(json);
  detailVisible.value = true;
};

const saveData = (json?: string) => {
  const dataForm = {
    id: selectId.value,
    content: json
  };

  baseService.put("/flow/bpmform", dataForm).then(() => {
    ElMessage.success({
      message: t("prompt.success"),
      duration: 500,
      onClose: () => {
        designerVisible.value = false;
        state.getDataList();
      }
    });
  });
};
</script>
