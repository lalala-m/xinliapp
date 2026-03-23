<template>
  <el-dialog v-model="visible" :title="!dataForm.id ? $t('add') : $t('update')" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" label-width="120px">
      <el-form-item label="所属平台" prop="platform">
        <ren-select v-model="dataForm.platform" dict-type="ai_platform" placeholder="所属平台"></ren-select>
      </el-form-item>
      <el-form-item label="模型名称" prop="name">
        <el-input v-model="dataForm.name" placeholder="模型名称"></el-input>
      </el-form-item>
      <el-form-item label="模型标识" prop="model">
        <el-input v-model="dataForm.model" placeholder="模型标识"></el-input>
      </el-form-item>
      <el-form-item label="API地址" prop="apiUrl">
        <el-input v-model="dataForm.apiUrl" placeholder="API地址"></el-input>
      </el-form-item>
      <el-form-item label="API秘钥" prop="apiKey">
        <el-input v-model="dataForm.apiKey" placeholder="API秘钥"></el-input>
      </el-form-item>
      <el-form-item label="排序" prop="sort">
        <el-input-number v-model="dataForm.sort" placeholder="排序"></el-input-number>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <ren-radio-group v-model="dataForm.status" dict-type="model_status"></ren-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">{{ $t("cancel") }}</el-button>
      <el-button type="primary" @click="dataFormSubmitHandle()">{{ $t("confirm") }}</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import { ElMessage } from "element-plus/es";
import baseService from "@/service/baseService";
import RenRadioGroup from "@/components/ren-radio-group/src/ren-radio-group.vue";
import { useI18n } from "vue-i18n";
const { t } = useI18n();
const emit = defineEmits(["refreshDataList"]);

const visible = ref(false);
const dataFormRef = ref();

const dataForm = reactive({
  id: "",
  platform: "",
  name: "",
  model: "",
  apiUrl: "",
  apiKey: "",
  sort: 0,
  status: 1
});

const init = (id?: number) => {
  visible.value = true;
  dataForm.id = "";

  // 重置表单数据
  if (dataFormRef.value) {
    dataFormRef.value.resetFields();
  }

  if (id) {
    getModel(id);
  }
};

const getModel = (id: number) => {
  baseService.get("/sys/ai/model/" + id).then((res: any) => {
    Object.assign(dataForm, res.data);
  });
};

const rules = ref({
  platform: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  name: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  model: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  apiUrl: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  sort: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  status: [{ required: true, message: "必填项不能为空", trigger: "blur" }]
});

// 表单提交
const dataFormSubmitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }
    (!dataForm.id ? baseService.post : baseService.put)("/sys/ai/model", dataForm).then(() => {
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
