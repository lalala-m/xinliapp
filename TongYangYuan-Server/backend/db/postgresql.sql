CREATE TABLE sys_tenant (
    id int8 NOT NULL,
    datasource_id int8,
    tenant_name varchar(50),
    tenant_domain varchar(200),
    status int,
    remark varchar(200),
    user_id int8,
    username varchar(50),
    tenant_mode int,
    del_flag int,
    creator int8,
    create_date timestamp,
    updater int8,
    update_date timestamp,
    PRIMARY KEY (id)
);

CREATE INDEX idx_sys_tenant_create_date on sys_tenant(create_date);

COMMENT ON TABLE sys_tenant IS '租户管理';
COMMENT ON COLUMN sys_tenant.id IS 'id';
COMMENT ON COLUMN sys_tenant.datasource_id IS '租户数据源';
COMMENT ON COLUMN sys_tenant.tenant_name IS '租户名称';
COMMENT ON COLUMN sys_tenant.tenant_domain IS '租户域名';
COMMENT ON COLUMN sys_tenant.status IS '状态  0：停用    1：正常';
COMMENT ON COLUMN sys_tenant.remark IS '备注';
COMMENT ON COLUMN sys_tenant.user_id IS '登录账号ID';
COMMENT ON COLUMN sys_tenant.username IS '登录账号';
COMMENT ON COLUMN sys_tenant.tenant_mode IS '租户模式  0：字段模式   1：数据源模式';
COMMENT ON COLUMN sys_tenant.del_flag IS '删除标识 0：未删除    1：删除';
COMMENT ON COLUMN sys_tenant.creator IS '创建者';
COMMENT ON COLUMN sys_tenant.create_date IS '创建时间';
COMMENT ON COLUMN sys_tenant.updater IS '更新者';
COMMENT ON COLUMN sys_tenant.update_date IS '更新时间';


CREATE TABLE sys_tenant_datasource (
    id int8 NOT NULL,
    name varchar(50),
    driver_class_name varchar(200),
    url varchar(500),
    username varchar(100),
    password varchar(100),
    creator int8,
    create_date timestamp,
    updater int8,
    update_date timestamp,
    PRIMARY KEY (id)
);

COMMENT ON TABLE sys_tenant_datasource IS '租户数据源';
COMMENT ON COLUMN sys_tenant_datasource.id IS 'id';
COMMENT ON COLUMN sys_tenant_datasource.name IS '名称';
COMMENT ON COLUMN sys_tenant_datasource.driver_class_name IS '驱动';
COMMENT ON COLUMN sys_tenant_datasource.url IS 'url';
COMMENT ON COLUMN sys_tenant_datasource.username IS '用户名';
COMMENT ON COLUMN sys_tenant_datasource.password IS '密码';
COMMENT ON COLUMN sys_tenant_datasource.creator IS '创建者';
COMMENT ON COLUMN sys_tenant_datasource.create_date IS '创建时间';
COMMENT ON COLUMN sys_tenant_datasource.updater IS '更新者';
COMMENT ON COLUMN sys_tenant_datasource.update_date IS '更新时间';

create table sys_user
(
  id                   int8 not null,
  username             varchar(50),
  password             varchar(100),
  real_name            varchar(50),
  head_url             varchar(200),
  gender               int,
  email                varchar(100),
  mobile               varchar(20),
  dept_id              int8,
  super_admin          int,
  super_tenant         int,
  tenant_code          int8,
  status               int,
  remark               varchar(200),
  del_flag             int,
  creator              int8,
  create_date          timestamp,
  updater              int8,
  update_date          timestamp,
  primary key (id)
);
CREATE INDEX idx_sys_user_del_flag on sys_user(del_flag);
CREATE INDEX idx_sys_user_create_date on sys_user(create_date);

COMMENT ON TABLE sys_user IS '用户管理';
COMMENT ON COLUMN sys_user.id IS 'id';
COMMENT ON COLUMN sys_user.username IS '用户名';
COMMENT ON COLUMN sys_user.password IS '密码';
COMMENT ON COLUMN sys_user.real_name IS '姓名';
COMMENT ON COLUMN sys_user.head_url IS '头像';
COMMENT ON COLUMN sys_user.gender IS '性别   0：男   1：女    2：保密';
COMMENT ON COLUMN sys_user.email IS '邮箱';
COMMENT ON COLUMN sys_user.mobile IS '手机号';
COMMENT ON COLUMN sys_user.dept_id IS '部门ID';
COMMENT ON COLUMN sys_user.super_admin IS '超级管理员   0：否   1：是';
COMMENT ON COLUMN sys_user.status IS '状态  0：停用    1：正常';
COMMENT ON COLUMN sys_user.remark IS '备注';
COMMENT ON COLUMN sys_user.del_flag IS '删除标识  0：未删除    1：删除';
COMMENT ON COLUMN sys_user.creator IS '创建者';
COMMENT ON COLUMN sys_user.create_date IS '创建时间';
COMMENT ON COLUMN sys_user.updater IS '更新者';
COMMENT ON COLUMN sys_user.update_date IS '更新时间';


CREATE TABLE sys_user_token
(
    id                   int8 not null,
    user_id              int8,
    access_token         varchar(50) not null,
    access_token_expire  timestamp,
    refresh_token        varchar(50) not null,
    refresh_token_expire timestamp,
    create_date          timestamp,
    primary key (id)
);

COMMENT ON TABLE sys_user_token IS '用户管理';
COMMENT ON COLUMN sys_user_token.id IS 'id';
COMMENT ON COLUMN sys_user_token.user_id IS '用户ID';
COMMENT ON COLUMN sys_user_token.access_token IS 'accessToken';
COMMENT ON COLUMN sys_user_token.access_token_expire IS 'accessToken 过期时间';
COMMENT ON COLUMN sys_user_token.refresh_token IS 'refreshToken';
COMMENT ON COLUMN sys_user_token.refresh_token_expire IS 'refreshToken 过期时间';
COMMENT ON COLUMN sys_user_token.create_date IS '创建时间';


create table sys_dept
(
  id                   int8 not null,
  pid                  int8,
  pids                 varchar(500),
  name                 varchar(50),
  leader_id            int8,
  sort                 int,
  tenant_code          int8,
  del_flag             int,
  creator              int8,
  create_date          timestamp,
  updater              int8,
  update_date          timestamp,
  primary key (id)
);
CREATE INDEX idx_sys_dept_pid on sys_dept(pid);
CREATE INDEX idx_sys_dept_del_flag on sys_dept(del_flag);
CREATE INDEX idx_sys_dept_create_date on sys_dept(create_date);

COMMENT ON TABLE sys_dept IS '部门管理';
COMMENT ON COLUMN sys_dept.id IS 'id';
COMMENT ON COLUMN sys_dept.pid IS '上级ID';
COMMENT ON COLUMN sys_dept.pids IS '所有上级ID，用逗号分开';
COMMENT ON COLUMN sys_dept.name IS '部门名称';
COMMENT ON COLUMN sys_dept.sort IS '排序';
COMMENT ON COLUMN sys_dept.del_flag IS '删除标识  0：未删除    1：删除';
COMMENT ON COLUMN sys_dept.creator IS '创建者';
COMMENT ON COLUMN sys_dept.create_date IS '创建时间';
COMMENT ON COLUMN sys_dept.updater IS '更新者';
COMMENT ON COLUMN sys_dept.update_date IS '更新时间';


create table sys_menu
(
  id                   int8 not null,
  pid                  int8,
  url                  varchar(200),
  menu_type            int,
  open_style           int,
  menu_hide            int default 0,
  menu_hide            int default 0,
  icon                 varchar(50),
  permissions          varchar(200),
  sort                 int,
  del_flag             int,
  creator              int8,
  create_date          timestamp,
  updater              int8,
  update_date          timestamp,
  primary key (id)
);
CREATE INDEX idx_sys_menu_pid on sys_menu(pid);
CREATE INDEX idx_sys_menu_del_flag on sys_menu(del_flag);
CREATE INDEX idx_sys_menu_create_date on sys_menu(create_date);

COMMENT ON TABLE sys_menu IS '菜单管理';
COMMENT ON COLUMN sys_menu.id IS 'id';
COMMENT ON COLUMN sys_menu.pid IS '上级ID，一级菜单为0';
COMMENT ON COLUMN sys_menu.url IS '菜单URL';
COMMENT ON COLUMN sys_menu.menu_type IS '类型   0：菜单   1：按钮';
COMMENT ON COLUMN sys_menu.open_style IS '打开方式   0：内部   1：外部';
COMMENT ON COLUMN sys_menu.icon IS '菜单图标';
COMMENT ON COLUMN sys_menu.permissions IS '权限标识，如：sys:menu:save';
COMMENT ON COLUMN sys_menu.sort IS '排序';
COMMENT ON COLUMN sys_menu.del_flag IS '删除标识  0：未删除    1：删除';
COMMENT ON COLUMN sys_menu.creator IS '创建者';
COMMENT ON COLUMN sys_menu.create_date IS '创建时间';
COMMENT ON COLUMN sys_menu.updater IS '更新者';
COMMENT ON COLUMN sys_menu.update_date IS '更新时间';


