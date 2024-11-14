print("lua test")

local M = {}

function M.shoot(api)
    api:shootOnce()
end

return M