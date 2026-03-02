<script lang="ts">
import "@/assets/css/app.less";
import "@/assets/theme/index.less";
import "@/assets/theme/mobile.less";
import FullscreenLayout from "@/layout/fullscreen-layout.vue";
import Layout from "@/layout/index.vue";
import { ElConfigProvider } from "element-plus";
import { defineComponent, onMounted, reactive, watch } from "vue";
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import { useAppStore } from "@/store";
import app from "./constants/app";
import { EPageLayoutEnum, EThemeColor, EThemeSetting } from "./constants/enum";
import { getLocaleLang, supportLangs } from "./i18n";
import { IObject } from "./types/interface";
import { getThemeConfigCache, setThemeColor, updateTheme } from "./utils/theme";

export default defineComponent({
  name: "App",
  components: { Layout, FullscreenLayout, ElConfigProvider },
  setup() {
    const store = useAppStore();
    const route = useRoute();
    const { t, locale } = useI18n();
    const state = reactive({
      layout: location.href.includes("pop=true") ? EPageLayoutEnum.fullscreen : EPageLayoutEnum.page
    });
    const onInitLang = (vl: string, oldVl?: string) => {
      window.document.querySelector("html")?.setAttribute("lang", vl);
      document.title = t("ui.app.productName");
      if (oldVl && route.path !== "/login") {
        store.updateState({ appIsReady: false });
        location.reload();
      }
    };
    onMounted(() => {
      //读取主题色缓存
      const themeCache = getThemeConfigCache();
      const themeColor = themeCache[EThemeSetting.ThemeColor];
      setThemeColor(EThemeColor.ThemeColor, themeColor);
      updateTheme(themeColor);
      onInitLang(getLocaleLang());
    });
    watch(() => locale.value, onInitLang);
    watch(
      () => [route.path, route.query, route.fullPath],
      ([path, query, fullPath]) => {
        store.updateState({ activeTabName: fullPath });
        state.layout = app.fullscreenPages.includes(path as string) || (query as IObject)["pop"] ? EPageLayoutEnum.fullscreen : EPageLayoutEnum.page;
      }
    );
    return {
      store,
      state,
      pageTag: EPageLayoutEnum.page,
      locale: supportLangs[locale.value].el
    };
  }
});
</script>
<template>
  <el-config-provider :locale="locale">
    <div v-if="!store.state.appIsRender" v-loading="true" :element-loading-fullscreen="true" :element-loading-lock="true" style="width: 100vw; height: 100vh; position: absolute; top: 0; left: 0; z-index: 99999; background: #fff"></div>
    <template v-if="store.state.appIsReady">
      <layout v-if="state.layout === pageTag"> </layout>
      <fullscreen-layout v-else></fullscreen-layout>
    </template>
  </el-config-provider>
</template>
