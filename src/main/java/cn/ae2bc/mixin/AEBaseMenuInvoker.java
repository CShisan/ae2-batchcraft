package cn.ae2bc.mixin;

import appeng.menu.AEBaseMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.Consumer;

@Mixin(AEBaseMenu.class)
public interface AEBaseMenuInvoker {
    @Invoker("registerClientAction")
    <T> void ae2bc$registerClientAction(String name, Class<T> argumentClass, Consumer<T> handler);

    @Invoker("sendClientAction")
    <T> void ae2bc$sendClientAction(String name, T argument);
}
