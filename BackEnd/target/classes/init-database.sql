-- 数据库初始化脚本
-- 请在MySQL中执行此脚本

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS fuzz_testing_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE fuzz_testing_db;

-- 创建数据库配置表
CREATE TABLE IF NOT EXISTS database_config (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    db_type VARCHAR(50) NOT NULL DEFAULT 'MySQL',
    connection_url VARCHAR(200) NOT NULL,
    username VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL,
    status VARCHAR(50) DEFAULT 'UNTESTED',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_name (name)
);

-- 创建参数模板表
CREATE TABLE IF NOT EXISTS parameter_template (
    id INT AUTO_INCREMENT PRIMARY KEY,
    param_name VARCHAR(100) NOT NULL,
    description VARCHAR(200),
    category VARCHAR(50) NOT NULL,
    default_value VARCHAR(100),
    param_type VARCHAR(20) NOT NULL DEFAULT 'STRING',
    is_test_default TINYINT(1) DEFAULT 1,
    min_value VARCHAR(50),
    max_value VARCHAR(50),
    allowed_values VARCHAR(1000),
    value_range VARCHAR(200),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_param (param_name)
);

-- 插入测试数据库配置
INSERT IGNORE INTO database_config (name, db_type, connection_url, username, password, status) 
VALUES 
('Local MySQL Test', 'MySQL', 'jdbc:mysql://localhost:3306/fuzz_testing_db', 'root', '123456', 'UNTESTED'),
('Sample PostgreSQL', 'PostgreSQL', 'jdbc:postgresql://localhost:5432/testdb', 'postgres', '123456', 'UNTESTED');

-- 插入真实的MySQL参数数据（基于param.txt）
INSERT IGNORE INTO parameter_template (param_name, description, category, default_value, param_type, is_test_default, min_value, max_value, allowed_values) VALUES
-- InnoDB相关参数
('bulk_insert_buffer_size', 'MyISAM表批量插入缓冲区大小', 'MEMORY', '8388608', 'INTEGER', true, '0', '20971520', '["0", "20971520"]'),
('innodb_adaptive_hash_index', 'InnoDB自适应哈希索引', 'INNODB', 'ON', 'BOOLEAN', true, null, null, '["OFF", "ON"]'),
('innodb_change_buffering', 'InnoDB变更缓冲', 'INNODB', 'all', 'STRING', true, null, null, '["none", "all"]'),
('innodb_cmp_per_index_enabled', 'InnoDB每索引压缩统计', 'INNODB', 'OFF', 'BOOLEAN', true, null, null, '["ON", "OFF"]'),
('innodb_ddl_threads', 'InnoDB DDL线程数', 'INNODB', '4', 'INTEGER', true, '1', '4', '["1", "2", "3", "4"]'),
('innodb_file_per_table', 'InnoDB每表一个文件', 'INNODB', 'ON', 'BOOLEAN', true, null, null, '["OFF", "ON"]'),
('innodb_flush_neighbors', 'InnoDB刷新邻居页面', 'INNODB', '1', 'STRING', true, null, null, '["1", "2"]'),
('innodb_max_dirty_pages_pct', 'InnoDB最大脏页百分比', 'INNODB', '90', 'DECIMAL', true, '0', '99.99', '["0", "10", "50", "90"]'),
('innodb_max_dirty_pages_pct_lwm', 'InnoDB脏页低水位标记', 'INNODB', '10', 'DECIMAL', true, '0', '99.99', '["0", "5", "10"]'),
('innodb_random_read_ahead', 'InnoDB随机预读', 'INNODB', 'OFF', 'BOOLEAN', true, null, null, '["ON", "OFF"]'),
('innodb_read_ahead_threshold', 'InnoDB预读阈值', 'INNODB', '56', 'INTEGER', true, '0', '64', '["0", "8", "16", "32", "56"]'),
('innodb_stats_auto_recalc', 'InnoDB统计自动重算', 'INNODB', 'ON', 'BOOLEAN', true, null, null, '["OFF", "ON"]'),
('innodb_stats_method', 'InnoDB统计方法', 'INNODB', 'nulls_equal', 'STRING', true, null, null, '["nulls_unequal", "nulls_ignored", "nulls_equal"]'),
('innodb_stats_persistent_sample_pages', 'InnoDB持久化统计采样页数', 'INNODB', '20', 'INTEGER', true, '1', '1000', '["1", "10", "20"]'),
('innodb_use_fdatasync', 'InnoDB使用fdatasync', 'INNODB', 'OFF', 'BOOLEAN', true, null, null, '["ON", "OFF"]'),

-- 连接和并发参数
('concurrent_insert', '并发插入模式', 'CONNECTION', '1', 'STRING', true, null, null, '["0", "1", "2"]'),
('foreign_key_checks', '外键检查', 'CONNECTION', '1', 'BOOLEAN', true, null, null, '["0", "1"]'),

