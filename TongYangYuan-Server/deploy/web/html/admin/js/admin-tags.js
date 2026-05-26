// 标签管理功能
let stages = [];
let currentStageId = null;
let currentStage = null;
let issues = [];

// ========== 加载数据 ==========

// 加载所有阶段
async function loadStages() {
    const listEl = document.getElementById('stageList');
    listEl.innerHTML = '<div style="padding:20px;text-align:center;color:#999;">加载中...</div>';

    try {
        const response = await AdminCommon.request('/admin/life-stages');
        if (response && response.code === 200) {
            stages = response.data || [];
            renderStageList();
        } else {
            listEl.innerHTML = '<div style="padding:20px;text-align:center;color:#999;">加载失败</div>';
        }
    } catch (error) {
        console.error('加载阶段失败:', error);
        listEl.innerHTML = '<div style="padding:20px;text-align:center;color:#999;">加载失败</div>';
    }
}

// 渲染阶段列表
function renderStageList() {
    const listEl = document.getElementById('stageList');
    if (stages.length === 0) {
        listEl.innerHTML = '<div style="padding:20px;text-align:center;color:#999;">暂无阶段</div>';
        return;
    }

    listEl.innerHTML = stages.map(stage => {
        const issueCount = stage.issues ? stage.issues.length : 0;
        const isActive = currentStageId === stage.id;
        return `
            <div class="stage-item ${isActive ? 'active' : ''}" onclick="selectStage(${stage.id})">
                <span class="stage-icon">${stage.icon || '🏷️'}</span>
                <span class="stage-name">${stage.name}</span>
                <span class="stage-count">${issueCount}</span>
            </div>
        `;
    }).join('');
}

// 选择阶段
async function selectStage(stageId) {
    currentStageId = stageId;
    currentStage = stages.find(s => s.id === stageId);
    renderStageList();

    if (!currentStage) return;

    document.getElementById('issuesPanelTitle').textContent = `📋 ${currentStage.name} - 咨询问题标签`;
    
    // 加载该阶段的问题
    const contentEl = document.getElementById('issuesContent');
    contentEl.innerHTML = '<div style="padding:40px;text-align:center;color:#999;">加载中...</div>';

    try {
        const response = await AdminCommon.request(`/admin/life-stages/${stageId}/issues`);
        if (response && response.code === 200) {
            issues = response.data || [];
            renderIssues();
        } else {
            contentEl.innerHTML = '<div style="padding:40px;text-align:center;color:#999;">加载失败</div>';
        }
    } catch (error) {
        console.error('加载问题失败:', error);
        contentEl.innerHTML = '<div style="padding:40px;text-align:center;color:#999;">加载失败</div>';
    }
}

// 渲染问题列表（按分类分组）
function renderIssues() {
    const contentEl = document.getElementById('issuesContent');
    
    if (issues.length === 0) {
        contentEl.innerHTML = `
            <div class="empty-state">
                <div class="icon">📋</div>
                <p>该阶段下暂无问题标签</p>
                <button class="btn-sm btn-primary-sm" onclick="showAddIssueModal()" style="margin-top:12px;">添加问题</button>
            </div>
        `;
        return;
    }

    // 按分类分组
    const grouped = {};
    issues.forEach(issue => {
        const cat = issue.categoryName || '未分类';
        if (!grouped[cat]) grouped[cat] = [];
        grouped[cat].push(issue);
    });

    // 排序："其它"放到最后
    const categories = Object.keys(grouped).sort((a, b) => {
        if (a === '其它') return 1;
        if (b === '其它') return -1;
        return a.localeCompare(b, 'zh-CN');
    });

    contentEl.innerHTML = categories.map(category => {
        const categoryIssues = grouped[category];
        return `
            <div class="category-group">
                <div class="category-header">
                    <span>${category}</span>
                    <span style="font-size:12px;color:#999;font-weight:normal;">${categoryIssues.length} 个问题</span>
                </div>
                <div class="category-issues">
                    ${categoryIssues.map(issue => `
                        <div class="issue-tag ${issue.isCustomInput ? 'custom' : ''}">
                            ${issue.isCustomInput ? '✏️ ' : ''}${issue.issueName}
                            <button class="delete-btn" onclick="event.stopPropagation(); editIssue(${issue.id})" title="编辑">✎</button>
                            <button class="delete-btn" onclick="event.stopPropagation(); deleteIssue(${issue.id})" title="删除">×</button>
                        </div>
                    `).join('')}
                </div>
            </div>
        `;
    }).join('');
}

