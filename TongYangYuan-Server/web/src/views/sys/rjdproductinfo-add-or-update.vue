<template>
  <el-dialog v-model="visible" :title="!dataForm.productId ? $t('add') : $t('update')" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" label-width="120px">
      <el-form-item label="产品ID（与原系统一致）" prop="productId">
        <el-input v-model="dataForm.productId" placeholder="产品ID（与原系统一致）"></el-input>
      </el-form-item>
      <el-form-item label="产品型号（核心标识）" prop="productModel">
        <el-input v-model="dataForm.productModel" placeholder="产品型号（核心标识）"></el-input>
      </el-form-item>
      <el-form-item label="产品名称" prop="productName">
        <el-input v-model="dataForm.productName" placeholder="产品名称"></el-input>
      </el-form-item>
      <el-form-item label="产品规格" prop="productSpec">
        <el-input v-model="dataForm.productSpec" placeholder="产品规格"></el-input>
      </el-form-item>
      <el-form-item label="产品类型（如精密铸件、模具等）" prop="productType">
        <el-input v-model="dataForm.productType" placeholder="产品类型（如精密铸件、模具等）"></el-input>
      </el-form-item>
      <el-form-item label="租户编码" prop="tenantCode">
        <el-input v-model="dataForm.tenantCode" placeholder="租户编码"></el-input>
      </el-form-item>
      <el-form-item label="状态 0：停用 1：正常" prop="status">
        <el-input v-model="dataForm.status" placeholder="状态 0：停用 1：正常"></el-input>
      </el-form-item>
      <el-form-item label="从原系统同步的时间" prop="syncTime">
        <el-input v-model="dataForm.syncTime" placeholder="从原系统同步的时间"></el-input>
      </el-form-item>
      <el-form-item label="创建者（关联sys_user.id）" prop="creator">
        <el-input v-model="dataForm.creator" placeholder="创建者（关联sys_user.id）"></el-input>
      </el-form-item>
      <el-form-item label="创建时间" prop="createDate">
        <el-input v-model="dataForm.createDate" placeholder="创建时间"></el-input>
      </el-form-item>
      <el-form-item label="更新者（关联sys_user.id）" prop="updater">
        <el-input v-model="dataForm.updater" placeholder="更新者（关联sys_user.id）"></el-input>
      </el-form-item>
      <el-form-item label="更新时间" prop="updateDate">
        <el-input v-model="dataForm.updateDate" placeholder="更新时间"></el-input>
      </el-form-item>
      <el-form-item label="删除标识  0：未删除    1：删除" prop="delFlag">
        <el-input v-model="dataForm.delFlag" placeholder="删除标识  0：未删除    1：删除"></el-input>
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
  productId: "",
  productModel: "",
  productName: "",
  productSpec: "",
  productType: "",
  tenantCode: "",
  status: "",
  syncTime: "",
  creator: "",
  createDate: "",
  updater: "",
  updateDate: "",
  delFlag: ""
});

const rules = ref({});

const init = (productId?: number) => {
  visible.value = true;
  dataForm.productId = "";

  // 重置表单数据
  if (dataFormRef.value) {
    dataFormRef.value.resetFields();
  }

  if (productId) {
    getInfo(productId);
  }
};

// 获取信息
const getInfo = (productId: number) => {
  baseService.get("/sys/rjdproductinfo/" + productId).then((res) => {
    Object.assign(dataForm, res.data);
  });
};

// 表单提交
const dataFormSubmitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }
    (!dataForm.productId ? baseService.post : baseService.put)("/sys/rjdproductinfo", dataForm).then((res) => {
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
