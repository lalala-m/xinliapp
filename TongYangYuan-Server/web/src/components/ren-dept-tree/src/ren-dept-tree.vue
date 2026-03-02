<template>
  <div>
    <el-input v-model="showDeptName" :placeholder="placeholder" @click="deptDialog">
      <template v-slot:append>
        <el-button icon="search" @click="deptDialog"></el-button>
      </template>
    </el-input>
    <el-dialog v-model="visibleDept" width="30%" :modal="false" :title="placeholder" :close-on-click-modal="false" :close-on-press-escape="false">
      <el-form :inline="true">
        <el-form-item :label="$t('keyword')">
          <el-input v-model="filterText" :input-style="{ width: '150px' }"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="default">{{ $t("query") }}</el-button>
        </el-form-item>
      </el-form>
      <el-tree class="filter-tree" :data="deptList" style="margin-top: 20px" :default-expanded-keys="expandedKeys" :props="{ label: 'name', children: 'children' }" :expand-on-click-node="false" :filter-node-method="filterNode" :highlight-current="true" node-key="id" ref="treeRef"> </el-tree>
      <template v-slot:footer>
        <el-button type="default" @click="cancelHandle()">{{ $t("cancel") }}</el-button>
        <el-button v-if="query" type="info" @click="clearHandle()">{{ $t("clear") }}</el-button>
        <el-button type="primary" @click="commitHandle()">{{ $t("confirm") }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script lang="ts" setup>
import { nextTick, ref, watch } from "vue";
import { IObject } from "@/types/interface";
import baseService from "@/service/baseService";
import { ElMessage } from "element-plus";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const filterText = ref("");
const visibleDept = ref(false);
const deptList = ref<any[]>([]);
const showDeptName = ref("");
const expandedKeys = ref<any[]>([]);
const treeRef = ref();

const props = defineProps({
  modelValue: String,
  deptName: String,
  query: Boolean,
  placeholder: String
});

watch(
  () => filterText.value,
  (val) => {
    treeRef.value.filter(val);
  }
);

watch(
  () => props.deptName,
  (val) => {
    showDeptName.value = val as string;
  }
);

const deptDialog = () => {
  expandedKeys.value = [];
  visibleDept.value = true;
  getDeptList(props.modelValue);
};

const filterNode = (value: string, data: IObject) => {
  if (!value) return true;
  return data.name.indexOf(value) !== -1;
};

const getDeptList = (id?: string) => {
  return baseService.get("/sys/dept/list").then((res) => {
    deptList.value = res.data;
    nextTick(() => {
      if (id) {
        treeRef.value.setCurrentKey(id);
        expandedKeys.value = [id];
      }
    });
  });
};

const cancelHandle = () => {
  visibleDept.value = false;
  deptList.value = [];
  filterText.value = "";
};

const emit = defineEmits(["update:modelValue", "update:deptName"]);

const clearHandle = () => {
  emit("update:modelValue", "");
  emit("update:deptName", "");
  showDeptName.value = "";
  visibleDept.value = false;
  deptList.value = [];
  filterText.value = "";
};

const commitHandle = () => {
  const node = treeRef.value.getCurrentNode();
  if (!node) {
    ElMessage.error(t("dept.chooseerror"));
    return;
  }
  emit("update:modelValue", node.id);
  emit("update:deptName", node.name);
  showDeptName.value = node.name;
  visibleDept.value = false;
  deptList.value = [];
  filterText.value = "";
};
</script>
