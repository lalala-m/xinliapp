import app from "@/constants/app";
import { getLocaleLang } from "@/i18n";
import { IHttpResponse, IObject } from "@/types/interface";
import router from "@/router";
import axios, { AxiosRequestConfig } from "axios";
import qs from "qs";
import { getToken, getRefreshToken, getCache, setCache } from "./cache";
import { getValueByKeys } from "./utils";
import { ElMessage } from "element-plus";
import { CacheToken, CacheTenantCode } from "@/constants/cacheKey";

const http = axios.create({
  baseURL: app.api,
  timeout: app.requestTimeout
});

http.interceptors.request.use(
  function (config: any) {
    config.headers["X-Requested-With"] = "XMLHttpRequest";
    config.headers["Request-Start"] = new Date().getTime();
    config.headers["Accept-Language"] = getLocaleLang();
    config.headers["tenantCode"] = getCache(CacheTenantCode, { isParse: false }) || "";
    config.headers["Authorization"] = getToken();
    if (config.method?.toUpperCase() === "GET") {
      config.params = { ...config.params, _t: new Date().getTime() };
    }
    if (Object.values(config.headers).includes("application/x-www-form-urlencoded")) {
      config.data = qs.stringify(config.data);
    }
    return config;
  },
  function (error) {
    return Promise.reject(error);
  }
);

// 是否刷新
let isRefreshToken = false;
// 重试请求
let requests: any[] = [];

// 刷新token
const getAccessToken = (refreshToken: string) => {
  return http.post("/sys/auth/access-token?refreshToken=" + refreshToken);
};

http.interceptors.response.use(
  async (response: any) => {
    // 响应成功
    if (response.data.code === 0) {
      return response;
    }

    // refreshToken错误，跳转到登录页
    if (response.data.code === 400) {
      return redirectLogin();
    }

    if (response.data.code === 401) {
      const config = response.config;
      if (!isRefreshToken) {
        isRefreshToken = true;

        // 不存在 refreshToken，重新登录
        const refreshToken = getRefreshToken();
        if (!refreshToken) {
          return redirectLogin();
        }

        try {
          const { data } = await getAccessToken(refreshToken);
          // 设置新 token
          const token = {
            refresh_token: data.data.refresh_token,
            access_token: data.data.access_token
          };

          setCache(CacheToken, token, false);
          // eslint-disable-next-line
          config.headers!.Authorization = data.data.access_token;
          requests.forEach((cb: any) => {
            cb();
          });
          requests = [];
          return http(config);
        } catch (e) {
          requests.forEach((cb: any) => {
            cb();
          });
          return redirectLogin();
        } finally {
          requests = [];
          isRefreshToken = false;
        }
      } else {
        // 多个请求的情况
        return new Promise((resolve) => {
          requests.push(() => {
            // eslint-disable-next-line
            config.headers!.Authorization = getToken();
            resolve(http(config));
          });
        });
      }
    }

    // 错误提示
    ElMessage.error(response.data.msg);

    return Promise.reject(new Error(response.data.msg || "Error"));
  },
  (error) => {
    const status = getValueByKeys(error, "response.status", 500);
    const httpCodeLabel: IObject<string> = {
      400: "请求参数错误",
      401: "未授权，请登录",
      403: "拒绝访问",
      404: `请求地址出错: ${getValueByKeys(error, "response.config.url", "")}`,
      408: "请求超时",
      500: "API接口报500错误",
      501: "服务未实现",
      502: "网关错误",
      503: "服务不可用",
      504: "网关超时",
      505: "HTTP版本不受支持"
    };
    if (error && error.response) {
      console.error("请求错误", error.response.data);
    }
    if (status === 401) {
      redirectLogin();
    }
    return Promise.reject(new Error(httpCodeLabel[status] || "接口错误"));
  }
);

const redirectLogin = () => {
  router.replace("/login");
  return;
};

export default (o: AxiosRequestConfig): Promise<IHttpResponse> => {
  return new Promise((resolve, reject) => {
    http(o)
      .then((res) => {
        return resolve(res.data);
      })
      .catch(reject);
  });
};
