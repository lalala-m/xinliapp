// 页面加载完成后执行
document.addEventListener('DOMContentLoaded', function() {
    console.log('admin-learning.js loaded');
    AdminCommon.checkAuth();
    
    // 绑定按钮事件
    const btnAddPackage = document.getElementById('btnAddPackage');
    if (btnAddPackage) {
        console.log('btnAddPackage found, adding event listener');
        btnAddPackage.addEventListener('click', showAddPackageModal);
    } else {
        console.error('btnAddPackage button not found in DOM');
    }
    
    const btnRefresh = document.getElementById('btnRefresh');
    if (btnRefresh) {
        btnRefresh.addEventListener('click', refreshPackages);
    }

    loadPackages();
    loadCategories();
});

// 将关键函数暴露给全局 window 对象，以便 inline onclick 可以调用
window.showAddPackageModal = showAddPackageModal;
window.closePackageModal = closePackageModal;
window.savePackage = savePackage;
window.showAddVideoModal = showAddVideoModal;
window.closeVideoModal = closeVideoModal;
window.uploadVideoFile = uploadVideoFile;
window.saveVideoToPackage = saveVideoToPackage;
window.removeVideo = removeVideo;
window.editPackage = editPackage;
window.deletePackage = deletePackage;
window.searchPackages = searchPackages;
window.filterPackages = filterPackages;
window.uploadCoverImage = uploadCoverImage;

let currentPackages = [];
let currentEditingPackageId = null;
let tempVideos = []; // 暂存视频列表

// 加载分类列表
async function loadCategories() {
    // 模拟分类数据，实际应从后端获取
    const categories = ['儿童心理', '青少年成长', '亲子关系', '情绪管理', '学习障碍'];
    const filterSelect = document.getElementById('categoryFilter');
    const dataList = document.getElementById('categoryList');
    
    if (filterSelect) {
        filterSelect.innerHTML = '<option value="">全部分类</option>';
        categories.forEach(c => {
            const option = document.createElement('option');
            option.value = c;
            option.textContent = c;
            filterSelect.appendChild(option);
        });
    }
    
    if (dataList) {
        dataList.innerHTML = '';
        categories.forEach(c => {
            const option = document.createElement('option');
            option.value = c;
            dataList.appendChild(option);
        });
    }
}

// 加载学习包列表
async function loadPackages(page = 1) {
    const tableBody = document.getElementById('packageTableBody');
    AdminCommon.showLoading(tableBody);
    
    try {
        // 构建查询参数
        const params = new URLSearchParams({
            page: Math.max(page - 1, 0),
            size: 10
        });
        
        const category = document.getElementById('categoryFilter')?.value;
        const search = document.getElementById('searchInput')?.value;
        
        if (category) params.append('category', category);
        if (search) params.append('search', search);

        // TODO: 后端目前可能没有提供完整的分页接口，暂时使用 /learning/packages/page 如果有的话
        // 或者使用 /admin/learning/packages 并自行分页
        const response = await AdminCommon.request(`/learning/packages/page?${params.toString()}`);
        
        if (response && response.code === 200) {
            const pageData = response.data;
            currentPackages = pageData.content || [];
            renderPackages(currentPackages);
            // renderPagination(pageData); // 如果有分页组件
        } else {
             // Fallback for demo/dev if API not ready
             console.warn('API returned error or no data, checking structure', response);
             if (response && Array.isArray(response.data)) {
                 currentPackages = response.data;
                 renderPackages(currentPackages);
             } else {
                 tableBody.innerHTML = '<tr><td colspan="9" class="no-data">暂无数据</td></tr>';
             }
        }
    } catch (error) {
        console.error('加载学习包失败:', error);
        tableBody.innerHTML = '<tr><td colspan="9" class="error">加载失败</td></tr>';
    }
}

