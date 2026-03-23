<template>
  <div class="mod-sys__rjddailyworkauditrecord">
    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-button @click="state.getDataList()">{{ $t("query") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="info" @click="state.exportHandle()">{{ $t("export") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button v-if="state.hasPermission('sys:rjddailyworkauditrecord:save')" type="primary" @click="addOrUpdateHandle()">{{ $t("add") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button v-if="state.hasPermission('sys:rjddailyworkauditrecord:delete')" type="danger" @click="state.deleteHandle()">{{ $t("deleteBatch") }}</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="state.dataListLoading" :data="state.dataList" border @selection-change="state.dataListSelectionChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="id" label="审核记录ID" header-align="center" align="center"></el-table-column>
      <el-table-column prop="dailyRecordId" label="关联日结单ID" header-align="center" align="center"></el-table-column>
      <el-table-column prop="auditNode" label="审核节点：system=系统 manual=人工" header-align="center" align="center"></el-table-column>
      <el-table-column prop="auditOrder" label="审核顺序：1=系统 2=人工" header-align="center" align="center"></el-table-column>
      <el-table-column prop="systemConfigId" label="关联系统审核配置ID（仅system节点）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="auditRuleDetail" label="系统审核规则详情" header-align="center" align="center"></el-table-column>
      <el-table-column prop="allocatedAuditDetail" label="分摊校验详情" header-align="center" align="center"></el-table-column>
      <el-table-column prop="auditType" label="审核类型：1=系统 2=人工" header-align="center" align="center"></el-table-column>
      <el-table-column prop="auditStatus" label="审核状态：0=待审核 1=通过 2=驳回" header-align="center" align="center"></el-table-column>
      <el-table-column prop="auditorId" label="审核人ID（system=0，manual=sys_user.id）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="auditorName" label="审核人姓名（system=系统）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="auditTime" label="审核完成时间" header-align="center" align="center"></el-table-column>
      <el-table-column prop="auditRemark" label="审核备注" header-align="center" align="center"></el-table-column>
      <el-table-column prop="tenantCode" label="租户编码" header-align="center" align="center"></el-table-column>
      <el-table-column prop="creator" label="创建者（system=0）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="createDate" label="创建时间" header-align="center" align="center"></el-table-column>
      <el-table-column prop="delFlag" label="删除标识：0=未删 1=已删" header-align="center" align="center"></el-table-column>
      <el-table-column :label="$t('handle')" fixed="right" header-align="center" align="center" width="150">
        <template v-slot="scope">
          <el-button v-if="state.hasPermission('sys:rjddailyworkauditrecord:update')" type="primary" link @click="addOrUpdateHandle(scope.row.id)">{{ $t("update") }}</el-button>
          <el-button v-if="state.hasPermission('sys:rjddailyworkauditrecord:delete')" type="primary" link @click="state.deleteHandle(scope.row.id)">{{ $t("delete") }}</el-button>
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
import AddOrUpdate from "./rjddailyworkauditrecord-add-or-update.vue";

const view = reactive({
  getDataListURL: "/sys/rjddailyworkauditrecord/page",
  getDataListIsPage: true,
  exportURL: "/sys/rjddailyworkauditrecord/export",
  deleteURL: "/sys/rjddailyworkauditrecord",
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
