<template>
  <el-dialog v-model="visible" :title="!dataForm.id ? $t('add') : $t('update')" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" :label-width="$i18n.locale === 'en-US' ? '120px' : '80px'">
      <el-form-item :label="$t('post.postCode')" prop="postCode">
        <el-input v-model="dataForm.postCode"></el-input>
      </el-form-item>
      <el-form-item :label="$t('post.postName')" prop="postName">
        <el-input v-model="dataForm.postName"></el-input>
      </el-form-item>
      <el-form-item :label="$t('post.sort')" prop="sort">
        <el-input-number v-model="dataForm.sort" :min="0"></el-input-number>
      </el-form-item>
      <el-form-item :label="$t('post.status')" prop="status">
        <ren-radio-group v-model="dataForm.status" dict-type="post_status"></ren-radio-group>
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
  postCode: "",
  postName: "",
  sort: 0,
  status: 1
});

const rules = ref({
  postCode: [{ required: true, message: t("validate.required"), trigger: "blur" }],
  postName: [{ required: true, message: t("validate.required"), trigger: "blur" }],
  sort: [{ required: true, message: t("validate.required"), trigger: "blur" }],
  status: [{ required: true, message: t("validate.required"), trigger: "blur" }]
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
  baseService.get("/sys/post/" + id).then((res) => {
    Object.assign(dataForm, res.data);
  });
};

// 表单提交
const dataFormSubmitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }
    (!dataForm.id ? baseService.post : baseService.put)("/sys/post", dataForm).then(() => {
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
