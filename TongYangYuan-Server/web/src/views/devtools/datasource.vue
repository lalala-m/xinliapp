<template>
  <div class="mod-datasource">
    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-input v-model="state.dataForm.connName" placeholder="连接名" clearable></el-input>
      </el-form-item>
      <el-form-item prop="dbType">
        <el-select v-model="state.dataForm.dbType" clearable placeholder="数据库类型" class="w-percent-100">
          <el-option value="MySQL" label="MySQL"></el-option>
          <el-option value="Oracle" label="Oracle"></el-option>
          <el-option value="SQLServer" label="SQLServer"></el-option>
          <el-option value="PostgreSQL" label="PostgreSQL"></el-option>
          <el-option value="DM" label="DM"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="state.getDataList()">查询</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="addOrUpdateHandle()">新增</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="danger" @click="state.deleteHandle()">删除</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="state.dataListLoading" :data="state.dataList" show-overflow-tooltip border @selection-change="state.dataListSelectionChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="connName" label="连接名" header-align="center" align="center"></el-table-column>
      <el-table-column prop="dbType" label="数据库类型" header-align="center" align="center"></el-table-column>
      <el-table-column prop="connUrl" label="数据库URL" c header-align="center" align="center"></el-table-column>
      <el-table-column prop="username" label="用户名" header-align="center" align="center"></el-table-column>
      <el-table-column prop="password" label="密码" header-align="center" align="center"></el-table-column>
      <el-table-column prop="status" label="状态" sortable="custom" header-align="center" align="center">
        <template v-slot="scope">
          <el-tag v-if="scope.row.status === 0" size="small" type="success">启用</el-tag>
          <el-tag v-else size="small" type="danger">禁用</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" header-align="center" align="center" width="180">
        <template v-slot="scope">
          <el-button type="primary" link @click="datasourceHandle(scope.row.id)">连接测试</el-button>
          <el-button type="primary" link @click="addOrUpdateHandle(scope.row.id)">编辑</el-button>
          <el-button type="primary" link @click="state.deleteHandle(scope.row.id)">删除</el-button>
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
import baseService from "@/service/baseService";
import { nextTick, reactive, ref, toRefs } from "vue";
import AddOrUpdate from "./datasource-add-or-update.vue";
import { ElMessage } from "element-plus";

const view = reactive({
  getDataListURL: "/devtools/datasource/page",
  getDataListIsPage: true,
  deleteURL: "/devtools/datasource",
  deleteIsBatch: true,
  dataForm: {
    connName: "",
    dbType: ""
  }
});

const state = reactive({ ...useView(view), ...toRefs(view) });

const datasourceHandle = (id: string) => {
  baseService.get("/devtools/datasource/test/" + id).then((res) => {
    if (res.code !== 0) {
      return ElMessage.error(res.msg);
    } else {
      return ElMessage.success(res.data);
    }
  });
};

const addOrUpdateRef = ref();
const addOrUpdateHandle = (id?: number) => {
  nextTick(() => {
    addOrUpdateRef.value.init(id);
  });
};
</script>
