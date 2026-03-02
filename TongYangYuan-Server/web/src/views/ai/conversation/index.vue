<template>
  <!-- 左侧对话组件-->
  <div class="chat-sidebar">
    <!-- 新建对话按钮 -->
    <div class="new-chat-btn">
      <el-button type="primary" @click="createNewChat">
        <el-icon><Plus /></el-icon> 新建对话
      </el-button>
    </div>

    <!-- 聊天记录 -->
    <div class="sidebar-section">
      <el-scrollbar>
        <div class="chat-history-list">
          <div v-for="(chat, index) in chatSessions" :key="index" class="chat-item" :class="{ active: currentChatIndex === index }" @click="switchChat(chat, index)">
            <div class="chat-item-content">
              <el-avatar :size="32" class="chat-avatar">
                <el-icon><ChatDotRound /></el-icon>
              </el-avatar>
              <span class="chat-title">{{ chat.title }}</span>
            </div>
            <div class="chat-item-actions">
              <el-button link @click.stop="editChatTitle(chat.id, index)">
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button link @click.stop="deleteChat(chat, index)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
      </el-scrollbar>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ChatDotRound, Plus, Edit, Delete } from "@element-plus/icons-vue";
import { ref } from "vue";
import { ElMessageBox } from "element-plus";
import baseService from "@/service/baseService";

// 状态变量
const currentChatIndex = ref(0);

// 聊天会话管理
const chatSessions = ref<{ id: number; modelId: number; title: string }[]>([]);

// 初始化聊天会话
baseService.get("/sys/ai/chat/conversation/list").then((res: any) => {
  chatSessions.value = res.data;

  switchChat(chatSessions.value[0], 0);
});

// 删除对话
const deleteChat = (chat: any, index: number) => {
  ElMessageBox.confirm("确定要删除这个对话吗？", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning"
  }).then(() => {
    baseService.delete("/sys/ai/chat/conversation", [chat.id]).then(() => {
      chatSessions.value.splice(index, 1);
      // 如果删除的是当前选中的对话，则切换到第一个对话
      if (currentChatIndex.value === index) {
        if (chatSessions.value.length > 0) {
          switchChat(chatSessions.value[0], 0);
        } else {
          switchChat(null, 0);
        }
      } else if (currentChatIndex.value > index) {
        // 如果删除的是当前选中对话之前的对话，则当前索引减1
        currentChatIndex.value--;
      }
    });
  });
};

// 编辑对话标题
const editChatTitle = (id: number, index: number) => {
  ElMessageBox.prompt("", "修改标题", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    inputValue: chatSessions.value[index].title
  }).then(({ value }) => {
    baseService.put("/sys/ai/chat/conversation", { id, title: value }).then(() => {
      chatSessions.value[index].title = value;
    });
  });
};

// 创建新对话
const createNewChat = () => {
  baseService.post("/sys/ai/chat/conversation", { title: "新对话" }).then((res: any) => {
    chatSessions.value.unshift({
      id: res.data.id,
      modelId: res.data.modelId,
      title: res.data.title
    });
    currentChatIndex.value = 0;

    switchChat(chatSessions.value[0], 0);
  });
};

const emit = defineEmits(["switchChat"]);
// 切换聊天
const switchChat = (chat: any, index: number) => {
  currentChatIndex.value = index;
  emit("switchChat", chat);
};
</script>

<style lang="scss" scoped>
.chat-sidebar {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: var(--el-bg-color);
  border-radius: 3px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);

  .new-chat-btn {
    padding: 16px;

    .el-button {
      width: 100%;
    }
  }

  .search-history {
    padding: 0 16px 16px;
  }

  .sidebar-section {
    margin-bottom: 16px;
    overflow-y: auto;
    height: calc(100vh - 220px);

    .section-title {
      padding: 8px 16px;
      font-size: 14px;
      color: var(--el-text-color-secondary);
      font-weight: 500;
    }

    .chat-history-list {
      .chat-item {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 8px 16px;
        cursor: pointer;
        transition: background-color 0.3s;

        &:hover {
          background-color: var(--el-fill-color-light);
        }

        &.active {
          background-color: var(--el-fill-color);
        }

        .chat-item-content {
          display: flex;
          align-items: center;
          flex: 1;
          overflow: hidden;
        }

        .chat-item-actions {
          display: flex;
          align-items: center;
          opacity: 0;
          transition: opacity 0.3s ease;

          .el-button {
            padding: 2px;
          }
        }

        &:hover .chat-item-actions {
          opacity: 1;
        }

        .chat-avatar {
          margin-right: 12px;
        }

        .chat-title {
          font-size: 14px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
    }
  }
}
</style>