create table sys_role
(
  id                   int8 not null,
  name                 varchar(32),
  remark               varchar(100),
  del_flag             int,
  dept_id              int8,
  tenant_code          int8,
  creator              int8,
  create_date          timestamp,
  updater              int8,
  update_date          timestamp,
  primary key (id)
);
CREATE INDEX idx_sys_role_dept_id on sys_role(dept_id);
CREATE INDEX idx_sys_role_del_flag on sys_role(del_flag);
CREATE INDEX idx_sys_role_create_date on sys_role(create_date);

COMMENT ON TABLE sys_role IS '角色管理';
COMMENT ON COLUMN sys_role.id IS 'id';
COMMENT ON COLUMN sys_role.name IS '角色名称';
COMMENT ON COLUMN sys_role.remark IS '备注';
COMMENT ON COLUMN sys_role.del_flag IS '删除标识  0：未删除    1：删除';
COMMENT ON COLUMN sys_role.dept_id IS '部门ID';
COMMENT ON COLUMN sys_role.creator IS '创建者';
COMMENT ON COLUMN sys_role.create_date IS '创建时间';
COMMENT ON COLUMN sys_role.updater IS '更新者';
COMMENT ON COLUMN sys_role.update_date IS '更新时间';


create table sys_role_user
(
  id                   int8 not null,
  role_id              int8,
  user_id              int8,
  creator              int8,
  create_date          timestamp,
  primary key (id)
);
CREATE INDEX idx_sys_role_user_role_id on sys_role_user(role_id);
CREATE INDEX idx_sys_role_user_user_id on sys_role_user(user_id);

COMMENT ON TABLE sys_role_user IS '角色用户关系';
COMMENT ON COLUMN sys_role_user.role_id IS '角色ID';
COMMENT ON COLUMN sys_role_user.user_id IS '用户ID';
COMMENT ON COLUMN sys_role_user.creator IS '创建者';
COMMENT ON COLUMN sys_role_user.create_date IS '创建时间';


create table sys_role_menu
(
  id                   int8 not null,
  role_id              int8,
  menu_id              int8,
  creator              int8,
  create_date          timestamp,
  primary key (id)
);
CREATE INDEX idx_sys_role_menu_role_id on sys_role_menu(role_id);
CREATE INDEX idx_sys_role_menu_menu_id on sys_role_menu(menu_id);

COMMENT ON TABLE sys_role_menu IS '角色菜单关系';
COMMENT ON COLUMN sys_role_menu.role_id IS '角色ID';
COMMENT ON COLUMN sys_role_menu.menu_id IS '菜单ID';
COMMENT ON COLUMN sys_role_menu.creator IS '创建者';
COMMENT ON COLUMN sys_role_menu.create_date IS '创建时间';


create table sys_role_data_scope
(
  id                   int8 not null,
  role_id              int8,
  dept_id              int8,
  creator              int8,
  create_date          timestamp,
  primary key (id)
);
CREATE INDEX idx_data_scope_role_id on sys_role_data_scope(role_id);

COMMENT ON TABLE sys_role_data_scope IS '角色数据权限';
COMMENT ON COLUMN sys_role_data_scope.role_id IS '角色ID';
COMMENT ON COLUMN sys_role_data_scope.dept_id IS '部门ID';
COMMENT ON COLUMN sys_role_data_scope.creator IS '创建者';
COMMENT ON COLUMN sys_role_data_scope.create_date IS '创建时间';


create table sys_dict_type
(
    id                   int8 NOT NULL,
    dict_type            varchar(100),
    dict_name            varchar(255),
    remark               varchar(255),
    sort                 int,
    creator              int8,
    create_date          timestamp,
    updater              int8,
    update_date          timestamp,
    primary key (id)
);
CREATE UNIQUE INDEX uk_sys_dict_type_dict_type on sys_dict_type(dict_type);

COMMENT ON TABLE sys_dict_type IS '字典类型';
COMMENT ON COLUMN sys_dict_type.id IS 'id';
COMMENT ON COLUMN sys_dict_type.dict_type IS '字典类型';
COMMENT ON COLUMN sys_dict_type.dict_name IS '字典名称';
COMMENT ON COLUMN sys_dict_type.remark IS '备注';
COMMENT ON COLUMN sys_dict_type.sort IS '排序';
COMMENT ON COLUMN sys_dict_type.creator IS '创建者';
COMMENT ON COLUMN sys_dict_type.create_date IS '创建时间';
COMMENT ON COLUMN sys_dict_type.updater IS '更新者';
COMMENT ON COLUMN sys_dict_type.update_date IS '更新时间';

create table sys_dict_data
(
    id                   int8 NOT NULL,
    dict_type_id         int8 NOT NULL,
    dict_label           varchar(255),
    dict_value           varchar(255),
    remark               varchar(255),
    sort                 int,
    creator              int8,
    create_date          timestamp,
    updater              int8,
    update_date          timestamp,
    primary key (id)
);
CREATE INDEX idx_sys_dict_data_sort on sys_dict_data(sort);
CREATE UNIQUE INDEX uk_dict_type_value on sys_dict_data(dict_type_id, dict_value);

COMMENT ON TABLE sys_dict_data IS '字典数据';
COMMENT ON COLUMN sys_dict_data.id IS 'id';
COMMENT ON COLUMN sys_dict_data.dict_type_id IS '字典类型ID';
COMMENT ON COLUMN sys_dict_data.dict_label IS '字典标签';
COMMENT ON COLUMN sys_dict_data.dict_value IS '字典值';
COMMENT ON COLUMN sys_dict_data.remark IS '备注';
COMMENT ON COLUMN sys_dict_data.sort IS '排序';
COMMENT ON COLUMN sys_dict_data.creator IS '创建者';
COMMENT ON COLUMN sys_dict_data.create_date IS '创建时间';
COMMENT ON COLUMN sys_dict_data.updater IS '更新者';
COMMENT ON COLUMN sys_dict_data.update_date IS '更新时间';


CREATE TABLE sys_region (
    id int8 NOT NULL,
    pid int8,
    name varchar(100),
    tree_level int,
    leaf int,
    sort int8,
    creator int8,
    create_date timestamp,
    updater int8,
    update_date timestamp,
    PRIMARY KEY (id)
);

COMMENT ON TABLE sys_region IS '行政区域';
COMMENT ON COLUMN sys_region.pid IS '上级ID';
COMMENT ON COLUMN sys_region.name IS '名称';
COMMENT ON COLUMN sys_region.tree_level IS '层级';
COMMENT ON COLUMN sys_region.leaf IS '是否叶子节点  0：否   1：是';
COMMENT ON COLUMN sys_region.sort IS '排序';
COMMENT ON COLUMN sys_region.creator IS '创建者';
COMMENT ON COLUMN sys_region.create_date IS '创建时间';
COMMENT ON COLUMN sys_region.updater IS '更新者';
COMMENT ON COLUMN sys_region.update_date IS '更新时间';


create table sys_params
(
  id                   int8 not null,
  param_code           varchar(32),
  param_value          varchar(2000),
  param_type           int DEFAULT 1 NOT NULL,
  remark               varchar(200),
  del_flag             int,
  creator              int8,
  create_date          timestamp,
  updater              int8,
  update_date          timestamp,
  primary key (id),
  UNIQUE (param_code)
);
CREATE INDEX idx_sys_params_del_flag on sys_params(del_flag);
CREATE INDEX idx_sys_params_create_date on sys_params(create_date);

COMMENT ON TABLE sys_params IS '参数管理';
COMMENT ON COLUMN sys_params.param_code IS '参数编码';
COMMENT ON COLUMN sys_params.param_value IS '参数值';
COMMENT ON COLUMN sys_params.param_type IS '类型   0：非系统参数   1：系统参数';
COMMENT ON COLUMN sys_params.remark IS '备注';
COMMENT ON COLUMN sys_params.del_flag IS '删除标识  0：未删除    1：删除';
COMMENT ON COLUMN sys_params.creator IS '创建者';
COMMENT ON COLUMN sys_params.create_date IS '创建时间';
COMMENT ON COLUMN sys_params.updater IS '更新者';
COMMENT ON COLUMN sys_params.update_date IS '更新时间';


create table sys_log_login
(
  id                   int8 not null,
  operation            int,
  user_agent           varchar(500),
  ip                   varchar(160),
  creator_name         varchar(50),
  creator              int8,
  create_date          timestamp,
  primary key (id)
);

CREATE INDEX idx_login_create_date on sys_log_login(create_date);

COMMENT ON TABLE sys_log_login IS '登录日志';
COMMENT ON COLUMN sys_log_login.id IS 'id';
COMMENT ON COLUMN sys_log_login.operation IS '用户操作';
COMMENT ON COLUMN sys_log_login.user_agent IS '用户代理';
COMMENT ON COLUMN sys_log_login.ip IS '操作IP';
COMMENT ON COLUMN sys_log_login.creator_name IS '用户名';
COMMENT ON COLUMN sys_log_login.creator IS '创建者';
COMMENT ON COLUMN sys_log_login.create_date IS '创建时间';


create table sys_log_operation
(
  id                   int8 not null,
  module               varchar(32),
  operation            varchar(50),
  request_uri          varchar(200),
  request_method       varchar(20),
  request_params       text,
  request_time         int,
  user_agent           varchar(500),
  ip                   varchar(160),
  status               int,
  creator_name         varchar(50),
  creator              int8,
  create_date          timestamp,
  primary key (id)
);
CREATE INDEX idx_operation_module on sys_log_operation(module);
CREATE INDEX idx_operation_create_date on sys_log_operation(create_date);

