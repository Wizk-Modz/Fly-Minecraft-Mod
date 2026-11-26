package com.wizkfly;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
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

// Đăng ký và xử lý các lệnh /fly
public final class FlyCommand {
    private static final PermissionCheck ADMIN_CHECK = new PermissionCheck.Require(Permissions.COMMANDS_GAMEMASTER);

    // Lớp tiện ích, không cho khởi tạo trực tiếp
    private FlyCommand() {
    }

    // Đăng ký cây lệnh /fly và các nhánh con vào hệ thống
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("fly")
            .executes(context -> toggleSelf(context.getSource()))
            .then(Commands.literal("reload")
                .requires(Commands.hasPermission(ADMIN_CHECK))
                .executes(context -> reloadConfig(context.getSource())))
            .then(Commands.literal("speed")
                .then(Commands.argument("value", FloatArgumentType.floatArg(0.01f, 0.5f))
                    .executes(context -> setSpeedSelf(context.getSource(), FloatArgumentType.getFloat(context, "value")))
                    .then(Commands.argument("target", EntityArgument.player())
                        .requires(Commands.hasPermission(ADMIN_CHECK))
                        .executes(context -> setSpeedOther(
                            context.getSource(),
                            FloatArgumentType.getFloat(context, "value"),
                            EntityArgument.getPlayer(context, "target"))))))
            .then(Commands.argument("target", EntityArgument.player())
                .requires(Commands.hasPermission(ADMIN_CHECK))
                .executes(context -> toggleOther(context.getSource(), EntityArgument.getPlayer(context, "target")))));
    }

    // Bật hoặc tắt chế độ bay cho người gửi lệnh
    private static int toggleSelf(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = requirePlayer(source);
        FlyManager.get().toggle(player);
        return 1;
    }

    // Bật hoặc tắt chế độ bay cho người chơi khác
    private static int toggleOther(CommandSourceStack source, ServerPlayer target) {
        boolean enabled = FlyManager.get().toggle(target);
        String adminKey = enabled ? "wizk-fly-mod.message.fly.other.enabled" : "wizk-fly-mod.message.fly.other.disabled";
        String targetKey = enabled ? "wizk-fly-mod.message.fly.by_admin.enabled" : "wizk-fly-mod.message.fly.by_admin.disabled";

        source.sendSuccess(() -> Component.translatable(adminKey, target.getDisplayName()), true);
        target.sendSystemMessage(Component.translatable(targetKey, source.getDisplayName())
            .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED));
        return 1;
    }

    // Đặt tốc độ bay cho bản thân người gửi lệnh
    private static int setSpeedSelf(CommandSourceStack source, float speed) throws CommandSyntaxException {
        ServerPlayer player = requirePlayer(source);
        FlyManager.get().setSpeed(player, speed);
        source.sendSuccess(() -> Component.translatable("wizk-fly-mod.message.fly.speed.self", String.format("%.2f", speed))
            .withStyle(ChatFormatting.GOLD), false);
        return 1;
    }

    // Đặt tốc độ bay cho người chơi khác theo chỉ định của quản trị viên
    private static int setSpeedOther(CommandSourceStack source, float speed, ServerPlayer target) {
        FlyManager.get().setSpeed(target, speed);
        String formatted = String.format("%.2f", speed);
        source.sendSuccess(() -> Component.translatable("wizk-fly-mod.message.fly.speed.other", target.getDisplayName(), formatted)
            .withStyle(ChatFormatting.GOLD), true);
        target.sendSystemMessage(Component.translatable("wizk-fly-mod.message.fly.speed.self", formatted)
            .withStyle(ChatFormatting.GOLD));
        return 1;
    }

    // Tải lại cấu hình mod từ tập tin
    private static int reloadConfig(CommandSourceStack source) {
        WizkFlyMod.reloadConfig();
        source.sendSuccess(() -> Component.translatable("wizk-fly-mod.message.config.reloaded")
            .withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    // Lấy đối tượng người chơi từ nguồn lệnh hoặc ném ngoại lệ nếu là console
    private static ServerPlayer requirePlayer(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            throw new SimpleCommandExceptionType(Component.translatable("wizk-fly-mod.error.player_only")).create();
        }
        return player;
    }
}