-- 存储引擎参数
('default_storage_engine', '默认存储引擎', 'ENGINE', 'InnoDB', 'STRING', true, null, null, '["MyISAM", "InnoDB"]'),
('delay_key_write', '延迟键写入', 'ENGINE', 'ON', 'STRING', true, null, null, '["OFF", "ALL"]'),

-- 查询优化参数
('eq_range_index_dive_limit', '等值范围索引深入限制', 'OPTIMIZER', '200', 'INTEGER', true, '0', '4294967295', '["0", "2", "10", "200"]'),
('group_concat_max_len', 'GROUP_CONCAT最大长度', 'OPTIMIZER', '1024', 'INTEGER', true, '4', '18446744073709551615', '["4", "64", "1024"]'),
('optimizer_prune_level', '优化器修剪级别', 'OPTIMIZER', '1', 'INTEGER', true, null, null, '["0", "1"]'),
('optimizer_search_depth', '优化器搜索深度', 'OPTIMIZER', '62', 'INTEGER', true, '0', '62', '["0", "4", "32", "62"]'),
('optimizer_switch', '优化器开关', 'OPTIMIZER', 'index_merge=on,index_merge_union=on', 'STRING', false, null, null, null),

-- 缓冲区参数
('join_buffer_size', '连接缓冲区大小', 'MEMORY', '262144', 'INTEGER', true, '128', '4294967295', '["128", "262144", "1048576"]'),
('key_buffer_size', '键缓冲区大小', 'MEMORY', '8388608', 'INTEGER', true, '4096', '4294967295', '["4096", "8388608", "16777216"]'),
('read_buffer_size', '读缓冲区大小', 'MEMORY', '131072', 'INTEGER', true, '8192', '2147483647', '["8192", "131072", "262144"]'),
('read_rnd_buffer_size', '随机读缓冲区大小', 'MEMORY', '262144', 'INTEGER', true, '1', '2147483647', '["1", "1024", "262144"]'),
('sort_buffer_size', '排序缓冲区大小', 'MEMORY', '262144', 'INTEGER', true, '32768', '4294967295', '["32768", "262144", "1048576"]'),
('preload_buffer_size', '预加载缓冲区大小', 'MEMORY', '32768', 'INTEGER', true, '1024', '1073741824', '["1024", "32768", "65536"]'),
('query_alloc_block_size', '查询分配块大小', 'MEMORY', '8192', 'INTEGER', true, '1024', '4294967295', '["1024", "8192", "16384"]'),

-- MyISAM参数
('myisam_data_pointer_size', 'MyISAM数据指针大小', 'MYISAM', '6', 'INTEGER', true, '2', '7', '["2", "3", "4", "5", "6", "7"]'),
('myisam_max_sort_file_size', 'MyISAM最大排序文件大小', 'MYISAM', '9223372036853727232', 'INTEGER', true, '0', '9223372036853727232', '["0", "128", "1024", "9223372036853727232"]'),
('myisam_sort_buffer_size', 'MyISAM排序缓冲区大小', 'MYISAM', '8388608', 'INTEGER', true, '4096', '4294967295', '["4096", "8388608", "16777216"]'),
('myisam_stats_method', 'MyISAM统计方法', 'MYISAM', 'nulls_unequal', 'STRING', true, null, null, '["nulls_equal", "nulls_ignored", "nulls_unequal"]'),
('myisam_use_mmap', 'MyISAM使用内存映射', 'MYISAM', 'OFF', 'BOOLEAN', true, null, null, '["ON", "OFF"]'),

-- 系统参数
('low_priority_updates', '低优先级更新', 'SYSTEM', 'OFF', 'BOOLEAN', true, null, null, '["ON", "OFF"]'),
('max_seeks_for_key', '键的最大查找次数', 'SYSTEM', '4294967295', 'INTEGER', true, '1', '4294967295', '["1", "4", "100", "4294967295"]'),
('max_sort_length', '最大排序长度', 'SYSTEM', '1024', 'INTEGER', true, '4', '8388608', '["4", "1024", "8192"]'),
('max_sp_recursion_depth', '存储过程最大递归深度', 'SYSTEM', '0', 'INTEGER', true, '0', '255', '["0", "5", "10", "255"]'),
('range_optimizer_max_mem_size', '范围优化器最大内存大小', 'SYSTEM', '8388608', 'INTEGER', true, '0', '18446744073709551615', '["0", "1048576", "8388608"]'),
('read_only', '只读模式', 'SYSTEM', 'OFF', 'BOOLEAN', true, null, null, '["ON", "OFF"]'),
('regexp_stack_limit', '正则表达式栈限制', 'SYSTEM', '8000000', 'INTEGER', true, '0', '2147483647', '["0", "1024", "8000000"]'),
('rewriter_enabled', '查询重写器启用', 'SYSTEM', 'OFF', 'BOOLEAN', true, null, null, '["ON", "OFF"]'),
('table_open_cache', '表打开缓存', 'SYSTEM', '4000', 'INTEGER', true, '1', '524288', '["1", "8", "100", "4000"]'),