COMMENT ON TABLE sys_log_operation IS '操作日志';
COMMENT ON COLUMN sys_log_operation.id IS 'id';
COMMENT ON COLUMN sys_log_operation.module IS '模块名称，如：sys';
COMMENT ON COLUMN sys_log_operation.operation IS '用户操作';
COMMENT ON COLUMN sys_log_operation.request_uri IS '请求URI';
COMMENT ON COLUMN sys_log_operation.request_method IS '请求方式';
COMMENT ON COLUMN sys_log_operation.request_params IS '请求参数';
COMMENT ON COLUMN sys_log_operation.request_time IS '请求时长(毫秒)';
COMMENT ON COLUMN sys_log_operation.user_agent IS '用户代理';
COMMENT ON COLUMN sys_log_operation.ip IS '操作IP';
COMMENT ON COLUMN sys_log_operation.status IS '状态  0：失败   1：成功';
COMMENT ON COLUMN sys_log_operation.creator_name IS '用户名';
COMMENT ON COLUMN sys_log_operation.creator IS '创建者';
COMMENT ON COLUMN sys_log_operation.create_date IS '创建时间';


create table sys_log_error
(
  id                   int8 not null,
  module               varchar(50),
  request_uri          varchar(200),
  request_method       varchar(20),
  request_params       text,
  user_agent           varchar(500),
  ip                   varchar(160),
  error_info           text,
  creator              int8,
  create_date          timestamp,
  primary key (id)
);
CREATE INDEX idx_error_module on sys_log_error(module);
CREATE INDEX idx_error_create_date on sys_log_error(create_date);

COMMENT ON TABLE sys_log_error IS '异常日志';
COMMENT ON COLUMN sys_log_error.id IS 'id';
COMMENT ON COLUMN sys_log_error.module IS '模块名称，如：sys';
COMMENT ON COLUMN sys_log_error.request_uri IS '请求URI';
COMMENT ON COLUMN sys_log_error.request_method IS '请求方式';
COMMENT ON COLUMN sys_log_error.request_params IS '请求参数';
COMMENT ON COLUMN sys_log_error.user_agent IS '用户代理';
COMMENT ON COLUMN sys_log_error.ip IS '操作IP';
COMMENT ON COLUMN sys_log_error.error_info IS '异常信息';
COMMENT ON COLUMN sys_log_error.creator IS '创建者';
COMMENT ON COLUMN sys_log_error.create_date IS '创建时间';


CREATE TABLE sys_language (
  table_name varchar(32) NOT NULL,
  table_id int8 NOT NULL,
  field_name varchar(32) NOT NULL,
  field_value varchar(200) NOT NULL,
  language varchar(10) NOT NULL,
  primary key (table_name, table_id, field_name, language)
);

CREATE INDEX idx_sys_language_table_id on sys_language(table_id);

COMMENT ON TABLE sys_language IS '国际化';
COMMENT ON COLUMN sys_language.table_name IS '表名';
COMMENT ON COLUMN sys_language.table_id IS '表主键';
COMMENT ON COLUMN sys_language.field_name IS '字段名';
COMMENT ON COLUMN sys_language.field_value IS '字段值';
COMMENT ON COLUMN sys_language.language IS '语言';


CREATE TABLE tb_news (
 id int8 NOT NULL,
 title varchar(100),
 content text,
 pub_date timestamp,
 dept_id int8,
 creator int8,
 create_date timestamp,
 updater int8,
 update_date timestamp,
 PRIMARY KEY (id)
);

COMMENT ON TABLE tb_news IS '新闻管理';
COMMENT ON COLUMN tb_news.id IS 'id';
COMMENT ON COLUMN tb_news.title IS '标题';
COMMENT ON COLUMN tb_news.content IS '内容';
COMMENT ON COLUMN tb_news.pub_date IS '发布时间';
COMMENT ON COLUMN tb_news.dept_id IS '创建者dept_id';
COMMENT ON COLUMN tb_news.creator IS '创建者';
COMMENT ON COLUMN tb_news.create_date IS '创建时间';
COMMENT ON COLUMN tb_news.updater IS '更新者';
COMMENT ON COLUMN tb_news.update_date IS '更新时间';


CREATE TABLE sys_notice (
    id int8 NOT NULL,
    notice_type int NOT NULL,
    title varchar(200),
    content text,
    receiver_type int,
    receiver_type_ids varchar(500),
    status int,
    sender_name varchar(50),
    sender_date timestamp,
    creator int8,
    create_date timestamp,
    PRIMARY KEY (id)
);
CREATE INDEX idx_sys_notice_create_date on sys_notice(create_date);

COMMENT ON TABLE sys_notice IS '通知管理';
COMMENT ON COLUMN sys_notice.id IS 'id';
COMMENT ON COLUMN sys_notice.notice_type IS '通知类型';
COMMENT ON COLUMN sys_notice.title IS '标题';
COMMENT ON COLUMN sys_notice.content IS '内容';
COMMENT ON COLUMN sys_notice.receiver_type IS '接收者  0：全部  1：部门';
COMMENT ON COLUMN sys_notice.receiver_type_ids IS '接收者ID，用逗号分开';
COMMENT ON COLUMN sys_notice.status IS '发送状态  0：草稿  1：已发布';
COMMENT ON COLUMN sys_notice.sender_name IS '发送者';
COMMENT ON COLUMN sys_notice.sender_date IS '发送时间';
COMMENT ON COLUMN sys_notice.creator IS '创建者';
COMMENT ON COLUMN sys_notice.create_date IS '创建时间';


CREATE TABLE sys_notice_user (
    receiver_id int8 NOT NULL,
    notice_id int8 NOT NULL,
    read_status int NOT NULL,
    read_date timestamp,
    PRIMARY KEY (receiver_id, notice_id)
);

COMMENT ON TABLE sys_notice_user IS '我的通知';
COMMENT ON COLUMN sys_notice_user.receiver_id IS '接收者ID';
COMMENT ON COLUMN sys_notice_user.notice_id IS '通知ID';
COMMENT ON COLUMN sys_notice_user.read_status IS '阅读状态  0：未读  1：已读';
COMMENT ON COLUMN sys_notice_user.read_date IS '阅读时间';

CREATE TABLE tb_product (
    id int8 NOT NULL,
    name varchar(100),
    content varchar(2000),
    creator int8,
    create_date timestamp,
    updater int8,
    update_date timestamp,
    PRIMARY KEY (id)
);

COMMENT ON TABLE tb_product IS '产品管理';
COMMENT ON COLUMN tb_product.id IS 'id';
COMMENT ON COLUMN tb_product.name IS '产品名称';
COMMENT ON COLUMN tb_product.content IS '产品介绍';
COMMENT ON COLUMN tb_product.creator IS '创建者';
COMMENT ON COLUMN tb_product.create_date IS '创建时间';
COMMENT ON COLUMN tb_product.updater IS '更新者';
COMMENT ON COLUMN tb_product.update_date IS '更新时间';

CREATE TABLE tb_product_params (
    id int8 NOT NULL,
    param_name varchar(100),
    param_value varchar(200),
    product_id int8,
    creator int8,
    create_date timestamp,
    updater int8,
    update_date timestamp,
    PRIMARY KEY (id)
);

COMMENT ON TABLE tb_product_params IS '产品参数管理';
COMMENT ON COLUMN tb_product_params.id IS 'id';
COMMENT ON COLUMN tb_product_params.param_name IS '参数名';
COMMENT ON COLUMN tb_product_params.param_value IS '参数值';
COMMENT ON COLUMN tb_product_params.product_id IS '产品ID';
COMMENT ON COLUMN tb_product_params.creator IS '创建者';
COMMENT ON COLUMN tb_product_params.create_date IS '创建时间';
COMMENT ON COLUMN tb_product_params.updater IS '更新者';
COMMENT ON COLUMN tb_product_params.update_date IS '更新时间';


create table sys_post (
    id int8 NOT NULL,
    post_code varchar(100),
    post_name varchar(100),
    sort int,
    status int,
    tenant_code          int8,
    creator int8,
    create_date timestamp,
    updater int8,
    update_date timestamp,
    PRIMARY KEY (id)
);

COMMENT ON TABLE sys_post IS '岗位管理';
COMMENT ON COLUMN sys_post.id IS 'id';
COMMENT ON COLUMN sys_post.post_code IS '岗位编码';
COMMENT ON COLUMN sys_post.post_name IS '岗位名称';
COMMENT ON COLUMN sys_post.sort IS '排序';
COMMENT ON COLUMN sys_post.status IS '状态  0：停用   1：正常';
COMMENT ON COLUMN sys_post.creator IS '创建者';
COMMENT ON COLUMN sys_post.create_date IS '创建时间';
COMMENT ON COLUMN sys_post.updater IS '更新者';
COMMENT ON COLUMN sys_post.update_date IS '更新时间';


CREATE TABLE sys_user_post (
    id                   int8 NOT NULL,
    post_id              int8,
    user_id              int8,
    creator              int8,
    create_date          timestamp,
    primary key (id)
);

CREATE INDEX idx_sys_user_post_post_id on sys_user_post(post_id);
CREATE INDEX idx_sys_user_post_user_id on sys_user_post(user_id);

