---
name: code-review
description: |
  执行全面的代码审查，遵循童康源项目工程实践。用于审查Pull Request、检查代码变更、
  提供代码质量反馈。覆盖安全性、性能、测试、设计审查，以及Android、Spring Boot、
  Web前端多技术栈的专项审查。
  
  适用场景：
  - 审查PR代码变更
  - 检查新功能实现
  - 重构代码评审
  - 安全漏洞扫描
  - 性能优化建议
  - 代码规范检查
invoke_when:
  - 用户要求"审查代码"
  - 用户要求"code review"
  - 用户要求"检查代码质量"
  - 用户提到"PR review"
  - 用户要求"找bug"
  - 用户要求"安全检查"
---

# 童康源代码审查规范

## 审查流程

### Phase 1: 上下文收集

审查前必须了解以下信息：

1. **变更范围**：读取 `git diff` 或 PR 变更文件列表
2. **关联需求**：检查是否有对应的 Issue 或需求文档
3. **技术栈**：确认涉及的技术（Android/Java/Spring Boot/Web/数据库）
4. **影响面**：评估变更对现有功能的影响

### Phase 2: 系统性检查

按照以下维度逐项检查：

#### 1. 功能正确性
- [ ] 业务逻辑是否符合需求
- [ ] 边界条件是否处理（null、空值、越界）
- [ ] 异常流程是否覆盖
- [ ] 并发场景是否安全

#### 2. 代码质量
- [ ] 命名是否清晰（变量、方法、类）
- [ ] 函数是否单一职责（不超过50行）
- [ ] 重复代码是否提取
- [ ] 注释是否必要且准确
- [ ] 魔法数字是否有常量定义

#### 3. 安全性
- [ ] SQL注入风险（使用参数化查询）
- [ ] XSS防护（输出编码）
- [ ] 敏感信息泄露（密码、密钥、token）
- [ ] 权限校验（接口是否有身份验证）
- [ ] 文件上传校验（类型、大小）

#### 4. 性能
- [ ] N+1查询问题
- [ ] 大数据量内存占用
- [ ] 循环内数据库/网络调用
- [ ] 缓存使用合理性
- [ ] 资源泄漏（连接、流）

#### 5. 测试
- [ ] 单元测试覆盖核心逻辑
- [ ] 边界条件测试
- [ ] 异常路径测试
- [ ] 集成测试（API契约）

#### 6. Android专项
- [ ] 主线程阻塞操作
- [ ] 内存泄漏（Activity/Fragment引用）
- [ ] 权限申请合规
- [ ] WebView安全配置
- [ ] 本地数据加密

#### 7. Spring Boot专项
- [ ] 事务边界正确
- [ ] 懒加载问题（@Transactional）
- [ ] 配置注入安全（@Value）
- [ ] API版本兼容性
- [ ] 日志敏感信息

#### 8. 数据库
- [ ] 索引合理性
- [ ] 迁移脚本幂等性
- [ ] 字段类型选择
- [ ] 关联查询优化

### Phase 3: 反馈输出

#### 严重级别定义

| 级别 | 定义 | 处理方式 |
|------|------|----------|
| **BLOCKER** | 功能错误、安全漏洞、数据丢失风险 | 必须修复才能合并 |
| **CRITICAL** | 性能严重问题、内存泄漏、并发bug | 必须修复 |
| **MAJOR** | 代码质量问题、设计缺陷、测试缺失 | 建议修复 |
| **MINOR** | 命名不规范、冗余代码、格式问题 | 可选修复 |
| **INFO** | 建议、优化思路、最佳实践 | 参考 |

#### 输出格式

```markdown
## 代码审查报告

### 概览
- **审查文件**: X个
- **问题总数**: X个（BLOCKER: X, CRITICAL: X, MAJOR: X, MINOR: X）
- **整体评价**: ✅ 通过 / ⚠️ 有条件通过 / ❌ 需要修改

### 详细问题

#### [BLOCKER] 问题标题
- **文件**: `path/to/file.java:42`
- **问题**: 具体描述
- **建议**: 如何修复
- **代码示例**:
  ```java
  // 修复前
  badCode();
  
  // 修复后
  goodCode();
  ```

### 正面反馈
- 代码结构清晰，职责分离良好
- 异常处理完善
- ...

### 建议
- 考虑使用XX设计模式优化
- 建议补充XX测试场景
```

## 审查原则

### DO
- 提供可操作的改进建议
- 解释"为什么"而不仅是"是什么"
- 认可好的代码实践
- 区分个人偏好和客观问题
- 考虑维护性和可读性

### DON'T
- 只批评不建设
- 纠结于纯风格问题（交给linter）
- 忽略上下文提不切实际的要求
- 对实验性代码过度苛刻
- 忽视测试代码质量

## 技术栈特定检查清单

### Android (Java/Kotlin)

```
□ Activity/Fragment生命周期处理
□ 异步操作使用线程池/RxJava/Coroutines
□ Bitmap内存管理
□ SharedPreferences加密敏感数据
□ 权限申请结果处理
□ ProGuard/R8混淆规则
□ 适配不同屏幕密度
```

### Spring Boot (Java)

```
□ RESTful API设计规范
□ 请求参数校验（@Valid）
□ 统一异常处理（@ControllerAdvice）
□ 响应包装（统一code/msg/data）
□ Swagger文档更新
□ 数据库连接池配置
□ 健康检查端点
```

### Web前端 (HTML/JS/CSS)

```
□ XSS防护（不直接使用innerHTML）
□ CSRF Token传递
□ 输入校验（前端+后端双重校验）
□ 响应式布局
□ 资源加载优化
□ 错误边界处理
```

### 数据库 (MySQL)

```
□ 索引设计（EXPLAIN验证）
□ 大表分页优化
□ 事务粒度控制
□ 乐观锁/悲观锁选择
□ 软删除实现
□ 字符集统一（utf8mb4）
```

## 示例审查

### 示例1: SQL注入风险

**问题代码**:
```java
// ❌ 危险！
String sql = "SELECT * FROM users WHERE phone = '" + phone + "'";
jdbcTemplate.query(sql, ...);
```

**修复建议**:
```java
// ✅ 安全
String sql = "SELECT * FROM users WHERE phone = ?";
jdbcTemplate.query(sql, new Object[]{phone}, ...);
```

### 示例2: 主线程阻塞

**问题代码**:
```java
// ❌ 阻塞主线程
public void loadData() {
    List<Data> data = api.fetchData(); // 同步网络请求
    updateUI(data);
}
```

**修复建议**:
```java
// ✅ 异步处理
public void loadData() {
    new Thread(() -> {
        List<Data> data = api.fetchData();
        runOnUiThread(() -> updateUI(data));
    }).start();
}
```

### 示例3: N+1查询

**问题代码**:
```java
// ❌ N+1问题
List<Order> orders = orderRepository.findAll();
for (Order order : orders) {
    User user = userRepository.findById(order.getUserId()); // 每次循环查数据库
}
```

**修复建议**:
```java
// ✅ JOIN FETCH
@Query("SELECT o FROM Order o JOIN FETCH o.user")
List<Order> findAllWithUser();
```

## 审查后动作

1. **确认修复**：作者修复后重新审查相关代码
2. **知识分享**：将典型问题记录到项目文档
3. **规则更新**：根据新问题更新审查清单
4. **工具改进**：将常见问题配置到静态检查工具

## 参考文档

- 项目 `AGENTS.md` - 项目特定规范
- 项目 `README.md` - 架构说明
- Google Java Style Guide
- Android Performance Patterns
- OWASP Top 10
