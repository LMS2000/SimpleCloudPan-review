<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch">
      <el-form-item label="角色名称" prop="description">
        <el-input v-model="queryParams.description" placeholder="请输入角色名称" clearable style="width: 240px"
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="权限字符" prop="roleName">
        <el-input v-model="queryParams.roleName" placeholder="请输入权限字符" clearable style="width: 240px"
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="状态" prop="enable">
        <el-select v-model="queryParams.enable" placeholder="角色状态" clearable style="width: 240px">
          <!-- <el-option
            v-for="dict in dict.type.sys_normal_disable"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          /> -->
        </el-select>
      </el-form-item>
      <!-- <el-form-item label="创建时间">
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
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete">删除
        </el-button>
      </el-col>
      <!-- <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
        >导出</el-button>
      </el-col> -->
      <!--  <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar> -->
    </el-row>

    <el-table v-loading="loading" :data="roleList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center"/>
      <el-table-column label="角色编号" prop="rid" width="120"/>
      <el-table-column label="角色名称" prop="description" :show-overflow-tooltip="true" width="150"/>
      <el-table-column label="权限字符" prop="roleName" :show-overflow-tooltip="true" width="150"/>
      <el-table-column label="状态" align="center" width="100">
        <template slot-scope="scope">
          <el-switch v-model="scope.row.enable" :active-value="0" :inactive-value="1"
                     @change="handleStatusChange(scope.row)"></el-switch>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ scope.row.createTime }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope" v-if="scope.row.rid !== 1">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
          <el-dropdown size="mini" @command="(command) => handleCommand(command, scope.row)">
            <el-button size="mini" type="text" icon="el-icon-d-arrow-right">更多</el-button>
            <el-dropdown-menu slot="dropdown">
              <!-- <el-dropdown-item command="handleDataScope" icon="el-icon-circle-check"
                >数据权限</el-dropdown-item> -->
              <el-dropdown-item command="handleAuthUser" icon="el-icon-user">分配用户</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 添加或修改角色配置对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="角色名称" prop="description">
          <el-input v-model="form.description" placeholder="请输入角色名称"/>
        </el-form-item>
        <el-form-item prop="roleName">
          <span slot="label">
            <el-tooltip content="控制器中定义的权限字符，如：@PreAuthorize(`@ss.hasRole('admin')`)" placement="top">
              <i class="el-icon-question"></i>
            </el-tooltip>
            权限字符
          </span>
          <el-input v-model="form.roleName" placeholder="请输入权限字符"/>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.enable">
            <el-radio :key="0" :label="0">
              启用
            </el-radio>
            <el-radio :key="1" :label="1">
              停用
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="权限">

          <el-tree class="tree-border" :data="menuOptions" show-checkbox ref="menu" node-key="aid"
                   :check-strictly="!form.menuCheckStrictly" empty-text="加载中，请稍候" :props="defaultProps"></el-tree>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 分配角色数据权限对话框 -->
    <!-- <el-dialog :title="title" :visible.sync="openDataScope" width="500px" append-to-body>
      <el-form :model="form" label-width="80px">
        <el-form-item label="角色名称">
          <el-input v-model="form.description" :disabled="true" />
        </el-form-item>
        <el-form-item label="权限字符">
          <el-input v-model="form.roleName" :disabled="true" />
        </el-form-item> -->
    <!--  <el-form-item label="权限范围">
          <el-select v-model="form.dataScope" @change="dataScopeSelectChange">
            <el-option
              v-for="item in dataScopeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item> -->
    <!--   <el-form-item label="数据权限" v-show="form.dataScope == 2">
          <el-checkbox v-model="deptExpand" @change="handleCheckedTreeExpand($event, 'dept')">展开/折叠</el-checkbox>
          <el-checkbox v-model="deptNodeAll" @change="handleCheckedTreeNodeAll($event, 'dept')">全选/全不选</el-checkbox>
          <el-checkbox v-model="form.deptCheckStrictly" @change="handleCheckedTreeConnect($event, 'dept')">父子联动</el-checkbox>
          <el-tree
            class="tree-border"
            :data="deptOptions"
            show-checkbox
            default-expand-all
            ref="dept"
            node-key="id"
            :check-strictly="!form.deptCheckStrictly"
            empty-text="加载中，请稍候"
            :props="defaultProps"
          ></el-tree>
        </el-form-item> -->
    <!--  </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitDataScope">确 定</el-button>
        <el-button @click="cancelDataScope">取 消</el-button>
      </div>
    </el-dialog> -->
  </div>
