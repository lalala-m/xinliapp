<template>
  <el-dialog v-model="state.visible" :title="$t('process.entrustTask')" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" :label-width="$i18n.locale === 'en-US' ? '120px' : '80px'">
      <el-form-item :label="$t('process.assignee')">
        <el-input v-model="dataForm.userName" class="input-with-select">
          <template v-slot:append>
            <el-button icon="search" @click="selectUserInfo()"></el-button>
          </template>
        </el-input>
      </el-form-item>
    </el-form>
    <template v-slot:footer>
      <el-button @click="state.visible = false">{{ $t("cancel") }}</el-button>
      <el-button type="primary" @click="delegateTask()">{{ $t("confirm") }}</el-button>
    </template>
    <select-user v-if="state.selectUserVisible" ref="selectUserRef"></select-user>
  </el-dialog>
</template>

<script lang="ts" setup>
import SelectUser from "./select-user.vue";
import useView from "@/hooks/useView";
import { nextTick, reactive, ref, toRefs } from "vue";
import baseService from "@/service/baseService";
import { IObject } from "@/types/interface";
import { useI18n } from "vue-i18n";
import { ElMessage } from "element-plus";
const { t } = useI18n();
const dataFormRef = ref();
const selectUserRef = ref();

const view = reactive({
  createdIsNeed: false,
  visible: false,
  selectUserVisible: false
});

const dataForm = reactive({
  id: "",
  userId: "",
  userName: "",
  taskId: ""
});

const state = reactive({ ...useView(view), ...toRefs(view) });

const rules = ref({
  userName: [{ required: true, message: t("validate.required"), trigger: "blur" }]
});

const init = () => {
  state.visible = true;
  nextTick(() => {
    dataFormRef.value.resetFields();
  });
};

const dataFormSubmitHandle = () => {
  //
};

// 委托
const delegateTask = () => {
  if (!dataForm.userId) {
    ElMessage.error(t("process.entrustError"));
    return;
  }
  const task = {
    taskId: dataForm.taskId,
    userId: dataForm.userId
  };
  baseService
    .post("/flow/common/delegate", task, {
      "content-type": "application/x-www-form-urlencoded"
    })
    .then(() => {
      ElMessage.success({
        message: t("prompt.success"),
        duration: 500,
        onClose: () => {
          state.visible = false;
          state.closeCurrentTab();
        }
      });
    });
};

const selectUserInfo = () => {
  state.selectUserVisible = true;
  nextTick(() => {
    selectUserRef.value.init(setUserInfo);
  });
};

const setUserInfo = (userInfo: IObject) => {
  dataForm.userId = userInfo.id;
  dataForm.userName = userInfo.realName;
};

defineExpose({
  init,
  dataForm
});
</script>
