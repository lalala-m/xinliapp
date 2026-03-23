<template>
  <el-dialog v-model="visible" :title="!dataForm.id ? $t('add') : $t('update')" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" label-width="125px">
      <el-form-item :label="$t('tenant.name')" prop="name">
        <el-input v-model="dataForm.name" :placeholder="$t('tenant.name')"></el-input>
      </el-form-item>
      <el-form-item :label="$t('tenant.driver')" prop="driverClassName">
        <ren-select v-model="dataForm.driverClassName" dict-type="tenant_datasource" :placeholder="$t('tenant.driver')" style="width: 100%"></ren-select>
      </el-form-item>
      <el-form-item label="URL" prop="url" class="tenant_datasource">
        <el-input v-model="dataForm.url" placeholder="URL"></el-input>
        <el-alert type="success" :closable="false"> jdbc:mysql://localhost:3306/security_enterprise?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&nullCatalogMeansCurrent=true </el-alert>
        <el-alert type="success" :closable="false"> jdbc:oracle:thin:@localhost:1521:xe </el-alert>
        <el-alert type="success" :closable="false"> jdbc:postgresql://localhost:5432/security_enterprise </el-alert>
        <el-alert type="success" :closable="false"> jdbc:sqlserver://localhost:1433;DatabaseName=security_enterprise </el-alert>
        <el-alert type="success" :closable="false"> jdbc:dm://localhost:5236/security_enterprise?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&nullCatalogMeansCurrent=true </el-alert>
      </el-form-item>
      <el-form-item :label="$t('tenant.username')" prop="username">
        <el-input v-model="dataForm.username" :placeholder="$t('tenant.username')"></el-input>
      </el-form-item>
      <el-form-item :label="$t('tenant.password')" prop="password">
        <el-input v-model="dataForm.password" :placeholder="$t('tenant.password')"></el-input>
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
  name: "",
  driverClassName: "",
  url: "",
  username: "",
  password: "",
  createDate: ""
});

const rules = ref({
  name: [{ required: true, message: t("validate.required"), trigger: "blur" }],
  driverClassName: [{ required: true, message: t("validate.required"), trigger: "blur" }],
  url: [{ required: true, message: t("validate.required"), trigger: "blur" }],
  username: [{ required: true, message: t("validate.required"), trigger: "blur" }],
  password: [{ required: true, message: t("validate.required"), trigger: "blur" }]
});

const init = (id?: number) => {
  visible.value = true;
  dataForm.id = "";

  // 重置表单数据
  if (dataFormRef.value) {
    dataFormRef.value.resetFields();
  }

  if (id) {
    getInfo();
  }
};
// 获取信息
const getInfo = () => {
  baseService.get("/sys/tenant/datasource/" + dataForm.id).then((res) => {
    Object.assign(dataForm, res.data);
  });
};
// 表单提交
const dataFormSubmitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }
    (!dataForm.id ? baseService.post : baseService.put)("/sys/tenant/datasource", dataForm).then(() => {
      ElMessage({
        message: t("prompt.success"),
        type: "success",
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

<style lang="less" scoped>
.tenant_datasource {
  .el-alert {
    margin-top: 8px;
    line-height: 14px;
  }
}
</style>
