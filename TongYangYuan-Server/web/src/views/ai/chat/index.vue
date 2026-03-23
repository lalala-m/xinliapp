<template>
  <el-row>
    <el-col :span="5" style="padding-right: 10px">
      <!-- 左侧对话组件 -->
      <Conversation @switch-chat="switchChat" />
    </el-col>
    <el-col :span="19">
      <div class="chat-container">
        <div class="toolbar">
          <el-select v-model="selectedModel" placeholder="选择模型" class="model-select" @change="switchModel">
            <el-option v-for="model in models" :key="model.id" :label="model.name" :value="model.id"> </el-option>
          </el-select>
          <el-button type="danger" plain :disabled="chatHistory.length === 0" @click="clearHistory">
            <el-icon><Delete /></el-icon>&nbsp;清空聊天
          </el-button>
        </div>
        <div ref="messagesContainer" class="chat-messages">
          <template v-if="chatHistory.length === 0">
            <div class="empty-chat">
              <el-icon><ChatDotRound /></el-icon>
              <p>开始一段新的对话吧</p>
            </div>
          </template>
          <template v-else>
            <div v-for="(message, index) in chatHistory" :key="index" :class="['message', message.type === 'user' ? 'user-message' : 'assistant-message']">
              <div class="message-avatar">
                <el-avatar v-if="message.type === 'assistant'" :icon="ChatDotRound" :size="40" />
                <el-avatar v-else :icon="User" :size="40" />
              </div>
              <div class="message-content">
                <div class="message-text" v-html="marked.parse(message.content)"></div>
                <div class="message-time" :style="message.type === 'user' ? '' : 'text-align: left'">
                  {{ message.createDate }}
                </div>
              </div>
            </div>
          </template>
        </div>

        <div class="chat-input">
          <el-input v-model="userInput" type="textarea" :rows="3" placeholder="请输入您的问题..." @keyup.enter="sendMessage"></el-input>
          <div class="input-actions">
            <span class="input-tip">按 Enter 发送</span>
            <el-button type="primary" :disabled="!userInput.trim()" @click="sendMessage"> 发送 </el-button>
          </div>
        </div>
      </div>
    </el-col>
  </el-row>
</template>

<script lang="ts" setup>
import { ref, nextTick } from "vue";
import { Delete, ChatDotRound, User } from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Marked } from "marked";
import hljs from "highlight.js";
import "highlight.js/styles/github.css";
import { markedHighlight } from "marked-highlight";
import Conversation from "../conversation/index.vue";
import baseService from "@/service/baseService";
import app from "@/constants/app";
import { EventSourceMessage, fetchEventSource } from "@microsoft/fetch-event-source";
import { getToken } from "@/utils/cache";

const marked = new Marked(
  markedHighlight({
    emptyLangClass: "hljs",
    langPrefix: "hljs language-",
    highlight(code, lang) {
      const language = hljs.getLanguage(lang) ? lang : "plaintext";
      return hljs.highlight(code, { language }).value;
    }
  })
);

const models = ref<any[]>([]);
const selectedModel = ref();
const userInput = ref("");
const chatHistory = ref<any[]>([]);
const messagesContainer = ref<HTMLElement | null>(null);
const abortController = ref<any>();
const conversationId = ref();

// 切换聊天
const switchChat = async (chat: any) => {
  // 初始化模型
  await initModels();
  if (!chat) {
    conversationId.value = null;
    chatHistory.value = [];
    return;
  }

  // 判断models里面，是否存在chat.modelId
  const model = models.value.find((item: any) => item.id === chat.modelId);
  if (model) {
    selectedModel.value = chat.modelId;
  } else {
    selectedModel.value = null;
  }

  conversationId.value = chat.id;
  baseService.get("/sys/ai/chat/message/list?conversationId=" + conversationId.value).then((res: any) => {
    chatHistory.value = res.data;
    scrollToBottom();
  });
};

// 初始化模型
const initModels = async () => {
  if (models.value.length > 0) {
    return;
  }

  const { data } = await baseService.get("/sys/ai/model/list");
  models.value = data;
};

// 切换模型
const switchModel = (modelId: number) => {
  baseService.put("/sys/ai/chat/conversation", { id: conversationId.value, modelId: modelId }).then(() => {
    ElMessage.success("切换模型成功");
  });
};

