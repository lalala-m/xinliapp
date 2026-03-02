<template>
  <el-dialog v-model="visible" :title="!dataForm.id ? $t('add') : $t('update')" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" :label-width="$i18n.locale === 'en-US' ? '120px' : '80px'">
      <el-form-item :label="$t('notice.type')" prop="noticeType">
        <ren-radio-group v-model="dataForm.noticeType" dict-type="notice_type"></ren-radio-group>
      </el-form-item>
      <el-form-item :label="$t('notice.title')" prop="title">
        <el-input v-model="dataForm.title" :placeholder="$t('notice.title')"></el-input>
      </el-form-item>
      <el-form-item prop="content" :label="$t('notice.content')">
        <!-- 富文本编辑器, 容器 -->
        <div id="J_quillEditor" style="height: 280px; width: 100%"></div>
        <!-- 自定义上传图片功能 (使用element upload组件) -->
        <el-upload :action="uploadUrl" :show-file-list="false" :before-upload="uploadBeforeUploadHandle" :on-success="uploadSuccessHandle" style="display: none">
          <el-button ref="uploadBtn" type="primary" size="small">{{ $t("upload.button") }}</el-button>
        </el-upload>
      </el-form-item>
      <el-form-item :label="$t('notice.receiverType')" prop="">
        <el-radio-group v-model="dataForm.receiverType">
          <el-radio :label="0">{{ $t("notice.receiverType0") }}</el-radio>
          <el-radio :label="1">{{ $t("notice.receiverType1") }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-show="dataForm.receiverType == 1" :label="$t('notice.selectDept')">
        <el-tree :data="deptList" :props="{ label: 'name', children: 'children' }" node-key="id" ref="deptListTree" accordion show-checkbox> </el-tree>
      </el-form-item>
    </el-form>
    <template v-slot:footer>
      <el-button @click="visible = false">{{ $t("cancel") }}</el-button>
      <el-button type="danger" @click="dataFormSubmitHandle(0)">{{ $t("notice.draft") }}</el-button>
      <el-button type="primary" @click="dataFormSubmitHandle(1)">{{ $t("notice.release") }}</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { nextTick, reactive, ref } from "vue";
import "quill/dist/quill.snow.css";
import Quill from "quill";
import { IObject } from "@/types/interface";
import app from "@/constants/app";
import { getToken } from "@/utils/cache";
import baseService from "@/service/baseService";
import { useI18n } from "vue-i18n";
import { ElMessage } from "element-plus";
const { t } = useI18n();
const emit = defineEmits(["refreshDataList"]);

const visible = ref(false);
const quillEditor = ref();
const quillEditorToolbarOptions = [["bold", "italic", "underline", "strike"], ["image"], [{ list: "ordered" }, { list: "bullet" }], [{ size: ["small", false, "large", "huge"] }], [{ color: [] }, { background: [] }], ["clean"]];
const uploadUrl = ref();
const deptList = ref([]);
const deptListTree = ref();
const uploadBtn = ref();
const dataFormRef = ref();

const dataForm = reactive({
  id: "",
  noticeType: 0,
  title: "",
  content: "",
  receiverType: 0,
  receiverTypeIds: "",
  receiverTypeList: [],
  status: 0,
  senderName: "",
  senderDate: "",
  creator: 0,
  createDate: ""
});

const validateContent = (rule: IObject, value: string, callback: (e?: Error) => any) => {
  if (quillEditor.value && quillEditor.value.getLength() <= 1) {
    return callback(new Error(t("validate.required")));
  }
  callback();
};

const rules = ref({
  noticeType: [{ required: true, message: t("validate.required"), trigger: "blur" }],
  title: [{ required: true, message: t("validate.required"), trigger: "blur" }],
  content: [
    { required: true, message: t("validate.required"), trigger: "blur" },
    { validator: validateContent, trigger: "blur" }
  ],
  receiverType: [{ required: true, message: t("validate.required"), trigger: "blur" }],
  receiverTypeIds: [{ required: true, message: t("validate.required"), trigger: "blur" }],
  status: [{ required: true, message: t("validate.required"), trigger: "blur" }],
  senderName: [{ required: true, message: t("validate.required"), trigger: "blur" }]
});

const init = (id: number) => {
  visible.value = true;
  dataForm.id = "";

  nextTick(() => {
    if (quillEditor.value) {
      quillEditor.value.deleteText(0, quillEditor.value.getLength());
    } else {
      quillEditorHandle();
    }

    // 重置表单数据
    if (dataFormRef.value) {
      dataFormRef.value.resetFields();
    }

    Promise.all([getDeptList()]).then(() => {
      if (id) {
        getInfo(id);
      }
    });
  });
};

// 富文本编辑器
const quillEditorHandle = () => {
  quillEditor.value = new Quill("#J_quillEditor", {
    modules: {
      toolbar: quillEditorToolbarOptions
    },
    theme: "snow"
  });
  // 自定义上传图片功能 (使用element upload组件)
  uploadUrl.value = `${app.api}/oss/file/upload?access_token=${getToken()}`;
  quillEditor.value.getModule("toolbar").addHandler("image", () => {
    uploadBtn.value.$el.click();
  });
  // 监听内容变化，动态赋值
  quillEditor.value.on("text-change", () => {
    if (quillEditor.value) {
      dataForm.content = quillEditor.value.root.innerHTML;
    }
  });
};

// 上传图片之前
const uploadBeforeUploadHandle = (file: IObject) => {
  if (file.type !== "image/jpg" && file.type !== "image/jpeg" && file.type !== "image/png" && file.type !== "image/gif") {
    ElMessage.error(t("upload.tip", { format: "jpg、png、gif" }));
    return false;
  }
};

// 上传图片成功
const uploadSuccessHandle = (res: IObject) => {
  if (res.code !== 0) {
    return ElMessage.error(res.msg);
  }
  if (quillEditor.value) {
    const selection = quillEditor.value.getSelection();
    if (selection) {
      quillEditor.value.insertEmbed(selection.index, "image", res.data.src);
    }
  }
};

// 获取部门列表
const getDeptList = () => {
  return baseService.get("/sys/dept/list").then((res) => {
    deptList.value = res.data;
  });
};

// 获取信息
const getInfo = (id: number) => {
  baseService.get(`/sys/notice/${id}`).then((res) => {
    Object.assign(dataForm, res.data);
    if (quillEditor.value) {
      quillEditor.value.root.innerHTML = dataForm.content;
    }

    // 接受者为部门
    if (dataForm.receiverType === 1) {
      deptListTree.value.setCheckedKeys(res.data.receiverTypeIds.split(","));
    }
  });
};

// 表单提交
const dataFormSubmitHandle = (status: number) => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }
    // 接受者为部门
    if (dataForm.receiverType === 1) {
      dataForm.receiverTypeIds = deptListTree.value.getCheckedKeys().join(",");
      dataForm.receiverTypeList = deptListTree.value.getCheckedKeys();
    } else {
      dataForm.receiverTypeIds = "";
    }
    dataForm.status = status;
    (!dataForm.id ? baseService.post : baseService.put)("/sys/notice", dataForm).then(() => {
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
