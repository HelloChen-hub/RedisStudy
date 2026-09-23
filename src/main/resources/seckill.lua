-- 获取库存
local stock = redis.call('GET', KEYS[1])
-- 秒杀：原子判断库存并扣减（KEYS[1] = 优惠券库存 key）
if stock == false or tonumber(stock) <= 0 then
    return 0
end

-- 判断用户是否已购
if redis.call('SISMEMBER', KEYS[2], ARGV[1]) == 1 then
    return -1
end

-- 原子性扣减库存 + 记录用户
redis.call('DECR', KEYS[1])
redis.call('SADD', KEYS[2], ARGV[1])

return 1
