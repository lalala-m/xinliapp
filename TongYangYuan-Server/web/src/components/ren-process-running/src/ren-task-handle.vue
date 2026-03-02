<template>
  <el-dialog v-model="visible" :title="state.handleTitle" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" :label-width="$i18n.locale === 'en-US' ? '120px' : '80px'">
      <el-form-item :label="$t('process.comment')" prop="comment">
        <el-input type="textarea" v-model="dataForm.comment" :placeholder="$t('process.comment')"></el-input>
      </el-form-item>
    </el-form>
    <template v-slot:footer>
      <el-button @click="visible = false">{{ $t("cancel") }}</el-button>
      <el-button type="primary" @click="dataFormSubmitHandle()">{{ $t("confirm") }}</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { nextTick, reactive, ref, toRefs } from "vue";
import baseService from "@/service/baseService";
import { useI18n } from "vue-i18n";
import { ElMessage } from "element-plus";
const { t } = useI18n();
const visible = ref(false);
const dataFormRef = ref();

const view = reactive({
  createdIsNeed: false,
  handleType: "",
  handleTitle: ""
});

const dataForm = reactive({
  comment: "",
  taskId: ""
});

const state = reactive({ ...useView(view), ...toRefs(view) });

const rules = ref({
  comment: [{ required: true, message: t("validate.required"), trigger: "blur" }]
});

const init = (type: string) => {
  visible.value = true;
  state.handleType = type;

  if (state.handleType === "complete") {
    state.handleTitle = t("process.completeTask");
  } else if (state.handleType === "reject") {
    state.handleTitle = t("process.rejectTask");
  }
  nextTick(() => {
    dataFormRef.value.resetFields();
  });
};

// 表单提交
const dataFormSubmitHandle = () => {
  if (state.handleType === "complete") {
    completeTask();
  } else if (state.handleType === "reject") {
    rejectTask();
  }
};

// 驳回
const rejectTask = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }
    if (!dataForm.taskId) {
      return false;
    }
    const params = {
      taskId: dataForm.taskId,
      comment: dataForm.comment
    };
    baseService
      .post("/flow/common/reject", params, {
        "content-type": "application/x-www-form-urlencoded"
      })
      .then((res) => {
        if (res.code !== 0) {
          ElMessage.error(res.msg);
          return;
        }
        ElMessage.success({
          message: t("prompt.success"),
          duration: 500,
          onClose: () => {
            visible.value = false;
            state.closeCurrentTab();
          }
        });
      });
  });
};

// 通过
const completeTask = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }
    if (!dataForm.taskId) {
      return false;
    }
    const params = {
      taskId: dataForm.taskId,
      comment: dataForm.comment
    };
    baseService
      .post("/flow/common/complete?", params, {
        "content-type": "application/x-www-form-urlencoded"
      })
      .then((res) => {
        if (res.code !== 0) {
          ElMessage.error(res.msg);
          return;
        }
        ElMessage.success({
          message: t("prompt.success"),
          duration: 500,
          onClose: () => {
            visible.value = false;
            state.closeCurrentTab();
          }
        });
      });
  });
};
defineExpose({
  init,
  dataForm
});
</script>