// 渲染学习包列表
function renderPackages(packages) {
    const tableBody = document.getElementById('packageTableBody');
    
    if (!packages || packages.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="9" class="no-data">暂无数据</td></tr>';
        return;
    }
    
    const sorted = [...packages].sort((a, b) => {
        const sa = typeof a.sortOrder === 'number' ? a.sortOrder : parseInt(a.sortOrder || '0', 10);
        const sb = typeof b.sortOrder === 'number' ? b.sortOrder : parseInt(b.sortOrder || '0', 10);
        return sa - sb;
    });
    
    tableBody.innerHTML = sorted.map(pkg => `
        <tr>
            <td>${pkg.id}</td>
            <td>
                ${pkg.coverImage ? `<img src="${pkg.coverImage}" alt="封面" style="width: 60px; height: 40px; object-fit: cover; border-radius: 4px;">` : '<span style="color:#999;">暂无</span>'}
            </td>
            <td>${pkg.title}</td>
            <td><span class="badge category">${pkg.category}</span></td>
            <td>${pkg.videoCount || 0}</td>
            <td>${pkg.totalDuration || 0}</td>
            <td>
                <span class="status-badge ${pkg.active ? 'active' : 'inactive'}">
                    ${pkg.active ? '启用' : '禁用'}
                </span>
            </td>
            <td>${pkg.sortOrder || 0}</td>
            <td>
                <div class="action-btns">
                    <button class="btn-sm btn-edit" onclick="editPackage(${pkg.id})">编辑</button>
                    <button class="btn-sm btn-delete" onclick="deletePackage(${pkg.id})">删除</button>
                </div>
            </td>
        </tr>
    `).join('');
}

// 显示添加学习包弹窗
function showAddPackageModal() {
    console.log('showAddPackageModal called');
    // alert('Add Package Button Clicked'); // Debugging
    currentEditingPackageId = null;
    const modalTitle = document.getElementById('modalTitle');
    if (modalTitle) modalTitle.textContent = '新增学习包';
    
    // 清空表单
    const pkgTitle = document.getElementById('pkgTitle');
    if (pkgTitle) pkgTitle.value = '';
    
    const pkgCategory = document.getElementById('pkgCategory');
    if (pkgCategory) pkgCategory.value = '';
    
    const pkgTags = document.getElementById('pkgTags');
    if (pkgTags) pkgTags.value = ''; 
    
    const pkgDescription = document.getElementById('pkgDescription');
    if (pkgDescription) pkgDescription.value = '';
    
    const pkgCoverImage = document.getElementById('pkgCoverImage');
    const coverPreview = document.getElementById('coverPreview');
    const coverStatus = document.getElementById('coverUploadStatus');
    if (pkgCoverImage) pkgCoverImage.value = '';
    if (coverPreview) {
        coverPreview.src = '';
        coverPreview.style.display = 'none';
    }
    if (coverStatus) {
        coverStatus.textContent = '';
        coverStatus.style.color = '#666';
    }
    
    const pkgSortOrder = document.getElementById('pkgSortOrder');
    if (pkgSortOrder) {
        let maxSort = 0;
        if (Array.isArray(currentPackages) && currentPackages.length > 0) {
            maxSort = currentPackages.reduce((max, p) => {
                const v = typeof p.sortOrder === 'number' ? p.sortOrder : parseInt(p.sortOrder || '0', 10);
                return isNaN(v) ? max : Math.max(max, v);
            }, 0);
        }
        pkgSortOrder.value = String(maxSort + 1);
    }
    
    const pkgStatus = document.getElementById('pkgStatus');
    if (pkgStatus) pkgStatus.value = 'true';
    
    // 清空暂存视频
    tempVideos = [];
    renderVideoList();

    AdminCommon.lockBodyScroll();
    
    const modal = document.getElementById('packageModal');
    if (modal) {
        modal.style.display = 'flex';
        modal.style.opacity = '1';
        modal.style.pointerEvents = 'auto';
        modal.classList.add('show', 'active');
        console.log('Modal show class added');
    } else {
        console.error('packageModal element not found');
    }
}

// 编辑学习包
async function editPackage(id) {
    const pkg = currentPackages.find(p => p.id === id);
    if (!pkg) return;
    
    currentEditingPackageId = id;
    document.getElementById('modalTitle').textContent = '编辑学习包';
    
    document.getElementById('pkgTitle').value = pkg.title;
    document.getElementById('pkgCategory').value = pkg.category;
    document.getElementById('pkgTags').value = pkg.issueTags || '';
    document.getElementById('pkgDescription').value = pkg.description || '';
    document.getElementById('pkgCoverImage').value = pkg.coverImage || '';
    document.getElementById('pkgSortOrder').value = pkg.sortOrder || 0;
    document.getElementById('pkgStatus').value = (pkg.active !== false).toString();
    
    // 加载视频
    tempVideos = [];
    try {
        if (pkg.videos) {
            tempVideos = pkg.videos;
        } else {
            const response = await AdminCommon.request(`/learning/packages/${id}`);
            if (response && response.code === 200 && response.data.videos) {
                tempVideos = response.data.videos;
            }
        }
    } catch (e) {
        console.error('加载视频失败', e);
    }
    
    renderVideoList();

    AdminCommon.lockBodyScroll();
    
    const modal = document.getElementById('packageModal');
    if (modal) {
        modal.style.display = 'flex';
        modal.style.opacity = '1';
        modal.style.pointerEvents = 'auto';
        modal.classList.add('show', 'active');
    }
}

