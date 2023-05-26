<template>

	<!-- 	设置操作新建多选 -->

	<div class="Files">







		<!-- 导入分享资源 -->
		<el-dialog title="导入分享资源" :visible.sync="dialogImportShareVisiable">
			<el-form ref="sluiceForm">

				<el-form-item label="分享链接:" label-width="120px">
					<el-input v-model="sharedResourceInfo.sharedLink"></el-input>
				</el-form-item>

				<el-form-item label="分享码:" label-width="120px">
					<el-input v-model="sharedResourceInfo.sharedSecret"></el-input>
				</el-form-item>
			</el-form>
			<div slot="footer" class="dialog-footer">
				<el-button @click="dialogImportShareVisiable=false">取 消</el-button>
				<el-button type="primary" @click="importSharedFileOrFolder">确 定</el-button>
			</div>
		</el-dialog>






		<!-- 显示分享链接和分享码 -->
		<el-dialog title="生成分享链接" :visible.sync="dialogShareLinkVisiable">
			<el-form ref="sluiceForm">

				<el-form-item label="分享链接:" label-width="120px">
					<el-input v-model="shareUrl"></el-input>
				</el-form-item>

				<el-form-item label="分享码:" label-width="120px">
					<el-input v-model="shareInfo.shareSecretKey"></el-input>
				</el-form-item>
			</el-form>
			<div slot="footer" class="dialog-footer">
				<el-button @click="closeShareLinkDialog">取 消</el-button>
				<el-button type="primary" @click="closeShareLinkDialog">确 定</el-button>
			</div>
		</el-dialog>


		<!--  建立分享链接 -->
		<el-dialog title="建立分享链接" :visible.sync="dialogShareVisible">
			<el-form>
				<el-form-item label="过期时间:" label-width="120px">
					<el-radio-group v-model="shareInfo.expiration">
						<el-radio label="1">1天</el-radio>
						<el-radio label="3">3天</el-radio>
						<el-radio label="7">一周</el-radio>
					</el-radio-group>
				</el-form-item>

				<el-form-item label="下载次数:" label-width="120px">
					<el-input-number v-model="shareInfo.downloadCount" controls-position="right" :min="10" :max="100">
					</el-input-number>
				</el-form-item>

				<el-form-item label="分享码:" label-width="120px">
					<el-input v-model="shareInfo.shareSecretKey"></el-input>
				</el-form-item>
			</el-form>
			<div slot="footer" class="dialog-footer">
				<el-button @click="dialogShareVisible=false;">取 消</el-button>
				<el-button type="primary" @click="createShareLink">确 定</el-button>
			</div>
		</el-dialog>








		<!-- 修改对话框 -->
		<el-dialog title="修改名称" :visible.sync="dialogRenameVisible">
			<el-form ref="sluiceForm">

				<el-form-item label="文件名:" label-width="120px">
					<el-input v-model="updateInfo.newName"></el-input>
				</el-form-item>
			</el-form>
			<div slot="footer" class="dialog-footer">
				<el-button @click="dialogRenameVisible=false;updateInfo.newName=''">取 消</el-button>
				<el-button type="primary" @click="doRename()">确 定</el-button>
			</div>
		</el-dialog>


		<!-- 新建文件夹对话框 -->
		<el-dialog title="新建文件夹" :visible.sync="dialogFolderVisible">
			<el-form ref="sluiceForm">

				<el-form-item label="文件夹名:" label-width="120px">
					<el-input v-model="mkdirName"></el-input>
				</el-form-item>
			</el-form>
			<div slot="footer" class="dialog-footer">
				<el-button @click="dialogFolderVisible=false;mkdirName=''">取 消</el-button>
				<el-button type="primary" @click="createFolder()">确 定</el-button>
			</div>
		</el-dialog>


		<!--    头部的查询新建等按钮 -->
		<div class="query-form" style="margin-top: 50px;">
			<!-- 在定义表单时 会增加一个model对象,作为子表单的父级对象, 否则取参数不方便,
		         挨个取 要在data中定义很多值, 通过外层对象去统一控制 -->
			<el-form :inline="true" ref="ruleFormRef">
				<!--   
		            prop的作用是底层可以获取到你的表单对象
		            prop 要和表单项的双向绑定的属性名一致,
		            这样 prop 才能关联到每一个表单项的属性值,
		            校验时,根据校验规则对关联的属性值对齐进行校验。
		            最后清空时,将prop关联的属性值清空, v-model 对应的视图就清空了
		          -->

				<el-form-item>

					<el-input style="width: 200px;" placeholder="请输入搜索内容" v-model="searchFile" clearable>
					</el-input>
					<el-button type="primary" icon="el-icon-search" @click="searchFileByName">
						搜索
					</el-button>
				</el-form-item>


				<el-form-item>
					<el-upload multiple action="http://localhost:8080/pan/file/upload" :before-upload="beforUploadFile"
						class="uploadButton" :show-file-list="false">
						<el-button type="primary" size="small" class="upload">
							<i class="iconfont icon-yunshangchuan"></i> 上传
						</el-button>
					</el-upload>
				</el-form-item>

				<el-form-item>
					<el-button type="primary" @click="dialogFolderVisible=true">
						<i class="el-icon-folder-add"></i>&nbsp;新建文件夹
					</el-button>
				</el-form-item>
				<el-form-item>
					<el-button type="primary" @click="dialogImportShareVisiable=true">
						<i class="el-icon-link"></i>&nbsp;导入分享资源
					</el-button>
				</el-form-item>
			</el-form>
		</div>

		<div>
			<el-table :data="tableList" element-loading-text="数据加载中" fit highlight-current-row
				row-class-name="myClassList">

				<el-table-column label="文件名称" width="200" align="center">

					<template slot-scope="scope">

						<div v-if="scope.row.type=='path'" @click="gotoDir(scope.row.data.folderName)">
							<img src="../../assets/img/pdf.png" style="height: 15px; width:15px" alt="" />
							{{ scope.row.data.folderName }}
						</div>

						<div v-else-if="scope.row.type=='file'">
							<img src="../../assets/img/pdf.png" style="height: 15px; width:15px" alt="" />
							{{ scope.row.data.fileName }}
						</div>
						<div v-else="scope.row.type=='dir'" @click="gotoDir(scope.row.data.folderName)">
							<img src="../../assets/img/folder.png" style="height: 15px; width:15px" alt="" />
							{{ setPathName(scope.row.data.folderName) }}
						</div>

					</template>

				</el-table-column>
				<!-- 	<el-table-column>

				</el-table-column> -->


				<el-table-column label="文件大小" align="center" sortable :formatter="fileSizeFormatter">

				</el-table-column>

				<el-table-column label="修改时间" align="center" sortable>
					<template slot-scope="scope">
						<div v-if="scope.row.type=='dir'||scope.row.type=='file'">
							{{ scope.row.data.updateTime }}
						</div>

					</template>
				</el-table-column>


				<el-table-column label="操作" width="300">
					<template slot-scope="scope">
						<div v-if="scope.row.type=='file'">
							<el-button type="text" size="small"
								@click="openUpdateName(scope.row.data.fileId,scope.row.data.fileName,'file')">重命名
							</el-button>
								<el-button type="text" size="small" @click="openDelete(scope.row.data.fileId,scope.row.type)">删除
							</el-button>
							<el-button type="text" size="small"
								@click="download(scope.row.data.fileUrl,scope.row.data.fileName)">下载
							</el-button>
							<el-button type="text" size="small"
								@click="showShareDialog(scope.row.data.fileId,'0',scope.row.data.shareLink)">

								<label v-if="scope.row.data.shareLink=='#'">分享</label>
								<label v-else>分享中</label>
							</el-button>
	<!-- 						<el-button type="text" size="small" @click="showDialogMove(scope.row.data.fileId)">移动
							</el-button> -->

						</div>
						<div v-else-if="scope.row.type=='dir'">
							<el-button type="text" size="small"
								@click="openUpdateName(scope.row.data.folderId,scope.row.data.folderName,'dir')">重命名
							</el-button>
							<el-button type="text" size="small" @click="openDelete(scope.row.data.folderId,scope.row.type)">删除
							</el-button>
							<el-button type="text" size="small"
								@click="downloadFolderByPath(scope.row.data.folderName)">下载
							</el-button>
							<el-button type="text" size="small"
								@click="showShareDialog(scope.row.data.folderId,'1',scope.row.data.shareLink)">

								<label v-if="scope.row.data.shareLink=='#'">分享</label>
								<label v-else>分享中</label>
							</el-button>
						</div>
					</template>
				</el-table-column>
			</el-table>
		</div>

	</div>
