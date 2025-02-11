-- 创建并使用数据库
create database if not exists z_picture;
use z_picture;

-- 用户表
     create table if not exists user
(
    # 通用字段
    id           bigint auto_increment primary key comment 'id',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime     timestamp    default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime   timestamp    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDeleted    tinyint      default 0                 not null comment '是否已删除',

    # 用户字段
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',

    # 索引
    unique key uk_userAccount (userAccount),
    index idx_userName (userName)
) comment '用户' collate = utf8mb4_unicode_ci;

-- 图片表
create table if not exists picture
(
    # 通用字段
    id         bigint auto_increment primary key comment 'id',
    createTime datetime  default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime   timestamp default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDeleted  tinyint   default 0                 not null comment '是否已删除',

    # 图片字段
    url        varchar(512)                        not null comment 'url',
    name       varchar(128)                        not null comment '名称',
    userId     bigint                              not null comment '创建用户id',
    profile    varchar(512)                        null comment '简介',
    category   varchar(64)                         null comment '分类',
    # todo 可以考虑使用数据库提供的JSON类型
    tags       varchar(512)                        null comment '标签（JSON数组）',
    picSize    bigint                              null comment '大小',
    picFormat  varchar(32)                         null comment '格式',
    picWidth   int                                 null comment '宽度',
    picHeight  int                                 null comment '高度',
    picScale   double                              null comment '宽高比',

    # 索引
    index idx_name (name),
    index idx_userId (userId),
    index idx_profile (profile),
    index idx_category (category),
    index idx_tags (tags)
) comment '图片' collate = utf8mb4_unicode_ci;