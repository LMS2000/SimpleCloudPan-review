<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!--部门数据-->
      <!--  <el-col :span="4" :xs="24">
        <div class="head-container">
          <el-input
            v-model="deptName"
            placeholder="请输入部门名称"
            clearable
            size="small"
            prefix-icon="el-icon-search"
            style="margin-bottom: 20px"
          />
        </div>
        <div class="head-container">
          <el-tree
            :data="deptOptions"
            :props="defaultProps"
            :expand-on-click-node="false"
            :filter-node-method="filterNode"
            ref="tree"
            node-key="id"
            default-expand-all
            highlight-current
            @node-click="handleNodeClick"
          />
        </div>
      </el-col> -->
      <!--用户数据-->
      <el-col >
        <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch"
          label-width="68px">
          <el-form-item label="用户名称" prop="username">
            <el-input v-model="queryParams.username" placeholder="请输入用户名称" clearable style="width: 240px"
              @keyup.enter.native="handleQuery" />
          </el-form-item>
          <!--  <el-form-item label="手机号码" prop="phonenumber">
            <el-input
              v-model="queryParams.phonenumber"
              placeholder="请输入手机号码"
              clearable
              style="width: 240px"
              @keyup.enter.native="handleQuery"
            />
          </el-form-item> -->
          <el-form-item label="状态" prop="enable">
            <el-select v-model="queryParams.enable" placeholder="用户状态" clearable style="width: 240px">
              <el-option key="0" label="使用中" value="0"></el-option>
              <el-option key="1" label="已停用" value="1"></el-option>
              <!-- <el-option
                v-for="dict in dict.type.sys_normal_disable"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              /> -->
            </el-select>
          </el-form-item>
          <!--  <el-form-item label="创建时间">
            <el-date-picker
              v-model="dateRange"
              style="width: 240px"
              value-format="yyyy-MM-dd"
              type="daterange"
              range-separator="-"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
            ></el-date-picker>
          </el-form-item> -->
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
            <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate">修改
            </el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete">
              删除</el-button>
          </el-col>
          <!-- <el-col :span="1.5">
            <el-button type="info" plain icon="el-icon-upload2" size="mini" @click="handleImport"
              v-hasPermi="['system:user:import']">导入</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport"
              v-hasPermi="['system:user:export']">导出</el-button>
          </el-col> -->
          <!--     <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" :columns="columns"></right-toolbar>
 -->
        </el-row>

        <el-table v-loading="loading" :data="userList" @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column label="用户编号" align="center" key="userId" prop="userId" v-if="columns[0].visible" />
          <el-table-column label="用户名称" align="center" key="username" prop="username" v-if="columns[1].visible"
            :show-overflow-tooltip="true" />
          <!--          <el-table-column label="用户昵称" align="center" key="nickName" prop="nickName" v-if="columns[2].visible" :show-overflow-tooltip="true" /> -->
          <!--          <el-table-column label="部门" align="center" key="deptName" prop="dept.deptName" v-if="columns[3].visible" :show-overflow-tooltip="true" /> -->
          <!--          <el-table-column label="手机号码" align="center" key="phonenumber" prop="phonenumber" v-if="columns[4].visible" width="120" /> -->
          <el-table-column label="已使用配额" align="center" key="useQuota" prop="useQuota" v-if="columns[2].visible"
            :show-overflow-tooltip="true">
            <template slot-scope="scope">
              {{ formatSize(scope.row.useQuota) }}
            </template>
          </el-table-column>
          <el-table-column label="全部配额" align="center" key="quota" prop="quota" v-if="columns[3].visible"
            :show-overflow-tooltip="true">
            <template slot-scope="scope">
              {{ formatSize(scope.row.quota) }}
            </template>
          </el-table-column>
          <el-table-column label="状态" align="center" key="enable" v-if="columns[4].visible">
            <template slot-scope="scope">
              <el-switch v-model="scope.row.enable" :active-value="0" :inactive-value="1"
                @input="handleStatusChange(scope.row)"></el-switch>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" align="center" prop="createTime" v-if="columns[5].visible" width="160">
            <template slot-scope="scope">
              <span>{{ scope.row.createTime }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="160" class-name="small-padding fixed-width">
            <template slot-scope="scope" v-if="scope.row.userId !== 1">
              <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)">修改</el-button>
              <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
              <el-dropdown  @command="(command) => handleCommand(command, scope.row)" size="mini">
                <el-button size="mini" type="text" icon="el-icon-d-arrow-right">更多</el-button>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item command="handleResetPwd" icon="el-icon-key">
                    重置密码</el-dropdown-item>
                  <el-dropdown-item command="handleAuthRole" icon="el-icon-circle-check">分配角色</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </template>
          </el-table-column>
        </el-table>

        <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize"
          @pagination="getList" />

      </el-col>
    </el-row>

    <!-- 添加或修改用户配置对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-row>
          <!-- <el-col :span="12">
            <el-form-item label="用户昵称" prop="nickName">
              <el-input v-model="form.nickName" placeholder="请输入用户昵称" maxlength="30" />
            </el-form-item>
          </el-col> -->
          <!-- <el-col :span="12">
            <el-form-item label="归属部门" prop="deptId">
              <treeselect v-model="form.deptId" :options="deptOptions" :show-count="true" placeholder="请选择归属部门" />
            </el-form-item>
          </el-col> -->
        </el-row>

        <el-row>
          <el-col :span="12">
            <el-form-item v-if="form.userId == undefined" label="用户名称" prop="username">
              <el-input v-model="form.username" placeholder="请输入用户名称" maxlength="30" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item v-if="form.userId == undefined" label="用户密码" prop="password">
              <el-input v-model="form.password" placeholder="请输入用户密码" type="password" maxlength="20" show-password />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" maxlength="50" />
            </el-form-item>
          </el-col>
          <!-- <el-col :span="12">
            <el-form-item label="用户性别">
              <el-select v-model="form.sex" placeholder="请选择性别">
                <el-option
                  v-for="dict in dict.type.sys_user_sex"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col> -->
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="form.enable">
                <!-- <el-radio v-for="dict in dict.type.sys_normal_disable" :key="dict.value" :label="dict.value">
                  {{dict.label}}</el-radio> -->
                <el-radio :key="0" :label="0">
                  启用
                </el-radio>
                <el-radio :key="1" :label="1">
                  停用
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <!-- <el-col :span="12">
            <el-form-item label="岗位">
              <el-select v-model="form.postIds" multiple placeholder="请选择岗位">
                <el-option
                  v-for="item in postOptions"
                  :key="item.postId"
                  :label="item.postName"
                  :value="item.postId"
                  :disabled="item.status == 1"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col> -->
          <el-col :span="12">
            <el-form-item label="角色">
              <el-select v-model="form.rids" multiple placeholder="请选择角色">
                <el-option v-for="item in roleOptions" :key="item.rid" :label="item.description" :value="item.rid"
                  :disabled="item.enable == 1"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-col :span="24">
          <el-form-item label="最大配额">
            <div class="block">
              <el-slider v-model="form.quota" :max="maxQuota" :step="sliderStep" :show-tooltip="showTooltip"
                :format-tooltip="formatTooltip" :marks="sliderMarks">
              </el-slider>
            </div>

          </el-form-item>
        </el-col>
        <el-row :span="24">


        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入内容"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
    <!-- 用户导入对话框 -->
    <!--  <el-dialog :title="upload.title" :visible.sync="upload.open" width="400px" append-to-body>
      <el-upload
        ref="upload"
        :limit="1"
        accept=".xlsx, .xls"
        :headers="upload.headers"
        :action="upload.url + '?updateSupport=' + upload.updateSupport"
        :disabled="upload.isUploading"
        :on-progress="handleFileUploadProgress"
        :on-success="handleFileSuccess"
        :auto-upload="false"
        drag
      >
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <div class="el-upload__tip text-center" slot="tip">
          <div class="el-upload__tip" slot="tip">
            <el-checkbox v-model="upload.updateSupport" /> 是否更新已经存在的用户数据
          </div>
          <span>仅允许导入xls、xlsx格式文件。</span>
          <el-link type="primary" :underline="false" style="font-size:12px;vertical-align: baseline;" @click="importTemplate">下载模板</el-link>
        </div>
      </el-upload>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitFileForm">确 定</el-button>
        <el-button @click="upload.open = false">取 消</el-button>
      </div>
    </el-dialog> -->
  </div>
