<template>
  <div class="mod-demo__news">
    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-input v-model="state.dataForm.title" :placeholder="$t('news.title')" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-date-picker v-model="state.daterange" type="daterange" value-format="YYYY-MM-DD" :range-separator="$t('datePicker.range')" :start-placeholder="$t('datePicker.start')" :end-placeholder="$t('datePicker.end')"> </el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button @click="state.getDataList()">{{ $t("query") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="addOrUpdateHandle()">{{ $t("add") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="danger" @click="state.deleteHandle()">{{ $t("deleteBatch") }}</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="state.dataListLoading" :data="state.dataList" border @selection-change="state.dataListSelectionChangeHandle" @sort-change="state.dataListSortChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="title" :label="$t('news.title')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="pubDate" :label="$t('news.pubDate')" sortable="custom" header-align="center" align="center" width="180"></el-table-column>
      <el-table-column prop="createDate" :label="$t('news.createDate')" sortable="custom" header-align="center" align="center" width="180"></el-table-column>
      <el-table-column :label="$t('handle')" fixed="right" header-align="center" align="center" width="150">
        <template v-slot="scope">
          <el-button type="primary" link @click="addOrUpdateHandle(scope.row.id)">{{ $t("update") }}</el-button>
          <el-button type="primary" link @click="state.deleteHandle(scope.row.id)">{{ $t("delete") }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"> </el-pagination>
    <!-- 弹窗, 新增 / 修改 -->
    <add-or-update :key="addKey" ref="addOrUpdateRef" @refreshDataList="state.getDataList"></add-or-update>
  </div>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { nextTick, reactive, ref, toRefs, watch } from "vue";
import AddOrUpdate from "./news-add-or-update.vue";

const view = reactive({
  getDataListURL: "/sys/news/page",
  getDataListIsPage: true,
  deleteURL: "/sys/news",
  deleteIsBatch: true,
  daterange: null,
  dataForm: {
    title: "",
    startDate: null as number | null,
    endDate: null as number | null
  }
});

const state = reactive({ ...useView(view), ...toRefs(view) });

watch(
  () => state.daterange,
  (val: number[] | null) => {
    if (val === null) {
      state.dataForm.startDate = null;
      state.dataForm.endDate = null;
    } else {
      state.dataForm.startDate = val[0];
      state.dataForm.endDate = val[1];
    }
  }
);

const addOrUpdateRef = ref();
const addKey = ref(0);
const addOrUpdateHandle = (id?: number) => {
  addKey.value++;
  nextTick(() => {
    addOrUpdateRef.value.init(id);
  });
};
</script>
