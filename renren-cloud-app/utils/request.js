import { useUserStore } from '@/store/user'
import config from '@/config'


const request = options => {
	const userStore = useUserStore()

	// 如果存在token，则追加到header里面
	if (userStore.token) {
		options.header.Authorization = userStore.token
	}

	// GET请求，添加时间戳，防止缓存
	if (options.method === 'GET') {
		options.data = {
			_t: new Date().getTime()
		}
	}

	return new Promise((resolve, reject) => {
		uni.request({
			url: config.baseUrl + options.url,
			method: options.method,
			data: options.data,
			header: {
				'content-type': 'application/json',
				...options.header,
			},
			success: (res) => {
				// 处理响应
				response(res.data, resolve, reject, userStore)
			},
			fail: (err) => {
				uni.showToast({
					icon: 'none',
					title: '网络错误，稍后再试',
				})
				reject(err);
			},
		});
	});
}

/**
 * 封装上传请求
 * url: 请求地址
 * filePath: 文件本地路径
 * name: 文件名
 * data: 请求参数
 * header: 请求头
 */
const upload = (url, filePath, name, data = {}, header = {}) => {
	const userStore = useUserStore()
	// 如果存在token，则追加到header里面
	if (userStore.token) {
		header.Authorization = userStore.token
	}

	console.log('userStore.token==', userStore.token)

	return new Promise((resolve, reject) => {
		uni.uploadFile({
			url: config.baseUrl + url,
			filePath: filePath,
			name: name || 'file',
			formData: data,
			header: header,
			success: (res) => {
				// 处理响应
				response(JSON.parse(res.data), resolve, reject, userStore)
			},
			fail: (err) => {
				reject(err)
			}
		});
	})
}

const response = (res, resolve, reject, userStore) => {
	// 请求成功
	if (res.code === 0) {
		return resolve(res)
	}

	// 未登录
	if (res.code === 401) {
		userStore.removeToken()

		// 跳转到登录页
		uni.navigateTo({
			url: '/pages/login/index'
		});
		return
	}

	// 请求失败
	uni.showToast({
		icon: 'none',
		title: res.msg
	})
	reject(res)
}

// 封装 GET 请求
const get = (url, data = {}, header = {}) => {
	return request({ url, method: 'GET', data, header });
}

// 封装 POST 请求
const post = (url, data = {}, header = {}) => {
	return request({ url, method: 'POST', data, header });
}

// 封装 PUT 请求
const put = (url, data = {}, header = {}) => {
	return request({ url, method: 'PUT', data, header });
}

// 封装 DELETE 请求
const del = (url, data = {}, header = {}) => {
	return request({ url, method: 'DELETE', data, header });
}

// 导出 封装的请求方法
export default { get, post, put, del, upload };