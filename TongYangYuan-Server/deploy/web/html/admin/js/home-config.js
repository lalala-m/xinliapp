// 首页配置管理
let banners = [];
let adCard = {};
let adImageBase64 = '';

// 页面加载
document.addEventListener('DOMContentLoaded', () => {
    loadConfig();
    loadSplashAds();
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

// 🔧 辅助函数：获取图片完整URL
function getFullImageUrl(url) {
    if (!url) return '';
    if (url.startsWith('http')) return url;
    // 使用 CONFIG.API_BASE_URL（已包含 /api）+ 相对路径
    // 如 http://localhost:8080/api + /uploads/images/xxx.jpg
    const baseUrl = (typeof CONFIG !== 'undefined' && CONFIG.API_BASE_URL) ? CONFIG.API_BASE_URL : 'http://139.196.5.153:8080/api';
    return baseUrl + url;
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
                    `<img src="${getFullImageUrl(banner.image)}" alt="预览图">` : 
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

// 处理轮播图图片选择 - 🔧 修复：先上传到服务器，再保存URL
async function handleBannerImageSelect(event, index) {
    const file = event.target.files[0];
    if (!file) return;
    
    if (file.size > 2 * 1024 * 1024) {
        AdminCommon.showError('图片大小不能超过 2MB');
        return;
    }
    
    try {
        AdminCommon.showSuccess('正在上传图片...');
        
        // 先上传到服务器
        const formData = new FormData();
        formData.append('file', file);
        
        // 🔧 修复：使用正确的 base URL（8080 端口，不是 5500）
        const token = AdminCommon.getToken();
        const baseUrl = (typeof CONFIG !== 'undefined' && CONFIG.API_BASE_URL) ? CONFIG.API_BASE_URL.replace(/\/api$/, '') : 'http://139.196.5.153:8080';
        const uploadRes = await fetch(baseUrl + '/api/upload/image', {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + token
            },
            body: formData
        });
        
        const uploadJson = await uploadRes.json();
        console.log('[Banner Upload] Response:', uploadJson);
        
        if (uploadJson.code === 200 && uploadJson.data) {
            // 保存图片URL而不是Base64
            banners[index].image = uploadJson.data;
            renderBanners();
            AdminCommon.showSuccess('图片上传成功');
        } else {
            AdminCommon.showError(uploadJson.message || '图片上传失败');
        }
    } catch (error) {
        console.error('[Banner Upload] Error:', error);
        AdminCommon.showError('图片上传失败: ' + error.message);
    }
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
        console.log('保存轮播图，数据:', banners);
        console.log('JSON数据:', JSON.stringify(banners));
        
        const response = await AdminCommon.request('/home/admin/banners', {
            method: 'PUT',
            body: JSON.stringify(banners)
        });
        
        console.log('保存响应:', response);
        
        if (response && response.code === 200) {
            AdminCommon.showSuccess('轮播图保存成功');
        } else {
            AdminCommon.showError(response?.message || '保存失败');
        }
    } catch (error) {
        console.error('保存轮播图出错:', error);
        AdminCommon.showError('保存失败: ' + error.message);
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
        preview.src = getFullImageUrl(adCard.image);
        preview.style.display = 'block';
        placeholder.style.display = 'none';
    } else {
        preview.style.display = 'none';
        placeholder.style.display = 'block';
    }
    
    // 更新预览（文字+背景图）
    updateAdCardPreview();
}

// 处理广告卡片图片选择 - 🔧 修复：先上传到服务器
async function handleAdImageSelect(event) {
    const file = event.target.files[0];
    if (!file) return;
    
    if (file.size > 2 * 1024 * 1024) {
        AdminCommon.showError('图片大小不能超过 2MB');
        return;
    }
    
    try {
        AdminCommon.showSuccess('正在上传图片...');
        
        // 先上传到服务器
        const formData = new FormData();
        formData.append('file', file);
        
        // 🔧 修复：使用正确的 base URL（8080 端口，不是 5500）
        const token = AdminCommon.getToken();
        const baseUrl = (typeof CONFIG !== 'undefined' && CONFIG.API_BASE_URL) ? CONFIG.API_BASE_URL.replace(/\/api$/, '') : 'http://139.196.5.153:8080';
        const uploadRes = await fetch(baseUrl + '/api/upload/image', {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + token
            },
            body: formData
        });
        
        const uploadJson = await uploadRes.json();
        console.log('[AdCard Upload] Response:', uploadJson);
        
        if (uploadJson.code === 200 && uploadJson.data) {
            adCard.image = uploadJson.data;
            adImageBase64 = '';
            
            const preview = document.getElementById('adImagePreview');
            const placeholder = document.getElementById('adImagePlaceholder');
            preview.src = uploadJson.data;
            preview.style.display = 'block';
            placeholder.style.display = 'none';
            
            updateAdCardPreview();
            AdminCommon.showSuccess('图片上传成功');
        } else {
            AdminCommon.showError(uploadJson.message || '图片上传失败');
        }
    } catch (error) {
        console.error('[AdCard Upload] Error:', error);
        AdminCommon.showError('图片上传失败: ' + error.message);
    }
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
            previewEl.style.backgroundImage = `url(${getFullImageUrl(imgSrc)})`;
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

// ==================== 开屏广告管理 ====================

let splashAds = [];

// 加载开屏广告
async function loadSplashAds() {
    try {
        const response = await AdminCommon.request('/splash-ads');
        if (response && response.code === 200) {
            splashAds = response.data || [];
            renderSplashAds();
        }
    } catch (error) {
        console.error('加载开屏广告失败:', error);
    }
}

// 渲染开屏广告列表
function renderSplashAds() {
    const container = document.getElementById('splashAdContainer');
    if (!container) return;
    
    if (splashAds.length === 0) {
        container.innerHTML = `
            <div style="text-align: center; padding: 40px; color: #999;">
                <div style="font-size: 48px; margin-bottom: 16px;">📱</div>
                <div>暂无开屏广告</div>
                <button class="btn-primary" onclick="addSplashAd()" style="margin-top: 16px;">+ 新增广告</button>
            </div>
        `;
        return;
    }

    container.innerHTML = `
        <div style="display: flex; flex-direction: column; gap: 16px;">
            ${splashAds.map(ad => `
                <div style="display: flex; gap: 16px; padding: 16px; background: #f8f9fa; border-radius: 8px; align-items: flex-start;">
                    <div style="width: 120px; height: 200px; border-radius: 8px; background: linear-gradient(135deg, #BDEBC8 0%, #A9C9FF 100%); display: flex; align-items: center; justify-content: center; overflow: hidden; flex-shrink: 0;">
                        ${ad.imageUrl ? `<img src="${getFullImageUrl(ad.imageUrl)}" style="width: 100%; height: 100%; object-fit: cover;">` : '<div style="color: #999; font-size: 12px;">暂无图片</div>'}
                    </div>
                    <div style="flex: 1; display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
                        <div>
                            <label style="font-size: 12px; color: #666;">标题</label>
                            <input type="text" class="form-control" value="${ad.title || ''}" onchange="updateSplashAd(${ad.id}, 'title', this.value)" style="width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px; margin-top: 4px;">
                        </div>
                        <div>
                            <label style="font-size: 12px; color: #666;">展示时长(秒)</label>
                            <input type="number" class="form-control" value="${ad.durationSeconds || 3}" min="1" max="10" onchange="updateSplashAd(${ad.id}, 'durationSeconds', parseInt(this.value))" style="width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px; margin-top: 4px;">
                        </div>
                        <div style="grid-column: span 2;">
                            <label style="font-size: 12px; color: #666;">图片</label>
                            <input type="file" accept="image/*" onchange="uploadSplashImage(${ad.id}, event)" style="margin-top: 4px;">
                        </div>
                        <div>
                            <label style="font-size: 12px; color: #666;">跳转链接</label>
                            <input type="text" class="form-control" value="${ad.linkUrl || ''}" placeholder="可选" onchange="updateSplashAd(${ad.id}, 'linkUrl', this.value)" style="width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px; margin-top: 4px;">
                        </div>
                        <div>
                            <label style="font-size: 12px; color: #666;">跳转类型</label>
                            <select onchange="updateSplashAd(${ad.id}, 'linkType', this.value)" style="width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px; margin-top: 4px;">
                                <option value="NONE" ${ad.linkType === 'NONE' ? 'selected' : ''}>无跳转</option>
                                <option value="INTERNAL" ${ad.linkType === 'INTERNAL' ? 'selected' : ''}>内部链接</option>
                                <option value="EXTERNAL" ${ad.linkType === 'EXTERNAL' ? 'selected' : ''}>外部链接</option>
                            </select>
                        </div>
                        <div>
                            <label style="font-size: 12px; color: #666;">开始日期</label>
                            <input type="date" class="form-control" value="${ad.startDate || ''}" onchange="updateSplashAd(${ad.id}, 'startDate', this.value)" style="width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px; margin-top: 4px;">
                        </div>
                        <div>
                            <label style="font-size: 12px; color: #666;">结束日期</label>
                            <input type="date" class="form-control" value="${ad.endDate || ''}" onchange="updateSplashAd(${ad.id}, 'endDate', this.value)" style="width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px; margin-top: 4px;">
                        </div>
                        <div style="display: flex; gap: 16px; align-items: center;">
                            <label style="display: flex; align-items: center; gap: 6px; cursor: pointer;">
                                <input type="checkbox" ${ad.isEnabled ? 'checked' : ''} onchange="updateSplashAd(${ad.id}, 'isEnabled', this.checked)">
                                <span style="font-size: 13px;">启用</span>
                            </label>
                            <label style="display: flex; align-items: center; gap: 6px; cursor: pointer;">
                                <input type="checkbox" ${ad.isSkippable ? 'checked' : ''} onchange="updateSplashAd(${ad.id}, 'isSkippable', this.checked)">
                                <span style="font-size: 13px;">可跳过</span>
                            </label>
                        </div>
                        <div style="font-size: 12px; color: #666; display: flex; gap: 16px; align-items: center;">
                            <span>👁️ 展示: ${ad.showCount || 0}</span>
                            <span>👆 点击: ${ad.clickCount || 0}</span>
                        </div>
                        <div style="grid-column: span 2; display: flex; gap: 12px;">
                            <button class="btn-primary" onclick="saveSplashAd(${ad.id})" style="padding: 8px 20px; font-size: 13px;">保存</button>
                            <button class="btn-danger" onclick="deleteSplashAd(${ad.id})" style="padding: 8px 20px; font-size: 13px;">删除</button>
                        </div>
                    </div>
                </div>
            `).join('')}
            <button class="btn-primary" onclick="addSplashAd()" style="align-self: flex-start;">+ 新增广告</button>
        </div>
    `;
}

function getFullImageUrl(url) {
    if (!url) return '';
    if (url.startsWith('http')) return url;
    // 使用完整的 API base URL（包含 /api），因为图片资源映射在 /api/uploads/ 下
    const baseUrl = CONFIG.API_BASE_URL || 'http://127.0.0.1:8080/api';
    return baseUrl + url;
}

function updateSplashAd(id, field, value) {
    const ad = splashAds.find(a => a.id === id);
    if (ad) {
        ad[field] = value;
    }
}

async function uploadSplashImage(id, event) {
    const file = event.target.files[0];
    if (!file) return;

    try {
        AdminCommon.showLoading('上传中...');
        const formData = new FormData();
        formData.append('file', file);

        const baseUrl = (CONFIG.API_BASE_URL || 'http://139.196.5.153:8080/api').replace(/\/api$/, '');
        const response = await fetch(baseUrl + '/api/upload/image', {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            },
            body: formData
        });

        const result = await response.json();
        if (result.code === 200 && result.data) {
            updateSplashAd(id, 'imageUrl', result.data);
            renderSplashAds();
            AdminCommon.showSuccess('上传成功');
        } else {
            throw new Error(result.message || '上传失败');
        }
    } catch (error) {
        AdminCommon.showError('上传失败: ' + error.message);
    } finally {
        AdminCommon.hideLoading();
    }
}

