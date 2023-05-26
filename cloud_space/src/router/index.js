import Vue from 'vue'
import VueRouter from 'vue-router'

import {getCurrentUser} from '../api/user.js'
const Index = () => import('views/index.vue')
const Files = () => import('views/files/Files.vue')
const Login = () => import('views/login/Login.vue')


const routes = [
  { path: '/', redirect: '/index' },
  {
    path: '/index',
    component: Index,
    redirect: '/files/%2Froot',
    children: [
      { path: '/files', redirect: '/files/%2Froot' },
      { path: '/files/:path', name: 'files', component: Files },
    ]
  },
  { path: '/login', component: Login },
]

Vue.use(VueRouter)



const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

router.beforeEach((to, from, next) => {
	
  if (to.path != '/login') {
		getCurrentUser().then(res=>{
		  var loginUser= res.data;
			if(loginUser==null||loginUser==undefined){
			
				router.replace('/login')
			}
			window.localStorage.setItem(
						    "userInfo",
						    JSON.stringify(loginUser)
						  );
		}).catch(error=>{
			console.log(error)
			
			 router.replace('/login')
		})
  }
  next()
})

export default router
