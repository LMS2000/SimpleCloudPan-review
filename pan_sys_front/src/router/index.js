import Vue from 'vue'
import Router from 'vue-router'
import store from "@/store";

import { Message } from 'element-ui' // 消息提示组件
Vue.use(Router)

/* Layout */
import Layout from '@/layout'

/**
 * Note: sub-menu only appear when route children.length >= 1
 * Detail see: https://panjiachen.github.io/vue-element-admin-site/guide/essentials/router-and-nav.html
 *
 * hidden: true                   if set true, item will not show in the sidebar(default is false)
 * alwaysShow: true               if set true, will always show the root menu
 *                                if not set alwaysShow, when item has more than one children route,
 *                                it will becomes nested mode, otherwise not show the root menu
 * redirect: noRedirect           if set noRedirect will no redirect in the breadcrumb
 * name:'router-name'             the name is used by <keep-alive> (must set!!!)
 * meta : {
    roles: ['admin','editor']    control the page roles (you can set multiple roles)
    title: 'title'               the name show in sidebar and breadcrumb (recommend set)
    icon: 'svg-name'/'el-icon-x' the icon show in the sidebar
    breadcrumb: false            if set false, the item will hidden in breadcrumb(default is true)
    activeMenu: '/example/list'  if set path, the sidebar will highlight the path you set
  }
 */

/**
 * constantRoutes
 * a base page that does not have permission requirements
 * all roles can be accessed
 */
export const constantRoutes = [
  {
    path: '/login',
    component: () => import('@/views/login/index'),
    hidden: true
  },

  {
    path: '/404',
    component: () => import('@/views/404'),
    hidden: true
  },

  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [{
      path: 'dashboard',
      name: 'Dashboard',
      component: () => import('@/views/dashboard/index'),
      meta: { title: 'Dashboard', icon: 'dashboard' }
    }]
  },



  {
    path: '/user',
    component: Layout,
    children: [
      {
        path: 'index',
        name: 'user',
        component: () => import('@/views/user/index'),
        meta: { title: '用户管理', icon: 'form' }
      }
    ]
  },
  {
    path: '/profile',
    component: Layout,
    hidden:true,
    children: [
      {
        path: 'index',
        component: () => import('@/views/user/profile/index'),
        name: 'profile',
        meta: { title: '个人中心', icon: 'form' }
      }
    ]
  },
  {
    path: '/role',
    component: Layout,
    children: [
      {
        path: 'index',
        component: () => import('@/views/role/index'),
        name: 'role',
        meta: { title: '角色管理', icon: 'form' }
      }
    ]
  },
  {
    path: '/user-auth',
    component: Layout,
    hidden: true,
    children: [
      {
        path: 'role/:userId(\\d+)',
        component: () => import('@/views/user/authRole'),
        name: 'AuthRole',
        meta: { title: '分配角色', activeMenu: '/user' }
      }
    ]
  },
  {
    path: '/role-auth',
    component: Layout,
    hidden: true,
    children: [
      {
        path: 'user/:rid(\\d+)',
        component: () => import('@/views/role/authUser'),
        name: 'AuthUser',
        meta: { title: '分配用户', activeMenu: '/role' }
      }
    ]
  },
  {
    path: '/authority',
    component: Layout,
    children: [
      {
        path: 'index',
        component: () => import('@/views/authority/index'),
        name: 'authority',
         meta: { title: '权限管理', icon: 'form' }
      }
    ]
  },


  

  // 404 page must be placed at the end !!!
  { path: '*', redirect: '/404', hidden: true }
]

const createRouter = () => new Router({
  // mode: 'history', // require service support
  scrollBehavior: () => ({ y: 0 }),
  routes: constantRoutes
})

const router = createRouter()
 router.beforeEach((to, from, next) => {

  if (to.path != '/login') {
     const token= localStorage.getItem('token')
     const user = JSON.parse(localStorage.getItem('userInfo'))
    if (!token||!user) {
        next('/login');
        Message.warning("登录过期");
      } else {
        store.commit('SET_AVATAR',user.avatar)
        console.log(user)
         // 如果需要登录且用户未登录，重定向到登录页面
        next(); // 允许路由通过
      }
  }
  next()
})
// Detail see: https://github.com/vuejs/vue-router/issues/1234#issuecomment-357941465
export function resetRouter() {
  const newRouter = createRouter()
  router.matcher = newRouter.matcher // reset router
}

export default router
