import { defineStore } from 'pinia'
const USER_KEY = 'userinfo';
const TOKEN_KEY = 'token';

export const useUserStore = defineStore('userStore', {
	state: () => ({
		// token
		token: uni.getStorageSync(TOKEN_KEY) || null,
		// 用户信息
		user: uni.getStorageSync(USER_KEY) || {}
	}),
	actions: {
		setToken(token) {
			this.token = token;
			uni.setStorage({
				key: TOKEN_KEY,
				data: this.token
			});
		},
		removeToken() {
			this.token = null;
			uni.setStorage({
				key: TOKEN_KEY,
				data: null
			});
		},
		setUserInfo(user) {
			this.user = user;
			uni.setStorage({
				key: USER_KEY,
				data: this.user
			});
		},
		removeUserInfo() {
			this.user = {};
			uni.setStorage({
				key: USER_KEY,
				data: this.user
			});
		}
	}
})