-- 脚本的位置是 "{命名空间}:{路径}"，那么 require 的格式为 "{命名空间}_{路径}"
-- 注意！require 取得的内容不应该被修改，应仅调用
local default = require("tacz_default_state_machine")
local STATIC_TRACK_LINE = default.STATIC_TRACK_LINE
local MAIN_TRACK = default.MAIN_TRACK
local main_track_states = default.main_track_states
-- main_track_states.start 和 main_track_states.idle 是我们要重写的状态。
local start_state = setmetatable({}, {__index = main_track_states.start})
local idle_state = setmetatable({}, {__index = main_track_states.idle})
-- reload_state 是定义的新状态，用于执行单发装填
local reload_state = {
    retreat = "reload_retreat",
    need_ammo = 0,
    loaded_ammo = 0
}
-- 重写 start 状态的 transition 函数，重定向到重写的 idle 状态
function start_state.transition(context, input)
    local state = main_track_states.start.transition(context, input)
    if (state == main_track_states.idle) then
        return idle_state
    end
end
-- 重写 idle 状态的 transition 函数，将输入 INPUT_RELOAD 重定向到新定义的 reload_state 状态
function idle_state.transition(context, input)
    if (input == INPUT_RELOAD) then
        return reload_state
    end
    local state = main_track_states.idle.transition(context, input)
    if (state == main_track_states.idle) then
        return idle_state
    end
    return state
end
-- 在 entry 函数里，我们根据情况选择播放 'reload_intro_empty' 或 'reload_intro' 动画，
-- 并初始化 需要的弹药数、已装填的弹药数。这决定了后续的 'loop' 动画进行几次循环。
function reload_state.entry(context)
    local isNoAmmo = not context:hasBulletInBarrel()
    if (isNoAmmo) then
        context:runAnimation("reload_intro_empty", context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK), false, PLAY_ONCE_HOLD, 0.2)
    else
        context:runAnimation("reload_intro", context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK), false, PLAY_ONCE_HOLD, 0.2)
    end
    reload_state.need_ammo = context:getMaxAmmoCount() - context:getAmmoCount()
    reload_state.loaded_ammo = 0
end
-- 在 update 函数里，循环播放 loop，让 loaded_ammo 变量自增。
function reload_state.update(context)
    if (reload_state.loaded_ammo > reload_state.need_ammo) then
        context:trigger(reload_state.retreat)
    else
        local track = context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK)
        if (context:isHolding(track)) then
            context:runAnimation("reload_loop", track, false, PLAY_ONCE_HOLD, 0)
            reload_state.loaded_ammo = reload_state.loaded_ammo + 1
        end
    end
end
-- 如果 loop 循环结束或者换弹被打断，退出到 idle 状态。否则由 idle 的 transition 函数决定下一个状态。
function reload_state.transition(context, input)
    if (input == reload_state.retreat or input == INPUT_CANCEL_RELOAD) then
        context:runAnimation("reload_end", context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK), false, PLAY_ONCE_STOP, 0.2)
        return idle_state
    end
    return idle_state.transition(context, input)
end
-- 用元表的方式继承默认状态机的属性
local M = setmetatable({
    main_track_states = setmetatable({
        -- 自定义的 start 和 idle 状态需要覆盖掉父级状态机的对应状态
        start = start_state,
        idle = idle_state,
        -- 新定义的 reload 状态也放进来，方便其他脚本调用
        reload = reload_state
    }, {__index = main_track_states})
}, {__index = default})
-- 先调用父级状态机的初始化函数，然后进行自己的初始化
function M.initialize(context)
    default.initialize(context)
    reload_state.need_ammo = 0
    reload_state.loaded_ammo = 0
end
-- 这里一定要写，用来生成覆盖状态后的默认状态表
function M.states()
    return M:default_states()
end
-- 导出状态机
return M