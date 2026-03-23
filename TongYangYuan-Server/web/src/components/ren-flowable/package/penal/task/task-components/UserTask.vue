<template>
  <div>
    <el-radio-group v-model="userTaskForm.type" @change="updateDataType">
      <el-radio label="candidateUsers">用户</el-radio>
      <el-radio label="candidateRole">角色</el-radio>
      <el-radio label="candidatePost">岗位</el-radio>
      <el-radio label="candidateDept">部门</el-radio>
      <el-radio label="startUserId">发起人</el-radio>
      <el-radio label="assignee">指定人</el-radio>
      <el-radio label="deptLeader">部门领导</el-radio>
      <el-radio label="variable">变量</el-radio>
      <!--      <el-radio label="superDeptLeader">上级部门领导</el-radio>-->
    </el-radio-group>
    <el-form-item label="选择用户" v-if="userTaskForm.type === 'assignee'">
      <el-input v-model="userTaskForm.assigneeName">
        <template #append>
          <el-button :icon="Search()" @click="userSelect" type="danger" />
        </template>
      </el-input>
    </el-form-item>
    <el-form-item label="选择用户" v-if="userTaskForm.type === 'candidateUsers'">
      <el-input v-model="userTaskForm.candidateUsersName">
        <template #append>
          <el-button :icon="Search()" @click="userGroupSelect" type="danger" />
        </template>
      </el-input>
    </el-form-item>
    <el-form-item label="选择角色" v-if="userTaskForm.type === 'candidateRole'">
      <el-input v-model="userTaskForm.candidateGroupsName">
        <template #append>
          <el-button :icon="Search()" @click="roleGroupSelect" type="danger" />
        </template>
      </el-input>
    </el-form-item>
    <el-form-item label="选择岗位" v-if="userTaskForm.type === 'candidatePost'">
      <el-input v-model="userTaskForm.candidateGroupsName">
        <template #append>
          <el-button :icon="Search()" @click="postGroupSelect" type="danger" />
        </template>
      </el-input>
    </el-form-item>
    <el-form-item label="选择部门" v-if="userTaskForm.type === 'candidateDept'">
      <el-input v-model="userTaskForm.candidateGroupsName">
        <template #append>
          <el-button :icon="Search()" @click="deptGroupSelect" type="danger" />
        </template>
      </el-input>
    </el-form-item>
    <el-form-item label="变量" v-if="userTaskForm.type === 'variable'">
      <el-input v-model="userTaskForm.variable" @blur="updateVariable"> </el-input>
    </el-form-item>
    <select-user v-if="visibleSelect" ref="selectUserRef"></select-user>
    <select-role v-if="visibleRole" ref="selectRoleRef"></select-role>
    <select-post v-if="visiblePost" ref="selectPostRef"></select-post>
    <select-dept v-if="visibleDept" ref="selectDeptRef"></select-dept>

    <el-form-item label="多人会签" v-if="userTaskForm.type === 'candidateUsers' || userTaskForm.type === 'candidatePost' || userTaskForm.type === 'candidateRole' || userTaskForm.type === 'candidateDept'">
      <el-select v-model="loopCharacteristics" @change="changeMultiInstance">
        <el-option label="非会签" value="" />
        <el-option label="并行会签" value="ParallelMultiInstance" />
        <el-option label="串行会签" value="SequentialMultiInstance" />
      </el-select>
    </el-form-item>
    <el-form-item label="完成条件" v-if="loopCharacteristics">
      <el-slider
        v-model="multiCompleted"
        @change="updateLoopCondition"
        :min="1"
        :max="100"
        show-input
        :format-tooltip="
          (val) => {
            return val + '%';
          }
        "
      />
    </el-form-item>
  </div>
</template>

