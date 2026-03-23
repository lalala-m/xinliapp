<template>
  <el-dialog v-model="visible" :title="$t('mail.send')" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" label-width="120px">
      <el-form-item prop="mailTo" :label="$t('mail.mailTo')">
        <el-input v-model="dataForm.mailTo" :placeholder="$t('mail.mailTo')"></el-input>
      </el-form-item>
      <el-form-item prop="mailCc" :label="$t('mail.mailCc')">
        <el-input v-model="dataForm.mailCc" :placeholder="$t('mail.mailCc')"></el-input>
      </el-form-item>
      <el-form-item prop="params" :label="$t('mail.params')">
        <el-input v-model="dataForm.params" :placeholder="t('mail.paramsTips')"></el-input>
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
import { isEmail } from "@/utils/utils";
import { IObject } from "@/types/interface";
import { useI18n } from "vue-i18n";
import { ElMessage } from "element-plus";
const { t } = useI18n();
const emit = defineEmits(["refreshDataList"]);

const visible = ref(false);
const dataFormRef = ref();

const dataForm = reactive({
  id: "",
  mailTo: "",
  mailCc: "",
  params: ""
});

const validateEmail = (rule: IObject, value: string, callback: (e?: Error) => any) => {
  if (!isEmail(value)) {
    return callback(new Error(t("validate.format", { attr: t("user.email") })));
  }
  callback();
};

const rules = ref({
  mailTo: [
    { required: true, message: t("validate.required"), trigger: "blur" },
    { validator: validateEmail, trigger: "blur" }
  ]
});

const init = () => {
  visible.value = true;
};

// 表单提交
const dataFormSubmitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }
    baseService
      .post("/message/mailtemplate/send", dataForm, {
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