// 渲染视频列表（暂存或已有）
function renderVideoList() {
    const container = document.getElementById('videoContainer');
    if (!tempVideos || tempVideos.length === 0) {
        container.innerHTML = '<div style="padding: 10px; color: #999; text-align: center;">暂无视频</div>';
        return;
    }
    
    container.innerHTML = tempVideos.map((video, index) => `
        <div class="video-item">
            <div style="flex: 1;">
                <div style="font-weight: bold;">${video.title}</div>
                <div style="font-size: 12px; color: #666;">时长: ${video.duration}秒</div>
                <div style="font-size: 12px; color: #999; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 300px;">${video.videoUrl}</div>
            </div>
            <div>
                <button class="action-btn delete-btn" onclick="removeVideo(${index})">删除</button>
            </div>
        </div>
    `).join('');
}

// 移除视频
async function removeVideo(index) {
    const video = tempVideos[index];
    if (video.id) {
        if (!confirm('确定要删除这个视频吗？')) return;
        try {
            await AdminCommon.request(`/admin/learning/videos/${video.id}`, { method: 'DELETE' });
        } catch (e) {
            console.error(e);
            // 即使后端删除失败，前端也先移除，或者提示错误
        }
    }
    tempVideos.splice(index, 1);
    renderVideoList();
}

// 显示添加视频弹窗
function showAddVideoModal() {
    document.getElementById('videoFile').value = '';
    document.getElementById('videoUrl').value = '';
    document.getElementById('videoTitle').value = '';
    document.getElementById('videoDuration').value = '';
    document.getElementById('videoDescription').value = '';
    document.getElementById('uploadProgress').style.display = 'none';

    AdminCommon.lockBodyScroll();
    
    const modal = document.getElementById('videoModal');
    if (modal) {
        modal.style.display = 'flex';
        modal.style.opacity = '1';
        modal.style.pointerEvents = 'auto';
        modal.classList.add('show', 'active');
    }
}

// 关闭视频弹窗
function closeVideoModal() {
    const modal = document.getElementById('videoModal');
    if (modal) {
        modal.classList.remove('show', 'active');
        modal.style.display = 'none';
        modal.style.opacity = '0';
        modal.style.pointerEvents = 'none';
    }
    AdminCommon.unlockBodyScroll();
}

// 上传视频文件
async function uploadVideoFile() {
    const fileInput = document.getElementById('videoFile');
    const file = fileInput.files[0];
    if (!file) {
        alert('请先选择视频文件');
        return;
    }
    
    const formData = new FormData();
    formData.append('file', file);
    
    const progressDiv = document.getElementById('uploadProgress');
    progressDiv.style.display = 'block';
    progressDiv.textContent = '上传中...';
    
    try {
        const token = localStorage.getItem('token');
        const baseUrl = (typeof CONFIG !== 'undefined' && CONFIG.API_BASE_URL) ? CONFIG.API_BASE_URL : 'http://localhost:8080/api';
        const response = await fetch(`${baseUrl}/admin/upload/video`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            },
            body: formData
        });
        
        const result = await response.json();
        
        if (result.code === 200) {
            document.getElementById('videoUrl').value = result.data;
            if (!document.getElementById('videoTitle').value) {
                document.getElementById('videoTitle').value = file.name;
            }
            progressDiv.textContent = '上传成功！';
            progressDiv.style.color = 'green';
        } else {
            throw new Error(result.message);
        }
    } catch (error) {
        console.error('上传失败:', error);
        progressDiv.textContent = '上传失败: ' + error.message;
        progressDiv.style.color = 'red';
    }
}

