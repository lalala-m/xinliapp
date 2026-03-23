<template>
  <el-select v-model="modelAsString" @change="$emit('update:modelValue', $event)" :placeholder="placeholder" clearable>
    <el-option :label="data.dictLabel" v-for="data in dataList" :key="data.dictValue" :value="data.dictValue">{{ data.dictLabel }}</el-option>
  </el-select>
</template>
<script lang="ts">
import { computed, defineComponent } from "vue";
import { getDictDataList } from "@/utils/utils";
import { useAppStore } from "@/store";
export default defineComponent({
  name: "RenSelect",
  props: {
    modelValue: [Number, String],
    dictType: String,
    placeholder: String
  },
  setup(props: any) {
    const store = useAppStore();
    return {
      modelAsString: computed({
        get() {
          return props.modelValue != undefined ? props.modelValue.toString() : "";
        },
        set(value: any) {
          // eslint-disable-next-line vue/no-mutating-props
          props.modelValue = value;
        }
      }),
      dataList: getDictDataList(store.state.dicts, props.dictType)
    };
  }
});
</script>
