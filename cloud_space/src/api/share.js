import request from '../utils/request.js'
const api_name = '/pan/share'

export function createLink(data) {
  return request({
    url: `${api_name}/createLink`,
    method: 'post',
		data
  })
}

export function importSharedResource(key,secret,path) {
  return request({
    url: `${api_name}/${key}/${secret}?curPath=${path}`,
    method: 'post'
  })
}

export function cancelShared(data) {
  return request({
    url: `${api_name}/cancel`,
    method: 'post',
		data
  })
}