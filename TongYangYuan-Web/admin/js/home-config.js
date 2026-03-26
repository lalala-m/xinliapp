// 首页配置管理
let banners = [];
let adCard = {};
let adImageBase64 = '';

// 页面加载
document.addEventListener('DOMContentLoaded', () => {
    loadConfig();
});

// 加载配置
async function loadConfig() {
    try {
        const response = await AdminCommon.request('/home/config');
        if (response && response.code === 200) {
            banners = response.data.banners || [];
            adCard = response.data.adCard || {};
            
            renderBanners();
            renderAdCardForm();
        } else {
            // 使用默认配置
            initDefaultConfig();
            renderBanners();
            renderAdCardForm();
        }
    } catch (error) {
        console.error('加载配置失败:', error);
        initDefaultConfig();
        renderBanners();
        renderAdCardForm();
    }
}

// 初始化默认配置
function initDefaultConfig() {
    if (banners.length === 0) {
        banners = [
            { image: '', link: '', title: '💎 会员限时优惠', subtitle: '年度会员8折优惠，享受全年无限次咨询' },
            { image: '', link: '', title: '👨‍⚕️ 专业导师团队', subtitle: '30+认证咨询师全程陪伴，定制专属方案' },
            { image: '', link: '', title: '🎁 首单优惠40%', subtitle: '轻松打开心灵窗户，让爱陪伴成长每一步' }
        ];
    }
    
    if (!adCard.title) {
        adCard = {
            image: '',
            title: '首单优惠 40%',
            subtitle: '轻松打开心灵窗户',
            link: '',
            buttonText: '点击咨询'
        };
    }
}

// 渲染轮播图列表
function renderBanners() {
    const list = document.getElementById('bannerList');
    const addBtn = document.getElementById('addBannerBtn');
    
    // 最多5张
    addBtn.style.display = banners.length >= 5 ? 'none' : 'inline-block';
    
    list.innerHTML = banners.map((banner, index) => `
        <div class="banner-item" data-index="${index}">
            <div class="banner-preview" onclick="triggerBannerImageUpload(${index})">
                ${banner.image ? 
                    `<img src="${banner.image}" alt="预览图">` : 
                    `<div class="banner-preview-text">
                        <h4>${banner.title || '标题'}</h4>
                        <p>${banner.subtitle || '副标题'}</p>
                    </div>`
                }
                <input type="file" id="bannerImage_${index}" accept="image/*" style="display: none;" 
                       onchange="handleBannerImageSelect(event, ${index})">
            </div>
            <div class="banner-form">
                <div class="form-row">
                    <div class="form-group">
                        <label>标题 <span>*</span></label>
                        <input type="text" class="form-control" value="${escapeHtml(banner.title || '')}" 
                               onchange="updateBanner(${index}, 'title', this.value)" placeholder="例如：会员限时优惠">
                    </div>
                    <div class="form-group">
                        <label>跳转链接（可选）</label>
                        <input type="text" class="form-control" value="${escapeHtml(banner.link || '')}" 
                               onchange="updateBanner(${index}, 'link', this.value)" placeholder="点击后跳转的链接">
                    </div>
                </div>
                <div class="form-group">
                    <label>副标题</label>
                    <input type="text" class="form-control" value="${escapeHtml(banner.subtitle || '')}" 
                           onchange="updateBanner(${index}, 'subtitle', this.value)" placeholder="简短描述">
                </div>
                <div class="banner-actions">
                    <button type="button" class="btn-secondary" onclick="uploadBannerImage(${index})">
                        ${banner.image ? '更换图片' : '上传图片'}
                    </button>
                    <button type="button" class="btn-warning" onclick="clearBannerImage(${index})">
                        清除图片
                    </button>
                    <button type="button" class="btn-danger" onclick="deleteBanner(${index})" 
                            ${banners.length <= 1 ? 'disabled style="opacity:0.5"' : ''}>
                        删除
                    </button>
                </div>
            </div>
        </div>
    `).join('');
}

// HTML转义
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// 更新轮播图数据
function updateBanner(index, field, value) {
    if (banners[index]) {
        banners[index][field] = value;
        if (field === 'title' || field === 'subtitle') {
            renderBanners(); // 重新渲染预览
        }
    }
}

// 触发轮播图图片上传
function triggerBannerImageUpload(index) {
    const input = document.getElementById(`bannerImage_${index}`);
    if (input) input.click();
}

// 处理轮播图图片选择
function handleBannerImageSelect(event, index) {
    const file = event.target.files[0];
    if (!file) return;
    
    if (file.size > 2 * 1024 * 1024) {
        AdminCommon.showError('图片大小不能超过 2MB');
        return;
    }
    
    const reader = new FileReader();
    reader.onload = function(e) {
        banners[index].image = e.target.result; // Base64
        renderBanners();
        AdminCommon.showSuccess('图片已上传');
    };
    reader.readAsDataURL(file);
}

// 上传轮播图图片
function uploadBannerImage(index) {
    const input = document.getElementById(`bannerImage_${index}`);
    if (input) input.click();
}

// 清除轮播图图片
function clearBannerImage(index) {
    banners[index].image = '';
    renderBanners();
}

// 删除轮播图
function deleteBanner(index) {
    if (banners.length <= 1) {
        AdminCommon.showError('至少保留一张轮播图');
        return;
    }
    
    if (confirm('确定要删除这张轮播图吗？')) {
        banners.splice(index, 1);
        renderBanners();
        AdminCommon.showSuccess('已删除');
    }
}

