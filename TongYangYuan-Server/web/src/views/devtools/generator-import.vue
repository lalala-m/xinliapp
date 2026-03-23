<template>
  <el-dialog v-model="visible" title="导入数据库表" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" label-width="120px">
      <el-form-item label="数据源" prop="datasourceId">
        <el-select v-model="dataForm.datasourceId" @change="getTableInfoList" style="width: 100%" placeholder="请选择数据源">
          <el-option label="默认数据源" value="0"></el-option>
          <el-option v-for="ds in dataForm.datasourceList" :key="ds.id" :label="ds.connName" :value="ds.id"> </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="选择表" prop="tableInfo">
        <el-select v-model="dataForm.tableInfo" value-key="tableName" :disabled="!dataForm.showTableSelect" style="width: 100%" placeholder="请选择表名">
          <el-option v-for="tableInfo in dataForm.tableInfoList" :key="tableInfo.tableName" :label="tableInfo.tableName" :value="tableInfo"> </el-option>
        </el-select>
      </el-form-item>
    </el-form>
    <template v-slot:footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="dataFormSubmitHandle">确定</el-button>
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

const dataForm = reactive({
  id: "",
  datasourceId: "",
  datasourceList: [] as IObject,
  tableInfoList: [] as IObject,
  tableInfo: { tableName: "" },
  showTableSelect: false
});

const validateTable = (rule: any, value: any, callback: (e?: Error) => any) => {
  if (!dataForm.tableInfo.tableName) {
    return callback(new Error("必填项不能为空"));
  }
  callback();
};
const rules = ref({
  datasourceId: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  tableInfo: [{ validator: validateTable, trigger: "blur" }]
});

const init = () => {
  visible.value = true;
  dataForm.id = "";

  // 重置表单数据
  if (dataFormRef.value) {
    dataFormRef.value.resetFields();
  }

  getDataSource();
};

// 获取数据源
const getDataSource = () => {
  baseService.get("/devtools/datasource/list").then((res) => {
    dataForm.datasourceList = res.data;
  });
};

// 获取数据表
const getTableInfoList = () => {
  dataForm.showTableSelect = false;
  dataForm.tableInfo.tableName = "";

  baseService.get("/devtools/datasource/table/list/" + dataForm.datasourceId).then((res) => {
    dataForm.tableInfoList = res.data;
    dataForm.showTableSelect = true;
  });
};

// 表单提交
const dataFormSubmitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }
    (!dataForm.id ? baseService.post : baseService.put)("/devtools/datasource/table", dataForm.tableInfo).then(() => {
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
