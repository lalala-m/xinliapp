// 咨询师管理功能
let currentPage = 1;
let pageSize = 10;
let totalPages = 1;
let consultants = [];
let currentConsultant = null;
let currentAvatarBase64 = ''; // 存储当前头像的 Base64 数据

// 人生阶段数据缓存（替代旧标签系统）
let lifeStages = [];
let selectedTagIds = new Set();
let tempSelectedTagIds = new Set();

// 头像文件选择处理
function handleAvatarFileSelect(event) {
    const file = event.target.files[0];
    if (!file) return;

    // 验证文件类型
    if (!file.type.startsWith('image/')) {
        AdminCommon.showError('请选择图片文件');
        return;
    }

    // 验证文件大小 (最大 2MB)
    if (file.size > 2 * 1024 * 1024) {
        AdminCommon.showError('图片大小不能超过 2MB');
        return;
    }

    const reader = new FileReader();
    reader.onload = function(e) {
        const base64 = e.target.result;
        currentAvatarBase64 = base64;

        // 更新预览
        const previewImg = document.getElementById('avatarPreviewImg');
        if (previewImg) {
            previewImg.src = base64;
        }

        // 清空 URL 输入框（优先使用 Base64）
        const urlInput = document.getElementById('editAvatarUrl');
        if (urlInput) {
            urlInput.value = '';
        }

        AdminCommon.showSuccess('头像已选择');
    };
    reader.readAsDataURL(file);
}

// 清除头像
function clearAvatar() {
    currentAvatarBase64 = '';
    const previewImg = document.getElementById('avatarPreviewImg');
    if (previewImg) {
        previewImg.src = '../images/default-avatar.svg';
    }
    const urlInput = document.getElementById('editAvatarUrl');
    if (urlInput) {
        urlInput.value = '';
    }
    const fileInput = document.getElementById('avatarInput');
    if (fileInput) {
        fileInput.value = '';
    }
}

// 更新头像预览（根据URL或Base64）
function updateAvatarPreview(avatarUrl) {
    const previewImg = document.getElementById('avatarPreviewImg');
    if (previewImg) {
        if (avatarUrl && avatarUrl.startsWith('data:')) {
            // Base64 图片
            previewImg.src = avatarUrl;
        } else if (avatarUrl) {
            // URL 图片
            previewImg.src = avatarUrl;
        } else {
            previewImg.src = '../images/default-avatar.png';
        }
    }
}

// 加载咨询师列表
async function loadConsultants() {
    const tableBody = document.getElementById('consultantTableBody');
    AdminCommon.showLoading(tableBody);

    try {
        const params = new URLSearchParams({
            page: currentPage,
            size: pageSize
        });

        // 添加筛选条件
        const statusFilter = document.getElementById('statusFilter')?.value;
        const verifyFilter = document.getElementById('verifyFilter')?.value;
        const searchInput = document.getElementById('searchInput')?.value;

        if (statusFilter) params.append('status', statusFilter);
        if (verifyFilter) params.append('verified', verifyFilter);
        if (searchInput) params.append('search', searchInput);

        // 修正 AdminCommon.request 调用
        // 后端 context-path=/api, Controller=@RequestMapping("/admin")
        // 所以完整路径是 /api/admin/consultants
        // AdminCommon 会自动拼接 CONFIG.API_BASE_URL (http://localhost:8080/api)
        
        const response = await AdminCommon.request('/admin/consultants');
        console.log('咨询师列表响应:', response);
        
        if (response && response.code === 200) {
            // ... existing success logic ...
            // 后端直接返回数组，不是分页对象
            const allConsultants = response.data || [];
            
            // ... (keep existing filtering logic) ...
            let filteredConsultants = allConsultants;
            
            if (statusFilter) {
                 // 假设 available 是 boolean，需要转换
                 const isAvailable = statusFilter === 'true';
                 filteredConsultants = filteredConsultants.filter(c => c.available === isAvailable);
            }
            
            // verified 筛选
            if (verifyFilter) {
                const isVerified = verifyFilter === 'true';
                filteredConsultants = filteredConsultants.filter(c => c.isCertificateVerified === isVerified);
            }
            
            if (searchInput) {
                const search = searchInput.toLowerCase();
                filteredConsultants = filteredConsultants.filter(c => 
                    (c.name && c.name.toLowerCase().includes(search)) || 
                    (c.specialty && c.specialty.toLowerCase().includes(search))
                );
            }

            // 计算分页
            totalPages = Math.ceil(filteredConsultants.length / pageSize);
            if (totalPages === 0) totalPages = 1;
            
            // 确保当前页不越界
            if (currentPage > totalPages) currentPage = totalPages;
            
            // 手动切片
            const start = (currentPage - 1) * pageSize;
            const end = start + pageSize;
            consultants = filteredConsultants.slice(start, end);
            
            renderConsultants();
            updatePagination();
        } else {
             const errorMsg = response ? response.message : '获取数据失败';
             console.error('API Error:', errorMsg);
             AdminCommon.showEmpty(tableBody, errorMsg);
        }
    } catch (error) {
        console.error('加载咨询师列表失败:', error);
        AdminCommon.showEmpty(tableBody, '加载失败');
    }
}

