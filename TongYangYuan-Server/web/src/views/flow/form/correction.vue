<template>
  <el-form :model="dataForm" :rules="rules" ref="dataFormRef" :disabled="disabled" label-width="100px">
    <el-form-item :label="$t('correction.post')" prop="applyPost">
      <el-input v-model="dataForm.applyPost"></el-input>
    </el-form-item>
    <el-row :gutter="40">
      <el-col :span="12">
        <el-form-item :label="$t('correction.entryDate')" prop="entryDate">
          <el-date-picker v-model="dataForm.entryDate" value-format="YYYY-MM-DD" style="width: 100%"></el-date-picker>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item :label="$t('correction.correctionDate')" prop="correctionDate">
          <el-date-picker v-model="dataForm.correctionDate" value-format="YYYY-MM-DD" style="width: 100%"></el-date-picker>
        </el-form-item>
      </el-col>
    </el-row>
    <el-form-item :label="$t('correction.workContent')" prop="workContent">
      <el-input type="textarea" v-model="dataForm.workContent"></el-input>
    </el-form-item>
    <el-form-item :label="$t('correction.achievement')" prop="achievement">
      <el-input type="textarea" v-model="dataForm.achievement"></el-input>
    </el-form-item>
  </el-form>
</template>
<script lang="ts" setup>
import baseService from "@/service/baseService";
import { onMounted, reactive, ref, toRefs } from "vue";
import { useI18n } from "vue-i18n";
import useView from "@/hooks/useView";
import { ElMessage } from "element-plus";
const { t } = useI18n();

const props = defineProps({
  instanceId: {
    type: String,
    required: false
  },
  disabled: {
    type: Boolean,
    default: false
  }
});

const dataFormRef = ref();
const dataForm = reactive({
  id: "",
  applyPost: "",
  entryDate: "",
  correctionDate: "",
  workContent: "",
  achievement: "",
  processDefinitionId: ""
});

const view = reactive({
  createdIsNeed: false
});

const state = reactive({ ...useView(view), ...toRefs(view) });

const rules = ref({
  applyPost: [{ required: true, message: t("validate.required"), trigger: "blur" }],
  entryDate: [{ required: true, message: t("validate.required"), trigger: "blur" }],
  correctionDate: [{ required: true, message: t("validate.required"), trigger: "blur" }],
  workContent: [{ required: true, message: t("validate.required"), trigger: "blur" }],
  achievement: [{ required: true, message: t("validate.required"), trigger: "blur" }],
  createTime: [{ required: true, message: t("validate.required"), trigger: "blur" }]
});

onMounted(() => {
  if (props.instanceId) {
    getInfo();
  }
});

const getInfo = () => {
  baseService.get("/flow/form/correction/" + props.instanceId).then((res) => {
    Object.assign(dataForm, res.data);
  });
};

// 提交表单
// definitionId: 流程定义ID
const submitForm = (definitionId: string) => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }
    // 设置流程定义ID
    dataForm.processDefinitionId = definitionId;
    baseService.post("/flow/form/correction/start", dataForm).then(() => {
      ElMessage.success({
        message: t("prompt.success"),
        duration: 500,
        onClose: () => {
          state.closeCurrentTab();
        }
      });
    });
  });
};

// 需要暴露出去，给其他组件调用(方法名只能为: submitForm)
defineExpose({
  submitForm
});
</script>
