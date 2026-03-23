<template>
  <el-dialog v-model="visible" :title="!dataForm.id ? $t('add') : $t('update')" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" label-width="120px">
      <el-form-item prop="title" :label="$t('news.title')">
        <el-input v-model="dataForm.title" :placeholder="$t('news.title')"></el-input>
      </el-form-item>
      <el-form-item prop="content" :label="$t('news.content')">
        <WangEditor v-model="dataForm.content"></WangEditor>
      </el-form-item>
      <el-form-item prop="pubDate" :label="$t('news.pubDate')">
        <el-date-picker v-model="dataForm.pubDate" type="datetime" format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DD HH:mm:ss" :placeholder="$t('news.pubDate')"></el-date-picker>
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
import WangEditor from "@/components/wang-editor/index.vue";

import { useI18n } from "vue-i18n";
import { ElMessage } from "element-plus";
const { t } = useI18n();
const emit = defineEmits(["refreshDataList"]);

const visible = ref(false);
const dataFormRef = ref();

const dataForm = reactive({
  id: "",
  title: "",
  content: "",
  pubDate: ""
});

const rules = ref({
  title: [{ required: true, message: t("validate.required"), trigger: "blur" }],
  content: [{ required: true, message: t("validate.required"), trigger: "blur" }],
  pubDate: [{ required: true, message: t("validate.required"), trigger: "blur" }]
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

const getInfo = (id: number) => {
  baseService.get(`/sys/news/${id}`).then((res) => {
    Object.assign(dataForm, res.data);
  });
};

// 表单提交
const dataFormSubmitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }
    (!dataForm.id ? baseService.post : baseService.put)("/sys/news", dataForm, {
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