// 渲染咨询师列表
function renderConsultants() {
    const tableBody = document.getElementById('consultantTableBody');
    
    if (!consultants || consultants.length === 0) {
        AdminCommon.showEmpty(tableBody);
        return;
    }

    tableBody.innerHTML = consultants.map(consultant => {
        // 审核状态判断：available=false 且没有 isCertificateVerified 可能是待审核
        // 或者根据 userStatus 判断（如果后端返回）
        const auditStatus = consultant.userStatus || (consultant.available ? 'ACTIVE' : 'INACTIVE');
        const isPending = auditStatus === 'INACTIVE' || auditStatus === 'PENDING';
        const isApproved = auditStatus === 'ACTIVE';
        
        return `
        <tr>
            <td>${consultant.id}</td>
            <td>
                <img src="${consultant.avatarUrl || '../images/default-avatar.svg'}" 
                     alt="${consultant.name}" 
                     style="width: 40px; height: 40px; border-radius: 50%; object-fit: cover;">
            </td>
            <td>${consultant.name}</td>
            <td><span style="color:#999;font-size:12px;">见下方标签</span></td>
            <td>⭐ ${consultant.rating ? consultant.rating.toFixed(1) : '0.0'}</td>
            <td>${consultant.servedCount || 0}</td>
            <td><span class="status-badge ${consultant.isCertificateVerified ? 'active' : 'inactive'}">${consultant.isCertificateVerified ? '已认证' : '未认证'}</span></td>
            <td><span class="status-badge ${isApproved ? 'active' : (isPending ? 'pending' : 'inactive')}">${isPending ? '审核中' : (isApproved ? '已通过' : '已拒绝')}</span></td>
            <td>
                <div class="action-btns">
                    <button class="btn-sm btn-view" onclick="viewConsultant(${consultant.id})">查看</button>
                    ${isPending ? `<button class="btn-sm btn-success" onclick="approveConsultant(${consultant.id})">通过</button>` : ''}
                    ${isPending ? `<button class="btn-sm btn-warning" onclick="rejectConsultant(${consultant.id})">拒绝</button>` : ''}
                    ${!isPending ? `<button class="btn-sm btn-edit" onclick="editConsultant(${consultant.id})">编辑</button>` : ''}
                    <button class="btn-sm btn-delete" onclick="deleteConsultant(${consultant.id})">删除</button>
                </div>
            </td>
        </tr>
    `}).join('');
}

// 更新分页
function updatePagination() {
    document.getElementById('pageInfo').textContent = `第 ${currentPage} 页 / 共 ${totalPages} 页`;
    document.getElementById('prevBtn').disabled = currentPage <= 1;
    document.getElementById('nextBtn').disabled = currentPage >= totalPages;
}

