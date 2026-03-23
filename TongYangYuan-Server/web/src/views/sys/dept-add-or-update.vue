<template>
  <el-dialog v-model="visible" :title="!dataForm.id ? $t('add') : $t('update')" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" label-width="120px">
      <el-form-item prop="name" :label="$t('dept.name')">
        <el-input v-model="dataForm.name" :placeholder="$t('dept.name')"></el-input>
      </el-form-item>
      <el-form-item prop="parentName" :label="$t('dept.parentName')" class="dept-list">
        <el-popover :width="400" ref="deptListPopover" placement="bottom-start" trigger="click" popper-class="popover-pop">
          <template v-slot:reference>
            <el-input v-model="dataForm.parentName" :readonly="true" :placeholder="$t('dept.parentName')">
              <template v-slot:suffix>
                <el-icon v-if="dataForm.pid !== '0'" @click.stop="deptListTreeSetDefaultHandle()" class="el-input__icon">
                  <circle-close />
                </el-icon>
              </template>
            </el-input>
          </template>
          <div class="popover-pop-body">
            <el-tree :data="deptList" :props="{ label: 'name', children: 'children' }" node-key="id" ref="deptListTree" :highlight-current="true" :expand-on-click-node="false" accordion @current-change="deptListTreeCurrentChangeHandle"></el-tree>
          </div>
        </el-popover>
      </el-form-item>
      <el-form-item prop="leaderName" :label="$t('dept.leaderName')">
        <el-input v-model="dataForm.leaderName" class="input-with-select" clearable @clear="dataForm.leaderId = ''">
          <template v-slot:append>
            <el-button icon="search" @click="selectUserInfo()"></el-button>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item prop="sort" :label="$t('dept.sort')">
        <el-input-number v-model="dataForm.sort" controls-position="right" :min="0" :label="$t('dept.sort')"></el-input-number>
      </el-form-item>
    </el-form>
    <template v-slot:footer>
      <el-button @click="visible = false">{{ $t("cancel") }}</el-button>
      <el-button type="primary" @click="dataFormSubmitHandle()">{{ $t("confirm") }}</el-button>
    </template>
  </el-dialog>

  <select-user v-if="selectUserVisible" ref="selectUserRef"></select-user>
</template>

<script lang="ts" setup>
import { computed, nextTick, reactive, ref } from "vue";
import baseService from "@/service/baseService";
import { IObject } from "@/types/interface";
import { useAppStore } from "@/store";
import { useI18n } from "vue-i18n";
import { ElMessage } from "element-plus";
import SelectUser from "@/components/ren-process-running/src/select-user.vue";

const store = useAppStore();
const { t } = useI18n();

const emit = defineEmits(["refreshDataList"]);
const visible = ref(false);
const selectUserVisible = ref(false);
const dataFormRef = ref();
const selectUserRef = ref();
const deptList = ref([]);
const deptListPopover = ref();
const deptListTree = ref();

const dataForm = reactive({
  id: "",
  name: "",
  pid: "",
  leaderId: "",
  leaderName: "",
  parentName: "",
  sort: 0
});

const user = computed(() => store.state.user);

const rules = ref({
  name: [{ required: true, message: t("validate.required"), trigger: "blur" }],
  parentName: [{ required: true, message: t("validate.required"), trigger: "change" }]
});

const init = (id?: number) => {
  visible.value = true;
  dataForm.id = "";

  // 重置表单数据
  if (dataFormRef.value) {
    dataFormRef.value.resetFields();
  }

  getDeptList().then(() => {
    if (id) {
      getInfo(id);
    } else {
      deptListTreeSetDefaultHandle();
    }
  });
};

// 获取部门列表
const getDeptList = () => {
  return baseService.get("/sys/dept/list").then((res) => {
    if (res.code !== 0) {
      return ElMessage.error(res.msg);
    }
    deptList.value = res.data;
  });
};

const selectUserInfo = () => {
  selectUserVisible.value = true;
  nextTick(() => {
    selectUserRef.value.init(setUserInfo);
  });
};

const setUserInfo = (userInfo: IObject) => {
  dataForm.leaderId = userInfo.id;
  dataForm.leaderName = userInfo.realName;
};

// 获取信息
const getInfo = (id: number) => {
  baseService.get(`/sys/dept/${id}`).then((res) => {
    Object.assign(dataForm, res.data);

    if (dataForm.pid === "0") {
      return deptListTreeSetDefaultHandle();
    }
    deptListTree.value.setCurrentKey(dataForm.pid);
  });
};

// 上级部门树, 设置默认值
const deptListTreeSetDefaultHandle = () => {
  dataForm.pid = "0";
  dataForm.parentName = t("dept.parentNameDefault");
};

// 上级部门树, 选中
const deptListTreeCurrentChangeHandle = (data: IObject) => {
  dataForm.pid = data.id;
  dataForm.parentName = data.name;
  deptListPopover.value.hide();
};

// 表单提交
const dataFormSubmitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }
    (!dataForm.id ? baseService.post : baseService.put)("/sys/dept", dataForm).then((res) => {
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

<style lang="less" scoped>
.mod-sys__dept {
  .dept-list {
    .el-input__inner,
    .el-input__suffix {
      cursor: pointer;
    }
  }
}
</style>
