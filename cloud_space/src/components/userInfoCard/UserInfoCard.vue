<template>
	<div class="UserInfoCard" v-if="userInfo">
		<div class="storageProgressContainer">
			<div class="">{{ storageSize }}MB/1G</div>
			<el-progress :percentage="
          storageProgress.toFixed(0) * 1 ? storageProgress.toFixed(0) * 1 : 0
        " :format="() => storageProgress + '%'" color="#696bcc" class="storageProgress"></el-progress>
		</div>

		<div class="avatar">
			<img :src="userInfo.avatar" alt="" v-if="userInfo.avatar" />
			<img src="~assets/img/avatar.png" alt="" v-else />
		</div>
		<div class="userName">{{ userInfo.nickname }}</div>
		<div class="menuContainer">
			<div class="menu" :class="$store.state.isUserInfoCardMenuShow ? 'showMenu' : ''">
				<div class="group">
					<el-upload action="http://localhost:8080/pan/user/uploadAvatar" multiple :show-file-list="false"
						:before-upload="beforeUploadFile">
						<div class="menuItem">更换头像</div>
					</el-upload>
					<!-- <div class="menuItem" @click="changeNickName">修改昵称</div> -->
				</div>
				<div class="group">
					<div class="menuItem" @click="isResetPasswordDialogShow=true">修改密码</div>
						<div class="menuItem" @click="isAboutDialogShow = true">关于</div>
				</div>
				<div class="group">
					<div class="menuItem" @click="userLogout">退出登录</div>
				</div>
			</div>
			<i class="iconfont icon-setting" @click="showMenu"></i>
		</div>

		<!-- 修改昵称的输入框dialog -->
		<el-dialog title="修改密码" :visible.sync="isResetPasswordDialogShow" width="400px">
			<el-form>

				<el-form-item label="旧密码:" label-width="120px">
					<el-input show-password v-model="resetPasswordInfo.oldPassword" autocomplete="off" class="nickNameInput">
					</el-input>

				</el-form-item>
				<el-form-item label="新密码:" label-width="120px">
					<el-input show-password v-model="resetPasswordInfo.newPassword" autocomplete="off" class="nickNameInput">
					</el-input>


				</el-form-item>
			</el-form>

			<div slot="footer" class="dialog-footer">
				<el-button @click="isResetPasswordDialogShow = false" size="small">取 消</el-button>
				<el-button type="primary" @click="resetPwd" size="small">确 定</el-button>
			</div>
		</el-dialog>

		<!-- '关于'框dialog -->
		<el-dialog title="关于小破盘" :visible.sync="isAboutDialogShow" width="500px" class="aboutDialog">
			本项目是参考了gitee上的小破盘项目的前端，但是很多组件不好改就自己写了，后端使用springboot,mp等框架，采用RBAC认证方式，文件上传采用异步任务
			删除文件使用的定时任务
		</el-dialog>
	</div>
</template>

