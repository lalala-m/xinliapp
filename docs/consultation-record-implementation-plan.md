# 咨询记录与电子签名PDF生成功能 - 实施方案

## 任务概述
当用户点击"结束咨询并评价"时，需要：
1. 生成包含完整聊天记录的文档（带时间戳）
2. 要求用户进行电子签名
3. 将签名与咨询内容合成PDF保存
4. 三端（管理后台、咨询师端、用户端）都能查看聊天记录凭证

## 详细实施计划

### 阶段一：后端API扩展
1. **数据库表扩展**
   - [ ] 1.1 扩展 consultation_records 表（添加字段）
   - [ ] 1.2 扩展 consultation_signatures 表（添加字段）
   - [ ] 1.3 创建 consultation_chat_export 表（存储导出的聊天记录）

2. **新增后端服务类**
   - [ ] 2.1 ConsultationPdfService - PDF生成服务
   - [ ] 2.2 ConsultationChatExportService - 聊天记录导出服务

3. **新增Controller端点**
   - [ ] 3.1 POST /api/consultation-records/{id}/export-chat - 导出聊天记录
   - [ ] 3.2 POST /api/consultation-records/{id}/generate-pdf - 生成PDF
   - [ ] 3.3 GET /api/consultation-records/{id}/pdf - 下载PDF
   - [ ] 3.4 POST /api/consultation-records/{id}/signature-with-pdf - 签名并生成PDF

### 阶段二：咨询师端Web功能
4. **chat.html 页面增强**
   - [ ] 4.1 添加"结束咨询"按钮（已存在）
   - [ ] 4.2 添加咨询总结表单
   - [ ] 4.3 添加电子签名画布
   - [ ] 4.4 聊天记录预览功能

5. **PDF预览与下载**
   - [ ] 5.1 预览生成的PDF
   - [ ] 5.2 下载PDF功能

### 阶段三：家长端App功能
6. **手机端chat.html 增强**
   - [ ] 6.1 添加签名确认功能
   - [ ] 6.2 添加签名画布
   - [ ] 6.3 PDF预览与签名
   - [ ] 6.4 查看历史咨询记录凭证

7. **Android端Java代码**
   - [ ] 7.1 添加PDF查看Activity
   - [ ] 7.2 添加签名捕获功能
   - [ ] 7.3 添加WebAppInterface新方法

### 阶段四：管理后台功能
8. **admin后台扩展**
   - [ ] 8.1 添加咨询记录查看页面
   - [ ] 8.2 添加聊天记录查看功能
   - [ ] 8.3 添加PDF预览与下载

### 关键技术实现

#### PDF生成策略
- 使用 iTextPDF 或 OpenPDF 库
- PDF包含：
  - 咨询基本信息（时间、咨询师、家长、儿童信息）
  - 完整聊天记录（带时间戳）
  - 电子签名图片
  - 签署时间戳

#### 电子签名实现
- 使用Canvas绘制签名
- 将签名转换为Base64或PNG图片
- 签名与PDF合并

### 文件清单

**新增文件：**
1. `TongYangYuan-Server/src/main/java/.../dto/ChatExportDTO.java`
2. `TongYangYuan-Server/src/main/java/.../service/ConsultationPdfService.java`
3. `TongYangYuan-Server/src/main/java/.../service/ConsultationChatExportService.java`
4. `TongYangYuan-Server/src/main/java/.../controller/ConsultationPdfController.java`
5. `TongYangYuan-Server/database/migration_v3_consultation_pdf.sql`

**修改文件：**
1. `TongYangYuan-Server/pom.xml` - 添加PDF依赖
2. `TongYangYuan-Server/src/main/java/.../entity/ConsultationRecord.java`
3. `TongYangYuan-Server/src/main/java/.../entity/ConsultationSignature.java`
4. `TongYangYuan-Web/chat.html` - 添加签名功能
5. `TongYangYuan/app/src/main/assets/chat.html` - 添加签名功能
6. `TongYangYuan-Server/src/main/java/.../config/SecurityConfig.java`

### 测试清单
- [ ] 咨询师结束咨询并提交总结
- [ ] 生成完整聊天记录PDF
- [ ] 电子签名功能正常
- [ ] PDF预览功能正常
- [ ] 家长端签名确认功能
- [ ] 管理后台查看记录功能
- [ ] 三端数据一致性测试
