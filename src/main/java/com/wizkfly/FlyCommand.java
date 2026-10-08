package com.wizkfly;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionCheck;
import net.minecraft.server.permissions.Permissions;

// Đăng ký và xử lý lệnh /fly
public final class FlyCommand {
    private static final PermissionCheck ADMIN_CHECK = new PermissionCheck.Require(Permissions.COMMANDS_GAMEMASTER);

    // Lớp tiện ích, không cho khởi tạo
    private FlyCommand() {
    }

    // Đăng ký lệnh /fly vào dispatcher
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("fly")
            .requires(FlyCommand::canUseFly)
            .executes(context -> toggleSelf(context.getSource()))
            .then(Commands.argument("target", EntityArgument.player())
                .requires(Commands.hasPermission(ADMIN_CHECK))
                .executes(context -> toggleOther(context.getSource(), EntityArgument.getPlayer(context, "target")))));
    }

    // Kiểm tra nguồn lệnh được phép dùng chức năng Fly
    private static boolean canUseFly(CommandSourceStack source) {
        return WizkFlyMod.config().permission() == FlyConfig.Permission.ALL || isAdmin(source);
    }

    // Bật/tắt Fly cho người gửi lệnh
    private static int toggleSelf(CommandSourceStack source) throws CommandSyntaxException {
        FlyManager.get().toggle(requirePlayer(source));
        return 1;
    }

    // Bật/tắt Fly cho player được chỉ định
    private static int toggleOther(CommandSourceStack source, ServerPlayer target) throws CommandSyntaxException {
        boolean enabled = FlyManager.get().toggle(target);
        Component status = Component.translatable(
            enabled ? "wizk-fly-mod.message.fly.enabled" : "wizk-fly-mod.message.fly.disabled")
            .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED);
        source.sendSuccess(() -> Component.translatable("wizk-fly-mod.message.fly.other", target.getDisplayName(), status), true);
        return 1;
    }

    // Lấy player gửi lệnh, báo lỗi nếu nguồn không phải player
    private static ServerPlayer requirePlayer(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            throw new SimpleCommandExceptionType(Component.translatable("wizk-fly-mod.error.player_only")).create();
        }
        return player;
    }

    // Kiểm tra nguồn lệnh có quyền quản trị
    private static boolean isAdmin(CommandSourceStack source) {
        return ADMIN_CHECK.check(source.permissions());
    }
}
