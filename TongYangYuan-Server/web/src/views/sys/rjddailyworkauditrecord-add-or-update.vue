<template>
  <el-dialog v-model="visible" :title="!dataForm.id ? $t('add') : $t('update')" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" label-width="120px">
      <el-form-item label="审核记录ID" prop="id">
        <el-input v-model="dataForm.id" placeholder="审核记录ID"></el-input>
      </el-form-item>
      <el-form-item label="关联日结单ID" prop="dailyRecordId">
        <el-input v-model="dataForm.dailyRecordId" placeholder="关联日结单ID"></el-input>
      </el-form-item>
      <el-form-item label="审核节点：system=系统 manual=人工" prop="auditNode">
        <el-input v-model="dataForm.auditNode" placeholder="审核节点：system=系统 manual=人工"></el-input>
      </el-form-item>
      <el-form-item label="审核顺序：1=系统 2=人工" prop="auditOrder">
        <el-input v-model="dataForm.auditOrder" placeholder="审核顺序：1=系统 2=人工"></el-input>
      </el-form-item>
      <el-form-item label="关联系统审核配置ID（仅system节点）" prop="systemConfigId">
        <el-input v-model="dataForm.systemConfigId" placeholder="关联系统审核配置ID（仅system节点）"></el-input>
      </el-form-item>
      <el-form-item label="系统审核规则详情" prop="auditRuleDetail">
        <el-input v-model="dataForm.auditRuleDetail" placeholder="系统审核规则详情"></el-input>
      </el-form-item>
      <el-form-item label="分摊校验详情" prop="allocatedAuditDetail">
        <el-input v-model="dataForm.allocatedAuditDetail" placeholder="分摊校验详情"></el-input>
      </el-form-item>
      <el-form-item label="审核类型：1=系统 2=人工" prop="auditType">
        <el-input v-model="dataForm.auditType" placeholder="审核类型：1=系统 2=人工"></el-input>
      </el-form-item>
      <el-form-item label="审核状态：0=待审核 1=通过 2=驳回" prop="auditStatus">
        <el-input v-model="dataForm.auditStatus" placeholder="审核状态：0=待审核 1=通过 2=驳回"></el-input>
      </el-form-item>
      <el-form-item label="审核人ID（system=0，manual=sys_user.id）" prop="auditorId">
        <el-input v-model="dataForm.auditorId" placeholder="审核人ID（system=0，manual=sys_user.id）"></el-input>
      </el-form-item>
      <el-form-item label="审核人姓名（system=系统）" prop="auditorName">
        <el-input v-model="dataForm.auditorName" placeholder="审核人姓名（system=系统）"></el-input>
      </el-form-item>
      <el-form-item label="审核完成时间" prop="auditTime">
        <el-input v-model="dataForm.auditTime" placeholder="审核完成时间"></el-input>
      </el-form-item>
      <el-form-item label="审核备注" prop="auditRemark">
        <el-input v-model="dataForm.auditRemark" placeholder="审核备注"></el-input>
      </el-form-item>
      <el-form-item label="租户编码" prop="tenantCode">
        <el-input v-model="dataForm.tenantCode" placeholder="租户编码"></el-input>
      </el-form-item>
      <el-form-item label="创建者（system=0）" prop="creator">
        <el-input v-model="dataForm.creator" placeholder="创建者（system=0）"></el-input>
      </el-form-item>
      <el-form-item label="创建时间" prop="createDate">
        <el-input v-model="dataForm.createDate" placeholder="创建时间"></el-input>
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
  id: "",
  dailyRecordId: "",
  auditNode: "",
  auditOrder: "",
  systemConfigId: "",
  auditRuleDetail: "",
  allocatedAuditDetail: "",
  auditType: "",
  auditStatus: "",
  auditorId: "",
  auditorName: "",
  auditTime: "",
  auditRemark: "",
  tenantCode: "",
  creator: "",
  createDate: "",
  delFlag: ""
});

const rules = ref({});

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
  baseService.get("/sys/rjddailyworkauditrecord/" + id).then((res) => {
    Object.assign(dataForm, res.data);
  });
};

// 表单提交
const dataFormSubmitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }
    (!dataForm.id ? baseService.post : baseService.put)("/sys/rjddailyworkauditrecord", dataForm).then((res) => {
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
