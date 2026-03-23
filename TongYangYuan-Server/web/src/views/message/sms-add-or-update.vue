<template>
  <el-dialog v-model="visible" :title="!dataForm.id ? $t('add') : $t('update')" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" label-width="120px">
      <el-form-item prop="smsCode" :label="$t('sms.smsCode')">
        <el-input v-model="dataForm.smsCode" :placeholder="$t('sms.smsCode')"></el-input>
      </el-form-item>
      <el-form-item prop="remark" :label="$t('sms.remark')">
        <el-input v-model="dataForm.remark" :placeholder="$t('sms.remark')"></el-input>
      </el-form-item>
      <el-divider></el-divider>
      <el-form-item :label="$t('sms.platform')">
        <el-radio-group v-model="dataForm.platform">
          <el-radio :label="1">{{ $t("sms.platform1") }}</el-radio>
          <el-radio :label="2">{{ $t("sms.platform2") }}</el-radio>
          <el-radio :label="3">{{ $t("sms.platform3") }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <template v-if="dataForm.platform === 1">
        <el-form-item prop="config.aliyunAccessKeyId" :label="$t('sms.aliyunAccessKeyId')">
          <el-input v-model="dataForm.config.aliyunAccessKeyId" :placeholder="$t('sms.aliyunAccessKeyIdTips')"></el-input>
        </el-form-item>
        <el-form-item prop="config.aliyunAccessKeySecret" :label="$t('sms.aliyunAccessKeySecret')">
          <el-input v-model="dataForm.config.aliyunAccessKeySecret" :placeholder="$t('sms.aliyunAccessKeySecretTips')"></el-input>
        </el-form-item>
        <el-form-item prop="config.aliyunSignName" :label="$t('sms.aliyunSignName')">
          <el-input v-model="dataForm.config.aliyunSignName" :placeholder="$t('sms.aliyunSignName')"></el-input>
        </el-form-item>
        <el-form-item prop="config.aliyunTemplateCode" :label="$t('sms.aliyunTemplateCode')">
          <el-input v-model="dataForm.config.aliyunTemplateCode" :placeholder="$t('sms.aliyunTemplateCodeTips')"></el-input>
        </el-form-item>
      </template>
      <template v-else-if="dataForm.platform === 2">
        <el-form-item prop="config.qcloudAppId" :label="$t('sms.qcloudAppId')">
          <el-input v-model="dataForm.config.qcloudAppId" :placeholder="$t('sms.qcloudAppIdTips')"></el-input>
        </el-form-item>
        <el-form-item prop="config.qcloudAppKey" :label="$t('sms.qcloudAppKey')">
          <el-input v-model="dataForm.config.qcloudAppKey" :placeholder="$t('sms.qcloudAppKeyTips')"></el-input>
        </el-form-item>
        <el-form-item prop="config.qcloudSignName" :label="$t('sms.qcloudSignName')">
          <el-input v-model="dataForm.config.qcloudSignName" :placeholder="$t('sms.qcloudSignName')"></el-input>
        </el-form-item>
        <el-form-item prop="config.qcloudTemplateId" :label="$t('sms.qcloudTemplateId')">
          <el-input v-model="dataForm.config.qcloudTemplateId" :placeholder="$t('sms.qcloudTemplateIdTips')"></el-input>
        </el-form-item>
      </template>
      <template v-else-if="dataForm.platform === 3">
        <el-form-item prop="config.qiniuAccessKey" :label="$t('sms.qiniuAccessKey')">
          <el-input v-model="dataForm.config.qiniuAccessKey" :placeholder="$t('sms.qiniuAccessKeyTips')"></el-input>
        </el-form-item>
        <el-form-item prop="config.qiniuSecretKey" :label="$t('sms.qiniuSecretKey')">
          <el-input v-model="dataForm.config.qiniuSecretKey" :placeholder="$t('sms.qiniuSecretKeyTips')"></el-input>
        </el-form-item>
        <el-form-item prop="config.qiniuTemplateId" :label="$t('sms.qiniuTemplateId')">
          <el-input v-model="dataForm.config.qiniuTemplateId" :placeholder="$t('sms.qiniuTemplateIdTips')"></el-input>
        </el-form-item>
      </template>
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
  smsCode: "",
  remark: "",
  platform: 1,
  config: {
    aliyunAccessKeyId: "",
    aliyunAccessKeySecret: "",
    aliyunSignName: "",
    aliyunTemplateCode: "",
    qcloudAppId: "",
    qcloudAppKey: "",
    qcloudSignName: "",
    qcloudTemplateId: "",
    qiniuAccessKey: "",
    qiniuSecretKey: "",
    qiniuTemplateId: ""
  }
});

const rules = ref({
  smsCode: [{ required: true, message: t("validate.required"), trigger: "blur" }],
  "config.aliyunAccessKeyId": [{ required: true, message: t("validate.required"), trigger: "blur" }],
  "config.aliyunAccessKeySecret": [{ required: true, message: t("validate.required"), trigger: "blur" }],
  "config.aliyunSignName": [{ required: true, message: t("validate.required"), trigger: "blur" }],
  "config.aliyunTemplateCode": [{ required: true, message: t("validate.required"), trigger: "blur" }],
  "config.qcloudAppId": [{ required: true, message: t("validate.required"), trigger: "blur" }],
  "config.qcloudAppKey": [{ required: true, message: t("validate.required"), trigger: "blur" }],
  "config.qcloudSignName": [{ required: true, message: t("validate.required"), trigger: "blur" }],
  "config.qcloudTemplateId": [{ required: true, message: t("validate.required"), trigger: "blur" }],
  "config.qiniuAccessKey": [{ required: true, message: t("validate.required"), trigger: "blur" }],
  "config.qiniuSecretKey": [{ required: true, message: t("validate.required"), trigger: "blur" }],
  "config.qiniuTemplateId": [{ required: true, message: t("validate.required"), trigger: "blur" }]
});

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
  baseService.get(`/message/sms/${id}`).then((res) => {
    Object.assign(dataForm, res.data);
  });
};

// 表单提交
const dataFormSubmitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }
    (!dataForm.id ? baseService.post : baseService.put)("/message/sms", dataForm).then(() => {
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
