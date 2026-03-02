<template>
  <el-dialog v-model="visible" :title="!dataForm.id ? $t('add') : $t('update')" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" :label-width="$i18n.locale === 'en-US' ? '120px' : '80px'">
      <el-form-item label="产品名称" prop="name">
        <el-input v-model="dataForm.name" placeholder="产品名称"></el-input>
      </el-form-item>
      <el-form-item label="产品介绍" prop="content">
        <el-input v-model="dataForm.content" placeholder="产品介绍"></el-input>
      </el-form-item>
    </el-form>
    <vxe-toolbar perfect>
      <template v-slot:buttons>
        <vxe-button icon="fa fa-plus" status="perfect" @click="insertEvent(-1)">新增</vxe-button>
        <vxe-button icon="fa fa-trash-o" status="perfect" @click="removeEvent">删除</vxe-button>
      </template>
    </vxe-toolbar>
    <vxe-table border show-overflow ref="xTable" class="my_table_insert" max-height="400" :data="tableData" :edit-config="{ trigger: 'click', mode: 'cell', icon: 'fa fa-pencil' }">
      <vxe-column type="checkbox" width="60"></vxe-column>
      <vxe-column field="paramName" title="参数名" sortable :edit-render="{ name: 'input', defaultValue: '' }"></vxe-column>
      <vxe-column field="paramValue" title="参数值" :edit-render="{ name: 'input' }"></vxe-column>
    </vxe-table>
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
const tableData = ref([]);
const xTable = ref();
const dataFormRef = ref();

const dataForm = reactive({
  id: "",
  name: "",
  content: "",
  subList: []
});

const rules = ref({
  name: [{ required: true, message: t("validate.required"), trigger: "blur" }],
  content: [{ required: true, message: t("validate.required"), trigger: "blur" }],
  updater: [{ required: true, message: t("validate.required"), trigger: "blur" }],
  updateDate: [{ required: true, message: t("validate.required"), trigger: "blur" }]
});

const init = (id?: number) => {
  visible.value = true;
  dataForm.id = "";
  tableData.value = [];

  // 重置表单数据
  if (dataFormRef.value) {
    dataFormRef.value.resetFields();
  }

  if (id) {
    getInfo(id);
  }
};

const insertEvent = async (row: number) => {
  let record = {
    num: "1"
  };
  let { row: newRow } = await xTable.value.insertAt(record, row);
  await xTable.value.setActiveCell(newRow, "paramName");
};

const removeEvent = () => {
  const selectRecords = xTable.value.getCheckboxRecords();
  if (selectRecords.length) {
    xTable.value.removeCheckboxRow();
  } else {
    ElMessage.error("请至少选择一条数据");
  }
};

// 获取信息
const getInfo = (id: number) => {
  baseService.get(`/sys/product/${id}`).then((res) => {
    Object.assign(dataForm, res.data);
    tableData.value = res.data.subList;
  });
};

// 表单提交
const dataFormSubmitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }
    dataForm.subList = xTable.value.getTableData().tableData;
    (!dataForm.id ? baseService.post : baseService.put)("/sys/product", dataForm).then(() => {
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
