<template>
	<view>
		<uni-card :is-shadow="false" :border="false">
			<view class="container">
				<image class="avatar" :src="user.headUrl" @click="uploadAvatar">
				</image>
			</view>
			<view>
				<uni-forms labelPosition="left" :model="user" :rules="rules" ref="form" :label-width="50">
					<uni-forms-item label="姓名" name="realName">
						<uni-easyinput v-model="user.realName" />
					</uni-forms-item>
					<uni-forms-item label="性别" name="gender">
						<uni-data-checkbox v-model="user.gender" :localdata="genderList" />
					</uni-forms-item>
					<uni-forms-item label="邮箱" name="email">
						<uni-easyinput type="email" v-model="user.email" />
					</uni-forms-item>
					<uni-forms-item label="手机" name="mobile">
						<uni-easyinput v-model="user.mobile" />
					</uni-forms-item>
				</uni-forms>
				<view class="p_btn">
					<button type="primary" @click="onSubmit">提交</button>
				</view>
			</view>
		</uni-card>
	</view>
</template>

<script setup>
	import { reactive, ref } from 'vue';
	import { onLoad } from "@dcloudio/uni-app"
	import { useUserInfoApi, useUpdateUserApi } from '@/api/user';
	import { useUploadApi } from '@/api/upload';
	import { useUserStore } from '@/store/user'

	const userStore = useUserStore();

	const user = reactive({
		id: '',
		headUrl: '',
		realName: '',
		gender: 0,
		email: '',
		mobile: ''
	});

	const genderList = ref([{
		text: '男',
		value: 0
	}, {
		text: '女',
		value: 1
	}, {
		text: '保密',
		value: 2
	}])

	onLoad(() => {
		// 获取用户信息
		useUserInfoApi().then(res => {
			Object.assign(user, res.data)
		});
	})

	const uploadAvatar = async () => {
		// 获取选择的头像路径
		const tempFilePath = await new Promise((resolve, reject) => {
			uni.chooseImage({
				count: 1,
				sourceType: ['album', 'camera'],
				success: (res) => {
					resolve(res.tempFilePaths[0])
				},
				fail: (err) => reject(err)
			});
		});

		// 上传头像
		const { data } = await useUploadApi(tempFilePath)
		user.headUrl = data.src;
	}

	const form = ref();
	const rules = ref({
		realName: {
			rules: [{
				required: true,
				errorMessage: '必填项不能为空',
				trigger: ['blur', 'change']
			}]
		},
		email: {
			rules: [{
				required: true,
				errorMessage: '必填项不能为空',
				trigger: ['blur', 'change']
			}, {
				pattern: /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/,
				errorMessage: '邮箱不正确',
				trigger: ['blur', 'change']
			}]
		},
		mobile: {
			rules: [{
					required: true,
					errorMessage: '必填项不能为空',
					trigger: ['blur', 'change']
				},
				{
					pattern: /^1[3-9]\d{9}$/,
					errorMessage: '手机号码不正确',
					trigger: ['blur', 'change']
				}
			]
		}
	})

	const onSubmit = () => {
		form.value.validate().then(res => {
			useUpdateUserApi(user).then(res => {
				uni.showToast({
					icon: 'none',
					title: '操作成功！'
				})

				// 修改store用户信息
				const userInfo = userStore.user;
				userInfo.headUrl = user.headUrl;
				userStore.setUserInfo(userInfo)
			})
		})
	}
</script>

<style lang="scss" scoped>
	.container {
		display: flex;
		flex-direction: column;
		align-items: center;
		margin-bottom: 50rpx;
	}

	.avatar {
		width: 200rpx;
		height: 200rpx;
		border-radius: 50%;
		overflow: hidden;
	}

	.avatar image {
		width: 100%;
		height: 100%;
		object-fit: cover;
	}

	.p_btn {
		margin-top: 50rpx;
	}
</style>