package com.tacz.guns.api.vm;

import org.luaj.vm2.LuaValue;

public interface LuaLibrary {
    void install(LuaValue chunk);
}
