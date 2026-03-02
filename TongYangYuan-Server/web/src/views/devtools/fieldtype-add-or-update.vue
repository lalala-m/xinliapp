<template>
  <el-dialog v-model="visible" :title="!dataForm.id ? '新增' : '编辑'" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" label-width="120px">
      <el-form-item label="字段类型" prop="columnType">
        <el-input v-model="dataForm.columnType" placeholder="字段类型"></el-input>
      </el-form-item>
      <el-form-item label="属性类型" prop="attrType">
        <el-input v-model="dataForm.attrType" placeholder="属性类型"></el-input>
      </el-form-item>
      <el-form-item label="属性包名" prop="packageName">
        <el-input v-model="dataForm.packageName" placeholder="属性包名"></el-input>
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
  columnType: "",
  attrType: "",
  packageName: "",
  createDate: ""
});

const rules = ref({
  columnType: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  attrType: [{ required: true, message: "必填项不能为空", trigger: "blur" }]
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
  baseService.get(`/devtools/fieldtype/${id}`).then((res) => {
    Object.assign(dataForm, res.data);
  });
};
// 表单提交
const dataFormSubmitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }
    (!dataForm.id ? baseService.post : baseService.put)("/devtools/fieldtype", dataForm).then(() => {
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