// 上一页
function prevPage() {
    if (currentPage > 1) {
        currentPage--;
        // loadConsultants(); // 不再重新请求，而是从内存中取
        refreshPageData();
    }
}

// 下一页
function nextPage() {
    if (currentPage < totalPages) {
        currentPage++;
        // loadConsultants();
        refreshPageData();
    }
}

// 刷新当前页数据（前端分页）
function refreshPageData() {
    if (window.allConsultants) {
        const start = (currentPage - 1) * pageSize;
        const end = start + pageSize;
        consultants = window.allConsultants.slice(start, end);
        renderConsultants();
        updatePagination();
    } else {
        loadConsultants();
    }
}

// 搜索咨询师
function searchConsultants() {
    currentPage = 1;
    loadConsultants();
}

// 筛选咨询师
function filterConsultants() {
    currentPage = 1;
    loadConsultants();
}

// 刷新咨询师列表
function refreshConsultants() {
    currentPage = 1;
    loadConsultants();
    AdminCommon.showSuccess('已刷新');
}

// 查看咨询师详情
async function viewConsultant(consultantId) {
    try {
        const response = await AdminCommon.request(`/admin/consultants/${consultantId}`);
        if (response && response.code === 200) {
            currentConsultant = response.data;
            showConsultantModal(currentConsultant, false);
        } else {
            AdminCommon.showError('获取咨询师详情失败');
        }
    } catch (error) {
        AdminCommon.showError('获取咨询师详情失败');
    }
}

// 显示咨询师详情模态框
function showConsultantModal(consultant, isEdit) {
    const modal = document.getElementById('consultantModal');
    const details = document.getElementById('consultantDetails');
    const form = document.getElementById('consultantForm');
    const actions = document.getElementById('modalActions');
    
    // 清空操作区
    actions.innerHTML = '';

    if (isEdit) {
        // 编辑模式
        document.getElementById('modalTitle').textContent = '编辑咨询师';
        details.style.display = 'none';
        form.style.display = 'block';

        // 隐藏手机号输入框 (编辑时不修改关联账号)
        const phoneGroup = document.getElementById('editPhone').closest('.form-group');
        if (phoneGroup) phoneGroup.style.display = 'none';

        // 清空头像状态
        currentAvatarBase64 = '';

        // 填充表单
        document.getElementById('editConsultantId').value = consultant.id;
        document.getElementById('editName').value = consultant.name || '';
        document.getElementById('editTitle').value = consultant.title || '';
        document.getElementById('editAvatarUrl').value = consultant.avatarUrl || '';
        setSpecialtyValue(''); // specialty字段已废弃，使用人生阶段标签
        document.getElementById('editIntro').value = consultant.intro || '';
        document.getElementById('editIdentityTier').value = consultant.identityTier || 'BRONZE';
        
        // 加载并设置标签
        loadConsultantTags(consultant.id);

        // 更新头像预览
        updateAvatarPreview(consultant.avatarUrl || '');

        // 添加按钮: 取消, 保存
        actions.innerHTML = `
            <button class="btn-secondary" onclick="cancelEdit()">取消</button>
            <button class="btn-primary" onclick="submitConsultantForm()">保存提交</button>
        `;

    } else {
        // 查看模式
        document.getElementById('modalTitle').textContent = '咨询师详情';
        details.style.display = 'block';
        form.style.display = 'none';
        
        details.innerHTML = `
            <div style="display: flex; gap: 24px; align-items: flex-start;">
                <div style="flex-shrink: 0; text-align: center;">
                    <img src="${consultant.avatarUrl || '../images/default-avatar.svg'}"
                         alt="${consultant.name}"
                         style="width: 120px; height: 120px; border-radius: 8px; object-fit: cover; border: 1px solid #f0f0f0;"
                         id="detailAvatarImg">
                    <div style="margin-top: 10px;">
                        <span class="status-badge ${consultant.available ? 'active' : 'inactive'}">
                            ${consultant.available ? '在线' : '离线'}
                        </span>
                    </div>
                </div>
                <div style="flex: 1; line-height: 1.8;">
                    <div style="margin-bottom: 16px;">
                        <h3 style="margin: 0; font-size: 20px; color: #333; display: inline-block;">${consultant.name}</h3>
                        <span style="color: #666; margin-left: 8px; font-size: 14px;">${consultant.title || ''}</span>
                    </div>
                    
                    <div style="display: grid; grid-template-columns: auto 1fr; gap: 8px 16px; font-size: 14px;">
                        <span style="color: #999;">咨询师ID:</span>
                        <span>${consultant.id}</span>
                        
                        <span style="color: #999;">等级:</span>
                        <span>${consultant.identityTier || '普通'}</span>
                        
                        <span style="color: #999;">评分:</span>
                        <span>⭐ ${consultant.rating ? consultant.rating.toFixed(1) : '0.0'} (${consultant.servedCount || 0}次咨询)</span>
                        
                        <span style="color: #999;">认证状态:</span>
                        <span>${consultant.isCertificateVerified ? '<span style="color:#52c41a">✅ 已认证</span>' : '<span style="color:#ff4d4f">❌ 未认证</span>'}</span>
                        
                        <span style="color: #999;">擅长人生阶段:</span>
                        <span id="viewConsultantStages">加载中...</span>
                        
                        <span style="color: #999;">个人简介:</span>
                        <span style="color: #666;">${consultant.intro || '-'}</span>
                    </div>
                </div>
            </div>
        `;
        
        // 添加按钮: 认证, 编辑, 删除, 关闭
        // 注意: verifyConsultant 和 deleteConsultant 需要传入 ID
        actions.innerHTML = `
            ${!consultant.isCertificateVerified ? `<button class="btn-success" onclick="verifyConsultant(${consultant.id})">认证通过</button>` : ''}
            <button class="btn-warning" onclick="showEditForm()">编辑</button>
            <button class="btn-danger" onclick="deleteConsultant(${consultant.id})">删除</button>
            <button class="btn-secondary" onclick="closeModal()">关闭</button>
        `;
    }
    
    modal.classList.add('show');
}

