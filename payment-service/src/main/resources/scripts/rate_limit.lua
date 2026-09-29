local capacity = tonumber(ARGV[1])
local refillRate = tonumber(ARGV[2])
local now = tonumber(ARGV[3])

local tokens = tonumber(redis.call('HGET', KEYS[1], 'tokens'))
local lastRefillTime = tonumber(redis.call('HGET', KEYS[1], 'lastRefillTime'))

-- First request for this merchant
if tokens == nil then
    tokens = capacity
    lastRefillTime = now
end

-- Calculate tokens generated since the last calculation
local elapsedMillis = now - lastRefillTime
local elapsedSeconds = elapsedMillis / 1000

local newTokens = elapsedSeconds * refillRate

tokens = math.min(capacity, tokens + newTokens)

local allowed = 0

if tokens >= 1 then
    tokens = tokens - 1
    allowed = 1
end

-- Save the updated bucket state
redis.call(
    'HSET',
    KEYS[1],
    'tokens', tokens,
    'lastRefillTime', now
)

return allowed