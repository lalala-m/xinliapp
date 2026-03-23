<template>
  <div class="rr-login">
    <div class="rr-login-wrap">
      <div class="rr-login-left hidden-sm-and-down">
        <p class="rr-login-left-title">{{ $t("ui.app.productName") }}</p>
      </div>

      <div class="rr-login-right">
        <div class="rr-login-right-main">
          <h4 class="rr-login-right-main-title">
            {{ $t("ui.login.loginBtn") }}
            <span class="rr-login-right-main-lang">
              <lang>
                <svg-icon name="fanyiline"></svg-icon>
              </lang>
            </span>
          </h4>
          <el-form ref="formRef" label-width="80px" :status-icon="true" :model="login" :rules="rules" @keyup.enter="onLogin">
            <el-form-item v-if="state.tenantMode === 'code'" label-width="0" prop="tenantCode">
              <el-select v-model="state.tenantCode" :placeholder="$t('tenant.select')" style="width: 100%" @change="onChange">
                <el-option v-for="tenant in state.tenantList" :key="tenant.tenantCode" :label="tenant.tenantName" :value="tenant.tenantCode"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label-width="0" prop="username">
              <el-input v-model="login.username" :placeholder="$t('ui.login.userNamePlaceholder')" prefix-icon="user" autocomplete="off"></el-input>
            </el-form-item>
            <el-form-item label-width="0" prop="password">
              <el-input :placeholder="$t('ui.login.passwordPlaceholder')" v-model="login.password" prefix-icon="lock" autocomplete="off" show-password></el-input>
            </el-form-item>
            <el-form-item label-width="0" prop="captcha">
              <el-space class="rr-login-right-main-code">
                <el-input v-model="login.captcha" :placeholder="$t('ui.login.captchaPlaceholder')" prefix-icon="first-aid-kit"></el-input>
                <img style="vertical-align: middle; height: 40px; cursor: pointer" :src="state.captchaUrl" @click="onRefreshCode" alt="" />
              </el-space>
            </el-form-item>
            <el-form-item label-width="0">
              <el-button type="primary" size="small" :disabled="state.loading" @click="onLogin" class="rr-login-right-main-btn">
                {{ $t("ui.login.loginBtn") }}
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>
    <div class="login-footer">
      <!--      <p>
        <a href="https://demo.cloud.renren.io/renren-cloud-tenant" target="_blank">{{ $t("login.demo") }}</a>
      </p>
      <p>
        <a href="https://www.renren.io/" target="_blank">{{ $t("ui.app.name") }}</a
        >{{ state.year }} © {{ $t("ui.app.copyright") }}
      </p>-->
    </div>
  </div>
</template>

<script lang="ts" setup>
import { onMounted, reactive, ref } from "vue";
import { CacheTenantCode, CacheToken } from "@/constants/cacheKey";
import Lang from "@/components/base/lang/index";
import baseService from "@/service/baseService";
import { getCache, setCache } from "@/utils/cache";
import { ElMessage } from "element-plus";
import { getUuid } from "@/utils/utils";
import app from "@/constants/app";
import SvgIcon from "@/components/base/svg-icon/index";
import { useAppStore } from "@/store";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";

const store = useAppStore();

const router = useRouter();
const { t } = useI18n();

const state = reactive({
  captchaUrl: "",
  loading: false,
  tenantList: [] as any[],
  tenantCode: "",
  tenantMode: "",
  year: new Date().getFullYear()
});

const login = reactive({ username: "", password: "", captcha: "", uuid: "" });

onMounted(async () => {
  //清理数据
  store.logout();
  getCaptchaUrl();
  state.tenantMode = app.tenantMode;

  if (app.tenantMode === "code") {
    await getTenantList();
  }
  state.tenantCode = getCache(CacheTenantCode, { isParse: false }) || "10000";
});
const formRef = ref();
const onLogin = () => {
  formRef.value.validate((valid: boolean) => {
    if (valid) {
      state.loading = true;
      baseService
        .post("/sys/auth/login", login)
        .then((res: any) => {
          state.loading = false;
          if (res.code === 0) {
            setCache(CacheToken, res.data, false);
            ElMessage.success(t("ui.login.loginOk"));
            router.push("/");
          } else {
            ElMessage.error(res.msg);
          }
        })
        .catch(() => {
          state.loading = false;
          onRefreshCode();
        });
    }
  });
};
const getTenantList = async () => {
  const res = await baseService.get("/sys/tenant/list");
  state.tenantList = res.data;
};
const onChange = (value: string) => {
  setCache(CacheTenantCode, value + "");
};

