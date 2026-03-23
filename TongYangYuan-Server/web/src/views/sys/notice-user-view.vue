<template>
  <div style="text-align: center; font-size: 28px">{{ dataForm.title }}</div>
  <el-divider></el-divider>
  <div v-html="dataForm.content"></div>
  <div>
    <hr size="1" color="#ddd" style="margin: 30px 0 10px 0" />
    <span>
      <el-icon style="color: #e6444a"><user-solid /></el-icon> {{ $t("notice.senderName") }}：{{ dataForm.senderName }}
    </span>
    <el-divider direction="vertical"></el-divider>
    <span>
      <el-icon style="color: #e6444a"><time /></el-icon> {{ $t("notice.senderDate") }}：{{ dataForm.senderDate }}
    </span>
    <el-divider direction="vertical" style="margin: 0px; padding: 0px"></el-divider>
    <span>
      <el-icon style="color: #e6a23c"><s-order /></el-icon>
      {{ $t("notice.type") }}：
      {{ state.getDictLabel("notice_type", dataForm.noticeType) }}
    </span>
    <hr size="1" color="#ddd" style="margin: 10px 0 30px 0" />
  </div>
  <div style="text-align: center">
    <el-button type="danger" @click="state.closeCurrentTab()">{{ $t("notice.close") }}</el-button>
  </div>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { onMounted, reactive, toRefs } from "vue";
import baseService from "@/service/baseService";
import { useRoute } from "vue-router";
const route = useRoute();

const dataForm = reactive({
  id: "",
  noticeType: 0,
  senderDate: "",
  senderName: "",
  content: "",
  title: ""
});

const view = reactive({
  getDataListURL: "/sys/notice/user/page",
  createdIsNeed: false,
  activatedIsNeed: false,
  getDataListIsPage: true
});

const state = reactive({ ...useView(view), ...toRefs(view) });

onMounted(() => {
  dataForm.id = route.query.id as string;
  dataForm.content = "";
  dataForm.senderDate = "";
  dataForm.senderName = "";
  dataForm.title = "";
  getInfo();
  state.query();
});

// 获取信息
const getInfo = () => {
  baseService.get(`/sys/notice/${dataForm.id}`).then((res) => {
    Object.assign(dataForm, res.data);
  });
};
</script>
