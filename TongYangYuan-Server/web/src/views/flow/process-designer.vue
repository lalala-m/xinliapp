<template>
  <div id="flowable-designer">
    <my-process-designer
      v-if="visible"
      :key="`designer-${reloadIndex}`"
      :options="{
        taskResizingEnabled: true,
        eventResizingEnabled: true
      }"
      v-model="xmlString"
      v-bind="controlForm"
      keyboard
      ref="processDesigner"
      @element-click="elementClick"
      @element-contextmenu="elementContextmenu"
      @init-finished="initModeler"
      @save-flowable="saveFlowable"
    />
    <my-properties-panel v-if="visible" :key="`penal-${reloadIndex}`" :bpmn-modeler="modeler" :prefix="controlForm.prefix" class="process-panel" />
  </div>
</template>

<script>
import translations from "@/components/ren-flowable/translations";
// 自定义渲染（隐藏了 label 标签）
import CustomRenderer from "@/components/ren-flowable/modules/custom-renderer";
// 自定义元素选中时的弹出菜单（修改 默认任务 为 用户任务）
import CustomContentPadProvider from "@/components/ren-flowable/package/designer/plugins/content-pad";
// 自定义左侧菜单（修改 默认任务 为 用户任务）
import CustomPaletteProvider from "@/components/ren-flowable/package/designer/plugins/palette";
import Log from "@/components/ren-flowable/package/Log";
import "bpmn-js/dist/assets/diagram-js.css";
import "bpmn-js/dist/assets/bpmn-font/css/bpmn.css";
import "bpmn-js/dist/assets/bpmn-font/css/bpmn-codes.css";
import baseService from "@/service/baseService";
import { ElMessage } from "element-plus";
export default {
  name: "App",
  props: {
    modelId: String
  },
  data() {
    return {
      visible: false,
      xmlString: "",
      modeler: null,
      reloadIndex: 0,
      controlDrawerVisible: false,
      infoTipVisible: false,
      pageMode: false,
      translationsSelf: translations,
      controlForm: {
        processId: "",
        processName: "",
        simulation: true,
        labelEditing: false,
        labelVisible: false,
        prefix: "flowable",
        headerButtonSize: "default",
        events: ["element.click", "element.contextmenu"],
        additionalModel: [CustomContentPadProvider, CustomPaletteProvider]
      },
      addis: {
        CustomContentPadProvider,
        CustomPaletteProvider
      }
    };
  },
  created() {
    if (this.modelId) {
      baseService.get("/flow/model/" + this.modelId).then((res) => {
        this.xmlString = res.data.bpmnXml;
        this.controlForm.processId = res.data.key;
        this.controlForm.processName = res.data.name;
        this.visible = true;
      });
    } else {
      this.visible = true;
    }
  },
  methods: {
    initModeler(modeler) {
      setTimeout(() => {
        this.modeler = modeler;
        const canvas = modeler.get("canvas");
        const rootElement = canvas.getRootElement();
        Log.prettyPrimary("Process Id:", rootElement.id);
        Log.prettyPrimary("Process Name:", rootElement.businessObject.name);
      }, 10);
    },
    reloadProcessDesigner(notDeep) {
      this.controlForm.additionalModel = [];
      for (const key in this.addis) {
        if (this.addis[key]) {
          this.controlForm.additionalModel.push(this.addis[key]);
        }
      }
      !notDeep && (this.xmlString = undefined);
      this.reloadIndex += 1;
      this.modeler = null; // 避免 panel 异常
    },
    saveFlowable(xml) {
      const canvas = this.modeler.get("canvas");
      const rootElement = canvas.getRootElement();

      const data = {
        id: this.modelId,
        key: rootElement.id,
        name: rootElement.businessObject.name,
        bpmnXml: xml
      };

      baseService
        .post("/flow/model", data, {
          "content-type": "application/x-www-form-urlencoded"
        })
        .then(() => {
          ElMessage.success({
            message: "操作成功",
            duration: 500,
            onClose: () => {
              this.$emit("refreshDataList");
            }
          });
        });
    },
    changeLabelEditingStatus(status) {
      this.addis.labelEditing = status ? { labelEditingProvider: ["value", ""] } : false;
      this.reloadProcessDesigner();
    },
    changeLabelVisibleStatus(status) {
      this.addis.customRenderer = status ? CustomRenderer : false;
      this.reloadProcessDesigner();
    },
    elementClick(element) {
      //console.log(element);
      this.element = element;
    },
    elementContextmenu(element) {
      console.log("elementContextmenu:", element);
    },
    changePageMode(mode) {
      const theme = mode
        ? {
            // dark
            stroke: "#ffffff",
            fill: "#333333"
          }
        : {
            // light
            stroke: "#000000",
            fill: "#ffffff"
          };
      const elements = this.modeler.get("elementRegistry").getAll();
      this.modeler.get("modeling").setColor(elements, theme);
    }
  }
};
</script>

<style lang="scss" scoped>
body {
  overflow: hidden;
  margin: 0;
  box-sizing: border-box;
}
#flowable-designer {
  width: 100%;
  height: calc(100vh - 115px);
  box-sizing: border-box;
  display: inline-grid;
  grid-template-columns: auto max-content;
}
.demo-info-bar {
  position: fixed;
  right: 8px;
  bottom: 108px;
  z-index: 1;
}
.demo-control-bar {
  position: fixed;
  right: 8px;
  bottom: 48px;
  z-index: 1;
}
.open-model-button {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  font-size: 32px;
  background: rgba(64, 158, 255, 1);
  color: #ffffff;
  cursor: pointer;
}
.zoom-in-right-enter-active,
.zoom-in-right-leave-active {
  opacity: 1;
  transform: scaleY(1) translateY(-48px);
  transition: all 300ms cubic-bezier(0.23, 1, 0.32, 1);
  transform-origin: right center;
}
.zoom-in-right-enter,
.zoom-in-right-leave-active {
  opacity: 0;
  transform: scaleX(0) translateY(-48px);
}
.info-tip {
  position: absolute;
  width: 480px;
  top: 0;
  right: 64px;
  z-index: 10;
  box-sizing: border-box;
  padding: 0 16px;
  color: #333333;
  background: #f2f6fc;
  transform: translateY(-48px);
  border: 1px solid #ebeef5;
  border-radius: 4px;
  &::before,
  &::after {
    content: "";
    width: 0;
    height: 0;
    border-width: 8px;
    border-style: solid;
    position: absolute;
    right: -15px;
    top: 50%;
  }
  &::before {
    border-color: transparent transparent transparent #f2f6fc;
    z-index: 10;
  }
  &::after {
    right: -16px;
    border-color: transparent transparent transparent #ebeef5;
    z-index: 1;
  }
}
.control-form {
  .el-radio {
    width: 100%;
    line-height: 32px;
  }
}
.element-overlays {
  box-sizing: border-box;
  padding: 8px;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 4px;
  color: #fafafa;
}
</style>
