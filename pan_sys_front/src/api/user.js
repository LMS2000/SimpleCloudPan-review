import request from '../utils/request.js'

const api_name = '/pan/user'


export function getCurrentUser() {
  return request({
    url: `${api_name}/get/login`,
    method: 'get'
  })
}


export function login(username, password) {
  return request({
    url: `/pan/login?username=${username}&password=${password}`,
    method: 'post'
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

export function setAvatar(data) {
  return request({
    url: `${api_name}/uploadAvatar`,
    method: 'post',
    data
  })
}

// export function resetPassword(data) {
//   return request({
//     url: `${api_name}/resetPassword`,
//     method: 'post',
// 		data
//   })
// }

export function getPageUser(data) {
  return request({
    url: `${api_name}/page`,
    method: 'post',
    data
  })
}

export function changeEnable(data) {
  return request({
    url: `${api_name}/change/enable`,
    method: 'post',
    data
  })
}


export function addUser(data) {
  return request({
    url: `${api_name}/add`,
    method: 'post',
    data
  })
}

export function updateUser(data) {
  return request({
    url: `${api_name}/update`,
    method: 'post',
    data
  })
}

export function deleteUser(userIds) {
  return request({
    url: `${api_name}/delete?userIds=${userIds}`,
    method: 'post'
  })
}

export function resetPwd(password, userId) {
  return request({
    url: `${api_name}/reset/${password}/${userId}`,
    method: 'post'
  })
}

export function resetCurrentUserPwd(data) {
  return request({
    url: `${api_name}/resetPassword`,
    method: 'post',
    data
  })
}
