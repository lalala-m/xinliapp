# 支付宝支付配置说明

## 当前配置状态

### ✅ 已完成
- [x] 应用签名密钥生成
- [x] 应用公钥上传到支付宝开放平台
- [x] 支付宝公钥已配置
- [x] 支付工具类已创建
- [x] 支付Activity已创建

### ⏳ 等待审核通过后填写
- [ ] APP_ID（应用ID）
- [ ] APP_PRIVATE_KEY（应用私钥）

---

## 配置步骤

### 1. 获取 APP_ID

支付宝开放平台审核通过后：
1. 登录 [支付宝开放平台](https://open.alipay.com/)
2. 进入「控制台」→「应用」
3. 找到您的应用，复制 **APPID**
4. 格式示例：`2024XXXXXXXXXXXX`

### 2. 获取/确认应用私钥

您之前生成了密钥对，需要找到配对的**私钥**。

私钥格式示例：
```
-----BEGIN RSA PRIVATE KEY-----
MIIEpAIBAAKCAQEAxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
...
-----END RSA PRIVATE KEY-----
```

**重要**：私钥必须与您上传到支付宝的公钥配对！

### 3. 填写配置

打开文件：`AlipayConfig.java`

找到以下位置并填写：

```java
public static final String APP_ID = "2024XXXXXXXXXXXX";  // 您的AppID

public static final String APP_PRIVATE_KEY = 
    "-----BEGIN RSA PRIVATE KEY-----\n" +
    "MIIEpAIBAAKCAQEA..." +  // 您的私钥
    "..." +
    "-----END RSA PRIVATE KEY-----";
```

### 4. 切换正式环境（上线前）

```java
public static final boolean USE_SANDBOX = false;  // 改为false
```

---

## 文件清单

| 文件 | 说明 |
|------|------|
| `AlipayConfig.java` | 支付配置（需填写APP_ID和私钥） |
| `AlipayUtil.java` | 支付工具类（签名、订单构建） |
| `AlipayResult.java` | 支付结果解析 |
| `AliPayActivity.java` | 支付页面Activity |

---

## 测试支付

当前配置为**沙箱模式**（`USE_SANDBOX = true`）：
- 不会调用真实支付宝
- 模拟支付流程
- 用于功能测试

---

## 常见问题

### Q: 私钥丢失了怎么办？
A: 需要重新生成密钥对，然后：
1. 重新生成RSA密钥对
2. 上传新的公钥到支付宝开放平台
3. 更新 `AlipayConfig.java` 中的私钥

### Q: 可以修改证书持有者信息吗？
A: 不可以。需要重新生成密钥。

### Q: 公钥模式和证书模式有什么区别？
A: 
- **公钥模式**：简单，只需配置公钥和私钥 ✅ 当前使用
- **证书模式**：更安全，需要上传证书文件，适合企业级应用

---

## 安全提醒

⚠️ **请勿将私钥提交到代码仓库！**

建议：
1. 将私钥存储在服务端
2. APP通过接口获取签名后的订单信息
3. 或者使用 `.gitignore` 忽略配置文件

---

## 联系方式

如有问题，请联系技术支持。
