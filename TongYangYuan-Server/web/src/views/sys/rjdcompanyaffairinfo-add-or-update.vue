<template>
  <el-dialog v-model="visible" :title="!dataForm.affairId ? $t('add') : $t('update')" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" label-width="120px">
      <el-form-item label="事务ID（自增）" prop="affairId">
        <el-input v-model="dataForm.affairId" placeholder="事务ID（自增）"></el-input>
      </el-form-item>
      <el-form-item label="事务名称（如成本内控会议、安全培训等）" prop="affairName">
        <el-input v-model="dataForm.affairName" placeholder="事务名称（如成本内控会议、安全培训等）"></el-input>
      </el-form-item>
      <el-form-item label="事务类型（会议/培训/内控/其他）" prop="affairType">
        <el-input v-model="dataForm.affairType" placeholder="事务类型（会议/培训/内控/其他）"></el-input>
      </el-form-item>
      <el-form-item label="事务发生日期" prop="affairDate">
        <el-input v-model="dataForm.affairDate" placeholder="事务发生日期"></el-input>
      </el-form-item>
      <el-form-item label="关联部门ID（关联sys_dept.id）" prop="deptId">
        <el-input v-model="dataForm.deptId" placeholder="关联部门ID（关联sys_dept.id）"></el-input>
      </el-form-item>
      <el-form-item label="租户编码" prop="tenantCode">
        <el-input v-model="dataForm.tenantCode" placeholder="租户编码"></el-input>
      </el-form-item>
      <el-form-item label="状态 0：停用 1：正常" prop="status">
        <el-input v-model="dataForm.status" placeholder="状态 0：停用 1：正常"></el-input>
      </el-form-item>
      <el-form-item label="从原系统同步的时间（无则填NULL）" prop="syncTime">
        <el-input v-model="dataForm.syncTime" placeholder="从原系统同步的时间（无则填NULL）"></el-input>
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
  affairId: "",
  affairName: "",
  affairType: "",
  affairDate: "",
  deptId: "",
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

const init = (affairId?: number) => {
  visible.value = true;
  dataForm.affairId = "";

  // 重置表单数据
  if (dataFormRef.value) {
    dataFormRef.value.resetFields();
  }

  if (affairId) {
    getInfo(affairId);
  }
};

// 获取信息
const getInfo = (affairId: number) => {
  baseService.get("/sys/rjdcompanyaffairinfo/" + affairId).then((res) => {
    Object.assign(dataForm, res.data);
  });
};

// 表单提交
const dataFormSubmitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }
    (!dataForm.affairId ? baseService.post : baseService.put)("/sys/rjdcompanyaffairinfo", dataForm).then((res) => {
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