-- SQL模式参数
('sql_auto_is_null', 'SQL自动IS NULL', 'SQL_MODE', 'OFF', 'BOOLEAN', true, null, null, '["ON", "OFF"]'),
('sql_big_selects', 'SQL大查询', 'SQL_MODE', 'ON', 'BOOLEAN', true, null, null, '["ON", "OFF"]'),
('sql_buffer_result', 'SQL缓冲结果', 'SQL_MODE', 'OFF', 'BOOLEAN', true, null, null, '["ON", "OFF"]'),
('sql_generate_invisible_primary_key', 'SQL生成不可见主键', 'SQL_MODE', 'OFF', 'BOOLEAN', true, null, null, '["ON", "OFF"]'),
('sql_mode', 'SQL模式', 'SQL_MODE', 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION', 'STRING', true, null, null, '["", "ONLY_FULL_GROUP_BY", "STRICT_TRANS_TABLES", "NO_ZERO_IN_DATE", "NO_ZERO_DATE", "ERROR_FOR_DIVISION_BY_ZERO", "NO_ENGINE_SUBSTITUTION"]');

-- 验证数据
SELECT 'Database Configs:' as info;
SELECT id, name, db_type, status FROM database_config;

SELECT 'Parameter Templates:' as info;
SELECT id, param_name, category, param_type, default_value, is_test_default FROM parameter_template LIMIT 10;

-- ========================================
-- 数据验证和完整性检查
-- ========================================

-- 验证数据库配置
SELECT '=== 数据库配置验证 ===' as section;
SELECT COUNT(*) as total_db_configs FROM database_config;
SELECT name, db_type, status FROM database_config;

-- 验证参数模板数据
SELECT '=== 参数模板验证 ===' as section;
SELECT COUNT(*) as total_parameters FROM parameter_template;

-- 验证参数类型分布
SELECT '=== 参数类型分布 ===' as section;
SELECT param_type, COUNT(*) as count FROM parameter_template GROUP BY param_type ORDER BY param_type;

-- 验证参数类别分布
SELECT '=== 参数类别分布 ===' as section;
SELECT category, COUNT(*) as count FROM parameter_template GROUP BY category ORDER BY category;

-- 验证设置范围分布 (暂时跳过，字段尚未添加)
SELECT '=== 设置范围分布 ===' as section;
SELECT 'value_range字段尚未添加，请先执行数据迁移' as message;

-- 验证约束条件统计
SELECT '=== 约束条件统计 ===' as section;
SELECT 
    COUNT(*) as total_parameters,
    SUM(CASE WHEN allowed_values IS NOT NULL AND allowed_values != '' THEN 1 ELSE 0 END) as with_candidate_values,
    SUM(CASE WHEN min_value IS NOT NULL OR max_value IS NOT NULL THEN 1 ELSE 0 END) as with_range_constraints,
    0 as with_value_range_placeholder,
    SUM(CASE WHEN is_test_default = 1 THEN 1 ELSE 0 END) as test_enabled
FROM parameter_template;

-- 验证必填字段完整性
SELECT '=== 数据完整性检查 ===' as section;
SELECT 'Parameters with missing param_name:' as check_type, COUNT(*) as count 
FROM parameter_template WHERE param_name IS NULL OR param_name = '';

SELECT 'Parameters with missing param_type:' as check_type, COUNT(*) as count 
FROM parameter_template WHERE param_type IS NULL;

SELECT 'Parameters with missing category:' as check_type, COUNT(*) as count 
FROM parameter_template WHERE category IS NULL OR category = '';

-- 验证JSON格式的候选取值
SELECT '=== JSON格式验证 ===' as section;
SELECT param_name, allowed_values 
FROM parameter_template 
WHERE allowed_values IS NOT NULL 
  AND allowed_values != ''
  AND allowed_values NOT REGEXP '^\\[.*\\]$'
LIMIT 5;

-- 显示一些示例参数
SELECT '=== 参数示例 ===' as section;
SELECT param_name, param_type, category, default_value,
       CASE WHEN allowed_values IS NOT NULL THEN 'Yes' ELSE 'No' END as has_candidates,
       CASE WHEN min_value IS NOT NULL OR max_value IS NOT NULL THEN 'Yes' ELSE 'No' END as has_range
FROM parameter_template 
WHERE param_name IN ('innodb_adaptive_hash_index', 'key_buffer_size', 'sql_mode', 'concurrent_insert')
ORDER BY param_name;

-- 最终验证消息
SELECT '=== 初始化完成 ===' as section;
SELECT 
    CASE 
        WHEN COUNT(*) >= 40 THEN 'SUCCESS: 数据库初始化成功完成'
        ELSE 'WARNING: 参数数量可能不足，请检查数据'
    END as initialization_status,
    COUNT(*) as total_parameters
FROM parameter_template;

-- 显示初始化时间
SELECT 'Database initialized at:' as info, NOW() as timestamp;