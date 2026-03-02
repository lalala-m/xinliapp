<template>
  <el-dialog v-model="visible" :title="insert ? $t('add') : $t('update')" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" label-width="120px">
      <el-form-item prop="parentName" label="上级区域">
        <ren-region-tree ref="regionListTree" v-model="dataForm.pid" placeholder="选择区域" v-model:parent-name="dataForm.parentName"></ren-region-tree>
      </el-form-item>
      <el-form-item prop="name" :label="$t('region.name')">
        <el-input v-model="dataForm.name" :placeholder="$t('region.name')"></el-input>
      </el-form-item>
      <el-form-item prop="id" :label="$t('region.id')">
        <el-input v-model="dataForm.id" :disabled="!insert" :placeholder="$t('region.id')"></el-input>
      </el-form-item>
      <el-form-item prop="sort" :label="$t('region.sort')">
        <el-input-number v-model="dataForm.sort" controls-position="right" :min="0" :label="$t('region.sort')"></el-input-number>
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
const insert = ref(true);
const dataFormRef = ref();

const dataForm = reactive({
  id: "",
  name: "",
  pid: "0",
  parentName: "",
  sort: 0
});

const rules = ref({
  id: [{ required: true, message: t("validate.required"), trigger: "blur" }],
  name: [{ required: true, message: t("validate.required"), trigger: "blur" }],
  sort: [{ required: true, message: t("validate.required"), trigger: "blur" }]
});

const init = (id?: string) => {
  insert.value = true;
  visible.value = true;
  dataForm.pid = "0";

  if (id) {
    insert.value = false;
    getInfo(id);
  } else {
    // 重置表单数据
    if (dataFormRef.value) {
      dataFormRef.value.resetFields();
    }
  }
};

// 获取信息
const getInfo = (id: string) => {
  baseService.get(`/sys/region/${id}`).then((res) => {
    Object.assign(dataForm, res.data);
  });
};

// 表单提交
const dataFormSubmitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }
    (insert.value ? baseService.post : baseService.put)("/sys/region", dataForm).then(() => {
      ElMessage.success({
        message: t("prompt.success"),
        duration: 500,
        onClose: () => {
          visible.value = false;
          emit("refreshDataList", dataForm);
        }
      });
    });
  });
};

defineExpose({
  init
});
</script>

<style lang="less">
.mod-sys__region {
  .region-list {
    .el-input__inner,
    .el-input__suffix {
      cursor: pointer;
    }
  }
}
</style>
