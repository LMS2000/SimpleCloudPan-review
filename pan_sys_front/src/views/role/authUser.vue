<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch">
      <el-form-item label="用户名称" prop="userName">
        <el-input v-model="queryParams.username" placeholder="请输入用户名称" clearable style="width: 240px"
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="openSelectUser">添加用户</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-circle-close" size="mini" :disabled="multiple"
                   @click="cancelAuthUserAll">批量取消授权
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-close" size="mini" @click="handleClose">关闭</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="userList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center"/>
      <el-table-column label="用户名称" prop="username" :show-overflow-tooltip="true"/>
      <el-table-column label="邮箱" prop="email" :show-overflow-tooltip="true"/>
      <el-table-column label="状态" align="center" prop="enable">
        <template slot-scope="scope">
          <!--  <dict-tag :options="dict.type.sys_normal_disable" :value="scope.row.status"/>
         -->
          <el-tag v-if="scope.row.enable==0" type="success">正常</el-tag>
          <el-tag v-else="scope.row.enable==1" type="warning">禁用</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ scope.row.createTime }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-circle-close" @click="cancelAuthUser(scope.row)">取消授权
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>
    <select-user ref="select" :rid="queryParams.rid" @ok="handleQuery"/>
  </div>
</template>

<script>
import {
  allocateRoleUser,
  releaseRoleToUser,
  grantRoleToUser
} from "../../api/userRole.js"
import selectUser from "./selectUser";

export default {
  name: "AuthUser",
  components: {
    selectUser
  },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中用户组
      userIds: [],
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 用户表格数据
      userList: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        rid: undefined,
        username: undefined
      }
    };
  },
  created() {
    const rid = this.$route.params && this.$route.params.rid;
    if (rid) {
      this.queryParams.rid = rid;
      this.getList();
    }
  },
  methods: {
    /** 查询授权用户列表 */
    getList() {
      this.loading = true;
      allocateRoleUser(this.queryParams).then(response => {
        if (response.code == 20000) {
          this.userList = response.data.records;
          this.total = response.data.total;
        } else {
          this.$modal.msgError(response.msg)
        }
        this.loading = false;
      });
    },
    // 返回按钮
    handleClose() {
      const obj = {
        path: "/role"
      };
      this.$router.go(-1)
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.userIds = selection.map(item => item.userId)
      this.multiple = !selection.length
    },
    /** 打开授权用户表弹窗 */
    openSelectUser() {
      this.$refs.select.show();
    },
    /** 取消授权按钮操作 */
    cancelAuthUser(row) {
      const rid = this.queryParams.rid;
      this.$modal.confirm('确认要取消该用户"' + row.username + '"角色吗？').then(function () {
        return releaseRoleToUser([row.userId], rid);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("取消授权成功");
      }).catch(() => {
      });
    },
    /** 批量取消授权按钮操作 */
    cancelAuthUserAll(row) {
      const rid = this.queryParams.rid;
      const userIds = this.userIds.join(",");
      this.$modal.confirm('是否取消选中用户授权数据项？').then(function () {
        return releaseRoleToUser(userIds, rid);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("取消授权成功");
      }).catch(() => {
      });
    }
  }
};
</script>
