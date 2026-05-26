package com.example.tongyangyuan.payment.config;

/**
 * 支付宝支付配置
 * 
 * 重要安全提示：
 * 1. 此文件包含敏感信息，请勿提交到公共代码仓库
 * 2. 生产环境建议将私钥存储在服务端，通过接口获取
 * 3. 商户私钥（APP_PRIVATE_KEY）需要您自己生成并保管
 * 
 * 配置说明：
 * - APP_ID: 支付宝开放平台审核通过后提供的应用ID
 * - APP_PRIVATE_KEY: 您的应用私钥（与上传的公钥配对）
 * - ALIPAY_PUBLIC_KEY: 支付宝公钥（从支付宝开放平台获取）
 */
public class AlipayConfig {
    
    // ==================== 基础配置 ====================
    
    /**
     * 应用ID
     * 从支付宝开放平台获取，审核通过后可见
     * 格式：2024XXXXXXXXXXXX
     */
    public static final String APP_ID = "";
    
    /**
     * 商户私钥
     * 您自己生成的RSA私钥，与上传到支付宝的公钥配对
     * 请妥善保管，不要泄露
     */
    public static final String APP_PRIVATE_KEY = 
        "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCeScUZzB/RPDVQ" +
        "MI3+KDOuQwiVsJ5/W7QzAfiXZJHGoQLoqlJZ1ZwvK2jNmsz+fbNqxWf9toh3pT/j" +
        "O4BkE2+r+UPH/Xt/2X3x9Y5guXkbvkNMYZ48DNApLfhZBlebeATK7/9ABdYX0v91" +
        "FcgJWOcPffFouX7btLgAbmpJcPbgeB10fZiTco+Ga3ZthWTPlvbfzTev85gWgdFF" +
        "91BzdA924AJB66PfCRewpkG5cSgv5k6l9PDYR345rsX7eom+1fH2uDNmScK97Rdq" +
        "MyUnoXgTO6lE5Rg4p+I4c3ZcWEskzuja4ngJv7HtVjFI/fyTRM3FTzPBioCLDZVW" +
        "007yLwhlAgMBAAECggEAVIz1Ms19ZWey1q3I+y0ARiJacJMSZtdWTzTLVBsNXztc" +
        "cWmhaYkXz35xkfoRbBM7BlqCLN0W0ko78SY0c8CKEyOn2CkHkghcJSXUssf/8BjW" +
        "m/M88biqbIS4tt0TaOFYNgqynsE/ViSH/kPvQUbyzaypihYjtDo5W0mfjrE/vjCz" +
        "LrX9wBdyBm6PHuYyZ/xvdj9OzJwQ6Qr2QTM9CRLTZsWFe3KRAlkDPTp+XDrU0nnp" +
        "U8P50Pv2rMCnSmA0X7ycazs0AonbSfxKnFUqkJCNa083+hPFPzjxFi41aualO1c8" +
        "k8g3dUsqthxOhHG0qNqm9oKHW2iNmjLouEHyUlwFgQKBgQDb3ibhBQII7BjPC3UG" +
        "CMxE69Ewr5+izxrEnT8QHLZkLLMONLoLIABA0koZ7DjnlShK6yyjWXY46bSTNpGr" +
        "QSX/SQfYr4RYE8n09h3o/9mZGP5byMou1c/1N3giR6tDtD1TZc80otRYStEj4oan" +
        "rMqT6m3D06T7nSutC6C6+2UpRQKBgQC4TPXp7w/9BTAtWcL2NYnP/xgm8b1nvQmc" +
        "gEiVMc4J/maTlpvP5JIy+Mpc27KeEUF8Zp/OrxsLhXBhat7cFAKCM3rQvquEEgxX" +
        "7XScDOe/apbJoAcOExiCqO0HChRDYg5+p+LhQL3ogOFwHi1gVV3nnwHGhzfGGHnU" +
        "9viNxm8EoQKBgEeWCx6UnNb+2A0Bc/alAq0mApRRei5uNA70C8ZZMuFm1F25uSFK" +
        "KAry7QGHsfdkzxuleoEs7ZsWLJ8J01/o9FAsHQvmCbcLqmPBhneIZfa89oQ32exp" +
        "3S3AA7NVlmOS2ay/pzlCEtU0dueyJ5mMXuDOITLzQ/JQeBn9gTWgCI3pAoGBAKGf" +
        "lFUHumy4nQxYWHpUbzwByKt2ehS8me4uLfjwyXDIMiqh/JhvKasbtVODs3udRGGV" +
        "GfZGZ6BN8ETuVOyJe7206bKgU4ep0DtX2Vh0lNOv5PaCqU01f9wtylfPBgAlwPJf" +
        "xr6laSW5yHnrbP27P3O9ZPA2aroFguo5f7XNXFShAoGAMjx2+bGstDHsgs/gwNuE" +
        "5wKNbLKR0UbqBuSg/UAw/0YrLJa3QdD4b8oFOM7zsOxBUfrY1qevyooPSp/a2xeg" +
        "1/JAN+3novFuzUn3P5z0NPiweEfsdprwFKAHpejGNlJb+hkZciddlTdxzufHGVat" +
        "9oUbT0KjDUPnDSESTpeyCIk=";
    
