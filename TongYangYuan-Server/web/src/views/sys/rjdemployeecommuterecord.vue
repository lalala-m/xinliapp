<template>
  <div class="mod-sys__rjdemployeecommuterecord">
    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-button @click="state.getDataList()">{{ $t("query") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="info" @click="state.exportHandle()">{{ $t("export") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button v-if="state.hasPermission('sys:rjdemployeecommuterecord:save')" type="primary" @click="addOrUpdateHandle()">{{ $t("add") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button v-if="state.hasPermission('sys:rjdemployeecommuterecord:delete')" type="danger" @click="state.deleteHandle()">{{ $t("deleteBatch") }}</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="state.dataListLoading" :data="state.dataList" border @selection-change="state.dataListSelectionChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="id" label="主键ID" header-align="center" align="center"></el-table-column>
      <el-table-column prop="empId" label="员工ID（关联sys_user.id）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="empName" label="员工姓名（冗余存储）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="deptId" label="所属部门ID（关联sys_dept.id）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="deptName" label="所属部门名称（冗余存储）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="postId" label="岗位ID（关联sys_post.id）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="postName" label="岗位名称（筛选管理人员）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="workDate" label="统计日期（与日结单一致）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="checkInTime" label="上班打卡时间（考勤同步）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="checkOutTime" label="下班打卡时间（考勤同步）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="attendanceStatus" label="出勤状态：1=正常 2=迟到 3=早退 4=旷工 5=事假 6=病假 7=其他" header-align="center" align="center"></el-table-column>
      <el-table-column prop="lunchBreakHours" label="午休扣除时长（小时）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="scheduledWorkingHours" label="标准工时（小时）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="actualWorkingHours" label="实际工时（自动计算）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="lateMinutes" label="迟到分钟数（自动计算）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="earlyLeaveMinutes" label="早退分钟数（自动计算）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="commuteRemark" label="备注" header-align="center" align="center"></el-table-column>
      <el-table-column prop="tenantCode" label="租户编码" header-align="center" align="center"></el-table-column>
      <el-table-column prop="syncStatus" label="考勤同步状态：0=未同步 1=成功 2=失败" header-align="center" align="center"></el-table-column>
      <el-table-column prop="syncTime" label="考勤同步时间" header-align="center" align="center"></el-table-column>
      <el-table-column prop="creator" label="创建者（关联sys_user.id）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="createDate" label="创建时间" header-align="center" align="center"></el-table-column>
      <el-table-column prop="updater" label="更新者（关联sys_user.id）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="updateDate" label="更新时间" header-align="center" align="center"></el-table-column>
      <el-table-column prop="delFlag" label="删除标识：0=未删 1=已删" header-align="center" align="center"></el-table-column>
      <el-table-column :label="$t('handle')" fixed="right" header-align="center" align="center" width="150">
        <template v-slot="scope">
          <el-button v-if="state.hasPermission('sys:rjdemployeecommuterecord:update')" type="primary" link @click="addOrUpdateHandle(scope.row.id)">{{ $t("update") }}</el-button>
          <el-button v-if="state.hasPermission('sys:rjdemployeecommuterecord:delete')" type="primary" link @click="state.deleteHandle(scope.row.id)">{{ $t("delete") }}</el-button>
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
import AddOrUpdate from "./rjdemployeecommuterecord-add-or-update.vue";

const view = reactive({
  getDataListURL: "/sys/rjdemployeecommuterecord/page",
  getDataListIsPage: true,
  exportURL: "/sys/rjdemployeecommuterecord/export",
  deleteURL: "/sys/rjdemployeecommuterecord",
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
