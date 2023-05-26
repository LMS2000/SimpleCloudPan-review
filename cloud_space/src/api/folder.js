import request from '../utils/request.js'
const api_name = '/pan/folder'
export function getUserPaths(path) {
  return request({
    url: `${api_name}/getUserDir?path=${path}`,
    method: 'post'
  })
}

export function createDir(path,parentPath) {
  return request({
    url: `${api_name}/createPath?path=${path}&parentPath=${parentPath}`,
    method: 'post'
  })
}
export function deleteDir(id) {
  return request({
    url: `${api_name}/delete/`+id,
    method: 'post'
  })
}
export function renameDir(folderVo) {
  return request({
    url: `${api_name}/rename`,
    method: 'post',
		folderVo
  })
}
export function renameV2(data) {
  return request({
    url: `${api_name}/rename`,
    method: 'post',
		data
  })
}

export function downloadFolder(path) {
	const formData = new FormData();
	  formData.append('path', path);
  return request({
    url: `${api_name}/download`,
    method: 'post',
		data:formData
  })
}

export function getTierFolder() {
  return request({
    url: `${api_name}/getTierDir`,
    method: 'get'
  })
}