<template>
  <div class="mod-generator">
    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-input v-model="state.dataForm.tableName" placeholder="表名"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="state.getDataList()">查询</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="importHandle()">导入数据库表</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="danger" @click="state.deleteHandle()">删除</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="state.dataListLoading" :data="state.dataList" border @selection-change="state.dataListSelectionChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="tableName" label="表名" header-align="center" align="center"></el-table-column>
      <el-table-column prop="tableComment" label="表说明" header-align="center" align="center"></el-table-column>
      <el-table-column prop="className" label="类名" header-align="center" align="center"></el-table-column>
      <el-table-column prop="createDate" label="创建时间" header-align="center" align="center"></el-table-column>
      <el-table-column label="操作" fixed="right" header-align="center" align="center" width="260">
        <template v-slot="scope">
          <el-button type="primary" link @click="editTableHandle(scope.row.id)">编辑</el-button>
          <el-button type="primary" link @click="generatorCodeHandle(scope.row.id)">生成代码</el-button>
          <el-button type="primary" link @click="generatorMenuHandle(scope.row)">创建菜单</el-button>
          <el-button type="primary" link @click="state.deleteHandle(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"> </el-pagination>
    <import ref="importRef" @refreshDataList="state.getDataList"></import>
    <edit-table ref="editTableRef" @refreshDataList="state.getDataList"></edit-table>
    <generator-code ref="generatorCodeRef" @refreshDataList="state.getDataList"></generator-code>
    <generator-menu ref="generatorMenuRef" @refreshDataList="state.getDataList"></generator-menu>
  </div>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { reactive, ref, toRefs } from "vue";
import Import from "./generator-import.vue";
import EditTable from "./generator-edittable.vue";
import GeneratorCode from "./generator-code.vue";
import GeneratorMenu from "./generator-menu.vue";
import { IObject } from "@/types/interface";

const view = reactive({
  getDataListURL: "/devtools/table/page",
  getDataListIsPage: true,
  deleteURL: "/devtools/table",
  deleteIsBatch: true,
  importVisible: false,
  editTableVisible: false,
  generatorCodeVisible: false,
  generatorMenuVisible: false,
  dataForm: {
    tableName: ""
  }
});

const state = reactive({ ...useView(view), ...toRefs(view) });

const importRef = ref();
const importHandle = () => {
  importRef.value.init();
};

const editTableRef = ref();
const editTableHandle = (id: string) => {
  editTableRef.value.init(id);
};

const generatorCodeRef = ref();
const generatorCodeHandle = (id: string) => {
  generatorCodeRef.value.init(id);
};

const generatorMenuRef = ref();
const generatorMenuHandle = (row: IObject) => {
  generatorMenuRef.value.dataForm.backendUrl = row.backendUrl;
  generatorMenuRef.value.dataForm.className = row.className;
  generatorMenuRef.value.init();
};
</script>