</template>

<script>
	import {
		createLink,
		importSharedResource,
		cancelShared
	} from '../../api/share.js'
	import {
		saveAs
	} from 'file-saver'
	import axios from 'axios'
	// import FunctionBar from "components/functionBar/FunctionBar.vue";
	import {
		getUserPaths,
		createDir,
		deleteDir,
		renameDir,
		renameV2,
		downloadFolder,
		getTierFolder
	} from '../../api/folder.js'
	import {
		getFiles,
		uploadFile,
		deleteFiles,
		renameFile,
		downloadFile,
		searchFile,
		checkUpload
	} from '../../api/file.js'
	export default {
		name: "Files",
		data() {
			return {
				listData: [],
				folderList: [],
				tierFolderList: [],
				searchFile: '', //查询文件
				dialogMoveVisiable: false,
				moveProps: {
					children: 'childrenList',
					label: 'folderName'
				},
				MoveFileInfo: {
					id: '',
					path: ''
				},
				updateInfo: { //重命名类
					id: '',
					type: '',
					name: '',
					newName: ''
				},
				sharedResourceInfo: {
					sharedLink: '',
					sharedSecret: ''
				},
				dialogImportShareVisiable: false,
				shareInfo: { //建立分享的一些信息
					sharedId: '', //被分享资源的id
					shareType: '', //被分享资源的类型
					shareSecretKey: '', //分享码
					downloadCount: '', // 下载次数
					expiration: '' //过期时间
				},
				dialogShareLinkVisiable: false,
				tableList: [],
				dialogFolderVisible: false, //显示新建文件夹框
				dialogRenameVisible: false, //显示修改文件夹框
				dialogShareVisible: false, //显示分享链接
				shareUrl: '', //分享的url
				mkdirName: '',
				newName: '',
				// 是否已经被创建
				// isCreated: false,
				// 排序方式
				sortType: "time",
				// 展示方式 icon table
				showType: "icon",
				// 搜索的文件夹
				searchFolder: [],
			};
		},
		created() {
			//   一进页面就是根目录 /root
			//如果是/root/asas的话就变成/root 

			//  /root/asda/demo  变成/root/asda
			let path = this.$route.params.path
			if (path != '/root') {
				let lastIndex = path.lastIndexOf('/')
				path = path.substring(0, lastIndex)

				let dir = {
					folderName: path
				}

				let pFolder = {
					type: 'path',
					data: dir
				}
				this.tableList.push(pFolder)
			}

			this.getFileList()
			this.getFolderList()
		},
		methods: {





			handleMoveClick(folderName) {
				console.log(folderName)
			},

			//搜索文件
			searchFileByName() {
				if (this.searchFile == '') {
					window.location.reload()
				}
				searchFile(this.searchFile).then(res => {
					if (res.code == 20000) {
						let files = res.data
						this.tableList = []
						for (var i = 0; i < files.length; i++) {
							let node = {
								type: 'file',
								data: files[i]
							}
							this.tableList.push(node)
						}
					}
				}).catch(error => {
					console.log(error)
					this.$message({
						type: 'error',
						message: '搜索失败!'
					});
				})
			},
			//获取文件夹目录树
			showDialogMove(id) {
				this.MoveFileInfo.id = id;
				this.dialogMoveVisiable = true;
				this.getTierDir()
			},
			getTierDir() {
				getTierFolder().then(res => {
					this.tierFolderList = res.data
				}).catch(error => {
					console.log(error)
					this.$message({
						type: 'error',
						message: '获取层级目录失败!'
					});
				})
			},
			//显示分享框
			showShareDialog(id, type, shareLink) {
				//判断是否已经是分享的状态
				if (shareLink == '#') {
					this.shareInfo.sharedId = id;
					this.shareInfo.shareType = type;
					this.dialogShareVisible = true;
					return;
				}
          
					let lastIndex= shareLink.lastIndexOf("/")
					let key= shareLink.substring(lastIndex+1)
         let sharedVo={
					 sharedId:id,
					 shareType:type,
					 shareKey: key
				 }
				
				this.$confirm('此操作将取消资源分享, 是否继续?', '提示', {
					confirmButtonText: '确定',
				 cancelButtonText: '取消',
					type: 'info'
				}).then(() => {
					//调用取消分享接口
          cancelShared(sharedVo).then(res=>{
						
						if(res.code==20000){
							this.$message({
								type: 'success',
								message: '取消分享!'
							});
						}
					}).catch(error=>{
            console.log(error)						
					})

				}).catch((error) => {
					 console.log(error)			
							// this.$message({
							// 	type: 'error',
							// 	message: '操作失败'
							// });
				});


			},
			//导入分享资源
			importSharedFileOrFolder() {
				let lastIndex = this.sharedResourceInfo.sharedLink.lastIndexOf("/")
				let key = this.sharedResourceInfo.sharedLink.substring(lastIndex + 1)

				importSharedResource(key, this.sharedResourceInfo.sharedSecret, this.$route.params.path).then(res => {
					if (res.code == 20000) {
						this.$message({
							type: 'success',
							message: '导入分享资源成功!'
						});
					} else {
						this.$message({
							type: 'error',
							message: res.msg
						});
					}
				}).catch(error => {
					console.log(error)
					this.$message({
						type: 'error',
						message: '导入分享资源失败!'
					});
				})
				this.sharedResourceInfo.sharedLink = ''
				this.sharedResourceInfo.sharedSecret = ''
				this.dialogImportShareVisiable = false
			},
			closeShareLinkDialog() {
				this.shareInfo.downloadCount = 0;
				this.shareInfo.expiration = 0,
					this.shareInfo.shareSecretKey = ''
				this.shareInfo.shareType = ''
				this.shareInfo.sharedId = ''
				this.shareUrl = ''
				this.dialogShareLinkVisiable = false
			},
			//发送请求并生成分享码
			createShareLink() {
				createLink(this.shareInfo).then(res => {
					if (res.code == 20000) {
						this.shareUrl = res.data
						this.$message({
							type: 'success',
							message: '分享成功!'
						});
						//跳转显示分享链接和分享码
						this.dialogShareVisible = false;
						this.dialogShareLinkVisiable = true;
					} else {
						this.$message({
							type: 'error',
							message: '分享失败!'
						});
					}
				}).catch(error => {
					console.log(error)
					this.$message({
						type: 'error',
						message: '分享失败!'
					});
				})
			},
			showFileShareDialog(url) {
				this.shareUrl = url;
				this.dialogShareVisible = true;
			},
			showFolderShareDialog(path) {
				downloadFolder(path).then(res => {
					this.shareUrl = res.data;
					this.dialogShareVisible = true;
				}).catch(error => {
					console.log(error)
				})
			},
			downloadFolderByPath(path) {

				downloadFolder(path).then(res => {
					let lastIndex = path.lastIndexOf("/");
					let folderName = path.substring(lastIndex + 1) + ".zip";

					// var byteCharacters = atob(res.data); // 对Base64编码进行解码
					// var byteNumbers = new Array(byteCharacters.length);
					// for (var i = 0; i < byteCharacters.length; i++) {
					//     byteNumbers[i] = byteCharacters[i].charCodeAt(0);
					// }
					// var byteArray = new Uint8Array(byteNumbers);
					// var blob = new Blob([byteArray], {type: 'application/zip'});
					// saveAs(blob, folderName); // 使用FileSaver.js库保存文件到本地
					console.log("开始下载，下载的文件夹为" + folderName)
					const url = res.data;
					const link = document.createElement('a');
					link.href = url;
					link.setAttribute('download', folderName);
					// 设置响应头的content-disposition属性
					link.setAttribute('content-disposition', `attachment; filename=${folderName}`);
					document.body.appendChild(link);
					link.click();
				}).catch(error => {
					console.log(error)
					this.$message({
						type: 'error',
						message: '下载失败!'
					});
				})
			},
			//上传文件
			beforUploadFile(file) {
				
			
				uploadFile(this.$route.params.path, file).then(res => {
					if (res.code == 20000) {
						this.$message({
							type: 'info',
							message: '上传文件中!'
						});
						let taskId =res.data;
						
						checkUpload(taskId).then(res=>{
											 let isUpload=res.data
											 
											 if(isUpload){
												 this.$message({
												 	type: 'success',
												 	message: '上传文件成功!'
												 });
											 }
						}).catch(error=>{
											console.log(error)
											this.$message({
												type: 'error',
												message: '上传失败!'
											});
						})
					} else {
						this.$message({
							type: 'error',
							message: '上传失败!'
						});
					}
				}).catch(error => {
					console.log(error)
					this.$message({
						type: 'error',
						message: '上传操作失败!'
					});
				})
				
				
			},
			//点击返回事件
			back(path) {
				console.log("返回路径为" + path)
				this.$router.push({
					name: "files",
					params: {
						path: path,
					},
				});
			},
			gotoDir(path) {

				console.log("进入路径为" + path)
				this.$router.push({
					name: "files",
					params: {
						path: path,
					},
				});
			},
			resetUpdateInfo() {
				this.updateInfo.id = '';
				this.updateInfo.name = '';
				this.updateInfo.newName = '';
			},
			//修改文件或者文件夹名
			doRename() {
				if (this.updateInfo.type == 'file') {

					renameFile(this.updateInfo.id, this.updateInfo.newName).then(res => {
						if (res.code == 20000) {
							this.$message({
								type: 'success',
								message: '修改成功!'
							});
						}
					}).catch(error => {
						console.log(error)
						this.$message({
							type: 'error',
							message: '修改失败!'
						});
					})
				} else if (this.updateInfo.type == 'dir') {
					//一些处理 /root
					let lastIndex = this.updateInfo.name.lastIndexOf("/");
					let newDir = this.updateInfo.name.substring(0, lastIndex + 1) + this.updateInfo.newName
					console.log(newDir)
					console.log(this.updateInfo.id)
					let folderVo = {
						folderId: this.updateInfo.id,
						newPath: newDir
					}
					console.log(folderVo)
					renameV2(folderVo).then(res => {
						if (res.code == 20000) {
							this.$message({
								type: 'success',
								message: '修改成功!'
							});
						}
					}).catch(error => {
						console.log(error)
						this.$message({
							type: 'error',
							message: '修改失败!'
						});
					})

				}
				this.resetUpdateInfo()
				this.dialogRenameVisible = false;
			},
			//修改名称
			openUpdateName(id, name, type) {
				this.updateInfo.id = id;
				this.updateInfo.name = name;
				this.updateInfo.type = type;
				this.dialogRenameVisible = true;
			},

			download(url, name) {


				downloadFile(url).then(res => {
					const url = window.URL.createObjectURL(new Blob([res.data]));
					const link = document.createElement('a');
					link.href = url;
					link.setAttribute('download', name);
					// 设置响应头的content-disposition属性
					link.setAttribute('content-disposition', `attachment; filename=${name}`);
					document.body.appendChild(link);
					link.click();
				})
				// axios({
				//         url: downloadFile(downInfo),
				//         method: 'POST'
				//       }).then((response) => {
				//         const url = window.URL.createObjectURL(new Blob([response.data]));
				//         const link = document.createElement('a');
				//         link.href = url;
				//         link.setAttribute('download', name);
				//         // 设置响应头的content-disposition属性
				//         link.setAttribute('content-disposition', `attachment; filename=${name}`);
				//         document.body.appendChild(link);
				//         link.click();
				//       });


			},

			//删除框框
			openDelete(id, type) {
				this.$confirm('此操作将永久删除该文件, 是否继续?', '提示', {
					confirmButtonText: '确定',
					cancelButtonText: '取消',
					type: 'warning'
				}).then(() => {
					//如果删除的是文件
					if (type == 'file') {
						let arrs = [id]
						deleteFiles(arrs).then(res => {
							if (res.code == 20000) {
								this.$message({
									type: 'success',
									message: '删除成功!'
								});
							} else {
								this.$message({
									type: 'error',
									message: '删除失败!'
								});
							}
						}).catch(error => {
							console.log(error)
						})

					} else if (type == 'dir') {
						deleteDir(id).then(res => {
							if (res.code == 20000) {
								this.$message({
									type: 'success',
									message: '删除成功!'
								});
							} else {
								this.$message({
									type: 'error',
									message: '删除失败!'
								});
							}
						})
					}
				}).catch(() => {
					this.$message({
						type: 'info',
						message: '已取消删除'
					});
				});
			},
			// 新建文件夹
			createFolder() {

				// console.log(foldervo)
				createDir(this.mkdirName, this.$route.params.path).then(res => {
					if (res.code == 20000) {
						this.$message({
							type: 'success',
							message: '添加成功!'
						});
					}
				}).catch(error => {
					console.log(error)
					this.$message({
						type: 'error',
						message: '添加失败!'
					});
				})
				this.dialogFolderVisible = false;
				this.mkdirName = '';
				this.tableList = []
				this.getFolderList()
				this.getFileList()
			},
			//重新设置文件夹名,如/root/demo改为 demo
			setPathName(path) {
				let lastIndex = path.lastIndexOf("/")
				path = path.substring(lastIndex + 1, path.length)
				return path;
			},
			//上传文件
			upload(file) {
				uploadFile(this.$route.params.path, file).then(res => {
					this.getFileList()
				}).catch(error => {
					console.log(error)
				})
			},
			//获取文件列表
			async getFileList() {


				getFiles(this.$route.params.path).then(res => {
					let files = res.data;
					if (res.code = 20000) {
						for (var i = 0; i < files.length; i++) {
							let node = {
								type: 'file',
								data: files[i]
							}
							this.tableList.push(node)
						}
						console.log(this.tableList)
						this.listData = files
					} else {
						//todo
					}
				}).catch(error => {
					console.log(error)
					this.$message({
						type: 'error',
						message: '获取文件列表失败!'
					});
				})
			},
			//格式化文件的大小
			fileSizeFormatter(row, column) {
				if (row.data.size == undefined) return '';
				return (row.data.size / 1048576).toFixed(2) + " MB"
			},
			handleSelectionChange(val) {

			},
			//获取文件夹列表
			async getFolderList() {

				getUserPaths(this.$route.params.path).then(res => {
					if (res.code == 20000) {
						let folderList = res.data;
						for (var i = 0; i < folderList.length; i++) {
							let node = {
								type: 'dir',
								data: folderList[i]
							}
							this.tableList.push(node)
						}
						console.log(this.folderList)
						this.folderList = folderList
					}
				}).catch(error => {
					console.log(error)
					this.$message({
						type: 'error',
						message: '获取文件夹列表失败!'
					});
				})
			}
		}
	};
</script>

<style scoped>
	.Files {
		width: 100%;
	}

	.left {
		display: flex;
	}

	.my-input .el-input__inner {
		height: 200px;
	}
</style>
