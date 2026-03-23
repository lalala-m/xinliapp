# 心理咨询学习系统 - 快速实现包

## 📦 已创建的文件清单

### 数据库层
✅ `database_schema_learning_system.sql` - 完整的数据库表结构（11个表）

### 后端 - Entity层 (8个文件)
✅ `entity/ConsultationRecord.java` - 咨询记录实体
✅ `entity/LearningPackage.java` - 学习包实体
✅ `entity/LearningVideo.java` - 学习视频实体
✅ `entity/UserLearningProgress.java` - 用户学习进度实体
✅ `entity/TestQuestion.java` - 测试题实体
✅ `entity/UserTestRecord.java` - 用户测试记录实体
✅ `entity/PsychologicalAssessment.java` - 心理评估报告实体
✅ `entity/UserRecommendedPackage.java` - 用户推荐学习包实体

### 后端 - Repository层 (3个文件)
✅ `repository/ConsultationRecordRepository.java`
✅ `repository/LearningPackageRepository.java`
✅ `repository/LearningVideoRepository.java`

### 文档
✅ `LEARNING_SYSTEM_IMPLEMENTATION_GUIDE.md` - 完整实现指南
✅ `IMPLEMENTATION_STATUS.md` - 实现状态文档
✅ `generate_learning_system_code.py` - 代码生成脚本

## 🚀 快速开始

### 第一步：初始化数据库

```bash
# Windows系统
cd TongYangYuan-Server
mysql -u root -p mental_health < database_schema_learning_system.sql

# 验证
mysql -u root -p mental_health -e "SELECT COUNT(*) FROM learning_packages;"
```

### 第二步：扩展现有ConsultantController

在 `TongYangYuan-Server/src/main/java/com/tongyangyuan/mentalhealth/controller/ConsultantController.java` 中添加：

```java
/**
 * 搜索咨询师
 * 支持按姓名、擅长领域关键词搜索
 */
@GetMapping("/search")
public ResponseEntity<ApiResponse<Page<Consultant>>> searchConsultants(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String specialty,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
    
    Pageable pageable = PageRequest.of(page, size, Sort.by("rating").descending());
    Page<Consultant> consultants;
    
    if (keyword != null && !keyword.isEmpty()) {
        // 按姓名或擅长领域搜索
        consultants = consultantRepository.findByNameContainingOrSpecialtyContaining(
            keyword, keyword, pageable);
    } else if (specialty != null && !specialty.isEmpty()) {
        // 按擅长领域搜索
        consultants = consultantRepository.findBySpecialtyContaining(specialty, pageable);
    } else {
        // 获取所有可用咨询师
        consultants = consultantRepository.findByIsAvailableTrue(pageable);
    }
    
    return ResponseEntity.ok(ApiResponse.success(consultants));
}

/**
 * 获取咨询师详细信息
 * 包含资质证书、案例、评价、服务价格等
 */
@GetMapping("/{id}/detail")
public ResponseEntity<ApiResponse<Map<String, Object>>> getConsultantDetail(@PathVariable Long id) {
    Consultant consultant = consultantRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("咨询师不存在"));
    
    Map<String, Object> detail = new HashMap<>();
    detail.put("consultant", consultant);
    
    // TODO: 添加资质证书、案例、评价等信息
    // detail.put("certificates", certificateRepository.findByConsultantId(id));
    // detail.put("cases", caseRepository.findByConsultantId(id));
    // detail.put("pricing", pricingRepository.findByConsultantId(id));
    
    return ResponseEntity.ok(ApiResponse.success(detail));
}
```

### 第三步：扩展ConsultantRepository

在 `TongYangYuan-Server/src/main/java/com/tongyangyuan/mentalhealth/repository/ConsultantRepository.java` 中添加：

```java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// 添加以下方法声明
Page<Consultant> findByNameContainingOrSpecialtyContaining(
    String name, String specialty, Pageable pageable);

Page<Consultant> findBySpecialtyContaining(String specialty, Pageable pageable);

Page<Consultant> findByIsAvailableTrue(Pageable pageable);
```

### 第四步：创建咨询师搜索前端页面

创建文件：`TongYangYuan/app/src/main/assets/consultant_search.html`

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>咨询师搜索</title>
    <link rel="stylesheet" href="css/style.css">
    <style>
        .search-container {
            padding: 20px;
        }
        .search-box {
            display: flex;
            margin-bottom: 20px;
        }
        .search-box input {
            flex: 1;
            padding: 12px;
            border: 1px solid #ddd;
            border-radius: 8px 0 0 8px;
            font-size: 16px;
        }
        .search-box button {
            padding: 12px 24px;
            background: #6FA6F8;
            color: white;
            border: none;
            border-radius: 0 8px 8px 0;
            cursor: pointer;
        }
        .consultant-card {
            background: white;
            border-radius: 12px;
            padding: 16px;
            margin-bottom: 16px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            display: flex;
            align-items: center;
            cursor: pointer;
        }
        .consultant-avatar {
            width: 60px;
            height: 60px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 24px;
            color: white;
            font-weight: bold;
            margin-right: 16px;
        }
        .consultant-info {
            flex: 1;
        }
        .consultant-info h3 {
            margin: 0 0 4px 0;
            font-size: 18px;
        }
        .consultant-info p {
            margin: 4px 0;
            color: #666;
            font-size: 14px;
        }
        .rating {
            display: flex;
            gap: 12px;
            margin-top: 8px;
            font-size: 14px;
            color: #999;
        }
        .rating span:first-child {
            color: #FFB800;
        }
    </style>