    /**
     * 支付宝公钥
     * 从支付宝开放平台获取（上传您的公钥后，平台会提供支付宝公钥）
     */
    public static final String ALIPAY_PUBLIC_KEY = 
        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAls7XpAe+yzo7XhN6E1lz" +
        "eUwvEdMIYRIiNVPeIh+JjO5l21Q3lDgg3iNnpr/ZNe0nx/TMoL6ya0t6FWq5W50z" +
        "QEBHukWcNXbdK/37zqvuONBAzs3CD0DC1Tt5itCVWSKpy25vBkn6XLnuQ991q1xm" +
        "OOgr0jM5XQ0kd6M8mlTGwxHVz+QsLZn4xqllkder4JhhO1/T/0EI73khbebWtAlQ" +
        "Lna+mioXktX/LknQIYgdQZNMUZGWsjpZT+5Rr+Hfscl2RwgQv+fCIP9pHjERFARX" +
        "syA/seW8peIiq2iAFNEmClelAIpDKQ00eNyu1yE/Xu++yl04GTkGY8xhXQbxs3Y7" +
        "zQIDAQAB";
    
    // ==================== 应用公钥（已提供） ====================
    
    /**
     * 应用公钥
     * 已上传到支付宝开放平台
     */
    public static final String APP_PUBLIC_KEY = 
        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnknFGcwf0Tw1UDCN/igz" +
        "rkMIlbCef1u0MwH4l2SRxqEC6KpSWdWcLytozZrM/n2zasVn/baId6U/4zuAZBNv" +
        "q/lDx/17f9l98fWOYLl5G75DTGGePAzQKS34WQZXm3gEyu//QAXWF9L/dRXICVjn" +
        "D33xaLl+27S4AG5qSXD24HgddH2Yk3KPhmt2bYVkz5b23803r/OYFoHRRfdQc3QP" +
        "duACQeuj3wkXsKZBuXEoL+ZOpfTw2Ed+Oa7F+3qJvtXx9rgzZknCve0XajMlJ6F4" +
        "EzupROUYOKfiOHN2XFhLJM7o2uJ4Cb+x7VYxSP38k0TNxU8zwYqAiw2VVtNO8i8I" +
        "ZQIDAQAB";
    
    // ==================== 环境配置 ====================
    
    /**
     * 是否使用沙箱环境
     * true: 沙箱环境（测试）
     * false: 正式环境（生产）
     */
    public static final boolean USE_SANDBOX = true;
    
    /**
     * 支付宝网关地址
     */
    public static final String ALIPAY_GATEWAY_URL = USE_SANDBOX 
        ? "https://openapi.alipaydev.com/gateway.do"  // 沙箱
        : "https://openapi.alipay.com/gateway.do";     // 正式
    
    /**
     * 字符编码
     */
    public static final String CHARSET = "UTF-8";
    
    /**
     * 签名算法
     */
    public static final String SIGN_TYPE = "RSA2";
    
    /**
     * 数据格式
     */
    public static final String FORMAT = "JSON";
    
    // ==================== 回调配置 ====================
    
    /**
     * 异步通知地址
     * 支付宝服务器主动通知商户服务器里指定的页面http/https路径
     * 需要外网可访问
     */
    public static final String NOTIFY_URL = "https://your-domain.com/api/payment/alipay/notify";
    
    /**
     * 同步通知地址
     * 支付完成后跳转的页面
     */
    public static final String RETURN_URL = "https://your-domain.com/payment/success";
    
    // ==================== 验证方法 ====================
    
    /**
     * 检查配置是否完整
     */
    public static boolean isConfigValid() {
        return !APP_ID.isEmpty() 
            && !APP_PRIVATE_KEY.isEmpty() 
            && !ALIPAY_PUBLIC_KEY.isEmpty();
    }
    
    /**
     * 获取配置缺失项
     */
    public static String getMissingConfig() {
        StringBuilder missing = new StringBuilder();
        if (APP_ID.isEmpty()) missing.append("APP_ID ");
        if (APP_PRIVATE_KEY.isEmpty()) missing.append("APP_PRIVATE_KEY ");
        if (ALIPAY_PUBLIC_KEY.isEmpty()) missing.append("ALIPAY_PUBLIC_KEY ");
        return missing.toString().trim();
    }
}
