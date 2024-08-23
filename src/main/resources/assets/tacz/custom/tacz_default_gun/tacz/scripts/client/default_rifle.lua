local M = {}

local track_line_top = 0

function new_track_line()
    local i = track_line_top;
    track_line_top = track_line_top + 1;
    return i
end

local STATIC_TRACK_LINE = new_track_line()
local BASE_TRACK = 0
local BOLT_CAUGHT_TRACK = 1
local SAFETY_TRACK = 2
local MOVE_ANIMATION_TRACK = 3
local ADS_TRACK = 4
local MAIN_TRACK = 5

local SHOOTING_TRACK_LINE = new_track_line()

local BLENDING_TRACK_LINE = new_track_line()
local MOVEMENT_TRACK = 0
local LOOP_TRACK = 1

function M.initialize(context)
    context:ensureTrackLineSize(track_line_top)
    context:ensureTracksAmount(STATIC_TRACK_LINE, 6)
    context:ensureTracksAmount(BLENDING_TRACK_LINE, 2)
end

function M.exit(context)

end

function M.states()
    local stateTable = {}
    stateTable[1] = {}
    return stateTable
end

return M