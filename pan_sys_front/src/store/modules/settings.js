import defaultSettings from '@/settings'

const {
  showSettings,
  fixedHeader,
  sidebarLogo
} = defaultSettings

const state = {
  showSettings: showSettings,
  fixedHeader: fixedHeader,
  sidebarLogo: sidebarLogo
}

const mutations = {

  SET_AVATAR(state, avatar) {
    state.user.avatar = avatar;
  }
}

const actions = {
  changeSetting({
                  commit
                }, data) {
    commit('CHANGE_SETTING', data)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
