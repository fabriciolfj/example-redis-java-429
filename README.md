# example-redis-java-429

## simulacao do script lua
```
-- Parâmetros recebidos
local rate = 2.0         -- ARGV[1]: Taxa de recarga (tokens/segundo)
local capacity = 5       -- ARGV[2]: Capacidade máxima
local now = 1742630400   -- ARGV[3]: Timestamp atual (exemplo)
local requested = 1      -- ARGV[4]: Tokens solicitados por requisição

-- Cálculo de TTL
local fill_time = capacity / rate
-- fill_time = 5 / 2 = 2.5 segundos (tempo para encher o balde)

local ttl = math.floor(fill_time * 2)
-- ttl = math.floor(2.5 * 2) = math.floor(5) = 5 segundos

-- Verificação de tokens existentes (primeira vez, então não existem)
local last_tokens = tonumber(redis.call("get", tokens_key))
if last_tokens == nil then
    last_tokens = capacity
end
-- last_tokens = 5 (balde cheio na primeira vez)

-- Verificação de timestamp anterior (primeira vez, então não existe)
local last_refreshed = tonumber(redis.call("get", timestamp_key))
if last_refreshed == nil then
    last_refreshed = 0
end
-- last_refreshed = 0

-- Cálculo do tempo decorrido
local delta = math.max(0, now - last_refreshed)
-- delta = now - 0 = 1742630400 (muito grande na primeira vez)

-- Reabastecimento de tokens com base no tempo decorrido
local filled_tokens = math.min(capacity, last_tokens + (delta * rate))
-- filled_tokens = min(5, 5 + (1742630400 * 2)) = 5 (limitado pela capacidade)

-- Verificação se há tokens suficientes
local allowed = filled_tokens >= requested
-- allowed = 5 >= 1 = true

-- Atualização do número de tokens após a requisição
local new_tokens = filled_tokens
if allowed then
    new_tokens = filled_tokens - requested
end
-- new_tokens = 5 - 1 = 4

-- Armazenamento dos valores atualizados
redis.call("setex", tokens_key, ttl, new_tokens)
-- Armazena 4 tokens com TTL de 5 segundos

redis.call("setex", timestamp_key, ttl, now)
-- Armazena o timestamp atual com TTL de 5 segundos

-- Resultado
return allowed and 1 or 0
-- Retorna 1 (requisição permitida)


segunda chamada
-- Parâmetros recebidos
local rate = 2.0             -- ARGV[1]: Taxa de recarga (tokens/segundo)
local capacity = 5           -- ARGV[2]: Capacidade máxima
local now = 1742630410       -- ARGV[3]: Timestamp atual (10 segundos depois)
local requested = 1          -- ARGV[4]: Tokens solicitados por requisição

-- Cálculo de TTL (igual ao anterior)
local fill_time = capacity / rate
-- fill_time = 5 / 2 = 2.5 segundos

local ttl = math.floor(fill_time * 2)
-- ttl = math.floor(2.5 * 2) = math.floor(5) = 5 segundos

-- Verificação de tokens existentes (agora já existem)
local last_tokens = tonumber(redis.call("get", tokens_key))
-- last_tokens = 4 (valor armazenado na requisição anterior)

-- Verificação de timestamp anterior (agora já existe)
local last_refreshed = tonumber(redis.call("get", timestamp_key))
-- last_refreshed = 1742630400 (timestamp da requisição anterior)

-- Cálculo do tempo decorrido
local delta = math.max(0, now - last_refreshed)
-- delta = 1742630410 - 1742630400 = 10 segundos

-- Reabastecimento de tokens com base no tempo decorrido
local filled_tokens = math.min(capacity, last_tokens + (delta * rate))
-- filled_tokens = min(5, 4 + (10 * 2)) = min(5, 24) = 5 (limitado pela capacidade)

-- Verificação se há tokens suficientes
local allowed = filled_tokens >= requested
-- allowed = 5 >= 1 = true

-- Atualização do número de tokens após a requisição
local new_tokens = filled_tokens
if allowed then
    new_tokens = filled_tokens - requested
end
-- new_tokens = 5 - 1 = 4

-- Armazenamento dos valores atualizados
redis.call("setex", tokens_key, ttl, new_tokens)
-- Armazena 4 tokens com TTL de 5 segundos

redis.call("setex", timestamp_key, ttl, now)
-- Armazena o novo timestamp com TTL de 5 segundos

-- Resultado
return allowed and 1 or 0
-- Retorna 1 (requisição permitida)
```