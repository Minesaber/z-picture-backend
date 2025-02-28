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

# 添加新列
ALTER TABLE picture
    ADD COLUMN reviewStatus  INT DEFAULT 0 NOT NULL COMMENT '审核状态：0-待审核; 1-通过; 2-拒绝',
    ADD COLUMN reviewMessage varchar(512)  NULL COMMENT '审核信息',
    ADD COLUMN reviewerId    BIGINT        NULL COMMENT '审核人 ID',
    ADD COLUMN reviewTime    DATETIME      NULL COMMENT '审核时间';

# 索引
CREATE INDEX idx_reviewStatus ON picture (reviewStatus);

# 添加新列
ALTER TABLE picture
    ADD COLUMN thumbnailUrl varchar(512) NULL COMMENT '缩略图 url';

-- 空间表
create table if not exists space
(
    # 通用字段
    id         bigint auto_increment comment 'id' primary key,
    createTime datetime  default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime   timestamp default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDeleted  tinyint   default 0                 not null comment '是否已删除',

    # 空间字段
    spaceName  varchar(128)                        null comment '空间名称',
    userId     bigint                              not null comment '创建用户 id',
    spaceLevel int       default 0                 null comment '空间级别：0-普通版 1-专业版 2-旗舰版',
    maxSize    bigint    default 0                 null comment '空间图片的最大总大小',
    maxCount   bigint    default 0                 null comment '空间图片的最大数量',
    totalSize  bigint    default 0                 null comment '当前空间下图片的总大小',
    totalCount bigint    default 0                 null comment '当前空间下的图片数量',

    # 索引
    index idx_userId (userId),
    index idx_spaceName (spaceName),
    index idx_spaceLevel (spaceLevel)
) comment '空间' collate = utf8mb4_unicode_ci;

# 添加新列
ALTER TABLE picture
    ADD COLUMN spaceId bigint null comment '空间 id（为空表示公共空间）';

# 索引
CREATE INDEX idx_spaceId ON picture (spaceId);

-- 添加新列
ALTER TABLE picture
    ADD COLUMN picColor varchar(16) null comment '图片主色调';

-- 微信用户表
CREATE TABLE IF NOT EXISTS user_wx
(
    # 通用字段
    id                 bigint auto_increment comment 'id' primary key,
    createTime         datetime  default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime           timestamp default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime         timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDeleted          tinyint   default 0                 not null comment '是否已删除',

    # 微信用户字段
    openId             varchar(256)                        not null comment '微信用户在当前公众号的唯一标识',
    isFollow           tinyint   DEFAULT 0                 not null comment '是否仍在关注：0-未关注 1-关注',
    isBound            tinyint   DEFAULT 0                 not null comment '是否绑定了账号：0-未绑定 1-绑定',
    userId             bigint    default null comment '绑定的用户id',
    firstSubscribeTime datetime  default null comment '首次关注时间',
    latestSubscribeTime  datetime  default null comment '最新关注时间',

    unique key uk_openId (openId),
    index idx_userId (userId)
) comment '微信用户' collate = utf8mb4_unicode_ci;