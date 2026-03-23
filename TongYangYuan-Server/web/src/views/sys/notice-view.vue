<template>
  <div style="text-align: center; font-size: 28px">{{ dataForm.title }}</div>
  <el-divider></el-divider>
  <div v-html="dataForm.content"></div>
  <div>
    <hr size="1" color="#ddd" style="margin: 30px 0 10px 0" />
    <span><i class="el-icon-user-solid" style="color: #e6444a"></i> {{ $t("notice.senderName") }}：{{ dataForm.senderName }}</span>
    <el-divider direction="vertical"></el-divider>
    <span><i class="el-icon-time" style="color: #e6444a"></i> {{ $t("notice.senderDate") }}：{{ dataForm.senderDate }}</span>
    <el-divider direction="vertical" style="margin: 0px; padding: 0px"></el-divider>
    <span>
      <i class="el-icon-s-order" style="color: #e6a23c"></i> {{ $t("notice.type") }}：
      {{ state.getDictLabel("notice_type", dataForm.noticeType) }}
    </span>
    <hr size="1" color="#ddd" style="margin: 10px 0 30px 0" />
  </div>
  <el-table v-loading="state.dataListLoading" :data="state.dataList" border @selection-change="state.dataListSelectionChangeHandle" style="width: 100%">
    <el-table-column prop="receiverName" :label="$t('notice.receiverName')" header-align="center" align="center"></el-table-column>
    <el-table-column prop="readStatus" :label="$t('notice.readStatus')" header-align="center" align="center">
      <template v-slot="scope">
        <el-tag v-if="scope.row.readStatus === 0" type="danger">{{ $t("notice.readStatus0") }}</el-tag>
        <el-tag v-else type="success">{{ $t("notice.readStatus1") }}</el-tag>
      </template>
    </el-table-column>
    <el-table-column prop="readDate" :label="$t('notice.readDate')" header-align="center" align="center"></el-table-column>
  </el-table>
  <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"> </el-pagination>
  <el-divider></el-divider>
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
  state.dataList = [];
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
