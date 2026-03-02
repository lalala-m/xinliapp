<template>
  <el-dialog v-model="visible" :title="!dataForm.id ? $t('add') : $t('update')" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" label-width="140px">
      <el-form-item label="名称" prop="name">
        <el-input v-model="dataForm.name" placeholder="名称"></el-input>
      </el-form-item>
      <el-form-item label="AppID" prop="appId">
        <el-input v-model="dataForm.appId" placeholder="AppID"></el-input>
      </el-form-item>
      <el-form-item label="AppSecret" prop="appSecret">
        <el-input v-model="dataForm.appSecret" placeholder="AppSecret"></el-input>
      </el-form-item>
      <el-form-item label="Token" prop="token">
        <el-input v-model="dataForm.token" placeholder="Token"></el-input>
      </el-form-item>
      <el-form-item label="EncodingAESKey" prop="aesKey">
        <el-input v-model="dataForm.aesKey" placeholder="EncodingAESKey"></el-input>
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
  id: "",
  name: "",
  appId: "",
  appSecret: "",
  token: "",
  aesKey: "",
  creator: "",
  createDate: "",
  updater: "",
  updateDate: ""
});

const rules = ref({
  name: [{ required: true, message: t("validate.required"), trigger: "blur" }],
  appId: [{ required: true, message: t("validate.required"), trigger: "blur" }],
  appSecret: [{ required: true, message: t("validate.required"), trigger: "blur" }],
  token: [{ required: true, message: t("validate.required"), trigger: "blur" }],
  aesKey: [{ required: true, message: t("validate.required"), trigger: "blur" }]
});

const init = (id?: number) => {
  visible.value = true;
  dataForm.id = "";

  // 重置表单数据
  if (dataFormRef.value) {
    dataFormRef.value.resetFields();
  }

  if (id) {
    getInfo(id);
  }
};

// 获取信息
const getInfo = (id: number) => {
  baseService.get("/sys/mp/account/" + id).then((res) => {
    Object.assign(dataForm, res.data);
  });
};

// 表单提交
const dataFormSubmitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }
    (!dataForm.id ? baseService.post : baseService.put)("/sys/mp/account", dataForm).then(() => {
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