const getCaptchaUrl = () => {
  login.uuid = getUuid();
  state.captchaUrl = `${app.api}/sys/auth/captcha?uuid=${login.uuid}`;
};

const onRefreshCode = () => {
  getCaptchaUrl();
};

const rules = ref({
  username: [{ required: true, message: t("validate.required"), trigger: "blur" }],
  password: [{ required: true, message: t("validate.required"), trigger: "blur" }],
  captcha: [{ required: true, message: t("validate.required"), trigger: "blur" }]
});
</script>

<style lang="less" scoped>
@import url("@/assets/theme/base.less");

.rr-login {
  width: 100vw;
  height: 100vh;
  background: #019ec4;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  @media only screen and (max-width: 992px) {
    .rr-login-wrap {
      width: 96% !important;
    }

    .rr-login-right {
      width: 100% !important;
    }
  }

  &-wrap {
    margin: 0 auto;
    width: 1000px;
    box-shadow: -4px 5px 10px rgba(0, 0, 0, 0.4);
    animation-duration: 1s;
    animation-fill-mode: both;
    border-radius: 5px;
    overflow: hidden;
  }

  &-left {
    justify-content: center;
    flex-direction: column;
    background-color: @--color-primary;
    color: #fff;
    float: left;
    width: 50%;

    &-title {
      text-align: center;
      color: #fff;
      font-weight: 300;
      letter-spacing: 2px;
      font-size: 32px;
    }
  }

  &-right {
    border-left: none;
    color: #fff;
    background-color: #fff;
    width: 50%;
    float: left;

    &-main {
      margin: 0 auto;
      width: 65%;

      &-title {
        color: #333;
        margin-bottom: 40px;
        font-weight: 500;
        font-size: 24px;
        text-align: center;
        letter-spacing: 4px;
      }

      &-lang .iconfont {
        font-size: 20px;
        color: #606266;
        font-weight: 800;
        width: 20px;
        height: 20px;
      }

      .el-input__inner {
        border-width: 0;
        border-radius: 0;
        border-bottom: 1px solid #dcdfe6;
      }

      &-code {
        width: 100%;

        .el-space__item:first-child {
          flex: 1;
        }
      }

      &-btn {
        width: 100%;
        height: 45px;
        font-size: 18px !important;
        letter-spacing: 2px;
        font-weight: 300 !important;
        cursor: pointer;
        margin-top: 30px;
        font-family: neo, sans-serif;
        transition: 0.25s;
      }
    }
  }

  .login-footer {
    text-align: center;
    position: absolute;
    bottom: 0;
    padding: 20px;
    color: rgba(255, 255, 255, 0.6);

    p {
      margin: 10px 0;
    }

    a {
      padding: 0 5px;
      color: rgba(255, 255, 255, 0.6);

      &:focus,
      &:hover {
        color: #fff;
      }
    }
  }

  &-left,
  &-right {
    position: relative;
    min-height: 500px;
    align-items: center;
    display: flex;
  }

  @keyframes animate-down {
    0%,
    60%,
    75%,
    90%,
    to {
      animation-timing-function: cubic-bezier(0.215, 0.61, 0.355, 1);
    }
    0% {
      opacity: 0;
      transform: translate3d(0, -3000px, 0);
    }
    60% {
      opacity: 1;
      transform: translate3d(0, 25px, 0);
    }
    75% {
      transform: translate3d(0, -10px, 0);
    }
    90% {
      transform: translate3d(0, 5px, 0);
    }
    to {
      transform: none;
    }
  }

  .animate-down {
    animation-name: animate-down;
  }
}
</style>
