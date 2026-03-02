<template>
	<view class="content">
		<image class="logo" src="/static/logo.png"></image>
		<view class="intro">
			<uni-card>
				湖南众码网络科技有限公司（Hunan ZhongMa Network Technology co.,
				LTD）是一家高新技术企业，致力面向企事业单位及软件开发商提供基于JAVA的基础平台及解决方案，企业用户涉及国内外软件服务、航空运输、政府采购、高校科研、地产、保险等行业。
			</uni-card>
		</view>
	</view>
</template>

<script setup>

</script>

<style>
	page {
		background-color: rgb(71 172 161);
	}

	.content {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
	}

	.logo {
		margin-top: 30px;
		width: 120px;
		height: 48px;
	}

	.intro {
		display: flex;
		justify-content: center;
		font-size: 36rpx;
		color: #8f8f94;
		margin-top: 20px;
	}
</style>