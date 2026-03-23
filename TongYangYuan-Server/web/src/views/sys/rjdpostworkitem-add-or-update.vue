<template>
  <el-dialog v-model="visible" :title="!dataForm.itemId ? $t('add') : $t('update')" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" label-width="120px">
      <el-form-item label="事项ID" prop="itemId">
        <el-input v-model="dataForm.itemId" placeholder="事项ID"></el-input>
      </el-form-item>
      <el-form-item label="岗位ID（关联sys_post.id）" prop="postId">
        <el-input v-model="dataForm.postId" placeholder="岗位ID（关联sys_post.id）"></el-input>
      </el-form-item>
      <el-form-item label="岗位名称（冗余存储）" prop="postName">
        <el-input v-model="dataForm.postName" placeholder="岗位名称（冗余存储）"></el-input>
      </el-form-item>
      <el-form-item label="工作事项名称" prop="workItemName">
        <el-input v-model="dataForm.workItemName" placeholder="工作事项名称"></el-input>
      </el-form-item>
      <el-form-item label="排序号" prop="sort">
        <el-input v-model="dataForm.sort" placeholder="排序号"></el-input>
      </el-form-item>
      <el-form-item label="租户编码" prop="tenantCode">
        <el-input v-model="dataForm.tenantCode" placeholder="租户编码"></el-input>
      </el-form-item>
      <el-form-item label="状态  0：停用   1：正常" prop="status">
        <el-input v-model="dataForm.status" placeholder="状态  0：停用   1：正常"></el-input>
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
  itemId: "",
  postId: "",
  postName: "",
  workItemName: "",
  sort: "",
  tenantCode: "",
  status: "",
  creator: "",
  createDate: "",
  updater: "",
  updateDate: "",
  delFlag: ""
});

const rules = ref({});

const init = (itemId?: number) => {
  visible.value = true;
  dataForm.itemId = "";

  // 重置表单数据
  if (dataFormRef.value) {
    dataFormRef.value.resetFields();
  }

  if (itemId) {
    getInfo(itemId);
  }
};

// 获取信息
const getInfo = (itemId: number) => {
  baseService.get("/sys/rjdpostworkitem/" + itemId).then((res) => {
    Object.assign(dataForm, res.data);
  });
};

// 表单提交
const dataFormSubmitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }
    (!dataForm.itemId ? baseService.post : baseService.put)("/sys/rjdpostworkitem", dataForm).then((res) => {
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