// ========== 阶段管理 ==========

function showAddStageModal() {
    document.getElementById('stageModalTitle').textContent = '新增阶段';
    document.getElementById('stageId').value = '';
    document.getElementById('stageCode').value = '';
    document.getElementById('stageName').value = '';
    document.getElementById('stageIcon').value = '';
    document.getElementById('stageSort').value = stages.length;
    document.getElementById('stageModal').classList.add('show');
}

function showEditStageModal(stageId) {
    const stage = stages.find(s => s.id === stageId);
    if (!stage) return;

    document.getElementById('stageModalTitle').textContent = '编辑阶段';
    document.getElementById('stageId').value = stage.id;
    document.getElementById('stageCode').value = stage.code;
    document.getElementById('stageName').value = stage.name;
    document.getElementById('stageIcon').value = stage.icon || '';
    document.getElementById('stageSort').value = stage.sortOrder || 0;
    document.getElementById('stageModal').classList.add('show');
}

function closeStageModal() {
    document.getElementById('stageModal').classList.remove('show');
}

async function saveStage() {
    const id = document.getElementById('stageId').value;
    const code = document.getElementById('stageCode').value.trim();
    const name = document.getElementById('stageName').value.trim();
    const icon = document.getElementById('stageIcon').value.trim();
    const sortOrder = parseInt(document.getElementById('stageSort').value) || 0;

    if (!code || !name) {
        AdminCommon.showError('请填写阶段编码和名称');
        return;
    }

    const data = { code, name, icon, sortOrder, isEnabled: true };

    try {
        let response;
        if (id) {
            response = await AdminCommon.request(`/admin/life-stages/${id}`, {
                method: 'PUT',
                body: JSON.stringify(data)
            });
        } else {
            response = await AdminCommon.request('/admin/life-stages', {
                method: 'POST',
                body: JSON.stringify(data)
            });
        }

        if (response && response.code === 200) {
            AdminCommon.showSuccess(id ? '更新成功' : '创建成功');
            closeStageModal();
            await loadStages();
            if (id && currentStageId === parseInt(id)) {
                await selectStage(parseInt(id));
            }
        } else {
            AdminCommon.showError(response?.message || '操作失败');
        }
    } catch (error) {
        console.error('保存阶段失败:', error);
        AdminCommon.showError('保存失败');
    }
}

async function deleteStage(stageId) {
    if (!AdminCommon.confirm('确定要删除该阶段吗？该阶段下的所有问题标签也会被删除。')) {
        return;
    }

    try {
        const response = await AdminCommon.request(`/admin/life-stages/${stageId}`, {
            method: 'DELETE'
        });

        if (response && response.code === 200) {
            AdminCommon.showSuccess('删除成功');
            if (currentStageId === stageId) {
                currentStageId = null;
                currentStage = null;
                document.getElementById('issuesPanelTitle').textContent = '📋 咨询问题标签';
                document.getElementById('issuesContent').innerHTML = `
                    <div class="empty-state">
                        <div class="icon">📋</div>
                        <p>请从左侧选择一个阶段查看问题标签</p>
                    </div>
                `;
            }
            await loadStages();
        } else {
            AdminCommon.showError(response?.message || '删除失败');
        }
    } catch (error) {
        console.error('删除阶段失败:', error);
        AdminCommon.showError('删除失败');
    }
}

// ========== 问题管理 ==========

