import request from '../utils/request.js'
const api_name = '/pan/user'
export function login(data) {
  return request({
    url: `/pan/login`,
    method: 'post',
    params:{
			username:data.username,
			password:data.password
		}
  })
}

export function getCurrentUser() {
  return request({
    url: `${api_name}/get/login`,
    method: 'get'
  })
}

export function logout() {
  return request({
    url: `${api_name}/logout`,
    method: 'post'
  })
}
export function register(data) {
  return request({
    url: `${api_name}/register`,
    method: 'post',
		data
  })
}
export function setAvatar(file) {
	const formData = new FormData();
	  formData.append('file', file);
	
  return request({
    url: `${api_name}/uploadAvatar`,
    method: 'post',
		data:formData
  })
}
export function resetPassword(data) {
  return request({
    url: `${api_name}/resetPassword`,
    method: 'post',
		data
  })
}