<script>
import { Search } from "@element-plus/icons-vue";
import SelectUser from "@/components/ren-process-running/src/select-user.vue";
import SelectRole from "@/components/ren-process-running/src/select-role.vue";
import SelectPost from "@/components/ren-process-running/src/select-post.vue";
import SelectDept from "@/components/ren-process-running/src/select-dept.vue";
import { nextTick } from "vue";
import lodash from "lodash";
export default {
  name: "UserTask",
  props: {
    id: String,
    type: String,
    businessObject: Object
  },
  components: {
    SelectUser,
    SelectRole,
    SelectPost,
    SelectDept
  },
  data() {
    return {
      visibleSelect: false,
      visibleRole: false,
      visiblePost: false,
      visibleDept: false,
      userQuery: {},
      loopCharacteristics: "",
      multiInstance: "",
      multiCompleted: 100,
      defaultTaskForm: {
        assignee: "",
        candidateUsers: [],
        candidateGroups: [],
        dueDate: "",
        followUpDate: "",
        priority: ""
      },
      userTaskForm: {
        type: "assignee",
        variable: ""
      }
    };
  },
  watch: {
    id: {
      immediate: true,
      handler() {
        this.bpmnElement = window.bpmnInstances.bpmnElement;
        this.$nextTick(() => this.resetTaskForm());
      }
    },
    businessObject: {
      immediate: true,
      handler(val) {
        this.bpmnElement = window.bpmnInstances.bpmnElement;
        this.getElementLoop(val);
      }
    }
  },
  methods: {
    getElementLoop(businessObject) {
      if (!businessObject.loopCharacteristics) {
        return;
      }

      const completionCondition = lodash.get(businessObject.loopCharacteristics, "completionCondition.body", "");
      if (completionCondition) {
        const rateMatch = completionCondition.match(/\d+\.\d+/g);
        if (rateMatch) {
          this.multiCompleted = lodash.round(lodash.multiply(100, rateMatch[0]));
        } else {
          this.multiCompleted = 100;
        }
      }

      if (businessObject.loopCharacteristics.isSequential) {
        this.loopCharacteristics = "SequentialMultiInstance";
      } else {
        this.loopCharacteristics = "ParallelMultiInstance";
      }

      this.multiLoopInstance = window.bpmnInstances.bpmnElement.businessObject.loopCharacteristics;
    },
    Search() {
      return Search;
    },
    changeMultiInstance() {
      if (!this.loopCharacteristics) {
        window.bpmnInstances.modeling.updateProperties(this.bpmnElement, { loopCharacteristics: null });
        return;
      }

      const completionCondition = window.bpmnInstances.moddle.create("bpmn:FormalExpression", { body: "${nrOfCompletedInstances/nrOfInstances >= 1 }" });

      if (this.loopCharacteristics === "ParallelMultiInstance") {
        this.multiLoopInstance = window.bpmnInstances.moddle.create("bpmn:MultiInstanceLoopCharacteristics", {
          completionCondition: completionCondition,
          collection: "${multiInstanceHandler.getList(execution)}",
          elementVariable: "assignee"
        });
      } else {
        this.multiLoopInstance = window.bpmnInstances.moddle.create("bpmn:MultiInstanceLoopCharacteristics", {
          isSequential: true,
          completionCondition: completionCondition,
          collection: "${multiInstanceHandler.getList(execution)}",
          elementVariable: "assignee"
        });
      }

      window.bpmnInstances.modeling.updateProperties(this.bpmnElement, {
        loopCharacteristics: this.multiLoopInstance
      });

      const taskAttr = Object.create(null);
      taskAttr["assignee"] = "${assignee}";
      window.bpmnInstances.modeling.updateProperties(this.bpmnElement, taskAttr);
    },
    updateLoopCondition() {
      const completionCondition = window.bpmnInstances.moddle.create("bpmn:FormalExpression", { body: "${nrOfCompletedInstances/nrOfInstances >= " + lodash.multiply(this.multiCompleted, 0.01) + " }" });
      window.bpmnInstances.modeling.updateModdleProperties(this.bpmnElement, this.multiLoopInstance, {
        completionCondition
      });
    },
    userSelect() {
      this.visibleSelect = true;
      nextTick(() => {
        this.$refs.selectUserRef.init((userInfo) => {
          this.userTaskForm.assigneeName = userInfo.realName;
          this.userTaskForm.assignee = userInfo.id;
          this.updateAssignee("assignee");
        }, false);
      });
    },
    userGroupSelect() {
      this.visibleSelect = true;
      nextTick(() => {
        this.$refs.selectUserRef.init((users) => {
          const candidateUsersName = [];
          this.userTaskForm.candidateUsers = [];
          for (const user of users) {
            candidateUsersName.push(user.realName);
            this.userTaskForm.candidateUsers.push(user.id);
          }
          this.userTaskForm.candidateUsersName = candidateUsersName.join(",");
          this.updateCandidateUsers("candidateUsers");
        }, true);
      });
    },
    roleGroupSelect() {
      this.visibleRole = true;
      nextTick(() => {
        this.$refs.selectRoleRef.init((roles) => {
          const candidateGroupsName = [];
          this.userTaskForm.candidateGroups = [];
          for (const role of roles) {
            candidateGroupsName.push(role.name);
            this.userTaskForm.candidateGroups.push(role.id);
          }
          this.userTaskForm.candidateGroupsName = candidateGroupsName.join(",");
          this.updateCandidateRole("candidateGroups");
        });
      });
    },
    postGroupSelect() {
      this.visiblePost = true;
      nextTick(() => {
        this.$refs.selectPostRef.init((posts) => {
          const candidateGroupsName = [];
          this.userTaskForm.candidateGroups = [];
          for (const post of posts) {
            candidateGroupsName.push(post.postName);
            this.userTaskForm.candidateGroups.push(post.id);
          }
          this.userTaskForm.candidateGroupsName = candidateGroupsName.join(",");
          this.updateCandidateRole("candidateGroups");
        });
      });
    },
    deptGroupSelect() {
      this.visibleDept = true;
      nextTick(() => {
        this.$refs.selectDeptRef.init((depts) => {
          const candidateGroupsName = [];
          this.userTaskForm.candidateGroups = [];
          for (const dept of depts) {
            candidateGroupsName.push(dept.name);
            this.userTaskForm.candidateGroups.push(dept.id);
          }
          this.userTaskForm.candidateGroupsName = candidateGroupsName.join(",");
          this.updateCandidateRole("candidateGroups");
        });
      });
    },
    cancelHandle() {
      console.log(22);
    },
    submitHandle() {
      console.log(22);
    },
    async resetTaskForm() {
      const assignee = this.bpmnElement?.businessObject["assignee"];
      const candidateUsers = this.bpmnElement?.businessObject["candidateUsers"];
      const candidateGroups = this.bpmnElement?.businessObject["candidateGroups"];

      const dataType = this.bpmnElement?.businessObject.$attrs["flowable:dataType"];
      const text = this.bpmnElement?.businessObject.$attrs["flowable:text"];

      this.userTaskForm["assignee"] = "";
      this.userTaskForm["candidateUsers"] = [];
      this.userTaskForm["candidateGroups"] = [];

      this.userTaskForm.type = dataType;

      if (dataType === "assignee") {
        this.userTaskForm.assignee = assignee;
        this.userTaskForm.assigneeName = text;
        return;
      }

      if (dataType === "variable") {
        this.userTaskForm.variable = assignee;
        return;
      }

      if (dataType === "candidateUsers") {
        this.userTaskForm.candidateUsers = candidateUsers?.split(",") || null;
        this.userTaskForm.candidateUsersName = text;

        return;
      }

      if (dataType === "candidateRole" || dataType === "candidatePost" || dataType === "candidateDept") {
        this.userTaskForm.candidateGroups = candidateGroups?.split(",") || null;
        this.userTaskForm.candidateGroupsName = text;
      }
    },
    updateAssignee(key) {
      const taskAttr = Object.create(null);
      taskAttr["candidateUsers"] = null;
      taskAttr["candidateGroups"] = null;
      taskAttr[key] = this.userTaskForm[key] || null;
      taskAttr["flowable:dataType"] = "assignee";
      taskAttr["flowable:text"] = this.userTaskForm["assigneeName"] || null;

      window.bpmnInstances.modeling.updateProperties(this.bpmnElement, taskAttr);
    },
    updateVariable() {
      const taskAttr = Object.create(null);
      taskAttr["candidateUsers"] = null;
      taskAttr["candidateGroups"] = null;
      taskAttr["assignee"] = this.userTaskForm.variable;

      window.bpmnInstances.modeling.updateProperties(this.bpmnElement, taskAttr);
    },
    updateCandidateUsers(key) {
      const taskAttr = Object.create(null);
      taskAttr["assignee"] = null;
      taskAttr["candidateGroups"] = null;
      taskAttr[key] = this.userTaskForm[key] && this.userTaskForm[key].length ? this.userTaskForm[key].join(",") : null;
      taskAttr["flowable:text"] = this.userTaskForm["candidateUsersName"] || null;

      window.bpmnInstances.modeling.updateProperties(this.bpmnElement, taskAttr);
    },
    updateCandidateRole(key) {
      const taskAttr = Object.create(null);
      taskAttr["assignee"] = null;
      taskAttr["candidateUsers"] = null;
      taskAttr[key] = this.userTaskForm[key] && this.userTaskForm[key].length ? this.userTaskForm[key].join(",") : null;
      taskAttr["flowable:text"] = this.userTaskForm["candidateGroupsName"] || null;

      window.bpmnInstances.modeling.updateProperties(this.bpmnElement, taskAttr);
    },
    updateDataType() {
      const type = this.userTaskForm.type;
      const taskAttr = Object.create(null);
      taskAttr["candidateUsers"] = null;
      taskAttr["candidateGroups"] = null;
      taskAttr["assignee"] = null;
      taskAttr["flowable:text"] = null;

      if (type === "deptLeader" || type === "superDeptLeader") {
        this.userTaskForm.assigneeName = null;
        taskAttr["loopCharacteristics"] = null;
        this.loopCharacteristics = "";
      } else if (type === "startUserId") {
        taskAttr["assignee"] = "${startUserId}";
        taskAttr["loopCharacteristics"] = null;
        this.loopCharacteristics = "";
      } else if (type === "variable") {
        taskAttr["assignee"] = this.userTaskForm.variable;
        taskAttr["loopCharacteristics"] = null;
        this.loopCharacteristics = "";
      } else if (type === "assignee") {
        taskAttr["loopCharacteristics"] = null;
        this.loopCharacteristics = "";
      }

      taskAttr["flowable:dataType"] = type;
      window.bpmnInstances.modeling.updateProperties(this.bpmnElement, taskAttr);
    }
  },
  beforeUnmount() {
    this.bpmnElement = null;
  }
};
</script>