</template>

<script>
import {
  pageRole,
  getRoleById,
  removeRoles,
  addRole,
  updateRole,
  changeEnable
} from "../../api/role.js";
import {
  getAuthorityTree
} from "../../api/authority.js";
import {
  getRoleAuthorityTree
} from '../../api/roleAuthority.js'

export default {
  name: "Role",
  data() {
    return {
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
      // 角色表格数据
      roleList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 是否显示弹出层（数据权限）
      openDataScope: false,
      menuExpand: false,
      menuNodeAll: false,
      deptExpand: true,
      deptNodeAll: false,

      // 菜单列表
      menuOptions: [],

      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        roleName: undefined,
        description: undefined,
        enable: undefined
      },
      // 表单参数
      form: {},
      defaultProps: {
        children: "children",
        label: "name"
      },
      // 表单校验
      rules: {
        roleName: [{
          required: true,
          message: "角色名称不能为空",
          trigger: "blur"
        }],
        description: [{
          required: true,
          message: "权限字符不能为空",
          trigger: "blur"
        }]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询角色列表 */
    getList() {
      this.loading = true;
      pageRole(this.queryParams).then(response => {
        this.roleList = response.data.records;
        this.total = response.data.total;
        this.loading = false;
      });
    },
    /** 查询菜单树结构 */
    getMenuTreeselect() {
      getAuthorityTree().then(response => {
        this.menuOptions = response.data;
      });
    },
    // 所有菜单节点数据
    getMenuAllCheckedKeys() {
      // 目前被选中的菜单节点
      let checkedKeys = this.$refs.menu.getCheckedKeys();
      // 半选中的菜单节点
      let halfCheckedKeys = this.$refs.menu.getHalfCheckedKeys();
      checkedKeys.unshift.apply(checkedKeys, halfCheckedKeys);
      return checkedKeys;
    },
    // // 所有部门节点数据
    // getDeptAllCheckedKeys() {
    //   // 目前被选中的部门节点
    //   let checkedKeys = this.$refs.dept.getCheckedKeys();
    //   // 半选中的部门节点
    //   let halfCheckedKeys = this.$refs.dept.getHalfCheckedKeys();
    //   checkedKeys.unshift.apply(checkedKeys, halfCheckedKeys);
    //   return checkedKeys;
    // },
    /** 根据角色ID查询菜单树结构 */
    getRoleMenuTreeselect(roleId) {
      return getRoleAuthorityTree(roleId).then(response => {
        this.menuOptions = response.data.authorityVos;
        return response;
      });
    },
    // /** 根据角色ID查询部门树结构 */
    // getDeptTree(roleId) {
    //   return deptTreeSelect(roleId).then(response => {
    //     this.deptOptions = response.depts;
    //     return response;
    //   });
    // },
    // 角色状态修改
    handleStatusChange(row) {
      let text = row.enable === 0 ? "启用" : "停用";

      this.$confirm('确认要"' + text + '""' + row.description + '"角色吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 用户点击了确定按钮，执行操作

        let changeVo = {
          'rid': row.rid,
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
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 取消按钮（数据权限）
    cancelDataScope() {
      this.openDataScope = false;
      this.reset();
    },
    // 表单重置
    reset() {
      if (this.$refs.menu != undefined) {
        this.$refs.menu.setCheckedKeys([]);
      }
      this.menuExpand = false,
        this.menuNodeAll = false,
        this.deptExpand = true,
        this.deptNodeAll = false,
        this.form = {
          rid: undefined,
          roleName: undefined,
          description: undefined,
          enable: 0,
          authorities: [],
          menuCheckStrictly: true,
          deptCheckStrictly: true,
          remark: undefined
        };
      this.resetForm("form");
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
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.aid || item.rid)
      this.single = selection.length != 1
      this.multiple = !selection.length
    },
    // 更多操作触发
    handleCommand(command, row) {
      switch (command) {
        case "handleDataScope":
          this.handleDataScope(row);
          break;
        case "handleAuthUser":
          this.handleAuthUser(row);
          break;
        default:
          break;
      }
    },
    // // 树权限（展开/折叠）
    // handleCheckedTreeExpand(value, type) {
    //   if (type == 'menu') {
    //     let treeList = this.menuOptions;
    //     for (let i = 0; i < treeList.length; i++) {
    //       this.$refs.menu.store.nodesMap[treeList[i].rid].expanded = value;
    //     }
    //   }
    // },
    // // 树权限（全选/全不选）
    // handleCheckedTreeNodeAll(value, type) {
    //   if (type == 'menu') {
    //     this.$refs.menu.setCheckedNodes(value ? this.menuOptions : []);
    //   }
    // },
    // // 树权限（父子联动）
    // handleCheckedTreeConnect(value, type) {
    //   if (type == 'menu') {
    //     this.form.menuCheckStrictly = value ? true : false;
    //   }
    // },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.getMenuTreeselect();
      this.open = true;
      this.title = "添加角色";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const roleId = row.rid || this.ids
      const roleMenu = this.getRoleMenuTreeselect(roleId);
      getRoleById(roleId).then(response => {
        this.form = response.data;
        this.open = true;
        roleMenu.then(res => {
          let checkedKeys = res.data.checks
          checkedKeys.forEach((v) => {
            this.$nextTick(() => {
              this.$refs.menu.setChecked(v, true, false);
            })
          })
        });
      });
      this.title = "修改角色";
    },
    /** 选择角色权限范围触发 */
    dataScopeSelectChange(value) {
      if (value !== '2') {
        this.$refs.dept.setCheckedKeys([]);
      }
    }
    // },
    // /** 分配数据权限操作 */
    // handleDataScope(row) {
    //   this.reset();
    //   const deptTreeSelect = this.getDeptTree(row.roleId);
    //   getRole(row.roleId).then(response => {
    //     this.form = response.data;
    //     this.openDataScope = true;
    //     this.$nextTick(() => {
    //       deptTreeSelect.then(res => {
    //         this.$refs.dept.setCheckedKeys(res.checkedKeys);
    //       });
    //     });
    //     this.title = "分配数据权限";
    //   });
    // },
    ,
    /** 分配用户操作 */
    handleAuthUser: function (row) {
      const roleId = row.rid;
      this.$router.push("/role-auth/user/" + roleId);
    },
    /** 提交按钮 */
    submitForm: function () {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.rid != undefined) {
            this.form.authorities = this.getMenuAllCheckedKeys();
            updateRole(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            this.form.authorities = this.getMenuAllCheckedKeys();
            addRole(this.form).then(response => {
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
      const arr = row.rid || this.ids;
      this.$modal.confirm('是否确认删除角色编号为"' + arr + '"的数据项？').then(function () {

        if (typeof arr === "number") {
          const roleIds = [arr];
          return removeRoles(roleIds);
        }
        const roleIds = arr;
        return removeRoles(roleIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
      });
    },
    // /** 导出按钮操作 */
    // handleExport() {
    //   this.download('system/role/export', {
    //     ...this.queryParams
    //   }, `role_${new Date().getTime()}.xlsx`)
    // }
  }
};
</script>
