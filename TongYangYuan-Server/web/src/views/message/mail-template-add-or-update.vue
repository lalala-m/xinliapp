<template>
  <el-dialog v-model="visible" :title="!dataForm.id ? $t('add') : $t('update')" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" label-width="120px">
      <el-form-item prop="name" :label="$t('mail.name')">
        <el-input v-model="dataForm.name" :placeholder="$t('mail.name')"></el-input>
      </el-form-item>
      <el-form-item prop="subject" :label="$t('mail.subject')">
        <el-input v-model="dataForm.subject" :placeholder="$t('mail.subject')"></el-input>
      </el-form-item>
      <el-form-item prop="content" :label="$t('mail.content')">
        <WangEditor v-model="dataForm.content"></WangEditor>
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
import "quill/dist/quill.snow.css";
import WangEditor from "@/components/wang-editor/index.vue";
import { useI18n } from "vue-i18n";
import { ElMessage } from "element-plus";
const { t } = useI18n();
const emit = defineEmits(["refreshDataList"]);

const visible = ref(false);
const dataFormRef = ref();

const dataForm = reactive({
  id: "",
  name: "",
  subject: "",
  content: ""
});

const rules = ref({
  name: [{ required: true, message: t("validate.required"), trigger: "blur" }],
  subject: [{ required: true, message: t("validate.required"), trigger: "blur" }],
  content: [{ required: true, message: t("validate.required"), trigger: "blur" }]
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
  baseService.get(`/message/mailtemplate/${id}`).then((res) => {
    Object.assign(dataForm, res.data);
  });
};

// 表单提交
const dataFormSubmitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }
    (!dataForm.id ? baseService.post : baseService.put)("/message/mailtemplate", dataForm, {
      "content-type": "application/x-www-form-urlencoded"
    }).then(() => {
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
