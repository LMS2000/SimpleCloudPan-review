import request from '../utils/request.js'

const api_name = '/pan/roleAuthority'

export function getRoleAuthorityTree(rid) {
  return request({
    url: `${api_name}/roleTree/${rid}`,
    method: 'get'
  })
}
