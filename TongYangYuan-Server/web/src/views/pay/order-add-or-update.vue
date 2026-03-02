<template>
  <el-dialog v-model="visible" :title="!dataForm.id ? $t('add') : $t('update')" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="dataForm" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" :label-width="$i18n.locale === 'en-US' ? '120px' : '80px'">
      <el-form-item :label="$t('order.productId')" prop="productId">
        <el-input v-model="dataForm.productId"></el-input>
      </el-form-item>
      <el-form-item :label="$t('order.productName')" prop="productName">
        <el-input v-model="dataForm.productName"></el-input>
      </el-form-item>
      <el-form-item :label="$t('order.payAmount')" prop="payAmount">
        <el-input v-model="dataForm.payAmount"></el-input>
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

const emit = defineEmits(["refreshDataList"]);
const { t } = useI18n();
const visible = ref(false);
const dataFormRef = ref();

const dataForm = reactive({
  id: "",
  orderId: "",
  productId: "",
  productName: "",
  payAmount: "",
  status: "",
  userId: ""
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
  baseService.post("/sys/pay/order", dataForm).then(() => {
    ElMessage.success({
      message: t("prompt.success"),
      duration: 500,
      onClose: () => {
        visible.value = false;
        emit("refreshDataList");
      }
    });
  });
};

defineExpose({
  init
});
</script>
