# 心理咨询学习系统完整实现指南

## 项目概述
本文档详细说明了心理咨询学习系统的完整实现方案，包括咨询师搜索、咨询记录管理、个性化学习包推荐、视频学习、测试评估等功能。

## 已完成的工作

### 1. 数据库设计
✅ 已创建完整的数据库表结构 (`database_schema_learning_system.sql`)
- 咨询记录表 (consultation_records)
- 学习包表 (learning_packages)
- 学习视频表 (learning_videos)
- 用户学习进度表 (user_learning_progress)
- 测试题库表 (test_questions)
- 用户测试记录表 (user_test_records)
- 心理状态评估报告表 (psychological_assessments)
- 用户推荐学习包表 (user_recommended_packages)
- 咨询师资质证书表 (consultant_certificates)
- 咨询师案例表 (consultant_cases)
- 咨询师服务价格表 (consultant_pricing)

### 2. 实体类 (Entity)
✅ 已创建以下实体类：
- ConsultationRecord.java - 咨询记录
- LearningPackage.java - 学习包
- LearningVideo.java - 学习视频
- UserLearningProgress.java - 用户学习进度
- TestQuestion.java - 测试题
- UserTestRecord.java - 用户测试记录
- PsychologicalAssessment.java - 心理评估报告
- UserRecommendedPackage.java - 用户推荐学习包

## 需要创建的文件清单

### 3. Repository层（数据访问层）

需要创建以下Repository接口：

```java
// TongYangYuan-Server/src/main/java/com/tongyangyuan/mentalhealth/repository/

ConsultationRecordRepository.java
LearningPackageRepository.java
LearningVideoRepository.java
UserLearningProgressRepository.java
TestQuestionRepository.java
UserTestRecordRepository.java
PsychologicalAssessmentRepository.java
UserRecommendedPackageRepository.java
ConsultantCertificateRepository.java
ConsultantCaseRepository.java
ConsultantPricingRepository.java
```

### 4. DTO层（数据传输对象）

需要创建以下DTO类：

```java
// TongYangYuan-Server/src/main/java/com/tongyangyuan/mentalhealth/dto/

// 咨询师相关
ConsultantSearchRequest.java - 咨询师搜索请求
ConsultantDetailDTO.java - 咨询师详情响应

// 咨询记录相关
ConsultationRecordDTO.java - 咨询记录DTO
ConsultationRecordRequest.java - 创建咨询记录请求
ConsultationRatingRequest.java - 咨询评价请求

// 学习包相关
LearningPackageDTO.java - 学习包DTO
RecommendedPackageDTO.java - 推荐学习包DTO

// 视频学习相关
LearningVideoDTO.java - 学习视频DTO
VideoProgressRequest.java - 视频进度更新请求
VerificationAnswerRequest.java - 验证题答案请求

// 测试相关
TestQuestionDTO.java - 测试题DTO
TestAnswerRequest.java - 测试答案提交请求
TestResultDTO.java - 测试结果DTO

// 评估相关
AssessmentReportDTO.java - 评估报告DTO
```

### 5. Service层（业务逻辑层）

需要创建以下Service类：

```java
// TongYangYuan-Server/src/main/java/com/tongyangyuan/mentalhealth/service/

ConsultationRecordService.java - 咨询记录服务
LearningPackageService.java - 学习包服务
LearningVideoService.java - 学习视频服务
UserLearningService.java - 用户学习服务
TestService.java - 测试服务
AssessmentService.java - 评估服务
RecommendationService.java - 推荐服务
```

### 6. Controller层（控制器层）

需要创建以下Controller类：

```java
// TongYangYuan-Server/src/main/java/com/tongyangyuan/mentalhealth/controller/

ConsultationRecordController.java - 咨询记录控制器
LearningPackageController.java - 学习包控制器
LearningVideoController.java - 学习视频控制器
UserLearningController.java - 用户学习控制器
TestController.java - 测试控制器
AssessmentController.java - 评估控制器
```

## API接口设计

### 咨询师相关API

```
GET /api/consultants/search - 搜索咨询师
  参数: keyword, specialty, page, size
  
GET /api/consultants/{id}/detail - 获取咨询师详情
  包含: 基本信息、资质证书、案例、评价、服务价格

POST /api/consultants/{id}/certificates - 添加资质证书（管理员）
POST /api/consultants/{id}/cases - 添加案例（咨询师）
POST /api/consultants/{id}/pricing - 设置服务价格（咨询师）
```

### 咨询记录相关API

```
GET /api/consultation-records - 获取咨询记录列表
GET /api/consultation-records/{id} - 获取咨询记录详情
POST /api/consultation-records - 创建咨询记录（咨询师）
PUT /api/consultation-records/{id}/rating - 用户评价咨询
POST /api/consultation-records/{id}/book-again - 再次预约该咨询师
```

### 学习包相关API

```
GET /api/learning/packages - 获取学习包列表
GET /api/learning/packages/{id} - 获取学习包详情
GET /api/learning/recommended - 获取推荐学习包
POST /api/learning/packages/{id}/start - 开始学习
```