COMMENT ON TABLE sys_user_post IS '用户岗位关系';
COMMENT ON COLUMN sys_user_post.id IS 'id';
COMMENT ON COLUMN sys_user_post.post_id IS '岗位ID';
COMMENT ON COLUMN sys_user_post.user_id IS '用户ID';
COMMENT ON COLUMN sys_post.creator IS '创建者';
COMMENT ON COLUMN sys_post.create_date IS '创建时间';


CREATE TABLE tb_excel_data (
    id int8 NOT NULL,
    real_name varchar(100),
    user_identity varchar(100),
    address varchar(200),
    join_date timestamp,
    class_name varchar(100),
    creator int8,
    create_date timestamp,
    primary key (id)
);

COMMENT ON TABLE tb_excel_data IS 'Excel导入演示';
COMMENT ON COLUMN tb_excel_data.id IS 'id';
COMMENT ON COLUMN tb_excel_data.real_name IS '学生姓名';
COMMENT ON COLUMN tb_excel_data.user_identity IS '身份证';
COMMENT ON COLUMN tb_excel_data.address IS '家庭地址';
COMMENT ON COLUMN tb_excel_data.join_date IS '入学日期';
COMMENT ON COLUMN tb_excel_data.class_name IS '班级名称';
COMMENT ON COLUMN tb_excel_data.creator IS '创建者';
COMMENT ON COLUMN tb_excel_data.create_date IS '创建时间';

CREATE TABLE tb_order (
    id int8 NOT NULL,
    order_id int8,
    product_id int8,
    product_name varchar(100),
    pay_amount numeric(10,2),
    status int,
    user_id int8,
    pay_at timestamp,
    create_date timestamp,
    primary key (id)
);

CREATE UNIQUE INDEX uk_tb_order_order_id on tb_order(order_id);

COMMENT ON TABLE tb_order IS '订单';
COMMENT ON COLUMN tb_order.id IS 'id';
COMMENT ON COLUMN tb_order.order_id IS '订单ID';
COMMENT ON COLUMN tb_order.product_id IS '产品ID';
COMMENT ON COLUMN tb_order.product_name IS '产品名称';
COMMENT ON COLUMN tb_order.pay_amount IS '支付金额';
COMMENT ON COLUMN tb_order.status IS '订单状态  -1：已取消   0：等待付款   1：已完成';
COMMENT ON COLUMN tb_order.user_id IS '购买用户ID';
COMMENT ON COLUMN tb_order.pay_at IS '支付时间';
COMMENT ON COLUMN tb_order.create_date IS '下单时间';


CREATE TABLE tb_alipay_notify_log (
    id int8 NOT NULL,
    out_trade_no int8,
    total_amount numeric(10,2),
    buyer_pay_amount numeric(10,2),
    receipt_amount numeric(10,2),
    invoice_amount numeric(10,2),
    notify_id varchar(50),
    buyer_id varchar(50),
    seller_id varchar(50),
    trade_no varchar(50),
    trade_status varchar(50),
    create_date timestamp,
    primary key (id)
);

COMMENT ON TABLE tb_alipay_notify_log IS '支付宝回调日志';
COMMENT ON COLUMN tb_alipay_notify_log.id IS 'id';
COMMENT ON COLUMN tb_alipay_notify_log.out_trade_no IS '订单号';
COMMENT ON COLUMN tb_alipay_notify_log.total_amount IS '订单金额';
COMMENT ON COLUMN tb_alipay_notify_log.buyer_pay_amount IS '付款金额';
COMMENT ON COLUMN tb_alipay_notify_log.receipt_amount IS '实收金额';
COMMENT ON COLUMN tb_alipay_notify_log.invoice_amount IS '开票金额';
COMMENT ON COLUMN tb_alipay_notify_log.notify_id IS '通知校验ID';
COMMENT ON COLUMN tb_alipay_notify_log.buyer_id IS '买家支付宝用户号';
COMMENT ON COLUMN tb_alipay_notify_log.seller_id IS '卖家支付宝用户号';
COMMENT ON COLUMN tb_alipay_notify_log.trade_no IS '支付宝交易号';
COMMENT ON COLUMN tb_alipay_notify_log.trade_status IS '交易状态';
COMMENT ON COLUMN tb_alipay_notify_log.create_date IS '创建时间';


CREATE TABLE tb_wechat_notify_log (
    id int8 NOT NULL,
    out_trade_no varchar(100),
    total int,
    payer_total int,
    currency varchar(50),
    payer_currency varchar(50),
    bank_type varchar(50),
    trade_state varchar(50),
    trade_state_desc varchar(500),
    trade_type varchar(50),
    transaction_id varchar(100),
    success_time varchar(100),
    create_date timestamp,
    primary key (id)
);

COMMENT ON TABLE tb_wechat_notify_log IS '微信回调日志';
COMMENT ON COLUMN tb_wechat_notify_log.id IS 'id';
COMMENT ON COLUMN tb_wechat_notify_log.out_trade_no IS '订单号';
COMMENT ON COLUMN tb_wechat_notify_log.total IS '订单总金额，单位为分';
COMMENT ON COLUMN tb_wechat_notify_log.payer_total IS '用户支付金额，单位为分';
COMMENT ON COLUMN tb_wechat_notify_log.currency IS 'CNY：人民币，境内商户号仅支持人民币';
COMMENT ON COLUMN tb_wechat_notify_log.payer_currency IS '用户支付币种';
COMMENT ON COLUMN tb_wechat_notify_log.bank_type IS '银行类型';
COMMENT ON COLUMN tb_wechat_notify_log.trade_state IS '交易状态';
COMMENT ON COLUMN tb_wechat_notify_log.trade_state_desc IS '交易状态描述';
COMMENT ON COLUMN tb_wechat_notify_log.trade_type IS '交易类型';
COMMENT ON COLUMN tb_wechat_notify_log.transaction_id IS '微信支付系统生成的订单号';
COMMENT ON COLUMN tb_wechat_notify_log.success_time IS '支付完成时间';
COMMENT ON COLUMN tb_wechat_notify_log.create_date IS '创建时间';



CREATE TABLE mp_account (
    id int8 NOT NULL,
    name varchar(100),
    app_id varchar(100),
    app_secret varchar(100),
    token varchar(100),
    aes_key varchar(100),
    creator int8,
    create_date timestamp,
    updater int8,
    update_date timestamp,
    primary key (id)
);

COMMENT ON TABLE mp_account IS '公众号账号管理';
COMMENT ON COLUMN mp_account.id IS 'id';
COMMENT ON COLUMN mp_account.name IS '名称';
COMMENT ON COLUMN mp_account.app_id IS 'AppID';
COMMENT ON COLUMN mp_account.app_secret IS 'AppSecret';
COMMENT ON COLUMN mp_account.token IS 'Token';
COMMENT ON COLUMN mp_account.aes_key IS 'EncodingAESKey';
COMMENT ON COLUMN mp_account.creator IS '创建者';
COMMENT ON COLUMN mp_account.create_date IS '创建时间';
COMMENT ON COLUMN mp_account.updater IS '更新者';
COMMENT ON COLUMN mp_account.update_date IS '更新时间';


CREATE TABLE mp_menu (
    id int8 NOT NULL,
    menu varchar(2000),
    app_id varchar(100),
    creator int8,
    create_date timestamp,
    updater int8,
    update_date timestamp,
    primary key (id)
);

COMMENT ON TABLE mp_menu IS '公众号自定义菜单';
COMMENT ON COLUMN mp_menu.id IS 'id';
COMMENT ON COLUMN mp_menu.menu IS '菜单json数据';
COMMENT ON COLUMN mp_menu.app_id IS 'AppID';
COMMENT ON COLUMN mp_menu.creator IS '创建者';
COMMENT ON COLUMN mp_menu.create_date IS '创建时间';
COMMENT ON COLUMN mp_menu.updater IS '更新者';
COMMENT ON COLUMN mp_menu.update_date IS '更新时间';



CREATE TABLE ai_model
(
    id               int8 NOT NULL,
    platform         varchar(200) NOT NULL,
    name             varchar(100) NOT NULL,
    model            varchar(100) NOT NULL,
    api_url          varchar(200) NOT NULL,
    api_key          varchar(200),
    status           int,
    sort             int,
    creator          int8,
    create_date      timestamp,
    primary key (id)
);

COMMENT ON TABLE ai_model IS 'AI模型';
COMMENT ON COLUMN ai_model.id IS 'id';
COMMENT ON COLUMN ai_model.platform IS '所属平台';
COMMENT ON COLUMN ai_model.name IS '模型名称';
COMMENT ON COLUMN ai_model.model IS '模型标识';
COMMENT ON COLUMN ai_model.api_url IS 'API地址';
COMMENT ON COLUMN ai_model.api_key IS 'API秘钥';
COMMENT ON COLUMN ai_model.status IS '状态  0：停用   1：正常';
COMMENT ON COLUMN ai_model.sort IS '排序';
COMMENT ON COLUMN ai_model.creator IS '创建者';

CREATE TABLE ai_chat_conversation
(
    id               int8 NOT NULL,
    title            varchar(200) NOT NULL,
    model_id         int8 NOT NULL,
    model            varchar(100) NOT NULL,
    user_id          int8,
    create_date      timestamp,
    primary key (id)
);

