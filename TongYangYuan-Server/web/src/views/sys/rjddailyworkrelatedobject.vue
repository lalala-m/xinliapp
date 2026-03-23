<template>
  <div class="mod-sys__rjddailyworkrelatedobject">
    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-button @click="state.getDataList()">{{ $t("query") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="info" @click="state.exportHandle()">{{ $t("export") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button v-if="state.hasPermission('sys:rjddailyworkrelatedobject:save')" type="primary" @click="addOrUpdateHandle()">{{ $t("add") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button v-if="state.hasPermission('sys:rjddailyworkrelatedobject:delete')" type="danger" @click="state.deleteHandle()">{{ $t("deleteBatch") }}</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="state.dataListLoading" :data="state.dataList" border @selection-change="state.dataListSelectionChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="id" label="主键ID" header-align="center" align="center"></el-table-column>
      <el-table-column prop="dailyRecordId" label="关联日结单ID（rjd_daily_work_record.id）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="relatedTypeId" label="关联对象类型ID（rjd_related_object_type_dict.type_id）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="relatedObjectId" label="关联对象ID（产品/客户/订单/事务ID）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="relatedObjectName" label="关联对象名称（冗余存储）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="allocatedHours" label="分摊到该对象的耗时（小时）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="allocatedRatio" label="分摊比例（%）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="allocatedRemark" label="分摊备注" header-align="center" align="center"></el-table-column>
      <el-table-column prop="tenantCode" label="租户编码" header-align="center" align="center"></el-table-column>
      <el-table-column prop="creator" label="创建者（关联sys_user.id）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="createDate" label="创建时间" header-align="center" align="center"></el-table-column>
      <el-table-column prop="updater" label="更新者（关联sys_user.id）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="updateDate" label="更新时间" header-align="center" align="center"></el-table-column>
      <el-table-column prop="delFlag" label="删除标识：0=未删 1=已删" header-align="center" align="center"></el-table-column>
      <el-table-column :label="$t('handle')" fixed="right" header-align="center" align="center" width="150">
        <template v-slot="scope">
          <el-button v-if="state.hasPermission('sys:rjddailyworkrelatedobject:update')" type="primary" link @click="addOrUpdateHandle(scope.row.id)">{{ $t("update") }}</el-button>
          <el-button v-if="state.hasPermission('sys:rjddailyworkrelatedobject:delete')" type="primary" link @click="state.deleteHandle(scope.row.id)">{{ $t("delete") }}</el-button>
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
import AddOrUpdate from "./rjddailyworkrelatedobject-add-or-update.vue";

const view = reactive({
  getDataListURL: "/sys/rjddailyworkrelatedobject/page",
  getDataListIsPage: true,
  exportURL: "/sys/rjddailyworkrelatedobject/export",
  deleteURL: "/sys/rjddailyworkrelatedobject",
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