// 关闭模态框
function closeModal() {
    const modal = document.getElementById('consultantModal');
    modal.classList.remove('show');
    currentConsultant = null;
    document.getElementById('consultantForm').reset();
}

// 切换到编辑模式
function showEditForm() {
    if (currentConsultant) {
        showConsultantModal(currentConsultant, true);
    }
}

// 显示添加咨询师模态框
function showAddConsultantModal() {
    const modal = document.getElementById('consultantModal');
    const details = document.getElementById('consultantDetails');
    const form = document.getElementById('consultantForm');
    const actions = document.getElementById('modalActions');

    document.getElementById('modalTitle').textContent = '添加咨询师';
    details.style.display = 'none';
    form.style.display = 'block';

    // 清空表单和头像状态
    currentAvatarBase64 = '';
    document.getElementById('editConsultantId').value = '';
    document.getElementById('editName').value = '';
    document.getElementById('editPhone').value = '';
    document.getElementById('editPhone').closest('.form-group').style.display = 'block'; // 显示手机号输入框
    document.getElementById('editTitle').value = '';
    document.getElementById('editAvatarUrl').value = '';
    setSpecialtyValue('');
    setSelectedTags([]);
    document.getElementById('editIntro').value = '';
    document.getElementById('editIdentityTier').value = 'BRONZE';

    // 重置头像预览
    updateAvatarPreview('');

    // 清空文件输入
    const fileInput = document.getElementById('avatarInput');
    if (fileInput) fileInput.value = '';

    // 添加按钮
    actions.innerHTML = `
        <button class="btn-secondary" onclick="closeModal()">取消</button>
        <button class="btn-primary" onclick="submitConsultantForm()">立即创建</button>
    `;

    modal.classList.add('show');
}