COMMENT ON TABLE ai_chat_conversation IS 'AI聊天对话';
COMMENT ON COLUMN ai_chat_conversation.id IS 'id';
COMMENT ON COLUMN ai_chat_conversation.title IS '对话标题';
COMMENT ON COLUMN ai_chat_conversation.model_id IS '模型ID';
COMMENT ON COLUMN ai_chat_conversation.model IS '模型标识';
COMMENT ON COLUMN ai_chat_conversation.user_id IS '用户ID';
COMMENT ON COLUMN ai_chat_conversation.create_date IS '创建时间';

CREATE TABLE ai_chat_message
(
    id               int8 NOT NULL,
    type             varchar(50) NOT NULL,
    content          varchar(5000) NOT NULL,
    model_id         int8 NOT NULL,
    model            varchar(100) NOT NULL,
    conversation_id  int8,
    create_date      timestamp,
    primary key (id)
);

COMMENT ON TABLE ai_chat_message IS 'AI聊天消息';
COMMENT ON COLUMN ai_chat_message.id IS 'id';
COMMENT ON COLUMN ai_chat_message.type IS '消息类型';
COMMENT ON COLUMN ai_chat_message.content IS '回答内容';
COMMENT ON COLUMN ai_chat_message.model_id IS '模型ID';
COMMENT ON COLUMN ai_chat_message.model IS '模型标识';
COMMENT ON COLUMN ai_chat_message.conversation_id IS '对话ID';
COMMENT ON COLUMN ai_chat_message.create_date IS '创建时间';


-- 初始数据
INSERT INTO sys_tenant (id, datasource_id, tenant_name, tenant_domain, remark, user_id, username, status, tenant_mode, del_flag, creator, create_date, updater, update_date) VALUES (10000, NULL, '默认租户', NULL, NULL, 1067246875800000001, 'admin', 1, 0, 0, NULL, now(), NULL, now());

INSERT INTO sys_user(id, username, password, real_name, gender, email, mobile, status, dept_id, tenant_code, super_admin, super_tenant, remark, del_flag, creator, create_date, updater, update_date) VALUES (1067246875800000001, 'admin', 'dc1fd00e3eeeb940ff46f457bf97d66ba7fcc36e0b20802383de142860e76ae6', '管理员', 0, 'root@jslh.io', '13612345678', 1, null, 10000, 1, 1, NULL, 0, NULL, now(), NULL, now());

INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1067246875800000002, 0, '', 0, 0, 'icon-lock', '', 0, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1067246875800000003, 1067246875800000002, 'sys/user', 0, 0, 'icon-user', '', 0, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1067246875800000004, 1067246875800000003, '', 0, 1, '', 'sys:user:page,sys:user:info', 0, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1067246875800000005, 1067246875800000003, '', 0, 1, '', 'sys:user:save,sys:dept:list,sys:role:list', 1, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1067246875800000006, 1067246875800000003, '', 0, 1, '', 'sys:user:update,sys:dept:list,sys:role:list', 2, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1067246875800000007, 1067246875800000003, '', 0, 1, '', 'sys:user:delete', 3, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1067246875800000008, 1067246875800000003, '', 0, 1, '', 'sys:user:export', 4, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1067246875800000009, 1067246875800000002, 'sys/dept', 0, 0, 'icon-apartment', '', 1, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1067246875800000010, 1067246875800000009, '', 0, 1, '', 'sys:dept:list,sys:dept:info', 0, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1067246875800000011, 1067246875800000009, '', 0, 1, '', 'sys:dept:save', 1, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1067246875800000012, 1067246875800000009, '', 0, 1, '', 'sys:dept:update', 2, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1067246875800000013, 1067246875800000009, '', 0, 1, '', 'sys:dept:delete', 3, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1067246875800000014, 1067246875800000002, 'sys/role', 0, 0, 'icon-team', '', 2, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1067246875800000015, 1067246875800000014, '', 0, 1, '', 'sys:role:page,sys:role:info', 0, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1067246875800000016, 1067246875800000014, '', 0, 1, '', 'sys:role:save,sys:menu:select,sys:dept:list', 1, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1067246875800000017, 1067246875800000014, '', 0, 1, '', 'sys:role:update,sys:menu:select,sys:dept:list', 2, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1067246875800000018, 1067246875800000014, '', 0, 1, '', 'sys:role:delete', 3, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1067246875800000019, 0, '', 0, 0, 'icon-setting', NULL, 1, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1067246875800000020, 1067246875800000019, 'sys/menu', 0, 0, 'icon-unorderedlist', NULL, 0, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1067246875800000021, 1067246875800000020, NULL, 0, 1, NULL, 'sys:menu:list,sys:menu:info', 0, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1067246875800000022, 1067246875800000020, NULL, 0, 1, NULL, 'sys:menu:save', 1, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1067246875800000023, 1067246875800000020, NULL, 0, 1, NULL, 'sys:menu:update', 2, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1067246875800000024, 1067246875800000020, NULL, 0, 1, NULL, 'sys:menu:delete', 3, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1067246875800000025, 1067246875800000019, 'sys/params', 0, 0, 'icon-fileprotect', '', 1, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1067246875800000026, 1067246875800000025, NULL, 0, 1, NULL, 'sys:params:page,sys:params:info', 0, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1067246875800000027, 1067246875800000025, NULL, 0, 1, NULL, 'sys:params:save', 1, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1067246875800000028, 1067246875800000025, NULL, 0, 1, NULL, 'sys:params:update', 2, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1067246875800000029, 1067246875800000025, NULL, 0, 1, NULL, 'sys:params:delete', 3, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1067246875800000030, 1067246875800000025, '', 0, 1, NULL, 'sys:params:export', 4, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1067246875800000031, 1067246875800000019, 'sys/dict-type', 0, 0, 'icon-gold', '', 2, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1067246875800000032, 1067246875800000031, '', 0, 1, '', 'sys:dict:page,sys:dict:info', 0, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1067246875800000033, 1067246875800000031, '', 0, 1, '', 'sys:dict:save', 1, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1067246875800000034, 1067246875800000031, '', 0, 1, '', 'sys:dict:update', 2, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1067246875800000035, 1067246875800000031, '', 0, 1, '', 'sys:dict:delete', 3, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1067246875800000036, 0, '', 0, 0, 'icon-container', '', 4, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1067246875800000037, 1067246875800000036, 'sys/log-login', 0, 0, 'icon-filedone', 'sys:log:login', 0, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1067246875800000038, 1067246875800000036, 'sys/log-operation', 0, 0, 'icon-solution', 'sys:log:operation', 1, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1067246875800000039, 1067246875800000036, 'sys/log-error', 0, 0, 'icon-file-exception', 'sys:log:error', 2, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1067246875800000040, 0, '', 0, 0, 'icon-desktop', '', 5, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1067246875800000041, 1067246875800000040, '{{ApiUrl}}/monitor', 0, 0, 'icon-medicinebox', '', 0, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1067246875800000042, 1067246875800000040, '{{ApiUrl}}/doc.html', 1, 0, 'icon-file-word', '', 1, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1127520061821603842, 0, '', 0, 0, 'icon-home', '', 2, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1127520228847177730, 1127520061821603842, 'tenant/tenant', 0, 0, 'icon-team', 'sys:tenant:all,tenant:datasource:all', 0, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1127521799777603585, 1127520061821603842, 'tenant/tenant-datasource', 0, 0, 'icon-database', 'tenant:datasource:all', 1, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1127521799777603586, 1127520061821603842, 'tenant/tenant-role', 0, 0, 'icon-carryout-fill', 'sys:tenantrole:all', 1, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1164489061834969089, 1067246875800000019, 'sys/region', 0, 0, 'icon-location', '0', 3, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1164492214366130178, 1164489061834969089, '', 0, 1, '', 'sys:region:list,sys:region:info', 0, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1164492872829915138, 1164489061834969089, '', 0, 1, '', 'sys:region:save', 1, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1164493252347318273, 1164489061834969089, '', 0, 1, '', 'sys:region:update', 2, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1164493391254278145, 1164489061834969089, '', 0, 1, '', 'sys:region:delete', 3, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1176372255559024642, 0, '', 0, 0, 'icon-windows', '', 999, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1206460008292216834, 1176372255559024642, 'sys/news', 0, 0, 'icon-file-word', 'demo:news:all', 0, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1067246875800000401, 0, '', 0, 0, 'icon-bell', '', 4, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1067246875800000402, 1067246875800000401, 'sys/notice', 0, 0, 'icon-bell', 'sys:notice:all,sys:dept:list', 0, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, menu_type, icon, permissions, sort, del_flag, creator, create_date, updater, update_date) VALUES (1067246875800000403, 1067246875800000401, 'sys/notice-user', 0, 0, 'icon-notification', 'sys:notice:all', 1, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, permissions, menu_type, icon, sort, del_flag, creator, create_date, updater, update_date) VALUES (1270380959719501825, 1176372255559024642, 'sys/product', 0, NULL, 0, 'icon-tag', 1, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, permissions, menu_type, icon, sort, del_flag, creator, create_date, updater, update_date) VALUES (1270380959719501826, 1270380959719501825, NULL, 0, 'demo:product:page,demo:product:info', 1, NULL, 0, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, permissions, menu_type, icon, sort, del_flag, creator, create_date, updater, update_date) VALUES (1270380959719501827, 1270380959719501825, NULL, 0, 'demo:product:save', 1, NULL, 1, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, permissions, menu_type, icon, sort, del_flag, creator, create_date, updater, update_date) VALUES (1270380959719501828, 1270380959719501825, NULL, 0, 'demo:product:update', 1, NULL, 2, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, permissions, menu_type, icon, sort, del_flag, creator, create_date, updater, update_date) VALUES (1270380959719501829, 1270380959719501825, NULL, 0, 'demo:product:delete', 1, NULL, 3, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, permissions, menu_type, icon, sort, del_flag, creator, create_date, updater, update_date) VALUES (1300278047072649217, 0, '', 0, '', 0, 'icon-filesearch', 3, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, permissions, menu_type, icon, sort, del_flag, creator, create_date, updater, update_date) VALUES (1341596622612987906, 1067246875800000002, 'sys/post', 0, NULL, 0, 'icon-pic-left', 1, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, permissions, menu_type, icon, sort, del_flag, creator, create_date, updater, update_date) VALUES (1341596622688485377, 1341596622612987906, NULL, 0, 'sys:post:page,sys:post:info', 1, NULL, 0, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, permissions, menu_type, icon, sort, del_flag, creator, create_date, updater, update_date) VALUES (1341596622755594242, 1341596622612987906, NULL, 0, 'sys:post:save', 1, NULL, 1, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, permissions, menu_type, icon, sort, del_flag, creator, create_date, updater, update_date) VALUES (1341596622835286018, 1341596622612987906, NULL, 0, 'sys:post:update', 1, NULL, 2, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, permissions, menu_type, icon, sort, del_flag, creator, create_date, updater, update_date) VALUES (1341596622902394881, 1341596622612987906, NULL, 0, 'sys:post:delete', 1, NULL, 3, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, permissions, menu_type, icon, sort, del_flag, creator, create_date, updater, update_date) VALUES (1341676084016852994, 1176372255559024642, 'sys/excel', 0, 'demo:excel:all', 0, 'icon-table', 1, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, permissions, menu_type, icon, sort, del_flag, creator, create_date, updater, update_date) VALUES (1343074487777677313, 0, '', 0, '', 0, 'icon-Dollar', 3, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, permissions, menu_type, icon, sort, del_flag, creator, create_date, updater, update_date) VALUES (1343074685589442561, 1343074487777677313, 'pay/order', 0, 'pay:order:all', 0, 'icon-unorderedlist', 0, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, permissions, menu_type, icon, sort, del_flag, creator, create_date, updater, update_date) VALUES (1343074794440019970, 1343074487777677313, 'pay/alipaynotifylog', 0, 'pay:alipayNotifyLog:all', 0, 'icon-filedone', 1, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, open_style, permissions, menu_type, icon, sort, del_flag, creator, create_date, updater, update_date) VALUES (1343074794440019971, 1343074487777677313, 'pay/wechatnotifylog', 0, 'pay:wechatNotifyLog:all', 0, 'icon-filedone', 2, 0, 1067246875800000001, now(), 1067246875800000001, now());

