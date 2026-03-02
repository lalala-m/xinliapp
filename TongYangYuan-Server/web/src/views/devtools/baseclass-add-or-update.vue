<template>
  <el-dialog v-model="visible" :title="!dataForm.id ? '新增' : '编辑'" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" label-width="120px">
      <el-form-item label="基类编码" prop="code">
        <el-input v-model="dataForm.code" placeholder="基类编码"></el-input>
      </el-form-item>
      <el-form-item label="基类包名" prop="packageName">
        <el-input v-model="dataForm.packageName" placeholder="基类包名"></el-input>
      </el-form-item>
      <el-form-item label="基类字段" prop="fields">
        <el-input v-model="dataForm.fields" placeholder="基类字段，多个字段，用英文逗号分隔"></el-input>
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="dataForm.remark" placeholder="备注"></el-input>
      </el-form-item>
    </el-form>
    <template v-slot:footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="dataFormSubmitHandle()">确定</el-button>
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
  packageName: "",
  code: "",
  fields: "",
  remark: ""
});

const rules = ref({
  packageName: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  code: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  fields: [{ required: true, message: "必填项不能为空", trigger: "blur" }]
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
  baseService.get(`/devtools/baseclass/${id}`).then((res) => {
    Object.assign(dataForm, res.data);
  });
};
// 表单提交
const dataFormSubmitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }
    (!dataForm.id ? baseService.post : baseService.put)("/devtools/baseclass", dataForm).then(() => {
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
