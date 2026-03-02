import request from '@/utils/request'

/**
 * 用户信息
 */
export function useUserInfoApi() {
	return request.get('/sys/user/info')
}

/**
 * 修改密码接口
 */
export function usePasswordApi(data) {
	return request.put('/sys/user/password', data)
}

/**
 * 修改用户信息接口
 */
export function useUpdateUserApi(data) {
	return request.put('/sys/user/app', data)
}