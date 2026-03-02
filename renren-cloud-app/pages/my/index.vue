<template>
	<view>
		<navigator url="/pages/my/personal" hover-class="hover-class">
			<view class="personal">
				<view class="personal-main">
					<image :src="user.headUrl" class="avatar"></image>
					<view class="personal-info">
						<view>{{user.username}}</view>
					</view>
				</view>
				<view class="right_icon">
					<uni-icons type="forward" size="20" color="#ccc"></uni-icons>
				</view>
			</view>
		</navigator>

		<view class="n-p" v-for="(item,index) in list" :key="index" :class="item.class" hover-class="hover-class"
			@click="onClick(item)">
			<view style="position: relative">
				<view class="p-left">
					<uni-icons :type="item.icon" size="20" :color="item.iconBackground"></uni-icons>
				</view>
			</view>
			<view class="p-right">
				<view class="p-right-main">
					<view class="p-right-main-name">{{item.name}}</view>
				</view>
				<view>
					<view class="right_icon">
						<uni-icons type="forward" size="20" color="#ccc"></uni-icons>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { ref } from 'vue';
	import { onLoad } from "@dcloudio/uni-app"
	import { useLogoutApi } from '@/api/login';
	import { useUserInfoApi } from '@/api/user';
	import { useUserStore } from '@/store/user'

	const userStore = useUserStore()
	const user = ref({})

	const list = [{
		name: '个人信息',
		id: 'personal',
		icon: 'person',
		iconBackground: '#398c0c',
	}, {
		name: '修改密码',
		id: 'password',
		icon: 'locked',
		iconBackground: '#3e26b8',
	}, {
		name: '关于我们',
		id: 'about',
		icon: 'bars',
		iconBackground: '#5fba97',
	}, {
		name: '退出登录',
		id: 'logout',
		class: 'new_line',
		icon: 'minus',
		iconBackground: 'red',
	}]

	onLoad(() => {
		// 获取用户信息
		useUserInfoApi().then(res => {
			user.value = res.data
			userStore.setUserInfo(res.data)
		});
	})

	const onClick = (item) => {
		if (item.id === 'logout') {
			useLogoutApi().then(res => {
				userStore.removeToken()
				userStore.removeUserInfo()

				uni.reLaunch({
					url: '/pages/login/index'
				});
			})
		} else {
			uni.navigateTo({
				url: '/pages/my/' + item.id
			});
		}
	}
	const onPersonal = () => {
		uni.navigateTo({
			url: '/pages/my/personal'
		});
	}
</script>

<style>
	page {
		background-color: #f7f7f7;
	}

	.personal {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 30rpx 0 40rpx 30rpx;
		background-color: #ffffff;
		margin-bottom: 25rpx;
	}

	.personal-main {
		display: flex;
		align-items: center;
	}

	.personal-info {
		display: flex;
		flex-direction: column;
	}

	.avatar {
		width: 120rpx;
		height: 120rpx;
		margin-right: 30rpx;
		border-radius: 50%;
		overflow: hidden;
	}

	.avatar image {
		width: 100%;
		height: 100%;
		object-fit: cover;
	}

	.navbar-right {
		padding: 0 40rpx;
	}

	.navbar-right-icon {
		margin-left: 50rpx;
	}

	.hover-class {
		background-color: #efefef;
	}

	.n-p {
		display: flex;
		align-items: center;
		background-color: #ffffff;
		border-bottom: solid 1px #efefef;
	}

	.new_line {
		margin-top: 25rpx;
	}


	.right_icon {
		position: relative;
		margin-right: 40rpx;
	}


	.p-left {
		display: flex;
		align-items: center;
		justify-content: center;
		width: 60rpx;
		height: 50rpx;
		padding: 10rpx;
		margin: 20rpx;
		color: #FFFFFF;
		border-radius: 10rpx;

	}

	.p-right {
		height: 50rpx;
		flex: 1;
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.p-right-main {
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.p-right-main-name {
		font-size: 32rpx;
		font-weight: 500;
	}
</style>