// 上传封面图片
async function uploadCoverImage() {
    const fileInput = document.getElementById('pkgCoverFile');
    const statusDiv = document.getElementById('coverUploadStatus');
    const previewImg = document.getElementById('coverPreview');
    const hiddenInput = document.getElementById('pkgCoverImage');

    if (!fileInput || !fileInput.files || !fileInput.files[0]) {
        alert('请先选择封面图片');
        return;
    }

    const file = fileInput.files[0];
    const formData = new FormData();
    formData.append('file', file);

    if (statusDiv) {
        statusDiv.style.display = 'block';
        statusDiv.style.color = '#666';
        statusDiv.textContent = '封面上传中...';
    }

    try {
        const token = localStorage.getItem('token');
        const baseUrl = (typeof CONFIG !== 'undefined' && CONFIG.API_BASE_URL) ? CONFIG.API_BASE_URL : 'http://localhost:8080/api';
        const response = await fetch(`${baseUrl}/admin/upload/image`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            },
            body: formData
        });

        const result = await response.json();

        if (result.code === 200) {
            if (hiddenInput) hiddenInput.value = result.data;
            if (statusDiv) {
                statusDiv.textContent = '封面上传成功';
                statusDiv.style.color = 'green';
            }
            if (previewImg) {
                previewImg.src = result.data;
                previewImg.style.display = 'block';
            }
        } else {
            throw new Error(result.message || '封面上传失败');
        }
    } catch (error) {
        console.error('封面上传失败:', error);
        if (statusDiv) {
            statusDiv.textContent = '封面上传失败: ' + error.message;
            statusDiv.style.color = 'red';
        }
    }
}

// 保存视频到暂存列表
function saveVideoToPackage() {
    const url = document.getElementById('videoUrl').value;
    const title = document.getElementById('videoTitle').value;
    const duration = document.getElementById('videoDuration').value;
    const desc = document.getElementById('videoDescription').value;
    
    if (!url || !title || !duration) {
        alert('请填写完整视频信息（需先上传视频）');
        return;
    }
    
    tempVideos.push({
        title: title,
        videoUrl: url,
        duration: parseInt(duration),
        description: desc
    });
    
    renderVideoList();
    closeVideoModal();
}

// 保存学习包
async function savePackage() {
    const title = document.getElementById('pkgTitle').value;
    const category = document.getElementById('pkgCategory').value;
    const tags = document.getElementById('pkgTags').value;
    const description = document.getElementById('pkgDescription').value;
    const coverImage = document.getElementById('pkgCoverImage').value;
    const sortOrder = document.getElementById('pkgSortOrder').value;
    const active = document.getElementById('pkgStatus').value === 'true';
    
    if (!title || !category) {
        alert('请填写标题和分类');
        return;
    }
    
    const packageData = {
        title,
        category,
        issueTags: tags,
        description,
        coverImage,
        sortOrder: parseInt(sortOrder),
        active
    };
    
    try {
        let packageId = currentEditingPackageId;
        
        if (packageId) {
            // 更新
            await AdminCommon.request(`/admin/learning/packages/${packageId}`, {
                method: 'PUT',
                body: JSON.stringify(packageData)
            });
        } else {
            // 新增
            const response = await AdminCommon.request('/admin/learning/packages', {
                method: 'POST',
                body: JSON.stringify(packageData)
            });
            if (response && response.code === 200) {
                packageId = response.data.id;
            }
        }
        
        // 保存新增的视频
        // 只有当 packageId 存在时才能保存视频
        if (packageId && tempVideos.length > 0) {
            const newVideos = tempVideos.filter(v => !v.id);
            for (const video of newVideos) {
                video.packageId = packageId;
                await AdminCommon.request('/admin/learning/videos', {
                    method: 'POST',
                    body: JSON.stringify(video)
                });
            }
        }
        
        closePackageModal();
        loadPackages();
        AdminCommon.showSuccess('保存成功');
        
    } catch (error) {
        console.error('保存失败:', error);
        AdminCommon.showError('保存失败');
    }
}

// 关闭弹窗
function closePackageModal() {
    const modal = document.getElementById('packageModal');
    if (modal) {
        modal.classList.remove('show', 'active');
        modal.style.display = 'none';
        modal.style.opacity = '0';
        modal.style.pointerEvents = 'none';
    }
    AdminCommon.unlockBodyScroll();
}

// 搜索/筛选
function searchPackages() {
    loadPackages(1);
}

function filterPackages() {
    loadPackages(1);
}

function refreshPackages() {
    loadPackages(1);
    AdminCommon.showSuccess('已刷新');
}

// 删除学习包
async function deletePackage(id) {
    if (!confirm('确定要删除这个学习包吗？这将同时删除其中的所有视频！')) return;
    
    try {
        const response = await AdminCommon.request(`/admin/learning/packages/${id}`, { method: 'DELETE' });
        if (response && response.code === 200) {
            AdminCommon.showSuccess('删除成功');
            loadPackages();
        }
    } catch (error) {
        console.error('删除失败:', error);
        AdminCommon.showError('删除失败');
    }
}