// 发送消息
const sendMessage = async () => {
  const content = userInput.value.trim();
  if (!content) {
    return;
  }

  if (!conversationId.value) {
    ElMessage.error("请新建对话");
    return;
  }

  if (!selectedModel.value) {
    ElMessage.error("请选择模型");
    return;
  }

  abortController.value = new AbortController();

  // 添加用户消息到历史
  const userMessage = {
    type: "user",
    content: content,
    createDate: nowTime()
  };
  chatHistory.value.push(userMessage);
  userInput.value = "";

  // 添加一个空的助手消息，用于流式显示回复
  const assistantMessage = ref<any>({
    type: "assistant",
    content: "",
    createDate: nowTime()
  });
  chatHistory.value.push(assistantMessage.value);

  await sendChatMessage(
    conversationId.value,
    selectedModel.value,
    content,
    abortController.value,
    (event: any) => {
      const data = JSON.parse(event.data);
      if (data.content) {
        assistantMessage.value.content += data.content;
      }
    },
    (error: any) => {
      console.error("发送消息错误:", error);
      stopStream();
    },
    () => {
      stopStream();
    }
  );
};

const sendChatMessage = async (conversationId: number, modelId: number, content: string, ctrl: any, onMessage: (event: EventSourceMessage) => void, onError: (error: Error) => void, onClose: () => void) => {
  const apiUrl = app.api + "/sys/ai/chat/message";
  return await fetchEventSource(apiUrl, {
    method: "POST",
    headers: {
      "Content-Type": "application/json;charset=UTF-8",
      token: getToken()
    },
    body: JSON.stringify({
      conversationId: conversationId,
      modelId: modelId,
      content: content
    }),
    openWhenHidden: true,
    onmessage: onMessage,
    onerror: onError,
    onclose: onClose,
    signal: ctrl.signal
  });
};

const stopStream = async () => {
  if (abortController.value) {
    abortController.value.abort();
  }
};

// 清空历史
const clearHistory = () => {
  if (!conversationId.value) {
    ElMessage.error("对话不存在");
    return;
  }

  ElMessageBox.confirm("确定要清空聊天记录？", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning"
  }).then(() => {
    baseService.delete("/sys/ai/chat/message/list?conversationId=" + conversationId.value, {}).then(() => {
      chatHistory.value = [];
      ElMessage.success("聊天历史已清空");
    });
  });
};

// 当前时间
const nowTime = () => {
  const date = new Date();
  return `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()} ${date.getHours()}:${date.getMinutes()}:${date.getSeconds()}`;
};

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
    }
  });
};
</script>

<style lang="scss" scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 140px);
  background-color: var(--el-bg-color);
  border-radius: 3px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.toolbar {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  border-bottom: 1px solid var(--el-border-color-light);
  background-color: var(--el-bg-color-overlay);

  .model-select {
    width: 150px;
    margin-right: 16px;
  }

  .model-option {
    display: flex;
    flex-direction: column;

    .model-name {
      font-weight: bold;
    }

    .model-desc {
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }
  }
}

.chat-messages {
  flex: 1;
  padding: 20px;
  overflow-y: auto;

  .empty-chat {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    color: var(--el-text-color-secondary);

    .el-icon {
      font-size: 48px;
      margin-bottom: 16px;
    }
  }

  :deep(pre) {
    background-color: #f8f9fa;
    border-radius: 4px;
    padding: 12px;
    margin: 8px 0;
    overflow-x: auto;
  }

  :deep(code) {
    font-family: Monaco, Consolas, Courier New, monospace;
    background-color: #f8f9fa;
    border-radius: 4px;
    padding: 2px 4px;
  }

  :deep(p) {
    margin: 8px 0;
  }

  :deep(ul),
  :deep(ol) {
    padding-left: 20px;
    margin: 8px 0;
  }

  :deep(blockquote) {
    border-left: 4px solid #dcdfe6;
    padding-left: 12px;
    margin: 8px 0;
    color: #606266;
  }

  .message {
    display: flex;
    margin-bottom: 20px;

    .message-avatar {
      flex-shrink: 0;
    }

    .message-content {
      flex: 1;
      padding: 12px 16px;
      border-radius: 8px;
      position: relative;

      .message-time {
        font-size: 12px;
        color: var(--el-text-color-secondary);
        margin-top: 4px;
        text-align: right;
      }
    }

    &.user-message {
      flex-direction: row-reverse;

      .message-avatar {
        margin-left: 12px;
        margin-right: 0;
      }

      .message-content {
        text-align: right;
      }
    }

    &.assistant-message {
      .message-avatar {
        margin-right: 12px;
      }

      .message-content {
        background-color: var(--el-color-primary-light-9);
      }
    }
  }
}

.chat-input {
  padding: 16px 20px;
  border-top: 1px solid var(--el-border-color-light);
  background-color: var(--el-bg-color-overlay);

  .input-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 8px;

    .input-tip {
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }
  }
}
</style>
