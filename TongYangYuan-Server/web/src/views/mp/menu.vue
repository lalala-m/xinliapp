<template>
  <el-row>
    <el-col :span="4" class="mp_tree">
      <el-input placeholder="输入关键字进行过滤" v-model="filterText"></el-input>
      <el-tree :data="data" :props="defaultProps" node-key="appId" check-on-click-node="true" highlight-current="true" :filter-node-method="filterNode" ref="tree" class="filter-tree" @node-click="handleNodeClick"> </el-tree>
    </el-col>
    <el-col :span="8" class="menu_setting_area">
      <div class="content">
        <div id="app-menu">
          <!-- 预览窗 -->
          <div class="weixin-preview">
            <div class="weixin-hd">
              <div class="weixin-title"></div>
            </div>
            <div class="weixin-bd">
              <ul class="weixin-menu" id="weixin-menu">
                <li v-for="(btn, i) in menu.button" :key="btn" class="menu-item" :class="{ current: selectedMenuIndex === i && selectedMenuLevel() === 1 }" @click="selectedMenu(i, $event)">
                  <div class="menu-item-title">
                    <i class="icon_menu_dot"></i>
                    <span>{{ btn.name }}</span>
                  </div>
                  <ul class="weixin-sub-menu" v-show="selectedMenuIndex === i">
                    <li v-for="(sub, i2) in btn.sub_button" :key="sub" class="menu-sub-item" :class="{ current: selectedSubMenuIndex === i2 && selectedMenuLevel() === 2 }" @click.stop="selectedSubMenu(i2, $event)">
                      <div class="menu-item-title">
                        <span>{{ sub.name }}</span>
                      </div>
                    </li>
                    <li v-if="btn.sub_button.length < 5" class="menu-sub-item" @click.stop="addMenu(2)">
                      <div class="menu-item-title">
                        <i class="icon14_menu_add"></i>
                      </div>
                    </li>
                    <i class="menu-arrow arrow_out"></i>
                    <i class="menu-arrow arrow_in"></i>
                  </ul>
                </li>
                <li class="menu-item" v-if="menu.button.length < 3" @click="addMenu(1)"><i class="icon14_menu_add"></i></li>
              </ul>
            </div>
          </div>
          <!-- 主菜单 -->
          <div class="weixin-menu-detail" v-if="selectedMenuLevel() === 1">
            <div class="menu-input-group" style="border-bottom: 2px #e8e8e8 solid">
              <div class="menu-name">{{ menu.button[selectedMenuIndex].name }}</div>
              <div class="menu-del" @click="delMenu">删除菜单</div>
            </div>
            <div class="menu-input-group">
              <div class="menu-label">菜单名称</div>
              <div class="menu-input">
                <el-input type="text" name="name" placeholder="请输入菜单名称" v-model="menu.button[selectedMenuIndex].name" @input="checkMenuName(menu.button[selectedMenuIndex].name)" />
                <p class="menu-tips" style="color: #e15f63" v-show="menuNameBounds">字数超过上限</p>
                <p class="menu-tips">字数不超过4个汉字或8个字母</p>
              </div>
            </div>
            <template v-if="menu.button[selectedMenuIndex].sub_button.length === 0">
              <div class="menu-input-group">
                <div class="menu-label">菜单内容</div>
                <div class="menu-input">
                  <el-select v-model="menu.button[selectedMenuIndex].type" name="type" style="width: 100%">
                    <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value"></el-option>
                  </el-select>
                </div>
              </div>
              <div class="menu-content" v-if="selectedMenuType() === 1">
                <div class="menu-input-group">
                  <p class="menu-tips">订阅者点击该子菜单会跳到以下链接</p>
                  <div class="menu-label">页面地址</div>
                  <div class="menu-input">
                    <input type="text" placeholder="" class="menu-input-text" v-model="menu.button[selectedMenuIndex].url" />
                    <p class="menu-tips cursor" @click="selectNewsUrl">从公众号图文消息中选择</p>
                  </div>
                </div>
              </div>
              <div class="menu-msg-content" v-else-if="selectedMenuType() === 2" v-loading="materialLoading">
                <div class="menu-msg-head"><i class="icon_msg_sender"></i>图文消息</div>
                <div class="menu-msg-panel">
                  <div class="menu-msg-select" v-if="!menu.button[selectedMenuIndex].media_id" @click="selectMaterialId">
                    <i class="icon36_common add_gray"></i>
                    <strong>从素材库中选择</strong>
                  </div>
                  <div class="menu-msg-select" v-else>
                    <div class="menu-msg-title"><i class="icon_msg_sender"></i>{{ material.title }}</div>
                    <a :href="material.url" target="_blank" class="el-button el-button--mini">查看</a>
                    <el-button size="small" type="danger" @click="delMaterialId">删除</el-button>
                  </div>
                </div>
              </div>
              <div class="menu-content" v-else-if="selectedMenuType() === 3">
                <div class="menu-input-group">
                  <p class="menu-tips">用于消息接口推送，不超过128字节</p>
                  <div class="menu-label">菜单KEY值</div>
                  <div class="menu-input">
                    <input type="text" placeholder="" class="menu-input-text" v-model="menu.button[selectedMenuIndex].key" />
                  </div>
                </div>
              </div>
              <div class="menu-content" v-else-if="selectedMenuType() === 4">
                <div class="menu-input-group">
                  <p class="menu-tips">订阅者点击该子菜单会跳到以下小程序</p>
                  <div class="menu-label">小程序APPID</div>
                  <div class="menu-input">
                    <input type="text" placeholder="小程序的appid（仅认证公众号可配置）" class="menu-input-text" v-model="menu.button[selectedMenuIndex].appid" />
                  </div>
                </div>
                <div class="menu-input-group">
                  <div class="menu-label">小程序路径</div>
                  <div class="menu-input">
                    <input type="text" placeholder="小程序的页面路径 pages/Index/index" class="menu-input-text" v-model="menu.button[selectedMenuIndex].pagepath" />
                  </div>
                </div>
                <div class="menu-input-group">
                  <div class="menu-label">备用网页</div>
                  <div class="menu-input">
                    <input type="text" placeholder="" class="menu-input-text" v-model="menu.button[selectedMenuIndex].url" />
                    <p class="menu-tips">旧版微信客户端无法支持小程序，用户点击菜单时将会打开备用网页。</p>
                  </div>
                </div>
              </div>
            </template>
          </div>
          <!-- 子菜单 -->
          <div class="weixin-menu-detail" v-if="selectedMenuLevel() === 2">
            <div class="menu-input-group" style="border-bottom: 2px #e8e8e8 solid">
              <div class="menu-name">{{ menu.button[selectedMenuIndex].sub_button[selectedSubMenuIndex].name }}</div>
              <div class="menu-del" @click="delMenu">删除子菜单</div>
            </div>
            <div class="menu-input-group">
              <div class="menu-label">子菜单名称</div>
              <div class="menu-input">
                <el-input type="text" placeholder="请输入子菜单名称" v-model="menu.button[selectedMenuIndex].sub_button[selectedSubMenuIndex].name" @input="checkMenuName(menu.button[selectedMenuIndex].sub_button[selectedSubMenuIndex].name)" />
                <p class="menu-tips" style="color: #e15f63" v-show="menuNameBounds">字数超过上限</p>
                <p class="menu-tips">字数不超过8个汉字或16个字母</p>
              </div>
            </div>
            <div class="menu-input-group">
              <div class="menu-label">子菜单内容</div>
              <div class="menu-input">
                <el-select v-model="menu.button[selectedMenuIndex].sub_button[selectedSubMenuIndex].type" name="type" style="width: 100%">
                  <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value"></el-option>
                </el-select>
              </div>
            </div>
            <div class="menu-content" v-if="selectedMenuType() === 1">
              <div class="menu-input-group">
                <p class="menu-tips">订阅者点击该子菜单会跳到以下链接</p>
                <div class="menu-label">页面地址</div>
                <div class="menu-input">
                  <input type="text" placeholder="" class="menu-input-text" v-model="menu.button[selectedMenuIndex].sub_button[selectedSubMenuIndex].url" />
                  <p class="menu-tips cursor" @click="selectNewsUrl">从公众号图文消息中选择</p>
                </div>
              </div>
            </div>
            <div class="menu-msg-content" v-else-if="selectedMenuType() == 2" v-loading="materialLoading">
              <div class="menu-msg-head"><i class="icon_msg_sender"></i>图文消息</div>
              <div class="menu-msg-panel">
                <div class="menu-msg-select" v-if="menu.button[selectedMenuIndex].sub_button[selectedSubMenuIndex].media_id == '' || menu.button[selectedMenuIndex].sub_button[selectedSubMenuIndex].media_id == undefined" @click="selectMaterialId">
                  <i class="icon36_common add_gray"></i>
                  <strong>从素材库中选择</strong>
                </div>
                <div class="menu-msg-select" v-else>
                  <i class="icon_msg_sender"></i>{{ material.title }}
                  <a :href="material.url" target="_blank" class="el-button el-button--mini">查看</a>
                  <el-button size="small" type="danger" @click="delMaterialId">删除</el-button>
                </div>
              </div>
            </div>
            <div class="menu-content" v-else-if="selectedMenuType() === 3">
              <div class="menu-input-group">
                <p class="menu-tips">用于消息接口推送，不超过128字节</p>
                <div class="menu-label">菜单KEY值</div>
                <div class="menu-input">
                  <input type="text" placeholder="" class="menu-input-text" v-model="menu.button[selectedMenuIndex].sub_button[selectedSubMenuIndex].key" />
                </div>
              </div>
            </div>
            <div class="menu-content" v-else-if="selectedMenuType() === 4">
              <div class="menu-input-group">
                <p class="menu-tips">订阅者点击该子菜单会跳到以下小程序</p>
                <div class="menu-label">小程序APPID</div>
                <div class="menu-input">
                  <input type="text" placeholder="小程序的appid（仅认证公众号可配置）" class="menu-input-text" v-model="menu.button[selectedMenuIndex].sub_button[selectedSubMenuIndex].appid" />
                </div>
              </div>
              <div class="menu-input-group">
                <div class="menu-label">小程序路径</div>
                <div class="menu-input">
                  <input type="text" placeholder="小程序的页面路径 pages/Index/index" class="menu-input-text" v-model="menu.button[selectedMenuIndex].sub_button[selectedSubMenuIndex].pagepath" />
                </div>
              </div>
              <div class="menu-input-group">
                <div class="menu-label">备用网页</div>
                <div class="menu-input">
                  <input type="text" placeholder="" class="menu-input-text" v-model="menu.button[selectedMenuIndex].sub_button[selectedSubMenuIndex].url" />
                  <p class="menu-tips">旧版微信客户端无法支持小程序，用户点击菜单时将会打开备用网页。</p>
                </div>
              </div>
            </div>
          </div>
          <div class="weixin-btn-group" style="padding-top: 20px">
            <el-button type="primary" @click="onMenuSubmit">发布菜单</el-button>
            <el-button type="danger" @click="onMenuClear">清空菜单</el-button>
          </div>
          <el-dialog title="选择图文" v-model="newsDialog">
            <el-table :data="newsList" stripe>
              <el-table-column label="图文标题" prop="content.articles[0].title"></el-table-column>
              <el-table-column label="操作">
                <template v-slot="{ row }">
                  <el-button type="primary" size="small" @click="setNewsUrl(row)">选择</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-dialog>
          <el-dialog title="选择素材" v-model="materialDialog">
            <el-table :data="materialList" stripe>
              <el-table-column label="素材名称">
                <template v-slot="{ row }">
                  <div v-for="(item, index) in row.content.articles" :key="item">({{ index + 1 }}).{{ item.title }}</div>
                </template>
              </el-table-column>
              <el-table-column label="操作">
                <template v-slot="{ row }">
                  <el-button type="primary" size="small" @click="setMaterialId(row)">选择</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-dialog>
        </div>
      </div>
    </el-col>
  </el-row>