// 提交表单辅助函数
function submitConsultantForm() {
    // 触发表单的 submit 事件
    // 由于 submit 按钮在 form 外部，我们需要手动触发
    // 或者使用 form.requestSubmit() (现代浏览器支持)
    const form = document.getElementById('consultantForm');
    if (form.reportValidity()) {
        form.dispatchEvent(new Event('submit'));
    }
}

// 取消编辑
function cancelEdit() {
    const id = document.getElementById('editConsultantId').value;
    if (id && currentConsultant) {
        showConsultantModal(currentConsultant, false);
    } else {
        closeModal();
    }
}

// 加载咨询师的标签
async function loadConsultantTags(consultantId) {
    try {
        const response = await AdminCommon.request(`/admin/consultants/${consultantId}/tags`);
        if (response && response.code === 200) {
            const tags = response.data || [];
            setSelectedTags(tags.map(t => t.id));
        } else {
            setSelectedTags([]);
        }
    } catch (error) {
        console.error('加载咨询师标签失败:', error);
        setSelectedTags([]);
    }
}

// 保存咨询师的标签
async function saveConsultantTags(consultantId, tagIds) {
    try {
        await AdminCommon.request(`/admin/consultants/${consultantId}/tags`, {
            method: 'PUT',
            body: JSON.stringify(tagIds)
        });
    } catch (error) {
        console.error('保存标签失败:', error);
    }
}

// 编辑/更新咨询师 (提交表单)
document.getElementById('consultantForm')?.addEventListener('submit', async function(e) {
    e.preventDefault();

    const id = document.getElementById('editConsultantId').value;
    const avatarUrl = document.getElementById('editAvatarUrl').value.trim();
    const tagIdsStr = document.getElementById('editTagIds').value;
    const tagIds = tagIdsStr ? tagIdsStr.split(',').map(v => parseInt(v.trim())).filter(v => !isNaN(v)) : [];
    
    const data = {
        name: document.getElementById('editName').value,
        title: document.getElementById('editTitle').value,
        // specialty字段已废弃，使用人生阶段标签
        intro: document.getElementById('editIntro').value,
        identityTier: document.getElementById('editIdentityTier').value,
        // 头像：如果有Base64优先用Base64，否则用URL
        avatarUrl: currentAvatarBase64 || avatarUrl || null,
        tagIds: tagIds
    };

    try {
        let response;
        if (id) {
            // 更新
            response = await AdminCommon.request(`/admin/consultants/${id}`, {
                method: 'PUT',
                body: JSON.stringify(data)
            });
            // 单独更新标签
            await saveConsultantTags(id, tagIds);
        } else {
            // 新增 - 使用新的专用接口
            const phone = document.getElementById('editPhone').value;
            if (!phone) {
                AdminCommon.showError('请输入手机号');
                return;
            }

            const createData = {
                phone: phone,
                name: data.name,
                title: data.title,
                // specialty字段已废弃，使用人生阶段标签
                intro: data.intro,
                identityTier: data.identityTier,
                avatarUrl: data.avatarUrl,
                tagIds: tagIds
            };

            response = await AdminCommon.request('/admin/consultants', {
                method: 'POST',
                body: JSON.stringify(createData)
            });
        }

        if (response && response.code === 200) {
            AdminCommon.showSuccess(id ? '更新成功' : '创建成功');
            closeModal();
            loadConsultants();
        } else {
            AdminCommon.showError(response?.message || '操作失败');
        }
    } catch (error) {
        AdminCommon.showError('操作失败');
    }
});

// 点击列表中的编辑按钮
async function editConsultant(consultantId) {
    // 先获取最新详情
    try {
        const response = await AdminCommon.request(`/admin/consultants/${consultantId}`);
        if (response && response.code === 200) {
            currentConsultant = response.data;
            showConsultantModal(currentConsultant, true);
        }
    } catch (error) {
        AdminCommon.showError('加载详情失败');
    }
}