// 添加轮播图
function addBanner() {
    if (banners.length >= 5) {
        AdminCommon.showError('最多支持5张轮播图');
        return;
    }
    
    banners.push({
        image: '',
        link: '',
        title: '新轮播图',
        subtitle: '点击编辑内容'
    });
    renderBanners();
    
    // 滚动到新添加的项
    setTimeout(() => {
        const items = document.querySelectorAll('.banner-item');
        if (items.length > 0) {
            items[items.length - 1].scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
    }, 100);
}

// 保存轮播图配置
async function saveBanners() {
    try {
        const response = await AdminCommon.request('/home/admin/banners', {
            method: 'PUT',
            body: JSON.stringify(banners)
        });
        
        if (response && response.code === 200) {
            AdminCommon.showSuccess('轮播图保存成功');
        } else {
            AdminCommon.showError(response?.message || '保存失败');
        }
    } catch (error) {
        AdminCommon.showError('保存失败');
    }
}

// 渲染广告卡片表单
function renderAdCardForm() {
    document.getElementById('adTitle').value = adCard.title || '';
    document.getElementById('adSubtitle').value = adCard.subtitle || '';
    document.getElementById('adButtonText').value = adCard.buttonText || '';
    document.getElementById('adLink').value = adCard.link || '';
    
    // 显示图片预览
    const preview = document.getElementById('adImagePreview');
    const placeholder = document.getElementById('adImagePlaceholder');
    if (adCard.image) {
        preview.src = adCard.image;
        preview.style.display = 'block';
        placeholder.style.display = 'none';
    } else {
        preview.style.display = 'none';
        placeholder.style.display = 'block';
    }
    
    // 更新预览（文字+背景图）
    updateAdCardPreview();
}

// 处理广告卡片图片选择
function handleAdImageSelect(event) {
    const file = event.target.files[0];
    if (!file) return;
    
    if (file.size > 2 * 1024 * 1024) {
        AdminCommon.showError('图片大小不能超过 2MB');
        return;
    }
    
    const reader = new FileReader();
    reader.onload = function(e) {
        adCard.image = e.target.result;
        adImageBase64 = e.target.result;
        
        const preview = document.getElementById('adImagePreview');
        const placeholder = document.getElementById('adImagePlaceholder');
        preview.src = e.target.result;
        preview.style.display = 'block';
        placeholder.style.display = 'none';
        
        // 更新效果预览区的背景图
        updateAdCardPreview();
        
        AdminCommon.showSuccess('图片已上传');
    };
    reader.readAsDataURL(file);
}

// 清除广告卡片图片
function clearAdImage() {
    adCard.image = '';
    adImageBase64 = '';
    
    const preview = document.getElementById('adImagePreview');
    const placeholder = document.getElementById('adImagePlaceholder');
    preview.style.display = 'none';
    placeholder.style.display = 'block';
    
    // 清除效果预览的背景图
    const previewEl = document.getElementById('adCardPreview');
    if (previewEl) {
        previewEl.style.backgroundImage = '';
    }
}

// 更新广告卡片预览
function updateAdCardPreview() {
    const title = document.getElementById('adTitle').value || '首单优惠 40%';
    const subtitle = document.getElementById('adSubtitle').value || '轻松打开心灵窗户';
    const buttonText = document.getElementById('adButtonText').value || '点击咨询';
    
    document.getElementById('previewTitle').textContent = title;
    document.getElementById('previewSubtitle').textContent = subtitle;
    document.getElementById('previewBtn').textContent = buttonText;
    
    // 更新预览区的背景图：优先使用 adCard.image，其次是 adImageBase64
    const previewEl = document.getElementById('adCardPreview');
    const imgSrc = adCard.image || adImageBase64 || '';
    if (previewEl) {
        if (imgSrc) {
            previewEl.style.backgroundImage = `url(${imgSrc})`;
            previewEl.style.backgroundSize = 'cover';
            previewEl.style.backgroundPosition = 'center';
        } else {
            previewEl.style.backgroundImage = '';
            previewEl.style.backgroundSize = '';
            previewEl.style.backgroundPosition = '';
        }
    }
    
    // 监听输入变化
    document.getElementById('adTitle').oninput = updateAdCardPreview;
    document.getElementById('adSubtitle').oninput = updateAdCardPreview;
    document.getElementById('adButtonText').oninput = updateAdCardPreview;
}

// 保存广告卡片
async function saveAdCard() {
    // 收集表单数据
    adCard.title = document.getElementById('adTitle').value;
    adCard.subtitle = document.getElementById('adSubtitle').value;
    adCard.buttonText = document.getElementById('adButtonText').value;
    adCard.link = document.getElementById('adLink').value;
    
    // 如果有新上传的图片
    if (adImageBase64) {
        adCard.image = adImageBase64;
    }
    
    try {
        const response = await AdminCommon.request('/home/admin/adCard', {
            method: 'PUT',
            body: JSON.stringify(adCard)
        });
        
        if (response && response.code === 200) {
            AdminCommon.showSuccess('广告卡片保存成功');
            adImageBase64 = ''; // 清空临时数据
        } else {
            AdminCommon.showError(response?.message || '保存失败');
        }
    } catch (error) {
        AdminCommon.showError('保存失败');
    }
}

// 页面离开前提示保存
window.addEventListener('beforeunload', (event) => {
    // 可以添加未保存提示
});

// 添加快捷键
document.addEventListener('keydown', (e) => {
    // Ctrl/Cmd + S 保存
    if ((e.ctrlKey || e.metaKey) && e.key === 's') {
        e.preventDefault();
        saveBanners();
        saveAdCard();
    }
});
