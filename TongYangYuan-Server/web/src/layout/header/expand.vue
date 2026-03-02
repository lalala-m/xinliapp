<script lang="ts">
import Lang from "@/components/base/lang";
import SvgIcon from "@/components/base/svg-icon";
import TenantSwitch from "@/components/tenant-switch";
import baseService from "@/service/baseService";
import { checkPermission } from "@/utils/utils";
import { useFullscreen } from "@vueuse/core";
import { computed, defineComponent, nextTick, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { useRouter } from "vue-router";
import { useAppStore } from "@/store";
import SettingSidebar from "../setting/index.vue";
import userLogo from "@/assets/images/user.png";
import "@/assets/css/header.less";
import { ElMessageBox } from "element-plus";
import app from "@/constants/app";

interface IExpand {
  userName?: string;
}

/**
 * 顶部右侧扩展区域
 */
export default defineComponent({
  name: "Expand",
  computed: {
    app() {
      return app;
    }
  },
  components: { SettingSidebar, SvgIcon, Lang, TenantSwitch },
  props: {
    userName: String
  },
  setup(props: IExpand) {
    const { t } = useI18n();
    const router = useRouter();
    const store = useAppStore();
    const { isFullscreen, toggle } = useFullscreen();
    const messageCount = ref(0);
    const tenantSwitch = ref(false);
    const tenantSwitchRef = ref();
    const tenantName = ref();

    watch(
      () => store.state.appIsLogin,
      (vl) => {
        if (vl) {
          getUnReadMessageCount();
          if (app.tenantMode !== "none") {
            getTenantInfo();
          }
        }
      }
    );

    const getUnReadMessageCount = () => {
      baseService.get("/sys/notice/mynotice/unread").then((res) => {
        messageCount.value = res.data;
      });
    };

    // 当前租户信息
    const getTenantInfo = () => {
      baseService.get("/sys/tenant/info").then((res) => {
        tenantName.value = res.data.tenantName;
      });
    };

    const onClickUserMenus = (path: string) => {
      if (path === "/login") {
        ElMessageBox.confirm(t("prompt.info", { handle: t("logout") }), t("prompt.title"), {
          confirmButtonText: t("confirm"),
          cancelButtonText: t("cancel"),
          type: "warning"
        })
          .then(() => {
            baseService.post("/sys/auth/logout").finally(() => {
              router.push(path);
            });
          })
          .catch(() => {
            //
          });
      } else {
        router.push(path);
      }
    };
    const onClickMessage = () => {
      router.push("/sys/notice-user");
    };
    const onSwitchTenant = () => {
      if (store.state.user.superAdmin === 1 && store.state.user.tenantCode === "10000") {
        tenantSwitch.value = true;
        nextTick(() => {
          if (tenantSwitchRef.value) {
            tenantSwitchRef.value.init();
          }
        });
      }
    };
    const messagePermission = computed(() => checkPermission(store.state.permissions, "sys:notice:all"));
    return {
      props,
      store,
      isFullscreen,
      messageCount,
      tenantSwitch,
      tenantSwitchRef,
      messagePermission,
      userLogo,
      tenantName,
      onClickUserMenus,
      onClickMessage,
      onSwitchTenant,
      toggle,
      t
    };
  }
});
</script>
<template>
  <div class="rr-header-right-items">
    <div class="hidden-xs-only" @click="onSwitchTenant" v-if="app.tenantMode != 'none'">
      <svg-icon name="team" :style="`margin-right: 5px;`"></svg-icon>
      <span style="font-size: 14px"> {{ t("ui.user.links.tenantSwitch") }}：{{ tenantName }} </span>
    </div>
    <div @click="toggle" class="hidden-xs-only">
      <span>
        <svg-icon :name="isFullscreen ? 'tuichuquanping' : 'fullscreen2'"></svg-icon>
      </span>
    </div>
    <div v-if="messagePermission">
      <el-badge :value="messageCount > 0 ? messageCount : ''" type="danger" :max="99" @click="onClickMessage">
        <el-icon class="icon">
          <bell />
        </el-icon>
      </el-badge>
    </div>
    <div>
      <lang></lang>
    </div>
    <div style="display: flex; justify-content: center; align-items: center">
      <img :src="userLogo" :alt="props.userName" style="width: 30px; height: 30px; border-radius: 50%; margin-top: 3px; margin-right: 5px" />
      <el-dropdown @command="onClickUserMenus">
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item icon="lock" command="/user/password">
              {{ t("ui.user.links.editPassword") }}
            </el-dropdown-item>
            <el-dropdown-item icon="switch-button" divided command="/login">
              {{ t("ui.user.links.logout") }}
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
        <span class="el-dropdown-link" style="display: flex">
          {{ props.userName }}
          <el-icon class="el-icon--right" style="font-size: 14px"><arrow-down /></el-icon>
        </span>
      </el-dropdown>
    </div>
    <setting-sidebar></setting-sidebar>
    <tenant-switch v-if="tenantSwitch" ref="tenantSwitchRef"></tenant-switch>
  </div>
</template>
