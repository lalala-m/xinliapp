<template>
  <el-dialog v-model="visible" :title="!dataForm.id ? $t('add') : $t('update')" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" label-width="120px">
      <el-form-item prop="name" :label="$t('role.name')">
        <el-input v-model="dataForm.name" :placeholder="$t('role.name')"></el-input>
      </el-form-item>
      <el-form-item prop="remark" :label="$t('role.remark')">
        <el-input v-model="dataForm.remark" :placeholder="$t('role.remark')"></el-input>
      </el-form-item>
      <el-row>
        <el-col :span="12">
          <el-form-item size="small" :label="$t('role.menuList')">
            <el-tree :data="menuList" :props="{ label: 'name', children: 'children' }" node-key="id" ref="menuListTree" accordion show-checkbox> </el-tree>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <template v-slot:footer>
      <el-button @click="visible = false">{{ $t("cancel") }}</el-button>
      <el-button type="primary" @click="dataFormSubmitHandle()">{{ $t("confirm") }}</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { nextTick, reactive, ref } from "vue";
import baseService from "@/service/baseService";
import { IObject } from "@/types/interface";
import { useI18n } from "vue-i18n";
import { ElMessage } from "element-plus";
const { t } = useI18n();

const emit = defineEmits(["refreshDataList"]);
const visible = ref(false);
const menuList = ref([]);
const menuListTree = ref();
const dataFormRef = ref();

const dataForm = reactive({
  id: "",
  name: "",
  menuIdList: [] as IObject[],
  remark: ""
});

const rules = ref({
  name: [{ required: true, message: t("validate.required"), trigger: "blur" }]
});

const init = (id?: number) => {
  visible.value = true;
  dataForm.id = "";

  // 重置表单数据
  if (dataFormRef.value) {
    dataFormRef.value.resetFields();
  }

  nextTick(() => {
    if (menuListTree.value) {
      menuListTree.value.setCheckedKeys([]);
    }

    Promise.all([getMenuList()]).then(() => {
      if (id) {
        getInfo(id);
      }
    });
  });
};

const getMenuList = async () => {
  const res = await baseService.get("/sys/menu/select");
  menuList.value = res.data;
};

// 获取信息
const getInfo = (id: number) => {
  baseService.get("/sys/tenant/role/" + id).then((res) => {
    Object.assign(dataForm, res.data);

    dataForm.menuIdList.forEach((item: IObject) => menuListTree.value.setChecked(item, true));
  });
};

const dataFormSubmitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }
    dataForm.menuIdList = [...menuListTree.value.getHalfCheckedKeys(), ...menuListTree.value.getCheckedKeys()];
    (!dataForm.id ? baseService.post : baseService.put)("/sys/tenant/role", dataForm).then(() => {
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
