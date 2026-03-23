<template>
  <el-dialog v-model="visible" :title="$t('oss.upload')" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-upload :action="url" :file-list="fileList" drag multiple :before-upload="beforeUploadHandle" :on-success="successHandle" class="text-center">
      <el-icon class="el-icon--upload"><upload-filled /></el-icon>
      <div class="el-upload__text" v-html="$t('upload.text')"></div>
      <template v-slot:tip>
        <div class="el-upload__tip">
          {{ t("upload.tip", { format: "jpg、png、gif" }) }}
        </div>
      </template>
    </el-upload>
  </el-dialog>
</template>

<script lang="ts" setup>
import { ref } from "vue";
import { getToken } from "@/utils/cache";
import { IObject } from "@/types/interface";
import app from "@/constants/app";
import { useI18n } from "vue-i18n";
import { ElMessage } from "element-plus";
const { t } = useI18n();
const emit = defineEmits(["refreshDataList"]);

const visible = ref(false);
const url = ref("");
const num = ref(0);
const fileList = ref<IObject[]>();

const init = () => {
  visible.value = true;
  url.value = `${app.api}/oss/file/upload?access_token=${getToken()}`;
  num.value = 0;
  fileList.value = [];
};

// 上传之前
const beforeUploadHandle = (file: IObject) => {
  if (file.type !== "image/jpg" && file.type !== "image/jpeg" && file.type !== "image/png" && file.type !== "image/gif") {
    ElMessage.error(t("upload.tip", { format: "jpg、png、gif" }));
    return false;
  }
  num.value++;
};

// 上传成功
const successHandle = (res: IObject, file: IObject, list: IObject[]) => {
  if (res.code !== 0) {
    return ElMessage.error(res.msg);
  }

  fileList.value = list;
  num.value--;
  if (num.value === 0) {
    ElMessage.success({
      message: t("prompt.success"),
      duration: 500,
      onClose: () => {
        visible.value = false;
        emit("refreshDataList");
      }
    });
  }
};

defineExpose({
  init
});
</script>
