import request from '../utils/request.js'

const api_name = '/pan/role'

export function pageRole(data) {
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

export function getRoleById(rid) {
  return request({
    url: `${api_name}/${rid}`,
    method: 'get'
  })
}

export function addRole(data) {
  return request({
    url: `${api_name}/add`,
    method: 'post',
    data
  })
}

export function updateRole(data) {
  return request({
    url: `${api_name}/update`,
    method: 'post',
    data
  })
}

export function removeRoles(list) {
  return request({
    url: `${api_name}/delete?rids=${list}`,
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
