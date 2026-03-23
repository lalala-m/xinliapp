<template>
  <el-form :model="dataForm" :rules="rules" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" label-width="120px">
    <el-form-item :label="$t('updatePassword.username')">
      <span>{{ user.username }}</span>
    </el-form-item>
    <el-form-item prop="password" :label="$t('updatePassword.password')">
      <el-input v-model="dataForm.password" type="password" :placeholder="$t('updatePassword.password')"></el-input>
    </el-form-item>
    <el-form-item prop="newPassword" :label="$t('updatePassword.newPassword')">
      <el-input v-model="dataForm.newPassword" type="password" :placeholder="$t('updatePassword.newPassword')"></el-input>
    </el-form-item>
    <el-form-item prop="confirmPassword" :label="$t('updatePassword.confirmPassword')">
      <el-input v-model="dataForm.confirmPassword" type="password" :placeholder="$t('updatePassword.confirmPassword')"></el-input>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="dataFormSubmitHandle">{{ $t("confirm") }}</el-button>
    </el-form-item>
  </el-form>
</template>

<script lang="ts" setup>
import { computed, reactive, ref } from "vue";
import { IObject } from "@/types/interface";
import baseService from "@/service/baseService";
import { useAppStore } from "@/store";
import { useI18n } from "vue-i18n";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
const { t } = useI18n();
const router = useRouter();

const dataFormRef = ref();
const dataForm = reactive({
  password: "",
  newPassword: "",
  confirmPassword: ""
});

const store = useAppStore();
const user = computed(() => store.state.user);

const validateConfirmPassword = (rule: IObject, value: string, callback: (e?: Error) => any) => {
  if (dataForm.newPassword !== value) {
    return callback(new Error(t("updatePassword.validate.confirmPassword")));
  }
  callback();
};

const rules = ref({
  password: [{ required: true, message: t("validate.required"), trigger: "blur" }],
  newPassword: [{ required: true, message: t("validate.required"), trigger: "blur" }],
  confirmPassword: [
    { required: true, message: t("validate.required"), trigger: "blur" },
    { validator: validateConfirmPassword, trigger: "blur" }
  ]
});

// 表单提交
const dataFormSubmitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }
    baseService.put("/sys/user/password", dataForm).then(() => {
      ElMessage.success({
        message: t("prompt.success"),
        duration: 500,
        onClose: () => {
          router.replace("/login");
        }
      });
    });
  });
};
</script>
