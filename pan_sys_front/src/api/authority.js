import request from '../utils/request.js'

const api_name = '/pan/authority'


export function getAuthorityTree() {
  return request({
    url: `${api_name}/treeList`,
    method: 'get'
  })
}

export function getAuthorityList() {
  return request({
    url: `${api_name}/list`,
    method: 'get'
  })
}

export function getAuthById(id) {
  return request({
    url: `${api_name}/${id}`,
    method: 'get'
  })
}

export function addAuthority(data) {
  return request({
    url: `${api_name}/save`,
    method: 'post',
    data
  })
}

export function udpateAuthority(data) {
  return request({
    url: `${api_name}/update`,
    method: 'post',
    data
  })
}

export function removeAuthority(id) {
  return request({
    url: `${api_name}/${id}`,
    method: 'delete'
  })
}

export function queryAuthTree(data) {
  return request({
    url: `${api_name}/list`,
    method: 'post',
    data
  })
}