</template>

<script>
  import {
    getPageUser,
    changeEnable,
    addUser,
    updateUser,
    deleteUser,
    resetPwd
  } from '../../api/user.js'

  import {getUserById,getInitInfo} from '../../api/userRole.js'
  export default {
    name: "User",
    data() {
      return {
        //每隔20%显示一个label
        sliderStep: 20,
        //开启显示标签
        showTooltip: true,
        sliderMarks: {
          // 0: '0',
          // 20: '20%',
          // 40: '40%',
          // 60: '60%',
          // 80: '80%',
          // 100: '100'
        },
        // 遮罩层
        loading: true,
        // 选中数组
        ids: [],
        // 非单个禁用
        single: true,
        // 非多个禁用
        multiple: true,
        // 显示搜索条件
        showSearch: true,
        // 总条数
        total: 0,
        // 用户表格数据
        userList: null,
        // 弹出层标题
        title: "",
        // 部门树选项
        deptOptions: undefined,
        // 是否显示弹出层
        open: false,
        // 部门名称
        deptName: undefined,
        // 默认密码
        initPassword: undefined,
        //最大用户使用配额
        maxQuota: undefined,
        // 日期范围
        dateRange: [],
        // 岗位选项
        postOptions: [],
        // 角色选项
        roleOptions: [],
        // 表单参数
        form: {},
        defaultProps: {
          children: "children",
          label: "label"
        },
        // // 用户导入参数
        // upload: {
        //   // 是否显示弹出层（用户导入）
        //   open: false,
        //   // 弹出层标题（用户导入）
        //   title: "",
        //   // 是否禁用上传
        //   isUploading: false,
        //   // 是否更新已经存在的用户数据
        //   updateSupport: 0,
        //   // 设置上传的请求头部
        //   headers: { Authorization: "Bearer " + getToken() },
        //   // 上传的地址
        //   url: process.env.VUE_APP_BASE_API + "/system/user/importData"
        // },
        // 查询参数
        queryParams: {
          pageNum: 1,
          pageSize: 10,
          username: undefined,
          enable: undefined
        },
        // 列信息
        columns: [{
            key: 0,
            label: `用户编号`,
            visible: true
          },
          {
            key: 1,
            label: `用户名称`,
            visible: true
          },
          {
            key: 2,
            label: `使用额度`,
            visible: true
          },
          {
            key: 3,
            label: `全部额度`,
            visible: true
          },
          {
            key: 4,
            label: `状态`,
            visible: true
          },
          {
            key: 5,
            label: `创建时间`,
            visible: true
          }
        ],
        // 表单校验
        rules: {
          username: [{
              required: true,
              message: "用户名称不能为空",
              trigger: "blur"
            },
            {
              min: 2,
              max: 20,
              message: '用户名称长度必须介于 2 和 20 之间',
              trigger: 'blur'
            }
          ],
          password: [{
              required: true,
              message: "用户密码不能为空",
              trigger: "blur"
            },
            {
              min: 5,
              max: 20,
              message: '用户密码长度必须介于 5 和 20 之间',
              trigger: 'blur'
            }
          ],
          email: [{
            type: "email",
            message: "请输入正确的邮箱地址",
            trigger: ["blur", "change"]
          }],
          phonenumber: [{
            pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/,
            message: "请输入正确的手机号码",
            trigger: "blur"
          }]
        }
      };
    },
    //创建时获取用户列表
    created() {

      this.getList();
      // this.getDeptTree();
      // this.getConfigKey("sys.user.initPassword").then(response => {
      //   this.initPassword = response.msg;
      // });
    },
    methods: {
      // 更多操作触发
      handleCommand(command, row) {
        switch (command) {
          case "handleResetPwd":
            this.handleResetPwd(row);
            break;
          case "handleAuthRole":
            this.handleAuthRole(row);
            break;
          default:
            break;
        }
      },
      formatTooltip(value) {
        // if (value === 0 || value === 100) {
        //   return value.toString();
        // }
        console.log("格式化容量")
        return this.formatSize(value);
      },
      //格式化容量
      formatSize(size) {
        if (size == null || size == 0) {
          return '';
        }
        const units = ['B', 'kB', 'MB', 'GB', 'TB'];

        let i = 0;
        while (size >= 1024 && i < units.length - 1) {
          size /= 1024;
          i++;
        }

        return `${size.toFixed(1)} ${units[i]}`;
      },
      //获取用户列表
      getList() {
        this.loading = true;
        // listUser(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
        //     this.userList = response.rows;
        //     this.total = response.total;
        //     this.loading = false;
        //   }
        // );

        //获取用户列表
        getPageUser(this.queryParams).then(res => {
          if (res.code == 20000) {
            this.userList = res.data.records;
            console.log(res.data)
            this.total = res.data.total;
            this.loading = false;
          }
        }).catch(error => {
          console.log(error)
        })
      },
      /** 搜索按钮操作 */
      handleQuery() {
        this.queryParams.pageNum = 1;
        this.getList();
      },
      /** 重置按钮操作 */
      resetQuery() {
        this.dateRange = [];
        this.resetForm("queryForm");
        // this.$refs.tree.setCurrentKey(null);
        this.handleQuery();
      },
      // 表单重置
      reset() {
        this.form = {
          userId: undefined,
          username: undefined,
          password: undefined,
          email: undefined,
          enable: 0,
          remark: undefined,
          rids: [],
          quota: undefined,
          useQuota: undefined
        };
        this.resetForm("form");
      },
      /** 新增按钮操作 */
      handleAdd() {
        this.reset();
        getInitInfo().then(response => {
          this.roleOptions = response.data.roles;
          this.open = true;
          this.title = "添加用户";
          this.maxQuota = response.data.maxQuota;
          this.initPassword = response.data.initPassword;
          this.form.password = response.data.initPassword;

          //设置marks
          //划分为5份  0% 20% 40% 60% 80% 100%

          let secondValue = this.maxQuota * 0.2;
          let forthValue = this.maxQuota * 0.4;
          let sixthValue = this.maxQuota * 0.6;
          let eighthValue = this.maxQuota * 0.8;
          let maxValue = this.maxQuota;
          this.sliderMarks = {
            0: "0B",
            [forthValue]: this.formatSize(forthValue),
            [eighthValue]: this.formatSize(eighthValue),
          };
        });
      },
      /** 修改按钮操作 */
      handleUpdate(row) {
        this.reset();
        const userId = row.userId || this.ids;
        console.log(userId)
        if(userId.includes(1)){
          this.$modal.msgError("不可对超级管理员操作");
          return;
        }
        getUserById(userId).then(response => {
          this.maxQuota = response.data.maxQuota;
          this.form = response.data.userVo;

          // this.postOptions = response.posts;
          this.roleOptions = response.data.roles;
          // this.$set(this.form, "postIds", response.postIds);
          this.$set(this.form, "roles", response.data.roles);
          this.open = true;
          this.title = "修改用户";
          this.form.password = "";
        });
      },
      /** 重置密码按钮操作 */
      handleResetPwd(row) {
        this.$prompt('请输入"' + row.username + '"的新密码', "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          closeOnClickModal: false,
          inputPattern: /^.{5,20}$/,
          inputErrorMessage: "用户密码长度必须介于 5 和 20 之间"
        }).then(({
          value
        }) => {
          resetPwd(value,row.userId).then(response => {
            this.$modal.msgSuccess("修改成功，新密码是：" + value);
          });
        }).catch(() => {});
      },
      /** 分配角色操作 */
      handleAuthRole: function(row) {
        const userId = row.userId;
        this.$router.push("/user-auth/role/" + userId);
      },
      /** 提交按钮 */
      submitForm: function() {
        this.$refs["form"].validate(valid => {
          if (valid) {
            if (this.form.userId != undefined) {
              updateUser(this.form).then(response => {
                this.$modal.msgSuccess("修改成功");
                this.open = false;
                this.getList();
              });
            } else {
              addUser(this.form).then(response => {
                this.$modal.msgSuccess("新增成功");
                this.open = false;
                this.getList();
              });
            }
          }
        });
      },
      /** 删除按钮操作 */
      handleDelete(row) {
        const arr = row.userId || this.ids;
         if( arr.includes(1)){
              this.$modal.msgError("不可对超级管理员操作");
              return;
         }

        this.$modal.confirm('是否确认删除用户编号为"' + arr + '"的数据项？').then(function() {
          if (typeof arr === "number") {
            const userIds = [arr];
            return deleteUser(userIds);
          }
          const userIds = arr;
          return deleteUser(userIds);
        }).then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
        }).catch(() => {});
      },
      // 用户状态修改
      handleStatusChange(row) {
        let text = row.enable === 0 ? "启用" : "停用";

        if(row.userId===1){
          this.$modal.msgError("不可对超级管理员操作");
          row.enable = row.enable === 0 ? 1 : 0;
          return;
        }


        this.$confirm('确认要"' + text + '""' + row.username + '"用户吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          // 用户点击了确定按钮，执行操作

          let changeVo = {
            'userId': row.userId,
            'enable': row.enable
          };

          changeEnable(changeVo).then(res => {
            if (res.code == 20000) {
              this.$modal.msgSuccess(text + "成功");
            }
          })
          // 执行其他操作...
        }).catch(() => {
          // 用户点击了取消按钮，取消操作
          this.$modal.msgSuccess(text + "取消操作");
          row.enable = row.enable === 0 ? 1 : 0;
          // 执行其他操作...
        });



      },
      handleSelectionChange(selection) {
        this.ids = selection.map(item => item.userId);
        this.single = selection.length != 1;
        this.multiple = !selection.length;
      },
      // 取消按钮
      cancel() {
        this.open = false;
        this.reset();
      },


    }



  }
</script>

<style>
  .el-slider__runway {
    width: 350px;
    /* 设置滑块的宽度 */
  }
</style>
