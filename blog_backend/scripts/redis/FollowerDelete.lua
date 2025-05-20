---@diagnostic disable: undefined-global
local follower = KEYS[1]  -- 关注者
local followee = KEYS[2]  -- 被关注者

-- 计算 Cuckoo Filter 和哈希表的键名
local filter_index = math.floor(follower / 2^20)  -- 除以 2^20 计算索引
local cuckoo_filter_key = "cuckoo_filter_" .. filter_index  -- 生成 Cuckoo Filter 的键名
local hash_table_key = "hash_table_" .. filter_index  -- 生成哈希表的键名

-- 拼接关注者和被关注者作为查找键
local lookup_key = follower .. "_" .. followee

-- 检查 Cuckoo Filter 中是否存在拼接后的键
local exists = redis.call("CF.EXISTS", cuckoo_filter_key, lookup_key)

if exists == 1 then
    -- 如果存在，从 Cuckoo Filter 中删除
    redis.call("CF.REMOVE", cuckoo_filter_key, lookup_key)

    -- 从哈希表中删除对应的值
    redis.call("HDEL", hash_table_key, lookup_key)

    return "Element removed successfully"
else
    return "Element does not exist in Cuckoo Filter"
end