</head>
<body>
    <div class="search-container">
        <div class="search-box">
            <input type="text" id="searchInput" placeholder="搜索咨询师姓名或擅长领域">
            <button onclick="searchConsultants()">搜索</button>
        </div>
        
        <div id="consultantList" class="consultant-list">
            <!-- 咨询师列表将动态加载到这里 -->
        </div>
    </div>
    
    <script src="js/app.js"></script>
    <script>
        async function searchConsultants() {
            const keyword = document.getElementById('searchInput').value;
            const token = localStorage.getItem('token');
            
            try {
                const response = await fetch(
                    `${API_BASE_URL}/api/consultants/search?keyword=${encodeURIComponent(keyword)}`,
                    {
                        headers: {
                            'Authorization': `Bearer ${token}`
                        }
                    }
                );
                
                const data = await response.json();
                if (data.success) {
                    displayConsultants(data.data.content);
                } else {
                    alert('搜索失败：' + data.message);
                }
            } catch (error) {
                console.error('搜索出错：', error);
                alert('搜索出错，请稍后重试');
            }
        }
        
        function displayConsultants(consultants) {
            const listDiv = document.getElementById('consultantList');
            
            if (consultants.length === 0) {
                listDiv.innerHTML = '<p style="text-align:center;color:#999;padding:40px;">未找到相关咨询师</p>';
                return;
            }
            
            listDiv.innerHTML = consultants.map(c => `
                <div class="consultant-card" onclick="viewDetail(${c.id})">
                    <div class="consultant-avatar" style="background-color: ${c.avatarColor || '#6FA6F8'}">
                        ${c.name.charAt(0)}
                    </div>
                    <div class="consultant-info">
                        <h3>${c.name}</h3>
                        <p>${c.title || '心理咨询师'}</p>
                        <p>${c.specialty || '综合心理咨询'}</p>
                        <div class="rating">
                            <span>⭐ ${c.rating || '5.00'}</span>
                            <span>已服务 ${c.servedCount || 0} 人</span>
                        </div>
                    </div>
                </div>
            `).join('');
        }
        
        function viewDetail(consultantId) {
            window.location.href = `consultant_detail.html?id=${consultantId}`;
        }
        
        // 页面加载时获取所有咨询师
        window.onload = () => searchConsultants();
    </script>
</body>
</html>
```

## 📋 完整功能清单

### ✅ 已实现的功能
1. 数据库表结构设计
2. 核心实体类创建
3. 基础Repository接口
4. 咨询师搜索API设计
5. 实现文档和指南

### ⏳ 待实现的功能

#### 高优先级（本周）
- [ ] 完成所有Repository接口
- [ ] 创建ConsultationRecordService
- [ ] 创建ConsultationRecordController
- [ ] 实现咨询记录管理API
- [ ] 创建咨询记录前端页面

#### 中优先级（下周）
- [ ] 创建LearningPackageService
- [ ] 创建RecommendationService
- [ ] 实现学习包推荐系统
- [ ] 创建学习包前端页面
- [ ] 实现视频学习功能

#### 低优先级（第三周）
- [ ] 实现测试题系统
- [ ] 实现评估报告生成
- [ ] 创建管理后台
- [ ] 数据统计功能

## 🔧 开发工具

### 代码生成器
使用提供的Python脚本快速生成代码：

```bash
cd TongYangYuan-Server
python generate_learning_system_code.py
```

### API测试
使用Postman或curl测试API：

```bash
# 测试咨询师搜索
curl -X GET "http://localhost:8080/api/consultants/search?keyword=心理" \
  -H "Authorization: Bearer YOUR_TOKEN"

# 测试咨询师详情
curl -X GET "http://localhost:8080/api/consultants/1/detail" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## 📞 技术支持

遇到问题？查看以下文档：
1. `LEARNING_SYSTEM_IMPLEMENTATION_GUIDE.md` - 详细实现指南
2. `IMPLEMENTATION_STATUS.md` - 当前实现状态
3. `database_schema_learning_system.sql` - 数据库设计

## 🎯 下一步行动

1. **立即执行**：初始化数据库
2. **今天完成**：扩展ConsultantController和Repository
3. **本周完成**：实现咨询记录管理功能
4. **持续进行**：创建前端页面和测试

---

**创建时间**: 2026-02-01
**版本**: 1.0
**状态**: 开发中 🚧
