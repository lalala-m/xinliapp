<template>
  <el-dialog v-model="visible" :title="!dataForm.configId ? $t('add') : $t('update')" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" label-width="120px">
      <el-form-item label="配置ID" prop="configId">
        <el-input v-model="dataForm.configId" placeholder="配置ID"></el-input>
      </el-form-item>
      <el-form-item label="配置名称（如：生产管理人员日结单审核规则）" prop="configName">
        <el-input v-model="dataForm.configName" placeholder="配置名称（如：生产管理人员日结单审核规则）"></el-input>
      </el-form-item>
      <el-form-item label="适用岗位ID（sys_post.id，NULL=全岗位）" prop="postId">
        <el-input v-model="dataForm.postId" placeholder="适用岗位ID（sys_post.id，NULL=全岗位）"></el-input>
      </el-form-item>
      <el-form-item label="适用岗位名称" prop="postName">
        <el-input v-model="dataForm.postName" placeholder="适用岗位名称"></el-input>
      </el-form-item>
      <el-form-item label="适用部门ID（sys_dept.id，NULL=全部门）" prop="deptId">
        <el-input v-model="dataForm.deptId" placeholder="适用部门ID（sys_dept.id，NULL=全部门）"></el-input>
      </el-form-item>
      <el-form-item label="适用部门名称" prop="deptName">
        <el-input v-model="dataForm.deptName" placeholder="适用部门名称"></el-input>
      </el-form-item>
      <el-form-item label="必填字段（逗号分隔）" prop="requiredFields">
        <el-input v-model="dataForm.requiredFields" placeholder="必填字段（逗号分隔）"></el-input>
      </el-form-item>
      <el-form-item label="填报耗时最小值（小时）" prop="workingHoursMin">
        <el-input v-model="dataForm.workingHoursMin" placeholder="填报耗时最小值（小时）"></el-input>
      </el-form-item>
      <el-form-item label="填报耗时最大值（小时）" prop="workingHoursMax">
        <el-input v-model="dataForm.workingHoursMax" placeholder="填报耗时最大值（小时）"></el-input>
      </el-form-item>
      <el-form-item label="关联对象校验：0=不校验 1=校验" prop="relatedObjectCheck">
        <el-input v-model="dataForm.relatedObjectCheck" placeholder="关联对象校验：0=不校验 1=校验"></el-input>
      </el-form-item>
      <el-form-item label="工时匹配：0=不校验 1=填报≤通勤" prop="actualHoursMatch">
        <el-input v-model="dataForm.actualHoursMatch" placeholder="工时匹配：0=不校验 1=填报≤通勤"></el-input>
      </el-form-item>
      <el-form-item label="自动通过：0=否 1=是" prop="autoPass">
        <el-input v-model="dataForm.autoPass" placeholder="自动通过：0=否 1=是"></el-input>
      </el-form-item>
      <el-form-item label="填报内容最小字数（0=不限制）" prop="fillContentMinLength">
        <el-input v-model="dataForm.fillContentMinLength" placeholder="填报内容最小字数（0=不限制）"></el-input>
      </el-form-item>
      <el-form-item label="分摊校验：0=不校验 1=总和=总耗时" prop="allocatedCheck">
        <el-input v-model="dataForm.allocatedCheck" placeholder="分摊校验：0=不校验 1=总和=总耗时"></el-input>
      </el-form-item>
      <el-form-item label="驳回后最大修改次数（0=不限制）" prop="modifyLimit">
        <el-input v-model="dataForm.modifyLimit" placeholder="驳回后最大修改次数（0=不限制）"></el-input>
      </el-form-item>
      <el-form-item label="关联对象最少数量（0=不限制）" prop="relatedObjectMinCount">
        <el-input v-model="dataForm.relatedObjectMinCount" placeholder="关联对象最少数量（0=不限制）"></el-input>
      </el-form-item>
      <el-form-item label="租户编码" prop="tenantCode">
        <el-input v-model="dataForm.tenantCode" placeholder="租户编码"></el-input>
      </el-form-item>
      <el-form-item label="状态：0=停用 1=启用" prop="status">
        <el-input v-model="dataForm.status" placeholder="状态：0=停用 1=启用"></el-input>
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
      <el-form-item label="删除标识：0=未删 1=已删" prop="delFlag">
        <el-input v-model="dataForm.delFlag" placeholder="删除标识：0=未删 1=已删"></el-input>
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
  configId: "",
  configName: "",
  postId: "",
  postName: "",
  deptId: "",
  deptName: "",
  requiredFields: "",
  workingHoursMin: "",
  workingHoursMax: "",
  relatedObjectCheck: "",
  actualHoursMatch: "",
  autoPass: "",
  fillContentMinLength: "",
  allocatedCheck: "",
  modifyLimit: "",
  relatedObjectMinCount: "",
  tenantCode: "",
  status: "",
  creator: "",
  createDate: "",
  updater: "",
  updateDate: "",
  delFlag: ""
});

const rules = ref({});

const init = (configId?: number) => {
  visible.value = true;
  dataForm.configId = "";

  // 重置表单数据
  if (dataFormRef.value) {
    dataFormRef.value.resetFields();
  }

  if (configId) {
    getInfo(configId);
  }
};

// 获取信息
const getInfo = (configId: number) => {
  baseService.get("/sys/rjdsystemauditconfig/" + configId).then((res) => {
    Object.assign(dataForm, res.data);
  });
};

// 表单提交
const dataFormSubmitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }
    (!dataForm.configId ? baseService.post : baseService.put)("/sys/rjdsystemauditconfig", dataForm).then((res) => {
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
