// 管理后台 - 咨询记录管理 JS

// API基础配置
const API_BASE = '';

// 状态
let currentPage = 0;
let pageSize = 20;
let totalPages = 0;
let currentRecordId = null;

// 加载咨询记录
async function loadRecords() {
    const tableBody = document.getElementById('recordsTableBody');
    tableBody.innerHTML = '<tr><td colspan="10" class="loading">加载中...</td></tr>';
    
    try {
        const status = document.getElementById('statusFilter').value;
        const consultant = document.getElementById('consultantFilter').value;
        const keyword = document.getElementById('searchKeyword').value;
        
        let url = `/api/consultation-records?page=${currentPage}&size=${pageSize}`;
        if (status) url += `&status=${status}`;
        
        const response = await AdminCommon.request(url);
        
        if (response && response.code === 200 && response.data) {
            renderRecords(response.data.content || []);
            totalPages = response.data.totalPages || 1;
            updatePagination();
        } else {
            // 使用模拟数据
            renderRecords(getMockRecords());
        }
    } catch (error) {
        console.error('加载咨询记录失败:', error);
        renderRecords(getMockRecords());
    }
}

// 渲染记录列表
function renderRecords(records) {
    const tableBody = document.getElementById('recordsTableBody');
    
    if (!records || records.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="10" class="loading">暂无记录</td></tr>';
        return;
    }
    
    tableBody.innerHTML = records.map(record => {
        const statusClass = getStatusClass(record.status);
        const statusText = getStatusText(record.status);
        
        // 签名状态
        const hasConsultantSig = record.consultantSignature === true;
        const hasParentSig = record.parentSignature === true;
        const signatureBadge = hasConsultantSig && hasParentSig 
            ? '<span class="status-badge completed">已签名</span>'
            : '<span class="status-badge pending">未完成</span>';
        
        // PDF状态
        const hasPdf = record.pdfFilePath;
        const pdfBadge = hasPdf 
            ? '<span style="color: #4CAF50;">✓ 已生成</span>'
            : '<span style="color: #999;">-</span>';
        
        return `
            <tr>
                <td>${record.id || 'N/A'}</td>
                <td>AP${record.appointmentId || 'N/A'}</td>
                <td>${record.consultantName || 'N/A'}</td>
                <td>${record.parentNickname || 'N/A'}</td>
                <td>${record.consultationType || 'N/A'}</td>
                <td>${formatDate(record.createdAt)}</td>
                <td><span class="status-badge ${statusClass}">${statusText}</span></td>
                <td>${signatureBadge}</td>
                <td>${pdfBadge}</td>
                <td>
                    <div class="action-btns">
                        <button class="btn-view" onclick="viewRecord(${record.id})">查看</button>
                        ${hasPdf ? `
                            <button class="btn-pdf" onclick="previewPdf(${record.id})">PDF</button>
                            <button class="btn-pdf" onclick="downloadPdf(${record.id})">下载</button>
                        ` : `
                            <button class="btn-pdf" onclick="generatePdf(${record.id})" style="background: #FF9800; color: white;">生成PDF</button>
                        `}
                        <button class="btn-signature" onclick="viewSignatures(${record.id})">签名</button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

// 获取模拟数据
function getMockRecords() {
    return [
        {
            id: 1,
            appointmentId: 1001,
            consultantName: '张咨询师',
            parentNickname: '李家长',
            consultationType: '儿童心理',
            status: 'COMPLETED',
            createdAt: '2026-04-27T10:00:00',
            consultantSignature: true,
            parentSignature: true,
            pdfFilePath: '/uploads/test.pdf'
        },
        {
            id: 2,
            appointmentId: 1002,
            consultantName: '王咨询师',
            parentNickname: '赵家长',
            consultationType: '学习困难',
            status: 'PENDING',
            createdAt: '2026-04-27T14:00:00',
            consultantSignature: false,
            parentSignature: false,
            pdfFilePath: null
        },
        {
            id: 3,
            appointmentId: 1003,
            consultantName: '李咨询师',
            parentNickname: '孙家长',
            consultationType: '行为问题',
            status: 'COMPLETED',
            createdAt: '2026-04-26T09:00:00',
            consultantSignature: true,
            parentSignature: true,
            pdfFilePath: null
        }
    ];
}

// 查看记录详情
function viewRecord(recordId) {
    window.open(`consultation-record-detail.html?recordId=${recordId}`, '_blank');
}

// 生成PDF
async function generatePdf(recordId) {
    if (!confirm('确定要生成PDF报告吗？')) return;
    
    try {
        const response = await AdminCommon.request(`/api/consultation/pdf/generate/${recordId}`, {
            method: 'POST'
        });
        
        if (response && response.success) {
            alert('PDF生成成功！');
            loadRecords();
        } else {
            alert('PDF生成失败: ' + (response.message || '未知错误'));
        }
    } catch (error) {
        alert('PDF生成失败: ' + error.message);
    }
}

// 预览PDF
async function previewPdf(recordId) {
    currentRecordId = recordId;
    const modal = document.getElementById('pdfModal');
    const iframe = document.getElementById('pdfIframe');
    
    iframe.src = '';
    modal.classList.add('show');
    
    // 获取PDF预览URL
    const previewUrl = `/api/consultation/pdf/preview/${recordId}`;
    iframe.src = previewUrl;
}

// 下载PDF
function downloadPdf(recordId) {
    window.open(`/api/consultation/pdf/download/${recordId}`, '_blank');
}

// 查看签名
function viewSignatures(recordId) {
    window.open(`signature-view.html?recordId=${recordId}`, '_blank');
}

// 导出记录
function exportRecords() {
    alert('导出功能开发中...');
}

// 刷新记录
function refreshRecords() {
    loadRecords();
}

// 清除筛选
function clearFilters() {
    document.getElementById('statusFilter').value = '';
    document.getElementById('consultantFilter').value = '';
    document.getElementById('startDate').value = '';
    document.getElementById('endDate').value = '';
    document.getElementById('searchKeyword').value = '';
    currentPage = 0;
    loadRecords();
}

// 上一页
function prevPage() {
    if (currentPage > 0) {
        currentPage--;
        loadRecords();
    }
}

// 下一页
function nextPage() {
    if (currentPage < totalPages - 1) {
        currentPage++;
        loadRecords();
    }
}

// 更新分页
function updatePagination() {
    document.getElementById('pageInfo').textContent = `第 ${currentPage + 1} 页，共 ${totalPages} 页`;
    document.getElementById('prevPage').disabled = currentPage === 0;
    document.getElementById('nextPage').disabled = currentPage >= totalPages - 1;
}

// 关闭PDF弹窗
function closePdfModal() {
    const modal = document.getElementById('pdfModal');
    const iframe = document.getElementById('pdfIframe');
    modal.classList.remove('show');
    iframe.src = '';
}

// 辅助函数
function getStatusClass(status) {
    switch (status) {
        case 'COMPLETED': return 'completed';
        case 'PENDING': return 'pending';
        case 'CANCELLED': return 'cancelled';
        default: return 'pending';
    }
}

function getStatusText(status) {
    switch (status) {
        case 'COMPLETED': return '已完成';
        case 'PENDING': return '待完成';
        case 'CANCELLED': return '已取消';
        default: return '未知';
    }
}

function formatDate(dateStr) {
    if (!dateStr) return 'N/A';
    const date = new Date(dateStr);
    return date.toLocaleDateString('zh-CN');
}

// 加载咨询师列表
async function loadConsultants() {
    try {
        const response = await AdminCommon.request('/api/consultants');
        if (response && response.code === 200) {
            const select = document.getElementById('consultantFilter');
            const consultants = response.data || [];
            consultants.forEach(c => {
                const option = document.createElement('option');
                option.value = c.id;
                option.textContent = c.name;
                select.appendChild(option);
            });
        }
    } catch (error) {
        console.error('加载咨询师列表失败:', error);
    }
}

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', () => {
    // 初始化侧边栏
    initSidebar('consultation-records');
    
    // 加载数据
    loadRecords();
    loadConsultants();
    
    // 点击弹窗外部关闭
    document.getElementById('pdfModal').addEventListener('click', (e) => {
        if (e.target.id === 'pdfModal') {
            closePdfModal();
        }
    });
});