local M = {}

function M.shoot(api)
    api:shootOnce(true)
end

function M.start_bolt(api)
    -- Return true to start ticking, since there are nothing needed to be check
    return true
end

function M.tick_bolt(api)
    -- Get total bolt time from script parameter in gun data
    local total_bolt_time = api:getScriptParams().bolt_time
    if (total_bolt_time == nil) then
        return false
    end
    if (api:getBoltTime() < total_bolt_time) then
        -- Bolt time less than total means we need to keep ticking, return true
        return true
    else
        -- Bolt time greater than total means that the bullet
        -- needs to be put from the magazine into the barrel, and then return false to end ticking.
        if (api:removeAmmoFromMagazine(1) ~= 0) then
            api:setAmmoInBarrel(true);
        end
        return false
    end
end

function M.start_reload(api)
    local neededAmmoAmount = api:getNeededAmmoAmount()
    -- If there is no ammo to be loaded, there is no need to start ticking, just return false
    if (neededAmmoAmount == 0) then
        return false
    end
    -- Initialize cache that will be used in reload ticking
    local cache = {
        reloaded_count = 0,
        needed_count = api:getNeededAmmoAmount(),
        interrupted_time = -1,
    }
    api:cacheScriptData(cache)
    -- Return true to start ticking
    return true
end

local function getReloadTimingFromParam(param)
    -- Need to convert time from seconds to milliseconds
    local intro_empty = param.intro_empty * 1000
    local intro = param.intro * 1000
    local loop = param.loop * 1000
    local ending = param.ending * 1000
    local intro_empty_feed = param.intro_empty_feed * 1000
    local loop_feed = param.loop_feed * 1000
    -- Check if any timing is nil
    if (intro_empty == nil or intro == nil or loop == nil or ending == nil or intro_empty_feed == nil or loop_feed == nil) then
        return nil
    end
    return intro_empty, intro, loop, ending, intro_empty_feed, loop_feed
end

function M.tick_reload(api)
    -- Get all timings from script parameter in gun data
    local param = api:getScriptParams();
    local intro_empty, intro, loop, ending, intro_empty_feed, loop_feed = getReloadTimingFromParam(param)
    if (intro_empty == nil) then
        return NOT_RELOADING, -1
    end
    -- Get reload time (The time from the start of reloading to the current time) and reload state from api
    local reload_time = api:getReloadTime()
    local reload_state_type = api:getReloadStateType():ordinal()
    -- Get cache from api, it will be used to count loaded ammo, mark reload interruptions, etc.
    local cache = api:getCachedScriptData()
    -- Handle interrupting reload
    if (cache.interrupted_time ~= -1) then
        local int_time = reload_time - cache.interrupted_time
        if (int_time >= ending) then
            return NOT_RELOADING, -1
        else
            return TACTICAL_RELOAD_FINISHING, ending - int_time
        end
    end
    -- Put an ammo into the barrel first
    local reloaded_count = cache.reloaded_count;
    if (reloaded_count == 0) then
        if (reload_state_type == EMPTY_RELOAD_FEEDING) then
            if (reload_time > intro_empty_feed) then
                api:setAmmoInBarrel(true)
                reloaded_count = reloaded_count + 1
            end
        else
            reloaded_count = reloaded_count + 1
        end
    end
    -- Load the ammo into the magazine one by one
    if (reloaded_count > 0) then
        local base_time = (reloaded_count -1) * loop + loop_feed
        if (reload_state_type == EMPTY_RELOAD_FEEDING) then
            base_time = base_time + intro_empty
        elseif (reload_state_type == TACTICAL_RELOAD_FEEDING) then
            base_time = base_time + intro
        end
        while (base_time < reload_time) do
            if (reloaded_count > cache.needed_count) then
                break
            end
            reloaded_count = reloaded_count + 1
            base_time = base_time + loop
            api:putAmmoInMagazine(1)
        end
    end
    -- Write back cache
    if (reloaded_count > cache.needed_count) then
        cache.interrupted_time = api:getReloadTime() - loop_feed + loop
    end
    cache.reloaded_count = reloaded_count
    api:cacheScriptData(cache)
    -- return reloadstate
    local total_time = cache.needed_count * loop
    if (reload_state_type == EMPTY_RELOAD_FEEDING) then
        total_time = total_time + intro_empty
    elseif (reload_state_type == TACTICAL_RELOAD_FEEDING) then
        total_time = total_time + intro
    end
    return reload_state_type, total_time - reload_time
end

function M.interrupt_reload(api)
    local cache = api:getCachedScriptData()
    if (cache ~= nil and cache.interrupted_time == -1) then
        cache.interrupted_time = api:getReloadTime()
    end
end

return M