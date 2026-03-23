<template>
  <el-dialog v-model="visible" :title="!dataForm.id ? $t('add') : $t('update')" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" label-width="125px">
      <el-form-item prop="tenantMode" label="租户模式">
        <el-radio-group v-model="dataForm.tenantMode">
          <el-radio :label="0">字段模式</el-radio>
          <el-radio :label="1">数据源模式</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="dataForm.tenantMode === 1" prop="datasourceId" :label="$t('tenant.datasource')">
        <el-select v-model="dataForm.datasourceId" :placeholder="$t('tenant.datasource')" style="width: 100%">
          <el-option v-for="datasource in datasourceList" :key="datasource.id" :label="datasource.name" :value="datasource.id"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item v-if="dataForm.tenantMode === 0" prop="roleIdList" label="租户套餐" class="role-list">
        <el-select v-model="dataForm.roleIdList" multiple placeholder="租户套餐">
          <el-option v-for="role in roleList" :key="role.id" :label="role.name" :value="role.id"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item prop="tenantName" :label="$t('tenant.tenantName')">
        <el-input v-model="dataForm.tenantName" :placeholder="$t('tenant.tenantName')"></el-input>
      </el-form-item>
      <el-form-item prop="tenantDomain" :label="$t('tenant.domain')">
        <el-input v-model="dataForm.tenantDomain" :placeholder="$t('tenant.domainTip')"></el-input>
      </el-form-item>
      <el-form-item prop="username" :label="$t('tenant.username')">
        <el-input v-model="dataForm.username" :placeholder="$t('tenant.username')" :disabled="!!dataForm.id"></el-input>
      </el-form-item>
      <el-form-item prop="password" :label="$t('tenant.password')" :class="{ 'is-required': !dataForm.id }">
        <el-input v-model="dataForm.password" type="password" :placeholder="$t('tenant.password')"></el-input>
      </el-form-item>
      <el-form-item prop="remark" :label="$t('tenant.remark')">
        <el-input v-model="dataForm.remark" :placeholder="$t('tenant.remark')"></el-input>
      </el-form-item>
      <el-form-item prop="status" :label="$t('tenant.status')">
        <el-radio-group v-model="dataForm.status">
          <el-radio-button :label="0">{{ $t("tenant.status0") }}</el-radio-button>
          <el-radio-button :label="1">{{ $t("tenant.status1") }}</el-radio-button>
        </el-radio-group>
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
import { IObject } from "@/types/interface";
import { useI18n } from "vue-i18n";
import { ElMessage } from "element-plus";

const { t } = useI18n();

const emit = defineEmits(["refreshDataList"]);
const visible = ref(false);
const dataFormRef = ref();
const roleList = ref<any[]>([]);
const datasourceList = ref<any[]>([]);

const dataForm = reactive({
  id: "",
  datasourceId: "",
  tenantDomain: "",
  tenantName: "",
  tenantMode: 0,
  roleIdList: [],
  username: "",
  password: "",
  realName: "",
  remark: "",
  status: 1
});

const validatePassword = (rule: IObject, value: string, callback: (e?: Error) => any) => {
  if (!dataForm.id && !/\S/.test(value)) {
    return callback(new Error(t("validate.required")));
  }
  callback();
};

const rules = ref({
  tenantMode: [{ required: true, message: t("validate.required"), trigger: "blur" }],
  username: [{ required: true, message: t("validate.required"), trigger: "blur" }],
  datasourceId: [{ required: true, message: t("validate.required"), trigger: "blur" }],
  roleIdList: [{ required: true, message: t("validate.required"), trigger: "blur" }],
  tenantName: [{ required: true, message: t("validate.required"), trigger: "change" }],
  password: [{ validator: validatePassword, trigger: "blur" }],
  realName: [{ required: true, message: t("validate.required"), trigger: "blur" }]
});

const init = (id?: number) => {
  visible.value = true;
  dataForm.id = "";

  // 重置表单数据
  if (dataFormRef.value) {
    dataFormRef.value.resetFields();
  }

  dataForm.datasourceId = "";
  dataForm.roleIdList = [];

  Promise.all([getDataSourceList(), getRoleList()]).then(() => {
    if (id) {
      getInfo(id);
    }
  });
};

// 获取租户数据源列表
const getDataSourceList = async () => {
  const res = await baseService.get("/sys/tenant/datasource/list");
  datasourceList.value = res.data;
};

// 获取角色列表
const getRoleList = async () => {
  const res = await baseService.get("/sys/tenant/role/list");
  roleList.value = res.data;
};

// 获取信息
const getInfo = (id: number) => {
  baseService.get(`/sys/tenant/${id}`).then((res) => {
    Object.assign(dataForm, res.data);
  });
};
// 表单提交
const dataFormSubmitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }

    if (!dataForm.roleIdList) {
      dataForm.roleIdList = [];
    }

    (!dataForm.id ? baseService.post : baseService.put)("/sys/tenant", {
      ...dataForm,
      roleIdList: [...dataForm.roleIdList]
    }).then(() => {
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