<script>
	import {
		uploadFile
	} from '../../api/file.js';
	import {
		getCurrentUser,
		logout,
		setAvatar,
		resetPassword
	} from '../../api/user.js'
	export default {
		name: "UserInfoCard",
		data() {
			return {
				resetPasswordInfo: {
					oldPassword: '',
					newPassword: ''
				},
				userInfo: {},
				// 是否显示menu
				isMenuShow: false,
				// 是否显示修改昵称的dialog
				isResetPasswordDialogShow: false,
				// 新的昵称
				newNickName: "",
				// 是否显示about的dialog
				isAboutDialogShow: false,
			};
		},
		methods: {
			//修改密码

			resetPwd() {
				resetPassword(this.resetPasswordInfo).then(res => {
					if (res.code == 20000) {
						this.$message.success("修改密码成功");
					}
					this.$message.success("跳转登录");
					 this.userLogout()
					 this.$router.push("/login");
				}).catch(error => {
					console.log(error)
					this.$message.error("修改密码失败，请稍后重试!");
				})

			},
			// 点击退出登录的回调
			userLogout() {
				logout().then(res => {
					localStorage.setItem('token', '')
				}).catch(error => {
					console.log(error)
				})
				window.localStorage.removeItem("userInfo");
				this.$store.commit("updateUserInfo", {});
				this.$router.push("/login");

			},

			// 显示菜单
			showMenu() {
				setTimeout(() => {
					console.log(this.$store.state.isUserInfoCardMenuShow)
				 let state=  this.$store.state.isUserInfoCardMenuShow;
				 if(state){
					 this.$store.commit("updateIsUserInfoCardMenuShow", false);
				 }else{
					 	this.$store.commit("updateIsUserInfoCardMenuShow", true);
				 }
				});
			},

			// 请求用户信息
			async getUserInfo() {
				// let res = await this.$request(
				//   `/educenter/member/getMemberInfo/${this.$store.state.userInfo.id}`
				// );
				getCurrentUser().then(res => {
					this.userInfo = res.data;
					this.$store.commit("updateUserInfo", this.userInfo);
				}).catch(error => {
					console.log(error)
				})
				// console.log(res);

			},

			// 上传成功的钩子
			beforeUploadFile(file) {

				setAvatar(file).then(res => {
					if (res.code == 20000) {
						let data = JSON.parse(JSON.stringify(this.userInfo));
						data.avatar = res.data;
						this.userInfo.avatar = res.data
						console.log(this.userInfo)
					}
				}).catch(error => {
					console.log(error)
					this.$message.error("头像设置失败，请稍后重试!");
				})
			},

			// 上传失败的钩子
			onError(err) {
				//   console.log(err);
				this.$message.error("头像设置失败，请稍后重试!");
			},

		},
		created() {
			this.getUserInfo();
		},
		mounted() {},
		computed: {
			// 内存进度条
			storageProgress() {
				return ((this.userInfo.useQuota / 10485760 / 1024) * 100).toFixed(2) * 1;
			},
			storageSize() {
				return (this.userInfo.useQuota / 10485760).toFixed(2);
			},
		},
		watch: {
			"$store.state.userInfo"(current) {
				this.userInfo = current;
			},
			// 上面监听不到内存属性的变化，因为但内存属性发生改变时，userInfo的地址没有发生变化
			"$store.state.userInfo.useQuota"(current) {
				this.userInfo.useQuota = current;
			},
		},
	};
</script>

<style scoped>
	.UserInfoCard {
		height: 75px;
		width: 240px;
		border-top: 1px solid #ccc;
		display: flex;
		align-items: center;
		padding: 20px;
		box-sizing: border-box;
		color: #25262b;
		position: relative;
	}

	.storageProgressContainer {
		position: absolute;
		top: -45px;
		left: 25px;
		width: 100%;
		font-size: 13px;
	}

	.storageProgress {
		display: flex;
		align-items: center;
		width: 85%;
		align-items: center;
		margin-top: 5px;
	}

	.storageProgress /deep/ .el-progress__text {
		font-size: 13px !important;
		margin-left: 12px;
	}

	.avatar img {
		width: 40px;
		height: 40px;
		object-fit: cover;
		border-radius: 50%;
	}

	.userName {
		font-size: 14px;
		margin-left: 15px;
		width: 110px;
		text-overflow: ellipsis;
		overflow: hidden;
		white-space: nowrap;
	}

	.iconfont {
		font-size: 18px;
		color: rgb(104, 104, 104);
		cursor: pointer;
	}

	.menuContainer {
		position: absolute;
		right: 20px;
	}

	.menu {
		position: absolute;
		background-color: white;
		/* display: none; */
		bottom: 30px;
		width: 150px;
		border: 1px solid #ddd;
		box-shadow: 0px 0px 10px 1px rgba(0, 0, 0, 0.1);
		z-index: 3000;
		padding: 5px 5px;
		border-radius: 7px;
		font-size: 15px;
		display: none;
	}

	.UserInfoCard /deep/ .el-upload {
		width: 100%;
		cursor: unset;
		text-align: left;
	}

	.showMenu {
		display: block;
	}

	.group {
		padding: 4px 0;
		border-bottom: 1px solid #eee;
	}

	.group:last-child {
		border: none;
	}

	.group>div {
		padding: 4px 20px;
		font-size: 14px;
		color: rgb(70, 70, 70);
		user-select: none;
	}

	.group>div:hover {
		background-color: #696bcc;
		color: white;
	}

	.UserInfoCard /deep/ .el-dialog {
		border-radius: 10px;
	}

	.UserInfoCard /deep/ .aboutDialog {
		line-height: 20px;
	}

	.UserInfoCard /deep/ .aboutDialog .el-dialog__body {
		padding-top: 10px;
	}
</style>
