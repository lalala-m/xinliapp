import vue from "@vitejs/plugin-vue";
import { resolve } from "path";
import { defineConfig, loadEnv, UserConfig, UserConfigExport } from "vite";
import { createHtmlPlugin } from "vite-plugin-html";
import tsconfigPaths from "vite-tsconfig-paths";
import { createSvgIconsPlugin } from "vite-plugin-svg-icons";
const prefix = `monaco-editor/esm/vs`;

export default (config: UserConfig): UserConfigExport => {
  const mode = config.mode as string;
  return defineConfig({
    base: "./",
    optimizeDeps: {
      include: [
        `${prefix}/language/json/json.worker`,
        `${prefix}/language/css/css.worker`,
        `${prefix}/language/html/html.worker`,
        `${prefix}/language/typescript/ts.worker`,
        `${prefix}/editor/editor.worker`,
        "@/../lib/vform/designer.umd.js"
      ]
    },
    plugins: [
      vue(),
      createHtmlPlugin({
        minify: true,
        /**
         * 需要注入 index.html ejs 模版的数据
         */
        inject: {
          data: {
            apiURL: loadEnv(mode, process.cwd()).VITE_APP_API,
            socketURL: loadEnv(mode, process.cwd()).VITE_APP_SOCKET,
            title: ""
          },
          tags: [
            {
              injectTo: "body-prepend",
              tag: "div",
              attrs: {
                id: "tag"
              }
            }
          ]
        }
      }),
      tsconfigPaths(),
      createSvgIconsPlugin({
        iconDirs: [resolve(__dirname, "src/assets/icons/svg")],
        symbolId: "icon-[dir]-[name]"
      })
    ],
    css: {
      preprocessorOptions: {
        scss: {
          api: "modern-compiler"
        }
      }
    },
    build: {
      chunkSizeWarningLimit: 1024,
      commonjsOptions: {
        include: /node_modules|lib/
      },
      rollupOptions: {
        output: {
          manualChunks: {
            monacoeditor: ["monaco-editor"],
            quill: ["quill"],
            lodash: ["lodash"],
            lib: ["sortablejs", "vxe-table", "xe-utils"],
            vlib: ["vue", "vue-router", "vue-i18n", "element-plus"]
          }
        }
      }
    },
    resolve: {
      alias: {
        // 配置别名
        "@": resolve(__dirname, "./src"),
        "vue-i18n": "vue-i18n/dist/vue-i18n.cjs.js"
      }
    },
    server: {
      open: false, // 自动启动浏览器
      host: "0.0.0.0", // localhost
      port: 8001, // 端口号
      hmr: { overlay: false }
    }
  });
};
