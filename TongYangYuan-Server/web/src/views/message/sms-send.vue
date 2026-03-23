<template>
  <el-dialog v-model="visible" :title="$t('sms.send')" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" label-width="120px">
      <el-form-item prop="mobile" :label="$t('sms.mobile')">
        <el-input v-model="dataForm.mobile" :placeholder="$t('sms.mobile')"></el-input>
      </el-form-item>
      <el-form-item prop="params" :label="$t('sms.params')">
        <el-input v-model="dataForm.params" :placeholder="$t('sms.paramsTips')"></el-input>
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
import { isMobile } from "@/utils/utils";
import { IObject } from "@/types/interface";
import { useI18n } from "vue-i18n";
import { ElMessage } from "element-plus";
const { t } = useI18n();
const emit = defineEmits(["refreshDataList"]);

const visible = ref(false);
const dataFormRef = ref();

const dataForm = reactive({
  smsCode: "",
  mobile: "",
  params: ""
});

const validateMobile = (rule: IObject, value: string, callback: (e?: Error) => any) => {
  if (!isMobile(value)) {
    return callback(new Error(t("validate.format", { attr: t("user.mobile") })));
  }
  callback();
};
const rules = ref({
  mobile: [
    { required: true, message: t("validate.required"), trigger: "blur" },
    { validator: validateMobile, trigger: "blur" }
  ]
});

const init = () => {
  visible.value = true;

  // 重置表单数据
  if (dataFormRef.value) {
    dataFormRef.value.resetFields();
  }
};

// 表单提交
const dataFormSubmitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }
    baseService
      .post("/message/sms/send", dataForm, {
        "content-type": "application/x-www-form-urlencoded"
      })
      .then(() => {
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
  init,
  dataForm
});
</script>