// 认证咨询师（证书认证）
async function verifyConsultant(consultantId) {
    if (!AdminCommon.confirm('确定要通过该咨询师的认证吗？')) return;

    try {
        await AdminCommon.request(`/admin/consultants/${consultantId}/verify`, {
            method: 'PUT'
        });
        
        AdminCommon.showSuccess('认证成功');
        AdminCommon.logAction('VERIFY_CONSULTANT', `认证咨询师 ID: ${consultantId}`);
        closeModal();
        loadConsultants();
    } catch (error) {
        AdminCommon.showError('认证失败');
    }
}

// 审核通过咨询师（激活账号）
async function approveConsultant(consultantId) {
    if (!AdminCommon.confirm('确定要通过该咨询师的审核吗？审核通过后该咨询师即可登录。')) return;

    try {
        const response = await AdminCommon.request(`/admin/consultants/${consultantId}/approve`, {
            method: 'PUT'
        });
        
        if (response && response.code === 200) {
            AdminCommon.showSuccess('审核通过');
            AdminCommon.logAction('APPROVE_CONSULTANT', `审核通过咨询师 ID: ${consultantId}`);
            loadConsultants();
        } else {
            AdminCommon.showError(response?.message || '审核失败');
        }
    } catch (error) {
        console.error('审核失败:', error);
        AdminCommon.showError('审核失败，请检查网络连接');
    }
}

// 审核拒绝咨询师（禁用账号）
async function rejectConsultant(consultantId) {
    if (!AdminCommon.confirm('确定要拒绝该咨询师的审核吗？拒绝后该咨询师将无法登录。')) return;

    try {
        const response = await AdminCommon.request(`/admin/consultants/${consultantId}/reject`, {
            method: 'PUT'
        });
        
        if (response && response.code === 200) {
            AdminCommon.showSuccess('已拒绝');
            AdminCommon.logAction('REJECT_CONSULTANT', `拒绝咨询师 ID: ${consultantId}`);
            loadConsultants();
        } else {
            AdminCommon.showError(response?.message || '操作失败');
        }
    } catch (error) {
        console.error('拒绝失败:', error);
        AdminCommon.showError('操作失败，请检查网络连接');
    }
}

// 删除咨询师
async function deleteConsultant(consultantId) {
    if (!AdminCommon.confirm('确定要删除该咨询师吗？此操作不可恢复！')) return;

    try {
        await AdminCommon.request(`/admin/consultants/${consultantId}`, {
            method: 'DELETE'
        });
        
        AdminCommon.showSuccess('删除成功');
        AdminCommon.logAction('DELETE_CONSULTANT', `删除咨询师 ID: ${consultantId}`);
        loadConsultants();
    } catch (error) {
        AdminCommon.showError('删除失败');
    }
}

// 加载人生阶段列表
async function loadLifeStages() {
    try {
        const response = await AdminCommon.request('/life-stages');
        if (response && response.code === 200) {
            lifeStages = response.data || [];
        }
    } catch (error) {
        console.error('加载人生阶段失败:', error);
    }
}

// 打开标签选择器（改为人生阶段选择）
function openTagSelector() {
    tempSelectedTagIds = new Set(selectedTagIds);
    renderTagSelector();
    document.getElementById('tagModal').classList.add('show');
    updateTagSelectCount();
}

// 关闭标签选择器
function closeTagModal() {
    document.getElementById('tagModal').classList.remove('show');
}

