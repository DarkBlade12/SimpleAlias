package com.darkblade12.simplealias.alias.action;

import org.bukkit.entity.Player;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

final class PlayerProxy implements InvocationHandler {
    private final Player source;
    private final boolean grantPermission;
    private final boolean silent;

    PlayerProxy(Player source, boolean grantPermission, boolean silent) {
        this.source = source;
        this.grantPermission = grantPermission;
        this.silent = silent;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        if (grantPermission && methodName.equals("hasPermission") || methodName.equals("isOp")) {
            return true;
        } else if (silent && (methodName.equals("sendMessage") || methodName.equals("sendRawMessage"))) {
            return null;
        }

        return method.invoke(source, args);
    }
}
