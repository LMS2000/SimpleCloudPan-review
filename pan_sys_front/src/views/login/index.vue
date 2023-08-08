<template>
  <div class="login-container">
    <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form" auto-complete="on"
      label-position="left">

      <div class="title-container">
        <h3 class="title">Login Form</h3>
      </div>

      <el-form-item prop="username">
        <span class="svg-container">
          <svg-icon icon-class="user" />
        </span>
        <el-input ref="username" v-model="loginForm.username" placeholder="Username" name="username" type="text"
          tabindex="1" auto-complete="on" />
      </el-form-item>

      <el-form-item prop="password">
        <span class="svg-container">
          <svg-icon icon-class="password" />
        </span>
        <el-input :key="passwordType" ref="password" v-model="loginForm.password" :type="passwordType"
          placeholder="Password" name="password" tabindex="2" auto-complete="on" @keyup.enter.native="handleLogin" />
        <span class="show-pwd" @click="showPwd">
          <svg-icon :icon-class="passwordType === 'password' ? 'eye' : 'eye-open'" />
        </span>
      </el-form-item>
      <el-form-item prop="code">
       <div class="check-code-panel">
         <el-input
           size="large"
           placeholder="请输入验证码"
           v-model.trim="loginForm.code"
         >
         </el-input>
         <img
           :src="sendCheckCodeUrl"
           class="check-code"
           @click="changeCheckCode(0)"
         />
       </div>

      </el-form-item>
      <el-button :loading="loading" type="primary" style="width:100%;margin-bottom:30px;"
        @click.native.prevent="handleLogin">Login</el-button>

      <!-- <div class="tips">
        <span style="margin-right:20px;">username: admin</span>
        <span> password: any</span>
      </div> -->

    </el-form>
  </div>
</template>

<script>
  import {
    validUsername
  } from '@/utils/validate'
  import {
    getCurrentUser,
    login
  } from '../../api/user.js'
  import {
    isManager
  } from '../../api/userRole.js'
  import store from "@/store";
  export default {
    name: 'Login',
    data() {
      const validateUsername = (rule, value, callback) => {
        if (!validUsername(value)) {
          callback(new Error('请输入账号'))
        } else {
          callback()
        }
      }
      const validatePassword = (rule, value, callback) => {
        if (value.length < 6) {
          callback(new Error('请输入不小于6位的密码'))
        } else {
          callback()
        }
      }
      const validCode = (rule, value, callback) => {
        if (value.length < 5) {
          callback(new Error('请输入校验码'))
        } else {
          callback()
        }
      }
      return {
        loginForm: {
          username: 'root',
          password: '12345678',
          code: ""
        },
        loginRules: {
          username: [{
            required: true,
            trigger: 'blur',
            validator: validateUsername
          }],
          password: [{
            required: true,
            trigger: 'blur',
            validator: validatePassword
          }],
          code: [{
            required: true,
            trigger: 'blur',
            validator: validCode
          }]
        },
        loading: false,
        passwordType: 'password',
        redirect: undefined,
        sendCheckCodeUrl:'',
        checkCodeUrl: 'http://localhost:9998/pan/user/checkCode'
      }
    },
    watch: {
      $route: {
        handler: function(route) {
          this.redirect = route.query && route.query.redirect
        },
        immediate: true
      }
    },
    methods: {
      showPwd() {
        if (this.passwordType === 'password') {
          this.passwordType = ''
        } else {
          this.passwordType = 'password'
        }
        this.$nextTick(() => {
          this.$refs.password.focus()
        })
      },
      changeCheckCode(type) {
      this.sendCheckCodeUrl = this.checkCodeUrl + "?type=" + type+"&time=" + new Date().getTime();

        // if (type == 0) {
        //   checkCodeUrl.value =
        //     api.checkCode + "?type=" + type + "&time=" + new Date().getTime();
        // } else {
        //   checkCodeUrl4SendMailCode.value =
        //     api.checkCode + "?type=" + type + "&time=" + new Date().getTime();
        // }
      },
      handleLogin() {
        this.$refs.loginForm.validate(valid => {

          if (valid) {
            this.loading = true


            login(this.loginForm).then(res => {
              if (res.code == 20000) {
                localStorage.setItem('token', res.data)
                this.loading = false
                this.$router.push({
                  path: this.redirect || '/'
                })

                //获取
                getCurrentUser().then(res => {
                  var loginUser = res.data;
                  if (loginUser == null || loginUser == undefined) {
                    return;
                  }
                  window.localStorage.setItem(
                    "userInfo",
                    JSON.stringify(loginUser)
                  );
                  //校验角色
                  isManager(loginUser.userId).then(res => {
                    let flag = res.data;
                    if (!flag) {
                      this.$model.msgError("权限不够")
                      return;
                    }
                  }).catch(error => {
                    return;
                  })

                })
                this.$model.msgSuccess("登录成功")
              } else {
                this.$model.msgError("账号或密码错误")
                this.loading = false
              }
            }).catch(error => {
              this.$model.msgError("登录错误")
              this.loading = false
            })




            //   }
            // })
          } else {
            this.loading = false
            console.log('error submit!!')
            return false
          }


        })
      }
    }
  }
</script>

<style lang="scss">
  /* 修复input 背景不协调 和光标变色 */
  /* Detail see https://github.com/PanJiaChen/vue-element-admin/pull/927 */

  $bg:#283443;
  $light_gray:#fff;
  $cursor: #fff;

  @supports (-webkit-mask: none) and (not (cater-color: $cursor)) {
    .login-container .el-input input {
      color: $cursor;
    }
  }

  /* reset element-ui css */
  .login-container {
    .el-input {
      display: inline-block;
      height: 47px;
      width: 85%;

      input {
        background: transparent;
        border: 0px;
        -webkit-appearance: none;
        border-radius: 0px;
        padding: 12px 5px 12px 15px;
        color: $light_gray;
        height: 47px;
        caret-color: $cursor;

        &:-webkit-autofill {
          box-shadow: 0 0 0px 1000px $bg inset !important;
          -webkit-text-fill-color: $cursor !important;
        }
      }
    }

    .el-form-item {
      border: 1px solid rgba(255, 255, 255, 0.1);
      background: rgba(0, 0, 0, 0.1);
      border-radius: 5px;
      color: #454545;
    }
  }
</style>

<style lang="scss" scoped>
  $bg:#2d3a4b;
  $dark_gray:#889aa4;
  $light_gray:#eee;

  .login-container {
    min-height: 100%;
    width: 100%;
    background-color: $bg;
    overflow: hidden;

    .login-form {
      position: relative;
      width: 520px;
      max-width: 100%;
      padding: 160px 35px 0;
      margin: 0 auto;
      overflow: hidden;
    }

    .tips {
      font-size: 14px;
      color: #fff;
      margin-bottom: 10px;

      span {
        &:first-of-type {
          margin-right: 16px;
        }
      }
    }

    .svg-container {
      padding: 6px 5px 6px 15px;
      color: $dark_gray;
      vertical-align: middle;
      width: 30px;
      display: inline-block;
    }

    .title-container {
      position: relative;

      .title {
        font-size: 26px;
        color: $light_gray;
        margin: 0px auto 40px auto;
        text-align: center;
        font-weight: bold;
      }
    }

    .show-pwd {
      position: absolute;
      right: 10px;
      top: 7px;
      font-size: 16px;
      color: $dark_gray;
      cursor: pointer;
      user-select: none;
    }
  }
  .check-code-panel {
    width: 100%;
    display: flex;
    .check-code {
      margin-left: 5px;
      cursor: pointer;
    }
  }
</style>
