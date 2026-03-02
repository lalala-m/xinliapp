<template>
  <div>
    <el-button type="primary" @click="completeTask()">{{ $t("process.completeTask") }}</el-button>
    <el-button type="warning" @click="rejectTask()">{{ $t("process.rejectTask") }}</el-button>
    <el-button type="info" @click="delegateTask()">{{ $t("process.entrustTask") }}</el-button>
    <ren-task-delegate v-if="renTaskDelegateVisible" ref="renTaskDelegateRef"></ren-task-delegate>
    <ren-task-handle v-if="renTaskHandleVisible" ref="renTaskHandleRef"></ren-task-handle>
  </div>
</template>

<script lang="ts" setup>
import { nextTick, reactive, ref } from "vue";
import RenTaskDelegate from "./ren-task-delegate.vue";
import RenTaskHandle from "./ren-task-handle.vue";
import { useRoute } from "vue-router";
const route = useRoute();

const renTaskDelegateVisible = ref(false);
const renTaskHandleVisible = ref(false);
const renTaskDelegateRef = ref();
const renTaskHandleRef = ref();
const dataForm = reactive({
  taskId: route.query.taskId
});

const completeTask = () => {
  renTaskHandleVisible.value = true;
  nextTick(() => {
    renTaskHandleRef.value.dataForm.taskId = dataForm.taskId;
    renTaskHandleRef.value.init("complete");
  });
};

const rejectTask = () => {
  renTaskHandleVisible.value = true;
  nextTick(() => {
    renTaskHandleRef.value.dataForm.taskId = dataForm.taskId;
    renTaskHandleRef.value.init("reject");
  });
};

const delegateTask = () => {
  renTaskDelegateVisible.value = true;
  nextTick(() => {
    renTaskDelegateRef.value.dataForm.taskId = dataForm.taskId;
    renTaskDelegateRef.value.init();
  });
};
</script>