// 渲染标签选择器（改为人生阶段列表）
function renderTagSelector() {
    const container = document.getElementById('tagCategoryList');
    if (lifeStages.length === 0) {
        container.innerHTML = '<div class="empty">暂无人生阶段数据</div>';
        return;
    }

    container.innerHTML = `
        <div style="border: 1px solid #e8e8e8; border-radius: 8px; padding: 12px; background: white;">
            <div style="font-weight: 600; font-size: 15px; color: #333; margin-bottom: 10px; display: flex; align-items: center; gap: 6px;">
                <span style="width: 4px; height: 16px; background: #6FA6F8; border-radius: 2px;"></span>
                选择擅长的人生阶段
            </div>
            <div style="display: flex; flex-wrap: wrap; gap: 8px;">
                ${lifeStages.map(stage => `
                    <label style="display: inline-flex; align-items: center; gap: 4px; padding: 6px 12px; 
                        border-radius: 16px; font-size: 13px; cursor: pointer; transition: all 0.2s;
                        background: ${tempSelectedTagIds.has(stage.id) ? '#6FA6F8' : '#f0f0f0'};
                        color: ${tempSelectedTagIds.has(stage.id) ? 'white' : '#666'};
                        border: 2px solid ${tempSelectedTagIds.has(stage.id) ? '#6FA6F8' : 'transparent'};"
                        onmouseover="this.style.borderColor='#6FA6F8'"
                        onmouseout="this.style.borderColor='${tempSelectedTagIds.has(stage.id) ? '#6FA6F8' : 'transparent'}'">
                        <input type="checkbox" value="${stage.id}" 
                            ${tempSelectedTagIds.has(stage.id) ? 'checked' : ''}
                            onchange="toggleTagSelection(${stage.id}, this.checked)"
                            style="display: none;">
                        <span style="font-size: 16px; margin-right: 2px;">${stage.icon || '🏷️'}</span>
                        ${stage.name}
                    </label>
                `).join('')}
            </div>
        </div>
    `;
}

// 切换标签选择
function toggleTagSelection(tagId, checked) {
    if (checked) {
        tempSelectedTagIds.add(tagId);
    } else {
        tempSelectedTagIds.delete(tagId);
    }
    renderTagSelector();
    updateTagSelectCount();
}

// 更新选择计数
function updateTagSelectCount() {
    const count = tempSelectedTagIds.size;
    document.getElementById('tagSelectCount').textContent = `已选择 ${count} 个阶段`;
}

// 确认标签选择
function confirmTagSelection() {
    selectedTagIds = new Set(tempSelectedTagIds);
    updateSelectedTagsDisplay();
    closeTagModal();
}

// 更新已选标签显示
function updateSelectedTagsDisplay() {
    const container = document.getElementById('selectedTags');
    const hiddenInput = document.getElementById('editTagIds');
    
    if (selectedTagIds.size === 0) {
        container.innerHTML = '<span style="color: #999; font-size: 13px;">尚未选择擅长阶段</span>';
        hiddenInput.value = '';
        return;
    }

    const tagNames = [];
    const tagIdList = Array.from(selectedTagIds);
    for (const stage of lifeStages) {
        if (selectedTagIds.has(stage.id)) {
            tagNames.push({ id: stage.id, name: stage.name });
        }
    }

    container.innerHTML = tagNames.map(t => `
        <span style="display: inline-flex; align-items: center; gap: 4px; padding: 4px 10px; 
            background: #6FA6F8; color: white; border-radius: 12px; font-size: 12px;">
            ${t.name}
            <span style="cursor: pointer; opacity: 0.8;" onclick="removeSelectedTag(${t.id})">×</span>
        </span>
    `).join('');

    hiddenInput.value = tagIdList.join(',');
}

// 移除已选标签
function removeSelectedTag(tagId) {
    selectedTagIds.delete(tagId);
    updateSelectedTagsDisplay();
}

// 设置已选标签（编辑时）
function setSelectedTags(tagIds) {
    selectedTagIds = new Set(tagIds || []);
    updateSelectedTagsDisplay();
}

// 页面加载完成后执行
document.addEventListener('DOMContentLoaded', () => {
    loadLifeStages();
    loadConsultants();
    
    // 搜索框回车事件
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                searchConsultants();
            }
        });
    }
});

function setSpecialtyValue(value) {
    const input = document.getElementById('editSpecialty');
    if (input) input.value = value || '';
}
