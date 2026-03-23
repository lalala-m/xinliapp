<template>
  <div class="mod-sys__rjddailyworkrecord">
    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-button @click="state.getDataList()">{{ $t("query") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="info" @click="state.exportHandle()">{{ $t("export") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button v-if="state.hasPermission('sys:rjddailyworkrecord:save')" type="primary" @click="addOrUpdateHandle()">{{ $t("add") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button v-if="state.hasPermission('sys:rjddailyworkrecord:delete')" type="danger" @click="state.deleteHandle()">{{ $t("deleteBatch") }}</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="state.dataListLoading" :data="state.dataList" border @selection-change="state.dataListSelectionChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="id" label="主键ID" header-align="center" align="center"></el-table-column>
      <el-table-column prop="postId" label="岗位ID（关联sys_post.id）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="postName" label="岗位名称（冗余存储）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="deptId" label="所属部门ID（关联sys_dept.id）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="deptName" label="所属部门名称（冗余存储）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="workDate" label="填报日期" header-align="center" align="center"></el-table-column>
      <el-table-column prop="workItemType" label="工作事项分类（关联rjd_post_work_item.work_item_name）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="relatedTypeId" label="关联事项类型ID（关联rjd_related_object_type_dict.type_id）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="relatedTypeName" label="关联事项类型名称" header-align="center" align="center"></el-table-column>
      <el-table-column prop="relatedSourceTable" label="关联事项来源表" header-align="center" align="center"></el-table-column>
      <el-table-column prop="relatedObjectName" label="关联事项名称" header-align="center" align="center"></el-table-column>
      <el-table-column prop="relatedObject" label="填报关联对象" header-align="center" align="center"></el-table-column>
      <el-table-column prop="fillContent" label="填报内容" header-align="center" align="center"></el-table-column>
      <el-table-column prop="workingHours" label="总耗时（小时）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="allocatedHoursSum" label="关联对象分摊耗时总和（系统自动计算）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="allocatedCheckStatus" label="分摊校验状态：0=未校验 1=通过 2=失败" header-align="center" align="center"></el-table-column>
      <el-table-column prop="modifyCount" label="驳回后修改次数" header-align="center" align="center"></el-table-column>
      <el-table-column prop="latestRejectReason" label="最新驳回原因" header-align="center" align="center"></el-table-column>
      <el-table-column prop="commuteActualHours" label="通勤实际工时（冗余自rjd_employee_commute_record）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="systemAuditStatus" label="系统审核状态：0=待审核 1=通过 2=驳回" header-align="center" align="center"></el-table-column>
      <el-table-column prop="manualAuditStatus" label="人工审核状态：0=待审核 1=通过 2=驳回" header-align="center" align="center"></el-table-column>
      <el-table-column prop="latestAuditNode" label="当前审核节点：system=系统 manual=人工" header-align="center" align="center"></el-table-column>
      <el-table-column prop="fillerId" label="填报人ID（关联sys_user.id）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="fillerName" label="填报人姓名（冗余存储）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="fillDatetime" label="填报时间" header-align="center" align="center"></el-table-column>
      <el-table-column prop="tenantCode" label="租户编码" header-align="center" align="center"></el-table-column>
      <el-table-column prop="status" label="状态：0=停用 1=正常" header-align="center" align="center"></el-table-column>
      <el-table-column prop="creator" label="创建者" header-align="center" align="center"></el-table-column>
      <el-table-column prop="createDate" label="创建时间" header-align="center" align="center"></el-table-column>
      <el-table-column prop="updater" label="更新者" header-align="center" align="center"></el-table-column>
      <el-table-column prop="updateDate" label="更新时间" header-align="center" align="center"></el-table-column>
      <el-table-column prop="delFlag" label="删除标识：0=未删 1=已删" header-align="center" align="center"></el-table-column>
      <el-table-column :label="$t('handle')" fixed="right" header-align="center" align="center" width="150">
        <template v-slot="scope">
          <el-button v-if="state.hasPermission('sys:rjddailyworkrecord:update')" type="primary" link @click="addOrUpdateHandle(scope.row.id)">{{ $t("update") }}</el-button>
          <el-button v-if="state.hasPermission('sys:rjddailyworkrecord:delete')" type="primary" link @click="state.deleteHandle(scope.row.id)">{{ $t("delete") }}</el-button>
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
import AddOrUpdate from "./rjddailyworkrecord-add-or-update.vue";

const view = reactive({
  getDataListURL: "/sys/rjddailyworkrecord/page",
  getDataListIsPage: true,
  exportURL: "/sys/rjddailyworkrecord/export",
  deleteURL: "/sys/rjddailyworkrecord",
  deleteIsBatch: true,
  dataForm: {}
});

const state = reactive({ ...useView(view), ...toRefs(view) });

const addKey = ref(0);
const addOrUpdateRef = ref();
const addOrUpdateHandle = (id?: number) => {
  addKey.value++;
  nextTick(() => {
    addOrUpdateRef.value.init(id);
  });
};
</script>
