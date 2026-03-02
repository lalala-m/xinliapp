<template>
	<view>
		<uni-card :is-shadow="false" :border="false">
			<view>
				<uni-forms labelPosition="left" :model="dataForm" ref="dataFormRef" :rules="rules" :label-width="80">
					<uni-forms-item label="旧密码" required name="password">
						<uni-easyinput type="password" v-model="dataForm.password" placeholder="请输入旧密码" />
					</uni-forms-item>
					<uni-forms-item label="新密码" required name="newPassword">
						<uni-easyinput type="password" v-model="dataForm.newPassword" placeholder="请输入新密码" />
					</uni-forms-item>
					<uni-forms-item label="确认密码" required name="confirmPassword">
						<uni-easyinput type="password" v-model="dataForm.confirmPassword" placeholder="请输入确认密码" />
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
	import { usePasswordApi } from '@/api/user';

	const dataFormRef = ref()
	const dataForm = reactive({
		password: "",
		newPassword: "",
		confirmPassword: ""
	})

	const rules = ref({
		password: {
			rules: [{
				required: true,
				errorMessage: '必填项不能为空',
				trigger: ['blur', 'change']
			}]
		},
		newPassword: {
			rules: [{
				required: true,
				errorMessage: '必填项不能为空',
				trigger: ['blur', 'change']
			}]
		},
		confirmPassword: {
			rules: [{
					required: true,
					errorMessage: '必填项不能为空',
					trigger: ['blur', 'change']
				},
				{
					validateFunction: function(rule, value, data, callback) {
						if (value !== dataForm.newPassword) {
							callback('新密码与确认密码不一致')
						}
						return true
					}
				}
			]
		}
	})


	const onSubmit = () => {
		dataFormRef.value.validate().then(res => {
			usePasswordApi(dataForm).then(res => {
				uni.showToast({
					title: '修改成功',
					duration: 2000
				});
			})
		})
	}
</script>

<style lang="scss">
	.p_avatar {
		display: flex;
		flex-direction: column;
		align-items: center;
		margin-bottom: 50rpx;
	}

	.p_btn {
		margin-top: 50rpx;
	}
</style>