### 视频学习相关API

```
GET /api/learning/videos/{id} - 获取视频详情
POST /api/learning/videos/{id}/progress - 更新观看进度
POST /api/learning/videos/{id}/verify - 提交验证题答案
GET /api/learning/progress - 获取学习进度
```

### 测试相关API

```
GET /api/tests/packages/{packageId}/questions - 获取测试题
POST /api/tests/packages/{packageId}/submit - 提交测试答案
GET /api/tests/records - 获取测试记录
GET /api/tests/records/{id} - 获取测试详情
```

### 评估相关API

```
GET /api/assessments - 获取评估报告列表
GET /api/assessments/{id} - 获取评估报告详情
GET /api/assessments/{id}/export - 导出评估报告
```

### 管理后台API

```
POST /api/admin/learning/packages - 创建学习包
PUT /api/admin/learning/packages/{id} - 更新学习包
DELETE /api/admin/learning/packages/{id} - 删除学习包

POST /api/admin/learning/videos - 上传视频
PUT /api/admin/learning/videos/{id} - 更新视频
DELETE /api/admin/learning/videos/{id} - 删除视频

POST /api/admin/tests/questions - 添加测试题
PUT /api/admin/tests/questions/{id} - 更新测试题
DELETE /api/admin/tests/questions/{id} - 删除测试题
POST /api/admin/tests/questions/batch - 批量导入测试题

GET /api/admin/statistics/consultations - 咨询统计
GET /api/admin/statistics/learning - 学习统计
GET /api/admin/statistics/tests - 测试统计
```

## 前端页面设计

### 移动端页面（Android WebView）

需要创建以下HTML页面：

```
TongYangYuan/app/src/main/assets/

consultant_search.html - 咨询师搜索页面
consultant_detail.html - 咨询师详情页面
consultation_records.html - 咨询记录页面
learning_packages.html - 学习包列表页面
learning_package_detail.html - 学习包详情页面
video_player.html - 视频播放页面
test_questions.html - 测试题页面
assessment_report.html - 评估报告页面
learning_records.html - 学习记录页面
```

### Web管理后台页面

需要创建以下HTML页面：

```
TongYangYuan-Web/admin/

consultants.html - 咨询师管理
learning_packages.html - 学习包管理
videos.html - 视频管理
test_questions.html - 题库管理
statistics.html - 数据统计
```

## 核心业务逻辑实现

### 1. 咨询师搜索功能
- 支持按姓名、擅长领域关键词搜索
- 支持筛选条件：评分、服务次数、身份等级
- 分页展示结果

### 2. 咨询记录管理
- 咨询结束后咨询师创建记录
- 标注核心困扰标签
- 用户可评价和再次预约

### 3. 学习包推荐系统
- 基于咨询记录的困扰标签自动匹配
- 生成推荐理由
- 推送到用户的"推荐学习"

### 4. 视频学习功能
- 支持倍速播放、暂停、回放
- 自动记录观看进度
- 断点续学
- 观看完成后弹出验证题

### 5. 测试题系统
- 完成学习包所有视频后解锁
- 支持单选、多选、判断题
- 15分钟答题时间限制
- 实时判分和错题解析

### 6. 心理状态评估
- 基于测试结果生成评估报告
- 包含知识掌握情况、心理状态评估、改善建议
- 支持查看历史报告和截图保存

## 数据库初始化

执行以下步骤初始化数据库：

```bash
# 1. 执行数据库脚本
mysql -u root -p mental_health < TongYangYuan-Server/database_schema_learning_system.sql

# 2. 验证表创建
mysql -u root -p mental_health -e "SHOW TABLES;"
```

## 后续开发步骤

1. ✅ 创建数据库表结构
2. ✅ 创建实体类
3. ⏳ 创建Repository接口
4. ⏳ 创建DTO类
5. ⏳ 创建Service类
6. ⏳ 创建Controller类
7. ⏳ 创建前端页面
8. ⏳ 集成测试
9. ⏳ 部署上线

## 技术栈

### 后端
- Spring Boot 3.x
- Spring Data JPA
- MySQL 8.0
- JWT认证

### 前端
- HTML5 + CSS3 + JavaScript
- Android WebView
- Bootstrap 5

## 注意事项

1. 所有API需要JWT认证
2. 管理员API需要额外的管理员权限验证
3. 视频文件需要配置文件存储服务（如OSS）
4. 测试题答案需要加密存储
5. 评估报告生成需要考虑性能优化
6. 推荐算法需要定期优化

## 测试计划

1. 单元测试：每个Service方法
2. 集成测试：API接口测试
3. 性能测试：并发用户测试
4. 安全测试：权限验证测试

## 部署说明

1. 数据库迁移
2. 后端服务部署
3. 前端资源部署
4. 配置文件更新
5. 监控和日志配置

---

**文档版本**: 1.0
**创建日期**: 2026-02-01
**最后更新**: 2026-02-01