async function saveSplashAd(id) {
    const ad = splashAds.find(a => a.id === id);
    if (!ad) return;

    try {
        AdminCommon.showLoading('保存中...');
        // 确保中文字符通过 Unicode 转义发送，避免 Windows bash 编码问题
        const body = JSON.stringify(ad).replace(/[\u007f-\uffff]/g, function(c) {
            return '\\u' + ('0000' + c.charCodeAt(0).toString(16)).slice(-4);
        });
        const response = await AdminCommon.request(`/splash-ads/${id}`, {
            method: 'PUT',
            body: body
        });

        if (response && response.code === 200) {
            AdminCommon.showSuccess('保存成功');
            loadSplashAds();
        } else {
            throw new Error(response?.message || '保存失败');
        }
    } catch (error) {
        AdminCommon.showError('保存失败: ' + error.message);
    } finally {
        AdminCommon.hideLoading();
    }
}

async function deleteSplashAd(id) {
    if (!confirm('确定要删除这条广告吗？')) return;

    try {
        AdminCommon.showLoading('删除中...');
        const response = await AdminCommon.request(`/splash-ads/${id}`, {
            method: 'DELETE'
        });

        if (response && response.code === 200) {
            AdminCommon.showSuccess('删除成功');
            loadSplashAds();
        } else {
            throw new Error(response?.message || '删除失败');
        }
    } catch (error) {
        AdminCommon.showError('删除失败: ' + error.message);
    } finally {
        AdminCommon.hideLoading();
    }
}

async function addSplashAd() {
    try {
        AdminCommon.showLoading('创建中...');
        const newAd = {
            title: '新广告',
            imageUrl: '',
            durationSeconds: 3,
            isEnabled: true,
            isSkippable: true,
            linkType: 'NONE',
            sortOrder: splashAds.length + 1
        };

        const body = JSON.stringify(newAd).replace(/[\u007f-\uffff]/g, function(c) {
            return '\\u' + ('0000' + c.charCodeAt(0).toString(16)).slice(-4);
        });
        const response = await AdminCommon.request('/splash-ads', {
            method: 'POST',
            body: body
        });

        if (response && response.code === 200) {
            AdminCommon.showSuccess('创建成功');
            loadSplashAds();
        } else {
            throw new Error(response?.message || '创建失败');
        }
    } catch (error) {
        AdminCommon.showError('创建失败: ' + error.message);
    } finally {
        AdminCommon.hideLoading();
    }
}

// 添加快捷键
document.addEventListener('keydown', (e) => {
    // Ctrl/Cmd + S 保存
    if ((e.ctrlKey || e.metaKey) && e.key === 's') {
        e.preventDefault();
        saveBanners();
        saveAdCard();
    }
});
