<template>
  <el-dialog v-model="visible" :title="!dataForm.id ? $t('add') : $t('update')" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" label-width="120px">
      <el-form-item label="主键ID" prop="id">
        <el-input v-model="dataForm.id" placeholder="主键ID"></el-input>
      </el-form-item>
      <el-form-item label="员工ID（关联sys_user.id）" prop="empId">
        <el-input v-model="dataForm.empId" placeholder="员工ID（关联sys_user.id）"></el-input>
      </el-form-item>
      <el-form-item label="员工姓名（冗余存储）" prop="empName">
        <el-input v-model="dataForm.empName" placeholder="员工姓名（冗余存储）"></el-input>
      </el-form-item>
      <el-form-item label="所属部门ID（关联sys_dept.id）" prop="deptId">
        <el-input v-model="dataForm.deptId" placeholder="所属部门ID（关联sys_dept.id）"></el-input>
      </el-form-item>
      <el-form-item label="所属部门名称（冗余存储）" prop="deptName">
        <el-input v-model="dataForm.deptName" placeholder="所属部门名称（冗余存储）"></el-input>
      </el-form-item>
      <el-form-item label="岗位ID（关联sys_post.id）" prop="postId">
        <el-input v-model="dataForm.postId" placeholder="岗位ID（关联sys_post.id）"></el-input>
      </el-form-item>
      <el-form-item label="岗位名称（筛选管理人员）" prop="postName">
        <el-input v-model="dataForm.postName" placeholder="岗位名称（筛选管理人员）"></el-input>
      </el-form-item>
      <el-form-item label="统计日期（与日结单一致）" prop="workDate">
        <el-input v-model="dataForm.workDate" placeholder="统计日期（与日结单一致）"></el-input>
      </el-form-item>
      <el-form-item label="上班打卡时间（考勤同步）" prop="checkInTime">
        <el-input v-model="dataForm.checkInTime" placeholder="上班打卡时间（考勤同步）"></el-input>
      </el-form-item>
      <el-form-item label="下班打卡时间（考勤同步）" prop="checkOutTime">
        <el-input v-model="dataForm.checkOutTime" placeholder="下班打卡时间（考勤同步）"></el-input>
      </el-form-item>
      <el-form-item label="出勤状态：1=正常 2=迟到 3=早退 4=旷工 5=事假 6=病假 7=其他" prop="attendanceStatus">
        <el-input v-model="dataForm.attendanceStatus" placeholder="出勤状态：1=正常 2=迟到 3=早退 4=旷工 5=事假 6=病假 7=其他"></el-input>
      </el-form-item>
      <el-form-item label="午休扣除时长（小时）" prop="lunchBreakHours">
        <el-input v-model="dataForm.lunchBreakHours" placeholder="午休扣除时长（小时）"></el-input>
      </el-form-item>
      <el-form-item label="标准工时（小时）" prop="scheduledWorkingHours">
        <el-input v-model="dataForm.scheduledWorkingHours" placeholder="标准工时（小时）"></el-input>
      </el-form-item>
      <el-form-item label="实际工时（自动计算）" prop="actualWorkingHours">
        <el-input v-model="dataForm.actualWorkingHours" placeholder="实际工时（自动计算）"></el-input>
      </el-form-item>
      <el-form-item label="迟到分钟数（自动计算）" prop="lateMinutes">
        <el-input v-model="dataForm.lateMinutes" placeholder="迟到分钟数（自动计算）"></el-input>
      </el-form-item>
      <el-form-item label="早退分钟数（自动计算）" prop="earlyLeaveMinutes">
        <el-input v-model="dataForm.earlyLeaveMinutes" placeholder="早退分钟数（自动计算）"></el-input>
      </el-form-item>
      <el-form-item label="备注" prop="commuteRemark">
        <el-input v-model="dataForm.commuteRemark" placeholder="备注"></el-input>
      </el-form-item>
      <el-form-item label="租户编码" prop="tenantCode">
        <el-input v-model="dataForm.tenantCode" placeholder="租户编码"></el-input>
      </el-form-item>
      <el-form-item label="考勤同步状态：0=未同步 1=成功 2=失败" prop="syncStatus">
        <el-input v-model="dataForm.syncStatus" placeholder="考勤同步状态：0=未同步 1=成功 2=失败"></el-input>
      </el-form-item>
      <el-form-item label="考勤同步时间" prop="syncTime">
        <el-input v-model="dataForm.syncTime" placeholder="考勤同步时间"></el-input>
      </el-form-item>
      <el-form-item label="创建者（关联sys_user.id）" prop="creator">
        <el-input v-model="dataForm.creator" placeholder="创建者（关联sys_user.id）"></el-input>
      </el-form-item>
      <el-form-item label="创建时间" prop="createDate">
        <el-input v-model="dataForm.createDate" placeholder="创建时间"></el-input>
      </el-form-item>
      <el-form-item label="更新者（关联sys_user.id）" prop="updater">
        <el-input v-model="dataForm.updater" placeholder="更新者（关联sys_user.id）"></el-input>
      </el-form-item>
      <el-form-item label="更新时间" prop="updateDate">
        <el-input v-model="dataForm.updateDate" placeholder="更新时间"></el-input>
      </el-form-item>
      <el-form-item label="删除标识：0=未删 1=已删" prop="delFlag">
        <el-input v-model="dataForm.delFlag" placeholder="删除标识：0=未删 1=已删"></el-input>
      </el-form-item>
    </el-form>
    <template v-slot:footer>
      <el-button @click="visible = false">{{ $t("cancel") }}</el-button>
      <el-button type="primary" @click="dataFormSubmitHandle()">{{ $t("confirm") }}</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { reactive, ref } from "vue";
import baseService from "@/service/baseService";
import { useI18n } from "vue-i18n";
import { ElMessage } from "element-plus";
const { t } = useI18n();
const emit = defineEmits(["refreshDataList"]);

const visible = ref(false);
const dataFormRef = ref();

const dataForm = reactive({
  id: "",
  empId: "",
  empName: "",
  deptId: "",
  deptName: "",
  postId: "",
  postName: "",
  workDate: "",
  checkInTime: "",
  checkOutTime: "",
  attendanceStatus: "",
  lunchBreakHours: "",
  scheduledWorkingHours: "",
  actualWorkingHours: "",
  lateMinutes: "",
  earlyLeaveMinutes: "",
  commuteRemark: "",
  tenantCode: "",
  syncStatus: "",
  syncTime: "",
  creator: "",
  createDate: "",
  updater: "",
  updateDate: "",
  delFlag: ""
});

const rules = ref({});

const init = (id?: number) => {
  visible.value = true;
  dataForm.id = "";

  // 重置表单数据
  if (dataFormRef.value) {
    dataFormRef.value.resetFields();
  }

  if (id) {
    getInfo(id);
  }
};

// 获取信息
const getInfo = (id: number) => {
  baseService.get("/sys/rjdemployeecommuterecord/" + id).then((res) => {
    Object.assign(dataForm, res.data);
  });
};

// 表单提交
const dataFormSubmitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }
    (!dataForm.id ? baseService.post : baseService.put)("/sys/rjdemployeecommuterecord", dataForm).then((res) => {
      ElMessage.success({
        message: t("prompt.success"),
        duration: 500,
        onClose: () => {
          visible.value = false;
          emit("refreshDataList");
        }
      });
    });
  });
};

defineExpose({
  init
});
</script>
