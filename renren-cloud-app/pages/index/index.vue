<template>
	<uni-list>
		<uni-list :border="true">
			<uni-list-chat :avatar-circle="true" title="人人微服务" avatar="/static/ico.jpg" note="renren-cloud 4.3 发布：人人微服务"
				badge-positon="left" badge-text="dot">
				<view class="chat-custom-right">
					<text class="chat-custom-text">刚刚</text>
					<uni-icons type="star-filled" color="#999" size="18"></uni-icons>
				</view>
			</uni-list-chat>
			<uni-list-chat :avatar-circle="true" title="人人企业版" avatar="/static/ico.jpg" note="renren-security 4.3 发布：人人企业版"
				badge-positon="left" badge-text="dot">
				<view class="chat-custom-right">
					<text class="chat-custom-text">刚刚</text>
					<uni-icons type="star-filled" color="#999" size="18"></uni-icons>
				</view>
			</uni-list-chat>
			<uni-list-chat :avatar-circle="true" title="人人低代码" avatar="/static/ico.jpg" note="renren-lowcode 2.0 发布：人人低代码"
				time="2024-10-08"></uni-list-chat>
			<uni-list-chat :avatar-circle="true" title="人人微服务" avatar="/static/ico.jpg" note="renren-cloud 4.2 发布：人人微服务"
				time="2024-06-06"></uni-list-chat>
			<uni-list-chat :avatar-circle="true" title="人人企业版" avatar="/static/ico.jpg" note="renren-security 4.2 发布：人人企业版"
				time="2024-05-17"></uni-list-chat>
			<uni-list-chat :avatar-circle="true" title="人人微服务" avatar="/static/ico.jpg" note="renren-cloud 4.2 发布：人人微服务"
				time="2024-03-06"></uni-list-chat>
			<uni-list-chat :avatar-circle="true" title="人人企业版" avatar="/static/ico.jpg" note="renren-security 4.1 发布：人人企业版"
				time="2024-03-05"></uni-list-chat>
			<uni-list-chat :avatar-circle="true" title="人人微服务" avatar="/static/ico.jpg" note="renren-cloud 4.0 发布：人人微服务"
				time="2023-12-29"></uni-list-chat>
			<uni-list-chat :avatar-circle="true" title="人人企业版" avatar="/static/ico.jpg" note="renren-security 4.0 发布：人人企业版"
				time="2023-12-29"></uni-list-chat>
		</uni-list>
	</uni-list>
</template>

<script setup>

</script>

<style>
	.chat-custom-right {
		flex: 1;
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		flex-direction: column;
		justify-content: space-between;
		align-items: flex-end;
	}

	.chat-custom-text {
		font-size: 12px;
		color: #999;
	}
</style>