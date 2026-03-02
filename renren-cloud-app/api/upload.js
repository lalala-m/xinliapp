import request from '@/utils/request'


/**
 * 上传文件
 */
export function useUploadApi(filePath) {
	return request.upload('/oss/file/upload', filePath)
}