function showAddIssueModal() {
    if (!currentStageId) {
        AdminCommon.showError('请先选择一个阶段');
        return;
    }

    // 填充阶段下拉框
    const stageSelect = document.getElementById('issueStageId');
    stageSelect.innerHTML = stages.map(s => 
        `<option value="${s.id}" ${s.id === currentStageId ? 'selected' : ''}>${s.name}</option>`
    ).join('');

    document.getElementById('issueModalTitle').textContent = '添加问题';
    document.getElementById('issueId').value = '';
    document.getElementById('issueCategory').value = '';
    document.getElementById('issueName').value = '';
    document.getElementById('issueIsCustom').checked = false;
    document.getElementById('issueSort').value = issues.length;
    document.getElementById('issueModal').classList.add('show');
}

function editIssue(issueId) {
    const issue = issues.find(i => i.id === issueId);
    if (!issue) return;

    // 填充阶段下拉框
    const stageSelect = document.getElementById('issueStageId');
    stageSelect.innerHTML = stages.map(s => 
        `<option value="${s.id}" ${s.id === issue.stageId ? 'selected' : ''}>${s.name}</option>`
    ).join('');

    document.getElementById('issueModalTitle').textContent = '编辑问题';
    document.getElementById('issueId').value = issue.id;
    document.getElementById('issueStageId').value = issue.stageId;
    document.getElementById('issueCategory').value = issue.categoryName;
    document.getElementById('issueName').value = issue.issueName;
    document.getElementById('issueIsCustom').checked = issue.isCustomInput || false;
    document.getElementById('issueSort').value = issue.sortOrder || 0;
    document.getElementById('issueModal').classList.add('show');
}

function closeIssueModal() {
    document.getElementById('issueModal').classList.remove('show');
}

async function saveIssue() {
    const id = document.getElementById('issueId').value;
    const stageId = parseInt(document.getElementById('issueStageId').value);
    const categoryName = document.getElementById('issueCategory').value.trim();
    const issueName = document.getElementById('issueName').value.trim();
    const isCustomInput = document.getElementById('issueIsCustom').checked;
    const sortOrder = parseInt(document.getElementById('issueSort').value) || 0;

    if (!stageId || !categoryName || !issueName) {
        AdminCommon.showError('请填写完整信息');
        return;
    }

    const data = { stageId, categoryName, issueName, isCustomInput, sortOrder, isEnabled: true };

    try {
        let response;
        if (id) {
            response = await AdminCommon.request(`/admin/life-stages/issues/${id}`, {
                method: 'PUT',
                body: JSON.stringify(data)
            });
        } else {
            response = await AdminCommon.request('/admin/life-stages/issues', {
                method: 'POST',
                body: JSON.stringify(data)
            });
        }

        if (response && response.code === 200) {
            AdminCommon.showSuccess(id ? '更新成功' : '创建成功');
            closeIssueModal();
            // 刷新当前阶段的问题列表
            if (currentStageId) {
                await selectStage(currentStageId);
            }
            // 刷新阶段列表的计数
            await loadStages();
        } else {
            AdminCommon.showError(response?.message || '操作失败');
        }
    } catch (error) {
        console.error('保存问题失败:', error);
        AdminCommon.showError('保存失败');
    }
}

async function deleteIssue(issueId) {
    if (!AdminCommon.confirm('确定要删除该问题标签吗？')) {
        return;
    }

    try {
        const response = await AdminCommon.request(`/admin/life-stages/issues/${issueId}`, {
            method: 'DELETE'
        });

        if (response && response.code === 200) {
            AdminCommon.showSuccess('删除成功');
            if (currentStageId) {
                await selectStage(currentStageId);
            }
            await loadStages();
        } else {
            AdminCommon.showError(response?.message || '删除失败');
        }
    } catch (error) {
        console.error('删除问题失败:', error);
        AdminCommon.showError('删除失败');
    }
}

// ========== 初始化 ==========

document.addEventListener('DOMContentLoaded', () => {
    loadStages();
});
