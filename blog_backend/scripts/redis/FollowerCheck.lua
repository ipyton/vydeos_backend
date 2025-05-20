---@diagnostic disable: undefined-global
local follower = KEYS[1]  -- 关注者
local followee = KEYS[2]  -- 被关注者

-- 计算 Cuckoo Filter 的键名
local filter_index = math.floor(follower / 2^20)  -- 除以 2^20 计算索引
local cuckoo_filter_key = "cuckoo_filter_" .. filter_index  -- 拼接生成 Cuckoo Filter 的键名

-- 拼接关注者和被关注者作为查找键
local lookup_key = follower .. "_" .. followee

-- 检查 Cuckoo Filter 中是否存在拼接后的键
local exists = redis.call("CF.EXISTS", cuckoo_filter_key, lookup_key)

if exists == 1 then
    -- 如果存在，使用拼接后的键从 Redis 中获取对应的值
    local value = redis.call("hget", filter_index, follower)
    return value
else
    -- 如果不存在，返回 nil
    return nil
end