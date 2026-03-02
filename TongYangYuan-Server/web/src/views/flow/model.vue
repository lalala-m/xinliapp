<template>
  <div class="mod__model">
    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-input v-model="state.dataForm.name" :placeholder="t('model.name')"></el-input>
      </el-form-item>
      <el-form-item>
        <el-input v-model="state.dataForm.modelKey" :placeholder="t('model.key')"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="state.getDataList()">{{ t("query") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" plain @click="addModel()">新建流程</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="state.dataListLoading" :data="state.dataList" show-overflow-tooltip border @selection-change="state.dataListSelectionChangeHandle" @sort-change="state.dataListSortChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="name" label="流程名称" header-align="center" align="center"></el-table-column>
      <el-table-column prop="key" label="流程Key" header-align="center" align="center"></el-table-column>
      <el-table-column prop="key" label="流程表单" header-align="center" align="center">
        <template v-slot="scope">
          <el-tag v-if="scope.row.metaInfo">
            <span v-if="scope.row.metaInfo.formType === '1'">{{ scope.row.metaInfo.formId }}</span>
            <span v-else @click="formPreview(scope.row.metaInfo.formId)">{{ scope.row.metaInfo.formName }} </span>
          </el-tag>
          <el-tag type="danger" v-else> 未配置 </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="deploymentId" label="发布状态" header-align="center" align="center" width="130">
        <template v-slot="scope">
          <span v-if="scope.row.deploymentId">
            <el-tag type="danger" v-if="scope.row.processDefinition.suspended"> 已挂起 </el-tag>
            <el-tag v-else> 已发布 </el-tag>
          </span>
          <el-tag type="warning" v-else>未发布</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="created" :label="t('model.createTime')" header-align="center" align="center" width="170"></el-table-column>
      <el-table-column :label="t('handle')" fixed="right" header-align="center" align="center" width="220">
        <template v-slot="scope">
          <el-button type="primary" link @click="addModel(scope.row.id)">{{ t("model.design") }}</el-button>
          <el-button type="primary" link @click="addForm(scope.row.id, scope.row.metaInfo)">表单</el-button>
          <el-button type="primary" link @click="deployHandle(scope.row.id)">发布</el-button>
          <el-dropdown @command="(command: string) => handleCommand(command, scope.row)">
            <el-button type="primary" class="el-dropdown-link" link>
              更多<el-icon class="el-icon--right"><arrow-down /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-if="scope.row.processDefinition && scope.row.processDefinition.suspended" command="active">激活</el-dropdown-item>
                <el-dropdown-item v-if="scope.row.processDefinition && !scope.row.processDefinition.suspended" command="suspended">挂起</el-dropdown-item>
                <el-dropdown-item command="delete">删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"> </el-pagination>

    <el-dialog v-model="flowVisible" title="流程设计器" fullscreen>
      <ProcessDesigner v-if="flowVisible" :modelId="modelId" @refreshDataList="state.getDataList"></ProcessDesigner>
    </el-dialog>

    <FormSelect v-if="formVisible" ref="formSelectRef"></FormSelect>

    <el-dialog v-model="previewVisible" title="表单预览">
      <v-form-render v-if="previewVisible" :formJson="formJson"></v-form-render>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { nextTick, reactive, ref, toRefs } from "vue";
import baseService from "@/service/baseService";
import { useI18n } from "vue-i18n";
import FormSelect from "./form-select.vue";
import ProcessDesigner from "./process-designer.vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { IObject } from "@/types/interface";
const { t } = useI18n();

const view = reactive({
  getDataListURL: "/flow/model/page",
  getDataListIsPage: true,
  deleteURL: "/flow/model",
  deleteIsBatch: false,
  dataForm: {
    name: "",
    modelKey: ""
  }
});

const handleCommand = (command: string, row: any) => {
  if (command === "active") {
    activeHandle(row.processDefinition.id);
  } else if (command === "suspended") {
    suspendHandle(row.processDefinition.id);
  } else if (command === "delete") {
    state.deleteHandle(row.id);
  }
};

const flowVisible = ref(false);

const state = reactive({ ...useView(view), ...toRefs(view) });

const modelId = ref();
const addModel = (id?: string) => {
  modelId.value = id;
  flowVisible.value = true;
};

const formSelectRef = ref();
const formVisible = ref(false);
const addForm = (id: string, metaInfo: any) => {
  modelId.value = id;
  formVisible.value = true;

  nextTick(() => {
    formSelectRef.value.init(saveForm, metaInfo);
  });
};

const saveForm = (form: IObject) => {
  const data = {
    id: modelId.value,
    formType: form.type,
    formId: form.id,
    formName: form.name
  };

  baseService
    .post("/flow/model", data, {
      "content-type": "application/x-www-form-urlencoded"
    })
    .then((res) => {
      ElMessage.success({
        message: t("prompt.success"),
        duration: 500,
        onClose: () => {
          state.getDataList();
        }
      });
    });
};

const previewVisible = ref(false);
const formJson = ref();
const formPreview = (formId: string) => {
  baseService.get("/flow/bpmform/" + formId).then((res) => {
    formJson.value = JSON.parse(res.data.content);
    previewVisible.value = true;
  });
  console.log(formId);
};

// 部署
const deployHandle = (id: string) => {
  ElMessageBox.confirm(t("prompt.info", { handle: "发布" }), t("prompt.title"), {
    confirmButtonText: t("confirm"),
    cancelButtonText: t("cancel"),
    type: "warning"
  })
    .then(() => {
      baseService.post(`/flow/model/deploy/${id}`).then((res) => {
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

// 激活
const activeHandle = (id: string) => {
  ElMessageBox.confirm(t("prompt.info", { handle: t("process.active") }), t("prompt.title"), {
    confirmButtonText: t("confirm"),
    cancelButtonText: t("cancel"),
    type: "warning"
  })
    .then(() => {
      baseService.put(`/flow/process/active/${id}`).then((res) => {
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

// 挂起
const suspendHandle = (id: string) => {
  ElMessageBox.confirm(t("prompt.info", { handle: t("process.suspend") }), t("prompt.title"), {
    confirmButtonText: t("confirm"),
    cancelButtonText: t("cancel"),
    type: "warning"
  })
    .then(() => {
      baseService.put(`/flow/process/suspend/${id}`).then((res) => {
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
</script>

<style scoped lang="scss">
.mod__model {
  :deep(.el-dropdown-link) {
    display: flex;
    align-items: center;
    line-height: normal;
    margin-left: 10px;
  }
}
</style>
