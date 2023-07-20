import request from '../utils/request.js'

const api_name = '/pan/userRole'

export function getUserById(userId) {
  return request({
    url: `${api_name}/get/${userId}`,
    method: 'get'
  })
}

export function isManager() {
  return request({
    url: `${api_name}/isManager`,
    method: 'get'
  })
}

export function getInitInfo() {
  return request({
    url: `${api_name}/get/initInfo`,
    method: 'get'
  })
}

export function setRolesForUser(userId, list) {
  return request({
    url: `${api_name}/set/${userId}?rids=${list}`,
    method: 'post'
  })
}

export function getUserAndRoles(userId) {
  return request({
    url: `${api_name}/getUserAndRoles/${userId}`,
    method: 'get'
  })
}

export function releaseRoleToUser(userIds, rid) {
  return request({
    url: `${api_name}/release/${rid}?userIds=${userIds}`,
    method: 'post'
  })
}

export function grantRoleToUser(userIds, rid) {
  return request({
    url: `${api_name}/grant/${rid}?userIds=${userIds}`,
    method: 'post'
  })
}

export function allocateRoleUser(data) {
  return request({
    url: `${api_name}/page/allocate`,
    method: 'post',
    data
  })
}

export function unAllocateRoleUser(data) {
  return request({
    url: `${api_name}/page/unAllocate`,
    method: 'post',
    data
  })
}
