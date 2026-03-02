<template>
  <el-dialog v-model="visible" title="Excel导入" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-upload name="file" :action="url" :file-list="fileList" drag :on-success="successHandle" class="text-center">
      <el-icon><upload /></el-icon>
      <div class="el-upload__text" v-html="$t('upload.text')"></div>
    </el-upload>
  </el-dialog>
</template>

<script lang="ts" setup>
import { ref } from "vue";
import app from "@/constants/app";
import { getToken } from "@/utils/cache";
import { IObject } from "@/types/interface";
import { useI18n } from "vue-i18n";
import { ElMessage } from "element-plus";
const { t } = useI18n();
const emit = defineEmits(["refreshDataList"]);

const visible = ref(false);
const url = ref("");
const fileList = ref([]);

const init = () => {
  visible.value = true;
  url.value = `${app.api}/sys/excel/import?access_token=${getToken()}`;
  fileList.value = [];
};

// 上传成功
const successHandle = (res: IObject) => {
  if (res.code !== 0) {
    return ElMessage.error(res.msg);
  }
  ElMessage.success({
    message: t("prompt.success"),
    duration: 500,
    onClose: () => {
      visible.value = false;
      emit("refreshDataList");
    }
  });
};

defineExpose({
  init
});
</script>
