import request from '@/utils/request'

/**
 * 登录接口
 */
export function useLoginApi(data) {
	return request.post('/sys/auth/login', data)
}


/**
 * 退出接口
 */
export function useLogoutApi() {
	return request.post('/sys/auth/logout')
}