local M = {}

function M.shoot(api)
    api:shootOnce(true)
end

function M.start_reload(api)
    local cache = {
        reloaded_count = 0,
        needed_count = api:getNeededAmmoAmount(),
        interrupted_time = -1
    }
    api:cacheScriptData(cache)
end

function M.tick_reload(api)
    local intro_empty, intro, loop, ending = 2.13 * 1000, 0.37 * 1000, 0.67 * 1000, 0.17 * 1000
    local intro_empty_feed, loop_feed = 1.67 * 1000, 0.4 * 1000
    local reload_time = api:getReloadTime()
    local reload_state_type = api:getReloadStateType():ordinal()
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
    -- cache write-back
    if (reloaded_count > cache.needed_count) then
        cache.interrupted_time = api:getReloadTime() - loop_feed + loop
    end
    cache.reloaded_count = reloaded_count
    api:cacheScriptData(cache)
    -- regular return
    return reload_state_type, 1
end

function M.interrupt_reload(api)
    local cache = api:getCachedScriptData()
    if (cache ~= nil and cache.interrupted_time == -1) then
        cache.interrupted_time = api:getReloadTime()
    end
end

return M