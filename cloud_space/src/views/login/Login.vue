<template>
  <div class="login">
    <div class="main">
      <div class="logoContainer">
        <div class="logo"><img src="~assets/img/logo.png" alt="" /></div>
        <div class="name">小破盘</div>
      </div>
      <div
        class="mainBox"
        :class="activeName == 'first' ? '' : 'mainBoxRegistered'"
      >
        <el-tabs
          v-model="activeName"
          type="card"
          @tab-click="handleClick"
          stretch
        >
          <el-tab-pane label="登录" name="first">
            <div class="loginInput">
              <el-form ref="form" :model="loginInfo" label-width="80px">
                <el-form-item>
                  <el-input
                    v-model="loginInfo.username"
                    placeholder="请输入用户名"
                  ></el-input>
                </el-form-item>
                <el-form-item>
                  <el-input
                    v-model="loginInfo.password"
                   show-password
                    placeholder="请输入密码"
                  ></el-input>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="onSubmit">登录</el-button>
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>
          <el-tab-pane label="注册" name="second">
            <div class="registeredInput">
              <el-form ref="form" :model="registeredInfo" label-width="80px">
                <el-form-item>
                  <el-input
                    v-model="registeredInfo.username"
                    placeholder="请输入用户名"
                  ></el-input>
                </el-form-item>
                <el-form-item>
                  <el-input
                    v-model="registeredInfo.password"
                    placeholder="请输入密码"
                   show-password
                  ></el-input>
                </el-form-item>
								<el-form-item>
								  <el-input
								    v-model="registeredInfo.checkPassword"
								    placeholder="请输入确定密码"
								    show-password
								  ></el-input>
								</el-form-item>
              
                <el-form-item>
                  <el-button type="primary" @click="clickRegistered"
                    >注册</el-button
                  >
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script>
	import axios from 'axios'
// 倒计时名称
let timer;
import Cookies from 'js-cookie';
import {login,register} from '../../api/user.js'
export default {
  name: "Login",
  data() {
    return {
      loginInfo: {
        username: "",
        password: "",
      },
      registeredInfo: {
        username: "",
        password: "",
				checkPassword:"",
        avatar: null,
      },
      activeName: "first",
      // 倒计时秒数
      countDownSecond: 60,
      // 是否显示秒数
      isCountDownShow: false,
    };
  },
  methods: {
    //   点击登录的回调
    async onSubmit() {

			axios.post('http://localhost:9998/pan/login?username='+this.loginInfo.username+"&password="
			+this.loginInfo.password, {
			}).then(res => {
			   // 处理响应的业务逻辑
				 var token= res.data.msg;
				 	console.log(res)
				 	if(res.data.code==200){

				 		
				 			   localStorage.setItem('token', token)
				 			  this.$router.push("/index");
				 	}else{
				 		 this.$message.warning("登录失败,账号或密码错误!");
				 	}
				 
			 })

			
    },

    handleClick(e) {
      console.log(e.name);
    },

    // // 获取验证码
    // async getCode() {
    //   this.isCountDownShow = true;
    //   let res = await this.$request(
    //     `/edumsm/msm/send/${this.registered.mobile}`
    //   );
    //   console.log(res);
    //   if (res.data.success) {
    //     this.startCountDown();
    //   }
    // },

    // 倒计时
    startCountDown() {
      this.countDownSecond = 60;
      timer = setInterval(() => {
        this.countDownSecond--;
        if (this.countDownSecond == 0) {
          clearInterval(timer);
          this.isCountDownShow = false;
        }
      }, 1000);
    },
    // 点击注册的回调
    async clickRegistered() {
      // let res = await this.$request(
      //   "/educenter/member/register",
      //   this.registered,
      //   "post",
      //   "params"
      // );
      // console.log(res);
      // // 如果注册成功，清空所有数据并跳转至登录界面，自动填写手机号码
      // if (res.data.success) {
      //   this.$message.success("注册成功!");
      //   this.login.mobile = this.registered.mobile;
      //   this.activeName = "first";
      //   this.registered = {
      //     mobile: "",
      //     password: "",
      //     code: "",
      //     nickname: "",
      //     avatar: null,
      //   };
      // } else {
      //   this.$message.error("注册失败,请稍后重试!");
      // }
			
			register(this.registeredInfo).then(res=>{
			    if(res.code==20000){
					this.$message.success("注册成功!");
					  this.loginInfo.username = this.registeredInfo.username;
					  this.activeName = "first";
					  this.registered = {
					    username: "",
					    password: "",
              checkPassword:""
					  };
					}else{
						this.$message.error("注册失败,请稍后重试!");
					}	
			}).catch(error=>{
				console.log(error)
				 this.$message.warning("系统错误！");
			})
    },
  },
};
</script>

<style scoped>
.login {
  background-color: #ecefff;
  height: 100vh;
}

.logoContainer {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  justify-content: center;
}

.logo {
  width: 50px;
}

.logo img {
  width: 100%;
}

.name {
  color: #25262b;
  font-size: 20px;
  letter-spacing: 4px;
  font-weight: bold;
  margin-left: 7px;
}

.main {
  width: 350px;
  height: 400px;
  position: absolute;
  right: 10vw;
  top: 15vh;
}

.mainBox {
  width: 350px;
  background-color: #fff;
  height: 330px;
  border-radius: 10px;
  overflow: hidden;
}

.mainBoxRegistered {
  height: 430px;
}

.el-form /deep/ .el-form-item__content {
  margin: 0 !important;
  padding: 0 20px;
}

.el-input /deep/ input {
  border-radius: 10px;
}

.loginInput {
  margin-top: 20px;
}

.el-tabs /deep/ .is-active,
.el-tabs /deep/ div:hover {
  color: #595bb3;
}

.el-tabs /deep/ .is-active {
  background-color: #fff;
}

.el-tabs /deep/ .el-tabs__item {
  border: none !important;
  font-size: 18px;
  height: 50px;
  line-height: 50px;
}

.el-tabs /deep/ .el-tabs__nav {
  border: none;
}

.el-tabs /deep/ .el-tabs__nav-scroll {
  background-color: #f5f5f6;
}

.el-input /deep/ .el-input__inner {
  height: 48px;
  font-size: 15px;
}

.el-button {
  width: 100%;
  background-color: #6c6dbb;
  border: none;
  border-radius: 10px;
  margin-top: 10px;
  height: 45px;
  font-size: 15px;
}

.el-button:hover {
  background-color: #595bb3;
}

.codeContainer {
  position: relative;
}

.codeButtonContainer {
  position: absolute;
  top: 50%;
  right: 30px;
  transform: translateY(-50%);
}

.getcode {
  background-color: #6c6dbb;
  color: white;
  height: 35px;
  margin: 0;
}

.countDown {
  color: rgb(141, 141, 141);
}
</style>