INSERT INTO sys_menu(id, pid, url, open_style, permissions, menu_type, icon, sort, del_flag, creator, create_date, updater, update_date) VALUES (1270380959719501800, 1176372255559024642, 'sys/charts', 0, '', 0, 'icon-tag', 1, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu (id, pid, url, permissions, menu_type, open_style, icon, sort, del_flag, creator, create_date, updater, update_date) VALUES (1419551957005963266, 0, '', '', 0, 0, 'icon-wechat-fill', 3, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu (id, pid, url, permissions, menu_type, open_style, icon, sort, del_flag, creator, create_date, updater, update_date) VALUES (1419553543706972161, 1419551957005963266, 'mp/account', 'mp:account:all', 0, 0, 'icon-user', 0, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu (id, pid, url, permissions, menu_type, open_style, icon, sort, del_flag, creator, create_date, updater, update_date) VALUES (1419963799817691137, 1419551957005963266, 'mp/menu', 'mp:menu:all', 0, 0, 'icon-unorderedlist', 1, 0, 1067246875800000001, now(), 1067246875800000001, now());



INSERT INTO sys_dict_type (id, dict_type, dict_name, remark, sort, creator, create_date, updater, update_date) VALUES (1899835801273425922, 'model_status', '模型状态', '', 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_dict_type (id, dict_type, dict_name, remark, sort, creator, create_date, updater, update_date) VALUES (1899836220481527809, 'ai_platform', 'AI 平台', '', 0, 1067246875800000001, now(), 1067246875800000001, now());

INSERT INTO sys_dict_data (id, dict_type_id, dict_label, dict_value, remark, sort, creator, create_date, updater, update_date) VALUES (1899835910191112194, 1899835801273425922, '禁用', '0', '', 1, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_dict_data (id, dict_type_id, dict_label, dict_value, remark, sort, creator, create_date, updater, update_date) VALUES (1899835947377811457, 1899835801273425922, '启用', '1', '', 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_dict_data (id, dict_type_id, dict_label, dict_value, remark, sort, creator, create_date, updater, update_date) VALUES (1899836364186771458, 1899836220481527809, 'OpenAI', 'OpenAI', '', 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_dict_data (id, dict_type_id, dict_label, dict_value, remark, sort, creator, create_date, updater, update_date) VALUES (1899839861087051778, 1899836220481527809, 'DeepSeek', 'DeepSeek', '', 1, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_dict_data (id, dict_type_id, dict_label, dict_value, remark, sort, creator, create_date, updater, update_date) VALUES (1899839914707034114, 1899836220481527809, 'Ollama', 'Ollama', '', 2, 1067246875800000001, now(), 1067246875800000001, now());

INSERT INTO sys_menu (id, pid, url, permissions, menu_type, open_style, icon, sort, del_flag, creator, create_date, updater, update_date) VALUES (1899830353732349953, 0, '', '', 0, 0, 'icon-font-colors', 4, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu (id, pid, url, permissions, menu_type, open_style, icon, sort, del_flag, creator, create_date, updater, update_date) VALUES (1899830660956729345, 1899830353732349953, 'ai/model/index', 'ai:model', 0, 0, 'icon-safetycertificate', 0, 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu (id, pid, url, permissions, menu_type, open_style, icon, sort, del_flag, creator, create_date, updater, update_date) VALUES (1899830944986607617, 1899830353732349953, 'ai/chat/index', 'ai:chat', 0, 0, 'icon-filesearch', 1, 0, 1067246875800000001, now(), 1067246875800000001, now());

INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1899830353732349953, 'name', 'AI Model', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1899830353732349953, 'name', 'AI 大模型', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1899830353732349953, 'name', 'AI 大模型', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1899830660956729345, 'name', 'AI Model', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1899830660956729345, 'name', 'AI 模型', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1899830660956729345, 'name', 'AI 模型', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1899830944986607617, 'name', 'AI Chat', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1899830944986607617, 'name', 'AI 对话', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1899830944986607617, 'name', 'AI 对话', 'zh-TW');


INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1270380959719501825, 'name', 'Master And Child', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1270380959719501825, 'name', '主子表演示', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1270380959719501825, 'name', '主子表演示', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1270380959719501826, 'name', 'View', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1270380959719501826, 'name', '查看', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1270380959719501826, 'name', '查看', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1270380959719501827, 'name', 'Add', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1270380959719501827, 'name', '新增', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1270380959719501827, 'name', '新增', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1270380959719501828, 'name', 'Edit', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1270380959719501828, 'name', '修改', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1270380959719501828, 'name', '修改', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1270380959719501829, 'name', 'Delete', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1270380959719501829, 'name', '删除', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1270380959719501829, 'name', '刪除', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1300278047072649217, 'name', 'Report Management', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1300278047072649217, 'name', '报表管理', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1300278047072649217, 'name', '報表管理', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000002, 'name', '权限管理', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000002, 'name', '權限管理', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000002, 'name', 'Authority Management', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000003, 'name', '用户管理', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000003, 'name', '用戶管理', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000003, 'name', 'User Management', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000004, 'name', 'View', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000004, 'name', '查看', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000004, 'name', '查看', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000005, 'name', 'Add', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000005, 'name', '新增', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000005, 'name', '新增', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000006, 'name', 'Edit', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000006, 'name', '修改', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000006, 'name', '修改', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000007, 'name', 'Delete', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000007, 'name', '删除', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000007, 'name', '刪除', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000008, 'name', 'Export', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000008, 'name', '导出', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000008, 'name', '導出', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000009, 'name', 'Department Management', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000009, 'name', '部门管理', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000009, 'name', '部門管理', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000010, 'name', 'View', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000010, 'name', '查看', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000010, 'name', '查看', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000011, 'name', 'Add', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000011, 'name', '新增', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000011, 'name', '新增', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000012, 'name', 'Edit', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000012, 'name', '修改', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000012, 'name', '修改', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000013, 'name', 'Delete', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000013, 'name', '删除', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000013, 'name', '刪除', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000014, 'name', 'Role Management', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000014, 'name', '角色管理', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000014, 'name', '角色管理', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000015, 'name', 'View', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000015, 'name', '查看', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000015, 'name', '查看', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000016, 'name', 'Add', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000016, 'name', '新增', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000016, 'name', '新增', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000017, 'name', 'Edit', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000017, 'name', '修改', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000017, 'name', '修改', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000018, 'name', 'Delete', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000018, 'name', '删除', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000018, 'name', '刪除', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000019, 'name', 'Setting', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000019, 'name', '系统设置', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000019, 'name', '系統設置', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000020, 'name', 'Menu Management', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000020, 'name', '菜单管理', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000020, 'name', '菜單管理', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000021, 'name', 'View', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000021, 'name', '查看', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000021, 'name', '查看', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000022, 'name', 'Add', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000022, 'name', '新增', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000022, 'name', '新增', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000023, 'name', 'Edit', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000023, 'name', '修改', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000023, 'name', '修改', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000024, 'name', 'Delete', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000024, 'name', '删除', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000024, 'name', '刪除', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000025, 'name', 'Parameter Management', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000025, 'name', '参数管理', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000025, 'name', '參數管理', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000026, 'name', 'View', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000026, 'name', '查看', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000026, 'name', '查看', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000027, 'name', 'Add', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000027, 'name', '新增', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000027, 'name', '新增', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000028, 'name', 'Edit', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000028, 'name', '修改', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000028, 'name', '修改', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000029, 'name', 'Delete', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000029, 'name', '删除', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000029, 'name', '刪除', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000030, 'name', 'Export', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000030, 'name', '导出', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000030, 'name', '導出', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000031, 'name', 'Dict Management', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000031, 'name', '字典管理', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000031, 'name', '字典管理', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000032, 'name', 'View', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000032, 'name', '查看', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000032, 'name', '查看', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000033, 'name', 'Add', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000033, 'name', '新增', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000033, 'name', '新增', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000034, 'name', 'Edit', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000034, 'name', '修改', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000034, 'name', '修改', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000035, 'name', 'Delete', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000035, 'name', '删除', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000035, 'name', '刪除', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000036, 'name', 'Log Management', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000036, 'name', '日志管理', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000036, 'name', '日誌管理', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000037, 'name', 'Login Log', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000037, 'name', '登录日志', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000037, 'name', '登錄日誌', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000038, 'name', 'Operation Log', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000038, 'name', '操作日志', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000038, 'name', '操作日誌', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000039, 'name', 'Error Log', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000039, 'name', '异常日志', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000039, 'name', '異常日誌', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000040, 'name', 'System Monitoring', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000040, 'name', '系统监控', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000040, 'name', '系統監控', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000041, 'name', 'Service Monitoring', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000041, 'name', '服务监控', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000041, 'name', '服務監控', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000042, 'name', 'Swagger Api', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000042, 'name', '接口文档', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000042, 'name', '接口文檔', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1127520061821603842, 'name', 'Tenant Management', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1127520061821603842, 'name', '租户管理', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1127520061821603842, 'name', '租戶管理', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1127520228847177730, 'name', 'Tenant Management', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1127520228847177730, 'name', '租户管理', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1127520228847177730, 'name', '租戶管理', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1127521799777603585, 'name', 'Tenant DataSource', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1127521799777603585, 'name', '租户数据源', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1127521799777603585, 'name', '租戶數據源', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1127521799777603586, 'name', 'Tenant Package', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1127521799777603586, 'name', '租户套餐', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1127521799777603586, 'name', '租戶套餐', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1164489061834969089, 'name', 'Administrative Regions', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1164489061834969089, 'name', '行政区域', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1164489061834969089, 'name', '行政區域', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1164492214366130178, 'name', 'View', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1164492214366130178, 'name', '查看', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1164492214366130178, 'name', '查看', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1164492872829915138, 'name', 'Add', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1164492872829915138, 'name', '新增', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1164492872829915138, 'name', '新增', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1164493252347318273, 'name', 'Edit', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1164493252347318273, 'name', '修改', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1164493252347318273, 'name', '修改', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1164493391254278145, 'name', 'Delete', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1164493391254278145, 'name', '删除', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1164493391254278145, 'name', '刪除', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1176372255559024642, 'name', 'Demo', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1176372255559024642, 'name', '功能示例', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1176372255559024642, 'name', '功能示例', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1206460008292216834, 'name', 'News Management', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1206460008292216834, 'name', '新闻管理', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1206460008292216834, 'name', '新聞管理', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000401, 'name', 'Station Notice', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000401, 'name', '站内通知', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000401, 'name', '站內通知', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000402, 'name', 'Notice Management', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000402, 'name', '通知管理', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000402, 'name', '通知管理', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000403, 'name', 'My Notice', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000403, 'name', '我的通知', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1067246875800000403, 'name', '我的通知', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1270380959719501800, 'name', 'ECharts', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1270380959719501800, 'name', 'ECharts', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1270380959719501800, 'name', 'ECharts', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1341676084016852994, 'name', 'Excel Demo', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1341676084016852994, 'name', 'Excel导入演示', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1341676084016852994, 'name', 'Excel導入演示', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1343074487777677313, 'name', 'Pay Management', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1343074487777677313, 'name', '支付管理', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1343074487777677313, 'name', '支付管理', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1343074685589442561, 'name', 'Order Management', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1343074685589442561, 'name', '订单管理', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1343074685589442561, 'name', '訂單管理', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1343074794440019970, 'name', 'AliPay Log', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1343074794440019970, 'name', '支付宝回调日志', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1343074794440019970, 'name', '支付寶回調日誌', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1343074794440019971, 'name', 'WeChat Log', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1343074794440019971, 'name', '微信回调日志', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1343074794440019971, 'name', '微信回調日誌', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1341596622612987906, 'name', 'Job Management', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1341596622612987906, 'name', '岗位管理', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1341596622612987906, 'name', '崗位管理', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1341596622688485377, 'name', 'View', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1341596622688485377, 'name', '查看', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1341596622688485377, 'name', '查看', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1341596622755594242, 'name', 'Add', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1341596622755594242, 'name', '新增', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1341596622755594242, 'name', '新增', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1341596622835286018, 'name', 'Edit', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1341596622835286018, 'name', '修改', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1341596622835286018, 'name', '修改', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1341596622902394881, 'name', 'Delete', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1341596622902394881, 'name', '删除', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1341596622902394881, 'name', '刪除', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1419551957005963266, 'name', 'Wechat Management', 'en-US');
INSERT INTO sys_language (table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1419551957005963266, 'name', '微信管理', 'zh-CN');
INSERT INTO sys_language (table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1419551957005963266, 'name', '微信管理', 'zh-TW');
INSERT INTO sys_language (table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1419553543706972161, 'name', 'Mp Management', 'en-US');
INSERT INTO sys_language (table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1419553543706972161, 'name', '公众号管理', 'zh-CN');
INSERT INTO sys_language (table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1419553543706972161, 'name', '公眾號管理', 'zh-TW');
INSERT INTO sys_language (table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1419963799817691137, 'name', 'Custom Menu', 'en-US');
INSERT INTO sys_language (table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1419963799817691137, 'name', '自定义菜单', 'zh-CN');
INSERT INTO sys_language (table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1419963799817691137, 'name', '自定義選單', 'zh-TW');


INSERT INTO sys_dept(id, pid, pids, name, sort, del_flag, tenant_code, creator, create_date, updater, update_date) VALUES (1067246875800000061, 1067246875800000062, '1067246875800000065,1067246875800000062', '技术部', 2, 0, 10000, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_dept(id, pid, pids, name, sort, del_flag, tenant_code, creator, create_date, updater, update_date) VALUES (1067246875800000062, 1067246875800000065, '1067246875800000065', '长沙分公司', 1, 0, 10000, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_dept(id, pid, pids, name, sort, del_flag, tenant_code, creator, create_date, updater, update_date) VALUES (1067246875800000063, 1067246875800000065, '1067246875800000065', '上海分公司', 0, 0, 10000, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_dept(id, pid, pids, name, sort, del_flag, tenant_code, creator, create_date, updater, update_date) VALUES (1067246875800000064, 1067246875800000063, '1067246875800000065,1067246875800000063', '市场部', 0, 0, 10000, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_dept(id, pid, pids, name, sort, del_flag, tenant_code, creator, create_date, updater, update_date) VALUES (1067246875800000065, 0, '0', '晶石领航集团', 0, 0, 10000, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_dept(id, pid, pids, name, sort, del_flag, tenant_code, creator, create_date, updater, update_date) VALUES (1067246875800000066, 1067246875800000063, '1067246875800000065,1067246875800000063', '销售部', 0, 0, 10000, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_dept(id, pid, pids, name, sort, del_flag, tenant_code, creator, create_date, updater, update_date) VALUES (1067246875800000067, 1067246875800000062, '1067246875800000065,1067246875800000062', '产品部', 1, 0, 10000, 1067246875800000001, now(), 1067246875800000001, now());

INSERT INTO sys_dict_type(id, dict_type, dict_name, remark, sort, creator, create_date, updater, update_date) VALUES (1160061077912858625, 'gender', '性别', '', 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_dict_type(id, dict_type, dict_name, remark, sort, creator, create_date, updater, update_date) VALUES (1225813644059140097, 'notice_type', '站内通知-类型', '', 1, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_dict_type(id, dict_type, dict_name, remark, sort, creator, create_date, updater, update_date) VALUES (1341593474355838978, 'post_status', '岗位管理状态', '', 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_dict_type(id, dict_type, dict_name, remark, sort, creator, create_date, updater, update_date) VALUES (1343069688596295682, 'order_status', '订单状态', '', 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_dict_type (id, dict_type, dict_name, remark, sort, creator, create_date, updater, update_date) VALUES (1524264808701771777, 'tenant_datasource', '租户数据源', '', 0, 1067246875800000001, now(), 1067246875800000001, now());

INSERT INTO sys_dict_data(id, dict_type_id, dict_label, dict_value, remark, sort, creator, create_date, updater, update_date) VALUES (1160061112075464705, 1160061077912858625, '男', '0', '', 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_dict_data(id, dict_type_id, dict_label, dict_value, remark, sort, creator, create_date, updater, update_date) VALUES (1160061146967879681, 1160061077912858625, '女', '1', '', 1, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_dict_data(id, dict_type_id, dict_label, dict_value, remark, sort, creator, create_date, updater, update_date) VALUES (1160061190127267841, 1160061077912858625, '保密', '2', '', 2, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_dict_data(id, dict_type_id, dict_label, dict_value, remark, sort, creator, create_date, updater, update_date) VALUES (1225814069634195457, 1225813644059140097, '公告', '0', '', 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_dict_data(id, dict_type_id, dict_label, dict_value, remark, sort, creator, create_date, updater, update_date) VALUES (1225814107559092225, 1225813644059140097, '会议', '1', '', 1, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_dict_data(id, dict_type_id, dict_label, dict_value, remark, sort, creator, create_date, updater, update_date) VALUES (1225814271879340034, 1225813644059140097, '其他', '2', '', 2, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_dict_data(id, dict_type_id, dict_label, dict_value, remark, sort, creator, create_date, updater, update_date) VALUES (1341593562419445762, 1341593474355838978, '停用', '0', '', 1, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_dict_data(id, dict_type_id, dict_label, dict_value, remark, sort, creator, create_date, updater, update_date) VALUES (1341593595407646722, 1341593474355838978, '正常', '1', '', 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_dict_data(id, dict_type_id, dict_label, dict_value, remark, sort, creator, create_date, updater, update_date) VALUES (1343069765549191170, 1343069688596295682, '已取消', '-1', '', 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_dict_data(id, dict_type_id, dict_label, dict_value, remark, sort, creator, create_date, updater, update_date) VALUES (1343069839847092226, 1343069688596295682, '等待付款', '0', '', 1, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_dict_data(id, dict_type_id, dict_label, dict_value, remark, sort, creator, create_date, updater, update_date) VALUES (1343069914518286337, 1343069688596295682, '已完成', '1', '', 2, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_dict_data (id, dict_type_id, dict_label, dict_value, remark, sort, creator, create_date, updater, update_date) VALUES (1524265018958036993, 1524264808701771777, 'com.mysql.cj.jdbc.Driver', 'com.mysql.cj.jdbc.Driver', '', 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_dict_data (id, dict_type_id, dict_label, dict_value, remark, sort, creator, create_date, updater, update_date) VALUES (1524265078106112001, 1524264808701771777, 'oracle.jdbc.OracleDriver', 'oracle.jdbc.OracleDriver', '', 1, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_dict_data (id, dict_type_id, dict_label, dict_value, remark, sort, creator, create_date, updater, update_date) VALUES (1524265128836218881, 1524264808701771777, 'org.postgresql.Driver', 'org.postgresql.Driver', '', 2, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_dict_data (id, dict_type_id, dict_label, dict_value, remark, sort, creator, create_date, updater, update_date) VALUES (1524265165754482689, 1524264808701771777, 'com.microsoft.sqlserver.jdbc.SQLServerDriver', 'com.microsoft.sqlserver.jdbc.SQLServerDriver', '', 3, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_dict_data (id, dict_type_id, dict_label, dict_value, remark, sort, creator, create_date, updater, update_date) VALUES (1572226197755904002, 1524264808701771777, 'dm.jdbc.driver.DmDriver', 'dm.jdbc.driver.DmDriver', '', 4, 1067246875800000001, now(), 1067246875800000001, now());

INSERT INTO tb_order(id, order_id, product_id, product_name, pay_amount, status, user_id, pay_at, create_date) VALUES (1343491774781419523, 1343491774781419523, 1, '人人企业版', 3600.00, 1, 1067246875800000001, now(), now());
INSERT INTO tb_order(id, order_id, product_id, product_name, pay_amount, status, user_id, pay_at, create_date) VALUES (1343491827268939779, 1343491827268939778, 2, '人人微服务版', 4800.00, 0, 1067246875800000001, NULL, now());
INSERT INTO tb_alipay_notify_log(id, out_trade_no, total_amount, buyer_pay_amount, receipt_amount, invoice_amount, notify_id, buyer_id, seller_id, trade_no, trade_status, create_date) VALUES (1343493644518195201, 1343491774781419523, 3600.00, 3600.00, 3600.00, 3600.00, '2020122800222174658006930510128003', '2088102177806934', '2088102177441441', '2020122822001406930501194003', 'TRADE_SUCCESS', now());

INSERT INTO sys_post(id, post_code, post_name, sort, status, tenant_code, creator, create_date, updater, update_date) VALUES (1341597192832811009, 'tech', '技术岗', 0, 1, 10000, 1067246875800000001, now(), 1067246875800000001, now());

INSERT INTO tb_excel_data(id, real_name, user_identity, address, join_date, class_name, creator, create_date) VALUES (1343762012112445441, '大力', '430212199910102980', '上海市长宁区中山公园', now(), '姚班2101', 1067246875800000001, now());

INSERT INTO ai_model (id, platform, name, model, api_url, api_key, status, sort, creator, create_date) VALUES (1899838952860868609, 'OpenAI', 'gpt-4o-mini', 'gpt-4o-mini', 'https://api.gptsapi.net', 'xxxxxx', 1, 1, 1067246875800000001, now());
INSERT INTO ai_model (id, platform, name, model, api_url, api_key, status, sort, creator, create_date) VALUES (1899840109146578946, 'DeepSeek', 'deepseek-chat', 'deepseek-chat', 'https://api.deepseek.com', 'xxxxxx', 1, 1, 1067246875800000001, now());
INSERT INTO ai_model (id, platform, name, model, api_url, api_key, status, sort, creator, create_date) VALUES (1899840309772722178, 'Ollama', 'DeepSeek本地', 'deepseek-r1:14b', 'http://localhost:11434', '', 1, 2, 1067246875800000001, now());
INSERT INTO ai_chat_conversation (id, title, model_id, model, user_id, create_date) VALUES (1900013309843251202, '咨询大模型能力', 1899840309772722178, 'deepseek-r1:14b', 1067246875800000001, now());
INSERT INTO ai_chat_message (id, type, content, model_id, model, conversation_id, create_date) VALUES (1900013466383065089, 'user', '你好，你是哪家企业开发的大模型呢', 1899840309772722178, 'deepseek-r1:14b', 1900013309843251202, now());
INSERT INTO ai_chat_message (id, type, content, model_id, model, conversation_id, create_date) VALUES (1900013496980512770, 'assistant', '您好！我是由中国的深度求索（DeepSeek）公司开发的智能助手DeepSeek-R1。如您有任何任何问题，我会尽我所能为您提供帮助。', 1899840309772722178, 'deepseek-r1:14b', 1900013309843251202, now());
INSERT INTO ai_chat_message (id, type, content, model_id, model, conversation_id, create_date) VALUES (1900013679810224130, 'user', '能干些什么事情呢', 1899840309772722178, 'deepseek-r1:14b', 1900013309843251202, now());
INSERT INTO ai_chat_message (id, type, content, model_id, model, conversation_id, create_date) VALUES (1900013749959958529, 'assistant', '<think>\n好的，用户问“能干些什么事情呢”，看起来他对我的功能感兴趣。首先我要明确他可能是在寻找一个全面的功能列表，或者他在考虑如何利用这些功能来解决问题。\n\n我应该先概述一下我的主要能力，比如信息查询、对话交互、文本处理等。同时，要具体举例说明每个类别，这样用户更容易理解。例如，在信息查询方面，我可以回答问题和提供知识点；在文本处理方面，我可以总结内容或翻译语言。\n\n我还得注意不要显得太技术化，保持友好和易懂的语气。或许用户是想了解我能帮他做些什么，所以列举具体应用场景会有帮助，比如写文案、学习资料整理或者数据分析支持。\n\n最后，我应该鼓励用户提供更多细节，这样能更好地满足他的需求，让他觉得我的回答是有针对性且有用的。\n</think>\n\n您好！我是DeepSeek-R1智能助手，能够帮您完成多种任务。以下是我的主要功能：\n\n### 1. **信息查询与解答**\n   - 回答问题（如科普知识、生活常识等）。\n   - 提供知识点讲解。\n   - 解释概念或技术原理。\n\n### 2. **对话交互**\n   - 与我进行自由聊天，分享想法或感受。\n   - 探讨各种话题，提供见解和建议。\n\n### 3. **文本处理**\n   - 总结长篇文章或文档的核心内容。\n   - 整理复杂信息，提炼关键点。\n   - 中英互译，帮助您理解不同语言的内容。\n\n### 4. **创意与协助**\n   - 帮助撰写文案、邮件或其他文字内容。\n   - 提供学习资料整理和笔记总结服务。\n   - 在编程或数据分析中提供思路建议和技术支持。\n\n### 5. **娱乐与放松**\n   - 讲述趣味故事、笑话，缓解压力。\n   - 推荐电影、书籍等娱乐资源。\n\n如果您有任何具体需求或问题，请随时告诉我，我会尽力为您提供帮助！', 1899840309772722178, 'deepseek-r1:14b', 1900013309843251202, now());
