<template>
  <el-dialog v-model="visible" :title="!dataForm.id ? $t('add') : $t('update')" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" label-width="120px">
      <el-form-item label="主键ID" prop="id">
        <el-input v-model="dataForm.id" placeholder="主键ID"></el-input>
      </el-form-item>
      <el-form-item label="岗位ID（关联sys_post.id）" prop="postId">
        <el-input v-model="dataForm.postId" placeholder="岗位ID（关联sys_post.id）"></el-input>
      </el-form-item>
      <el-form-item label="岗位名称（冗余存储）" prop="postName">
        <el-input v-model="dataForm.postName" placeholder="岗位名称（冗余存储）"></el-input>
      </el-form-item>
      <el-form-item label="所属部门ID（关联sys_dept.id）" prop="deptId">
        <el-input v-model="dataForm.deptId" placeholder="所属部门ID（关联sys_dept.id）"></el-input>
      </el-form-item>
      <el-form-item label="所属部门名称（冗余存储）" prop="deptName">
        <el-input v-model="dataForm.deptName" placeholder="所属部门名称（冗余存储）"></el-input>
      </el-form-item>
      <el-form-item label="填报日期" prop="workDate">
        <el-input v-model="dataForm.workDate" placeholder="填报日期"></el-input>
      </el-form-item>
      <el-form-item label="工作事项分类（关联rjd_post_work_item.work_item_name）" prop="workItemType">
        <el-input v-model="dataForm.workItemType" placeholder="工作事项分类（关联rjd_post_work_item.work_item_name）"></el-input>
      </el-form-item>
      <el-form-item label="关联事项类型ID（关联rjd_related_object_type_dict.type_id）" prop="relatedTypeId">
        <el-input v-model="dataForm.relatedTypeId" placeholder="关联事项类型ID（关联rjd_related_object_type_dict.type_id）"></el-input>
      </el-form-item>
      <el-form-item label="关联事项类型名称" prop="relatedTypeName">
        <el-input v-model="dataForm.relatedTypeName" placeholder="关联事项类型名称"></el-input>
      </el-form-item>
      <el-form-item label="关联事项来源表" prop="relatedSourceTable">
        <el-input v-model="dataForm.relatedSourceTable" placeholder="关联事项来源表"></el-input>
      </el-form-item>
      <el-form-item label="关联事项名称" prop="relatedObjectName">
        <el-input v-model="dataForm.relatedObjectName" placeholder="关联事项名称"></el-input>
      </el-form-item>
      <el-form-item label="填报关联对象" prop="relatedObject">
        <el-input v-model="dataForm.relatedObject" placeholder="填报关联对象"></el-input>
      </el-form-item>
      <el-form-item label="填报内容" prop="fillContent">
        <el-input v-model="dataForm.fillContent" placeholder="填报内容"></el-input>
      </el-form-item>
      <el-form-item label="总耗时（小时）" prop="workingHours">
        <el-input v-model="dataForm.workingHours" placeholder="总耗时（小时）"></el-input>
      </el-form-item>
      <el-form-item label="关联对象分摊耗时总和（系统自动计算）" prop="allocatedHoursSum">
        <el-input v-model="dataForm.allocatedHoursSum" placeholder="关联对象分摊耗时总和（系统自动计算）"></el-input>
      </el-form-item>
      <el-form-item label="分摊校验状态：0=未校验 1=通过 2=失败" prop="allocatedCheckStatus">
        <el-input v-model="dataForm.allocatedCheckStatus" placeholder="分摊校验状态：0=未校验 1=通过 2=失败"></el-input>
      </el-form-item>
      <el-form-item label="驳回后修改次数" prop="modifyCount">
        <el-input v-model="dataForm.modifyCount" placeholder="驳回后修改次数"></el-input>
      </el-form-item>
      <el-form-item label="最新驳回原因" prop="latestRejectReason">
        <el-input v-model="dataForm.latestRejectReason" placeholder="最新驳回原因"></el-input>
      </el-form-item>
      <el-form-item label="通勤实际工时（冗余自rjd_employee_commute_record）" prop="commuteActualHours">
        <el-input v-model="dataForm.commuteActualHours" placeholder="通勤实际工时（冗余自rjd_employee_commute_record）"></el-input>
      </el-form-item>
      <el-form-item label="系统审核状态：0=待审核 1=通过 2=驳回" prop="systemAuditStatus">
        <el-input v-model="dataForm.systemAuditStatus" placeholder="系统审核状态：0=待审核 1=通过 2=驳回"></el-input>
      </el-form-item>
      <el-form-item label="人工审核状态：0=待审核 1=通过 2=驳回" prop="manualAuditStatus">
        <el-input v-model="dataForm.manualAuditStatus" placeholder="人工审核状态：0=待审核 1=通过 2=驳回"></el-input>
      </el-form-item>
      <el-form-item label="当前审核节点：system=系统 manual=人工" prop="latestAuditNode">
        <el-input v-model="dataForm.latestAuditNode" placeholder="当前审核节点：system=系统 manual=人工"></el-input>
      </el-form-item>
      <el-form-item label="填报人ID（关联sys_user.id）" prop="fillerId">
        <el-input v-model="dataForm.fillerId" placeholder="填报人ID（关联sys_user.id）"></el-input>
      </el-form-item>
      <el-form-item label="填报人姓名（冗余存储）" prop="fillerName">
        <el-input v-model="dataForm.fillerName" placeholder="填报人姓名（冗余存储）"></el-input>
      </el-form-item>
      <el-form-item label="填报时间" prop="fillDatetime">
        <el-input v-model="dataForm.fillDatetime" placeholder="填报时间"></el-input>
      </el-form-item>
      <el-form-item label="租户编码" prop="tenantCode">
        <el-input v-model="dataForm.tenantCode" placeholder="租户编码"></el-input>
      </el-form-item>
      <el-form-item label="状态：0=停用 1=正常" prop="status">
        <el-input v-model="dataForm.status" placeholder="状态：0=停用 1=正常"></el-input>
      </el-form-item>
      <el-form-item label="创建者" prop="creator">
        <el-input v-model="dataForm.creator" placeholder="创建者"></el-input>
      </el-form-item>
      <el-form-item label="创建时间" prop="createDate">
        <el-input v-model="dataForm.createDate" placeholder="创建时间"></el-input>
      </el-form-item>
      <el-form-item label="更新者" prop="updater">
        <el-input v-model="dataForm.updater" placeholder="更新者"></el-input>
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
  id: "",
  postId: "",
  postName: "",
  deptId: "",
  deptName: "",
  workDate: "",
  workItemType: "",
  relatedTypeId: "",
  relatedTypeName: "",
  relatedSourceTable: "",
  relatedObjectName: "",
  relatedObject: "",
  fillContent: "",
  workingHours: "",
  allocatedHoursSum: "",
  allocatedCheckStatus: "",
  modifyCount: "",
  latestRejectReason: "",
  commuteActualHours: "",
  systemAuditStatus: "",
  manualAuditStatus: "",
  latestAuditNode: "",
  fillerId: "",
  fillerName: "",
  fillDatetime: "",
  tenantCode: "",
  status: "",
  creator: "",
  createDate: "",
  updater: "",
  updateDate: "",
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
  baseService.get("/sys/rjddailyworkrecord/" + id).then((res) => {
    Object.assign(dataForm, res.data);
  });
};

// 表单提交
const dataFormSubmitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }
    (!dataForm.id ? baseService.post : baseService.put)("/sys/rjddailyworkrecord", dataForm).then((res) => {
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
