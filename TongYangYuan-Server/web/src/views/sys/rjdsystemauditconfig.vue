<template>
  <div class="mod-sys__rjdsystemauditconfig">
    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-button @click="state.getDataList()">{{ $t("query") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="info" @click="state.exportHandle()">{{ $t("export") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button v-if="state.hasPermission('sys:rjdsystemauditconfig:save')" type="primary" @click="addOrUpdateHandle()">{{ $t("add") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button v-if="state.hasPermission('sys:rjdsystemauditconfig:delete')" type="danger" @click="state.deleteHandle()">{{ $t("deleteBatch") }}</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="state.dataListLoading" :data="state.dataList" border @selection-change="state.dataListSelectionChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="configId" label="配置ID" header-align="center" align="center"></el-table-column>
      <el-table-column prop="configName" label="配置名称（如：生产管理人员日结单审核规则）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="postId" label="适用岗位ID（sys_post.id，NULL=全岗位）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="postName" label="适用岗位名称" header-align="center" align="center"></el-table-column>
      <el-table-column prop="deptId" label="适用部门ID（sys_dept.id，NULL=全部门）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="deptName" label="适用部门名称" header-align="center" align="center"></el-table-column>
      <el-table-column prop="requiredFields" label="必填字段（逗号分隔）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="workingHoursMin" label="填报耗时最小值（小时）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="workingHoursMax" label="填报耗时最大值（小时）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="relatedObjectCheck" label="关联对象校验：0=不校验 1=校验" header-align="center" align="center"></el-table-column>
      <el-table-column prop="actualHoursMatch" label="工时匹配：0=不校验 1=填报≤通勤" header-align="center" align="center"></el-table-column>
      <el-table-column prop="autoPass" label="自动通过：0=否 1=是" header-align="center" align="center"></el-table-column>
      <el-table-column prop="fillContentMinLength" label="填报内容最小字数（0=不限制）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="allocatedCheck" label="分摊校验：0=不校验 1=总和=总耗时" header-align="center" align="center"></el-table-column>
      <el-table-column prop="modifyLimit" label="驳回后最大修改次数（0=不限制）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="relatedObjectMinCount" label="关联对象最少数量（0=不限制）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="tenantCode" label="租户编码" header-align="center" align="center"></el-table-column>
      <el-table-column prop="status" label="状态：0=停用 1=启用" header-align="center" align="center"></el-table-column>
      <el-table-column prop="creator" label="创建者（关联sys_user.id）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="createDate" label="创建时间" header-align="center" align="center"></el-table-column>
      <el-table-column prop="updater" label="更新者（关联sys_user.id）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="updateDate" label="更新时间" header-align="center" align="center"></el-table-column>
      <el-table-column prop="delFlag" label="删除标识：0=未删 1=已删" header-align="center" align="center"></el-table-column>
      <el-table-column :label="$t('handle')" fixed="right" header-align="center" align="center" width="150">
        <template v-slot="scope">
          <el-button v-if="state.hasPermission('sys:rjdsystemauditconfig:update')" type="primary" link @click="addOrUpdateHandle(scope.row.id)">{{ $t("update") }}</el-button>
          <el-button v-if="state.hasPermission('sys:rjdsystemauditconfig:delete')" type="primary" link @click="state.deleteHandle(scope.row.id)">{{ $t("delete") }}</el-button>
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
import AddOrUpdate from "./rjdsystemauditconfig-add-or-update.vue";

const view = reactive({
  getDataListURL: "/sys/rjdsystemauditconfig/page",
  getDataListIsPage: true,
  exportURL: "/sys/rjdsystemauditconfig/export",
  deleteURL: "/sys/rjdsystemauditconfig",
  deleteIsBatch: true,
  dataForm: {}
});

const state = reactive({ ...useView(view), ...toRefs(view) });

const addKey = ref(0);
const addOrUpdateRef = ref();
const addOrUpdateHandle = (configId?: number) => {
  addKey.value++;
  nextTick(() => {
    addOrUpdateRef.value.init(configId);
  });
};
</script>
