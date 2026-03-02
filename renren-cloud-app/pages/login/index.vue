<template>
	<view class="login">
		<uni-card :is-shadow="false" :border="false">
			<view class="login-title">
				人人开源
			</view>
			<uni-forms ref="loginForm" :modelValue="login" label-position="left">
				<uni-forms-item label="账号" required name="username">
					<uni-easyinput v-model="login.username" placeholder="账号" />
				</uni-forms-item>
				<uni-forms-item label="密码" required name="password">
					<uni-easyinput type="password" v-model="login.password" placeholder="密码" />
				</uni-forms-item>
				<uni-forms-item label="验证码" required name="captcha">
					<uni-easyinput v-model="login.captcha" placeholder="验证码" />
				</uni-forms-item>
				<uni-forms-item label=" ">
					<image :src="captchaUrl" style="height: 40px;width: 150px;" @click="getCaptchaUrl"></image>
				</uni-forms-item>
			</uni-forms>
			<view>
				<button type="primary" @click="loginSubmit">登录</button>
			</view>
		</uni-card>
	</view>
</template>

<script setup>
	import { reactive, ref } from 'vue';
	import { onLoad } from "@dcloudio/uni-app"
	import { useLoginApi } from '@/api/login';
	import { useUserStore } from '@/store/user'
	import config from '@/config'
	import { getUuid } from '@/utils/index'

	const userStore = useUserStore()
	const loginForm = ref()
	const captchaUrl = ref()

	const login = reactive({
		username: 'admin',
		password: 'admin',
		captcha: '',
		uuid: ''
	})

	onLoad(() => {
		if (userStore.token) {
			uni.reLaunch({
				url: '/pages/index/index'
			});
		} else {
			getCaptchaUrl();
		}
	})

	const getCaptchaUrl = () => {
		login.uuid = getUuid();
		captchaUrl.value = `${config.baseUrl}/sys/auth/captcha?uuid=${login.uuid}`;
	}

	const loginSubmit = () => {
		useLoginApi(login).then(res => {
			userStore.setToken(res.data.access_token)

			uni.reLaunch({
				url: '/pages/index/index'
			});
		}).catch(e => {
			getCaptchaUrl();
		})
	}
</script>

<style lang="scss" scoped>
	.login {
		display: flex;
		flex-direction: column;
		justify-content: center;
	}

	.login-title {
		margin: 50px 0;
		text-align: center;
		font-size: 28px;
		font-weight: bold;
		color: rgb(60, 156, 255);
	}
</style>