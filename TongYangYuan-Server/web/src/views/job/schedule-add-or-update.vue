<template>
  <el-dialog v-model="visible" :title="!dataForm.id ? $t('add') : $t('update')" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" label-width="120px">
      <el-form-item prop="beanName" :label="$t('schedule.beanName')">
        <el-input v-model="dataForm.beanName" :placeholder="$t('schedule.beanNameTips')"></el-input>
      </el-form-item>
      <el-form-item prop="params" :label="$t('schedule.params')">
        <el-input v-model="dataForm.params" :placeholder="$t('schedule.params')"></el-input>
      </el-form-item>
      <el-form-item prop="cronExpression" :label="$t('schedule.cronExpression')">
        <el-popover ref="cronPopover" popper-class="schedule-cron-popover" trigger="click">
          <Cron @submit="changeCron" @close="cronPopover.hide()" :lang="$i18n.locale"></Cron>
          <template v-slot:reference>
            <el-input v-model="dataForm.cronExpression" :placeholder="$t('schedule.cronExpressionTips')"></el-input>
          </template>
        </el-popover>
      </el-form-item>
      <el-form-item prop="remark" :label="$t('schedule.remark')">
        <el-input v-model="dataForm.remark" :placeholder="$t('schedule.remark')"></el-input>
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
import Cron from "@/components/cron/index";
import { useI18n } from "vue-i18n";
import { ElMessage } from "element-plus";
const { t } = useI18n();
const emit = defineEmits(["refreshDataList"]);

const visible = ref(false);
const dataFormRef = ref();
const cronPopover = ref();

const dataForm = reactive({
  id: "",
  beanName: "",
  params: "",
  cronExpression: "",
  remark: "",
  status: 0
});

const rules = ref({
  beanName: [{ required: true, message: t("validate.required"), trigger: "blur" }],
  cronExpression: [{ required: true, message: t("validate.required"), trigger: "blur" }]
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

const changeCron = (val: any) => {
  dataForm.cronExpression = val;
};

// 获取信息
const getInfo = (id: number) => {
  baseService.get(`/job/schedule/${id}`).then((res) => {
    Object.assign(dataForm, res.data);
  });
};

// 表单提交
const dataFormSubmitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }
    const fn = !dataForm.id ? baseService.post("/job/schedule", dataForm) : baseService.put("/job/schedule", dataForm);
    fn.then(() => {
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

<style lang="less">
.schedule-cron {
  &-popover {
    width: auto !important;
    min-width: 550px !important;
  }
}
</style>
