<template>
  <el-dialog v-model="visible" :title="$t('mail.config')" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" label-width="120px">
      <el-form-item prop="smtp" :label="$t('mail.config')">
        <el-input v-model="dataForm.smtp" :placeholder="$t('mail.config')"></el-input>
      </el-form-item>
      <el-form-item prop="port" :label="$t('mail.port')">
        <el-input v-model="dataForm.port" :placeholder="$t('mail.port')"></el-input>
      </el-form-item>
      <el-form-item prop="username" :label="$t('mail.username')">
        <el-input v-model="dataForm.username" :placeholder="$t('mail.username')"></el-input>
      </el-form-item>
      <el-form-item prop="password" :label="$t('mail.password')">
        <el-input v-model="dataForm.password" :placeholder="$t('mail.password')"></el-input>
      </el-form-item>
    </el-form>
    <template v-slot:footer>
      <el-button @click="visible = false">{{ $t("cancel") }}</el-button>
      <el-button type="primary" @click="dataFormSubmitHandle()">{{ $t("confirm") }}</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { reactive, ref } from "vue";
import baseService from "@/service/baseService";
import { useI18n } from "vue-i18n";
import { ElMessage } from "element-plus";
const { t } = useI18n();
const emit = defineEmits(["refreshDataList"]);

const visible = ref(false);
const dataFormRef = ref();

const dataForm = reactive({
  smtp: "",
  port: "",
  username: "",
  password: ""
});

const rules = ref({
  smtp: [{ required: true, message: t("validate.required"), trigger: "blur" }],
  port: [{ required: true, message: t("validate.required"), trigger: "blur" }],
  username: [{ required: true, message: t("validate.required"), trigger: "blur" }],
  password: [{ required: true, message: t("validate.required"), trigger: "blur" }]
});

const init = () => {
  visible.value = true;

  // 重置表单数据
  if (dataFormRef.value) {
    dataFormRef.value.resetFields();
  }

  getInfo();
};

// 获取信息
const getInfo = () => {
  baseService.get("/message/mailtemplate/config").then((res) => {
    Object.assign(dataForm, res.data);
  });
};

// 表单提交
const dataFormSubmitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }
    baseService.post("/message/mailtemplate/saveConfig", dataForm).then(() => {
      ElMessage.success({
        message: t("prompt.success"),
        duration: 500,
        onClose: () => {
          visible.value = false;
          emit("refreshDataList");
        }
      });
    });
  });
};

defineExpose({
  init
});
</script>
