<template>
  <el-dialog v-model="visible" :title="!dataForm.id ? $t('add') : $t('update')" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" label-width="120px">
      <el-form-item label="主键ID" prop="id">
        <el-input v-model="dataForm.id" placeholder="主键ID"></el-input>
      </el-form-item>
      <el-form-item label="关联日结单ID（rjd_daily_work_record.id）" prop="dailyRecordId">
        <el-input v-model="dataForm.dailyRecordId" placeholder="关联日结单ID（rjd_daily_work_record.id）"></el-input>
      </el-form-item>
      <el-form-item label="关联对象类型ID（rjd_related_object_type_dict.type_id）" prop="relatedTypeId">
        <el-input v-model="dataForm.relatedTypeId" placeholder="关联对象类型ID（rjd_related_object_type_dict.type_id）"></el-input>
      </el-form-item>
      <el-form-item label="关联对象ID（产品/客户/订单/事务ID）" prop="relatedObjectId">
        <el-input v-model="dataForm.relatedObjectId" placeholder="关联对象ID（产品/客户/订单/事务ID）"></el-input>
      </el-form-item>
      <el-form-item label="关联对象名称（冗余存储）" prop="relatedObjectName">
        <el-input v-model="dataForm.relatedObjectName" placeholder="关联对象名称（冗余存储）"></el-input>
      </el-form-item>
      <el-form-item label="分摊到该对象的耗时（小时）" prop="allocatedHours">
        <el-input v-model="dataForm.allocatedHours" placeholder="分摊到该对象的耗时（小时）"></el-input>
      </el-form-item>
      <el-form-item label="分摊比例（%）" prop="allocatedRatio">
        <el-input v-model="dataForm.allocatedRatio" placeholder="分摊比例（%）"></el-input>
      </el-form-item>
      <el-form-item label="分摊备注" prop="allocatedRemark">
        <el-input v-model="dataForm.allocatedRemark" placeholder="分摊备注"></el-input>
      </el-form-item>
      <el-form-item label="租户编码" prop="tenantCode">
        <el-input v-model="dataForm.tenantCode" placeholder="租户编码"></el-input>
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
  id: "",
  dailyRecordId: "",
  relatedTypeId: "",
  relatedObjectId: "",
  relatedObjectName: "",
  allocatedHours: "",
  allocatedRatio: "",
  allocatedRemark: "",
  tenantCode: "",
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
  baseService.get("/sys/rjddailyworkrelatedobject/" + id).then((res) => {
    Object.assign(dataForm, res.data);
  });
};

// 表单提交
const dataFormSubmitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }
    (!dataForm.id ? baseService.post : baseService.put)("/sys/rjddailyworkrelatedobject", dataForm).then((res) => {
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
