<template>
  <div class="mod-sys__user">
    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-input v-model="state.dataForm.username" :placeholder="$t('user.username')" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <ren-select v-model="state.dataForm.gender" dict-type="gender" :placeholder="$t('user.gender')"></ren-select>
      </el-form-item>
      <el-form-item>
        <ren-dept-tree v-model="state.dataForm.deptId" :placeholder="$t('dept.title')" :query="true"></ren-dept-tree>
      </el-form-item>
      <el-form-item>
        <el-button @click="state.getDataList()">{{ $t("query") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button v-if="state.hasPermission('sys:user:save')" type="primary" @click="addOrUpdateHandle()">{{ $t("add") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button v-if="state.hasPermission('sys:user:delete')" type="danger" @click="state.deleteHandle()">{{ $t("deleteBatch") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button v-if="state.hasPermission('sys:user:export')" type="info" @click="state.exportHandle()">{{ $t("export") }}</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="state.dataListLoading" :data="state.dataList" border @selection-change="state.dataListSelectionChangeHandle" @sort-change="state.dataListSortChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="username" :label="$t('user.username')" sortable="custom" header-align="center" align="center"></el-table-column>
      <el-table-column prop="deptName" :label="$t('user.deptName')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="email" :label="$t('user.email')" header-align="center" align="center"></el-table-column>
      <el-table-column prop="mobile" :label="$t('user.mobile')" sortable="custom" header-align="center" align="center"></el-table-column>
      <el-table-column prop="gender" :label="$t('user.gender')" sortable="custom" header-align="center" align="center">
        <template v-slot="scope">
          {{ state.getDictLabel("gender", scope.row.gender) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" :label="$t('user.status')" sortable="custom" header-align="center" align="center">
        <template v-slot="scope">
          <el-tag v-if="scope.row.status === 0" size="small" type="danger">{{ $t("user.status0") }}</el-tag>
          <el-tag v-else size="small" type="success">{{ $t("user.status1") }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createDate" :label="$t('user.createDate')" sortable="custom" header-align="center" align="center" width="180"></el-table-column>
      <el-table-column :label="$t('handle')" fixed="right" header-align="center" align="center" width="150">
        <template v-slot="scope">
          <el-button v-if="state.hasPermission('sys:user:update')" type="primary" link @click="addOrUpdateHandle(scope.row.id)">{{ $t("update") }}</el-button>
          <el-button v-if="state.hasPermission('sys:user:delete')" type="primary" link @click="state.deleteHandle(scope.row.id)">{{ $t("delete") }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"> </el-pagination>
    <!-- 弹窗, 新增 / 修改 -->
    <add-or-update ref="addOrUpdateRef" @refreshDataList="state.getDataList"></add-or-update>
  </div>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { reactive, ref, toRefs } from "vue";
import AddOrUpdate from "./user-add-or-update.vue";

const view = reactive({
  getDataListURL: "/sys/user/page",
  getDataListIsPage: true,
  deleteURL: "/sys/user",
  deleteIsBatch: true,
  exportURL: "/sys/user/export",
  dataForm: {
    username: "",
    deptId: "",
    postId: "",
    gender: ""
  }
});

const state = reactive({ ...useView(view), ...toRefs(view) });

const addOrUpdateRef = ref();
const addOrUpdateHandle = (id?: number) => {
  addOrUpdateRef.value.init(id);
};
</script>