</template>
<style>
@import "@/assets/mp/menu.css";
</style>
<script>
import { defineComponent, reactive } from "vue";
import baseService from "../../service/baseService";
import { ElMessage, ElMessageBox } from "element-plus";
export default defineComponent({
  setup() {
    return reactive({
      filterText: "",
      data: [],
      appId: "",
      defaultProps: {
        children: "children",
        label: "name"
      },
      options: [
        {
          value: "view",
          label: "跳转网页(view)"
        },
        {
          value: "media_id",
          label: "发送消息(media_id)"
        },
        {
          value: "miniprogram",
          label: "打开指定小程序(miniprogram)"
        },
        {
          value: "click",
          label: "自定义点击事件(click)"
        },
        {
          value: "scancode_push",
          label: "扫码上传消息(scancode_push)"
        },
        {
          value: "scancode_waitmsg",
          label: "扫码提示下发(scancode_waitmsg)"
        },
        {
          value: "pic_sysphoto",
          label: "系统相机拍照(pic_sysphoto)"
        },
        {
          value: "pic_photo_or_album",
          label: "弹出拍照或者相册(pic_photo_or_album)"
        },
        {
          value: "pic_weixin",
          label: "弹出微信相册(pic_weixin)"
        },
        {
          value: "location_select",
          label: "弹出地理位置选择器(location_select)"
        }
      ],
      menu: { button: [] }, //当前菜单
      selectedMenuIndex: "", //当前选中菜单索引
      selectedSubMenuIndex: "", //当前选中子菜单索引
      menuNameBounds: false, //菜单长度是否过长
      material: {
        title: "",
        url: "",
        thumb_url: ""
      },
      materialLoading: false,
      materialDialog: false,
      materialList: [],
      materialListOffset: 0,
      materialListTotal: 0,
      newsDialog: false,
      newsList: [],
      newsListOffset: 0,
      newsListTotal: 0
    });
  },
  created() {
    this.init();
  },
  watch: {
    filterText(val) {
      this.$refs.tree.filter(val);
    }
  },
  methods: {
    filterNode(value, data) {
      if (!value) return true;
      return data.name.indexOf(value) !== -1;
    },
    handleNodeClick(data) {
      this.appId = data.appId;
      this.getMenu(this.appId);
    },
    getMenu(appId) {
      baseService.get("/sys/mp/menu/" + appId).then((res) => {
        if (res.data == null) {
          this.menu = JSON.parse('{"button":[]}');
        } else {
          this.menu = JSON.parse(res.data.menu);
        }
      });
    },
    init() {
      baseService.get("/sys/mp/account/page").then((res) => {
        this.data = res.data.list;

        // 选中第一个
        if (res.data.total > 0) {
          this.$nextTick(() => {
            this.$refs.tree.setCurrentKey(this.data[0].appId);
            this.appId = this.data[0].appId;
            this.getMenu(this.appId);
          });
        }
      });
    },
    //选中主菜单
    selectedMenu(i) {
      this.selectedSubMenuIndex = "";
      this.selectedMenuIndex = i;
      let selectedMenu = this.menu.button[this.selectedMenuIndex];
      //清空选中media_id 防止再次请求
      if (selectedMenu.media_id && this.selectedMenuType() === 2) {
        this.getMaterial(selectedMenu.media_id);
      }
      //检查名称长度
      this.checkMenuName(selectedMenu.name);
    },
    //选中子菜单
    selectedSubMenu(i) {
      this.selectedSubMenuIndex = i;
      let selectedSubMenu = this.menu.button[this.selectedMenuIndex].sub_button[this.selectedSubMenuIndex];
      if (selectedSubMenu.media_id && this.selectedMenuType() === 2) {
        this.getMaterial(selectedSubMenu.media_id);
      }
      this.checkMenuName(selectedSubMenu.name);
    },
    //选中菜单级别
    selectedMenuLevel() {
      if (this.selectedMenuIndex !== "" && this.selectedSubMenuIndex === "") {
        //主菜单
        return 1;
      } else if (this.selectedMenuIndex !== "" && this.selectedSubMenuIndex !== "") {
        //子菜单
        return 2;
      } else {
        //未选中任何菜单
        return 0;
      }
    },
    //获取菜单类型 1. view网页类型，2. media_id类型和view_limited类型 3. click点击类型，4.miniprogram表示小程序类型
    selectedMenuType() {
      if (this.selectedMenuLevel() === 1 && this.menu.button[this.selectedMenuIndex].sub_button.length === 0) {
        //主菜单
        switch (this.menu.button[this.selectedMenuIndex].type) {
          case "view":
            return 1;
          case "media_id":
            return 2;
          case "view_limited":
            return 2;
          case "click":
            return 3;
          case "scancode_push":
            return 3;
          case "scancode_waitmsg":
            return 3;
          case "pic_sysphoto":
            return 3;
          case "pic_photo_or_album":
            return 3;
          case "pic_weixin":
            return 3;
          case "location_select":
            return 3;
          case "miniprogram":
            return 4;
        }
      } else if (this.selectedMenuLevel() === 2) {
        //子菜单
        switch (this.menu.button[this.selectedMenuIndex].sub_button[this.selectedSubMenuIndex].type) {
          case "view":
            return 1;
          case "media_id":
            return 2;
          case "view_limited":
            return 2;
          case "click":
            return 3;
          case "scancode_push":
            return 3;
          case "scancode_waitmsg":
            return 3;
          case "pic_sysphoto":
            return 3;
          case "pic_photo_or_album":
            return 3;
          case "pic_weixin":
            return 3;
          case "location_select":
            return 3;
          case "miniprogram":
            return 4;
        }
      } else {
        return 1;
      }
    },
    //添加菜单
    addMenu(level) {
      if (level === 1 && this.menu.button.length < 3) {
        this.menu.button.push({
          type: "view",
          name: "菜单名称",
          sub_button: [],
          url: ""
        });
        this.selectedMenuIndex = this.menu.button.length - 1;
        this.selectedSubMenuIndex = "";
      }
      if (level === 2 && this.menu.button[this.selectedMenuIndex].sub_button.length < 5) {
        this.menu.button[this.selectedMenuIndex].sub_button.push({
          type: "view",
          name: "子菜单名称",
          url: ""
        });
        this.selectedSubMenuIndex = this.menu.button[this.selectedMenuIndex].sub_button.length - 1;
      }
    },
    //删除菜单
    async delMenu() {
      if (this.selectedMenuLevel() === 1) {
        try {
          await ElMessageBox.confirm("删除后菜单下设置的内容将被删除", "提示");
        } catch {
          return;
        }
        if (this.selectedMenuIndex === 0) {
          this.menu.button.splice(this.selectedMenuIndex, 1);
          this.selectedMenuIndex = 0;
        } else {
          this.menu.button.splice(this.selectedMenuIndex, 1);
          this.selectedMenuIndex -= 1;
        }
        if (this.menu.button.length === 0) {
          this.selectedMenuIndex = "";
        }
      } else if (this.selectedMenuLevel() === 2) {
        if (this.selectedSubMenuIndex === 0) {
          this.menu.button[this.selectedMenuIndex].sub_button.splice(this.selectedSubMenuIndex, 1);
          this.selectedSubMenuIndex = 0;
        } else {
          this.menu.button[this.selectedMenuIndex].sub_button.splice(this.selectedSubMenuIndex, 1);
          this.selectedSubMenuIndex -= 1;
        }
        if (this.menu.button[this.selectedMenuIndex].sub_button.length === 0) {
          this.selectedSubMenuIndex = "";
        }
      }
    },
    //检查菜单名称长度
    checkMenuName(val) {
      if (this.selectedMenuLevel() === 1 && this.getMenuNameLen(val) <= 8) {
        this.menuNameBounds = false;
      } else if (this.selectedMenuLevel() === 2 && this.getMenuNameLen(val) <= 16) {
        this.menuNameBounds = false;
      } else {
        this.menuNameBounds = true;
      }
    },
    //获取菜单名称长度
    getMenuNameLen(val) {
      var len = 0;
      for (var i = 0; i < val.length; i++) {
        var a = val.charAt(i);
        // eslint-disable-next-line no-control-regex
        a.match(/[^\x00-\xff]/gi) != null ? (len += 2) : (len += 1);
      }
      return len;
    },
    //选择公众号素材库素材
    selectMaterialId() {
      this.materialDialog = true;
      this.getMaterialList();
    },
    //选择公众号图文链接
    selectNewsUrl() {
      this.newsDialog = true;
      this.getNewsList();
    },
    //设置选择的素材id
    setMaterialId(row) {
      let { mediaId, content } = row;
      if (this.selectedMenuLevel() === 1) {
        this.menu.button[this.selectedMenuIndex]["media_id"] = mediaId;
      } else if (this.selectedMenuLevel() === 2) {
        this.menu.button[this.selectedMenuIndex].sub_button[this.selectedSubMenuIndex]["media_id"] = mediaId;
      }
      let { articles } = content;
      let item = articles[0];
      this.material.title = item.title;
      this.material.url = item.url;
      this.materialDialog = false;
    },
    //删除选择的素材id
    delMaterialId() {
      if (this.selectedMenuLevel() === 1) {
        this.menu.button[this.selectedMenuIndex]["media_id"] = "";
      } else if (this.selectedMenuLevel() === 2) {
        this.menu.button[this.selectedMenuIndex].sub_button[this.selectedSubMenuIndex]["media_id"] = "";
      }
    },
    //设置选择的图文链接
    setNewsUrl(row) {
      let { content } = row;
      let { articles } = content;
      if (this.selectedMenuLevel() === 1) {
        this.menu.button[this.selectedMenuIndex]["url"] = articles[0].url;
      } else if (this.selectedMenuLevel() === 2) {
        this.menu.button[this.selectedMenuIndex].sub_button[this.selectedSubMenuIndex]["url"] = articles[0].url;
      }
      this.newsDialog = false;
    },
    //获取素材信息
    getMaterial(mediaId) {
      this.materialLoading = true;

      baseService.get("/sys/mp/material/get", { appId: this.appId, mediaId: mediaId }).then((res) => {
        this.material.title = res.data.articles[0].title;
        this.material.url = res.data.articles[0].url;
      });
      this.materialLoading = false;
    },
    getNewsList() {
      if (this.newsListOffset > 0 && this.newsListOffset >= this.newsListOffset) {
        return;
      }

      baseService.get("/sys/mp/material/page", { appId: this.appId, type: "news", offset: this.newsListOffset, limit: 10 }).then((res) => {
        this.newsList = this.newsList.concat(res.data.list);
        this.newsListOffset += res.data.list.length;
        this.newsListTotal = res.data.total;
      });
    },
    getMaterialList() {
      if (this.materialListOffset > 0 && this.materialListOffset >= this.materialListTotal) {
        return;
      }

      baseService.get("/sys/mp/material/page", { appId: this.appId, type: "news", offset: this.materialListOffset, limit: 10 }).then((res) => {
        this.materialList = this.materialList.concat(res.data.list);
        this.materialListOffset += res.data.list.length;
        this.materialListTotal = res.data.total;
      });
    },
    async onMenuSubmit() {
      await ElMessageBox.confirm("确定后发布当前自定义菜单", "提示");

      const dataForm = {};
      dataForm.appId = this.appId;
      dataForm.menu = JSON.stringify(this.menu);
      baseService.post("/sys/mp/menu/" + this.appId, dataForm).then(() => {
        ElMessage.success({
          message: this.$t("prompt.success"),
          duration: 500,
          onClose: () => {
            this.visible = false;
          }
        });
        ElMessage.success("发布成功");
        console.log(dataForm.menu);
      });
    },
    async onMenuClear() {
      try {
        await ElMessageBox.confirm("确定后将清空后公众号自定义菜单", "提示");
        baseService.delete("/sys/mp/menu/" + this.appId).then(() => {
          ElMessage.success({
            message: this.$t("prompt.success"),
            duration: 500,
            onClose: () => {
              this.visible = false;
            }
          });
        });
        ElMessage.success("清空成功");
        this.menu.button = [];
      } catch (err) {
        console.error(err);
      }
    }
  }
});
</script>
