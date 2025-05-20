---@diagnostic disable: undefined-global
local follower = KEYS[1]  -- 关注者
local followee = KEYS[2]  -- 被关注者

-- 计算 Cuckoo Filter 的键名
local filter_index = math.floor(follower / 2^20)  -- 除以 2^20 计算索引
local cuckoo_filter_key = "cuckoo_filter_" .. filter_index  -- 拼接生成 Cuckoo Filter 的键名
local hash_table_key = "hash_table_" .. filter_index  -- 生成哈希表的键名

-- 拼接关注者和被关注者作为查找键
local lookup_key = follower .. "_" .. followee

-- 检查 Cuckoo Filter 是否存在
local filter_exists = redis.call("EXISTS", cuckoo_filter_key)

-- 如果 Cuckoo Filter 不存在，创建一个新的 Cuckoo Filter
if filter_exists == 0 then
    redis.call("CF.RESERVE", cuckoo_filter_key, 1000, 0.95)  -- 可以根据需要调整容量和错误率
end

-- 检查哈希表是否存在
local hash_exists = redis.call("EXISTS", hash_table_key)

-- 如果哈希表不存在，创建一个新的哈希表
if hash_exists == 0 then
    redis.call("HSET", hash_table_key, "initial", "0")  -- 可根据需求初始化哈希表
end

-- 尝试将拼接后的键添加到 Cuckoo Filter
local add_result = redis.call("CF.ADD", cuckoo_filter_key, lookup_key)

-- 如果添加成功，则将值存入哈希表
if add_result == 1 then
    redis.call("HSET", hash_table_key, lookup_key, "1")  -- 存储一个示例值，可以根据需求修改
    return "Element added successfully"
else
    return "Element already exists in Cuckoo Filter"
end
