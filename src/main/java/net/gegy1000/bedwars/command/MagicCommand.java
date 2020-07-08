package net.gegy1000.bedwars.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.gegy1000.bedwars.BedWarsMod;
import net.gegy1000.bedwars.item.CustomItem;
import net.minecraft.command.arguments.IdentifierArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class MagicCommand {
    public static final DynamicCommandExceptionType CUSTOM_ITEM_NOT_FOUND = new DynamicCommandExceptionType(arg -> {
        return new TranslatableText("Custom item with id '%s' was not found!", arg);
    });

    // @formatter:off
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("magic")
            .then(literal("customize")
            .then(argument("custom", IdentifierArgumentType.identifier())
                .executes(MagicCommand::customizeHeld)
            ))
            .then(literal("clearAllocation").executes(MagicCommand::clearAllocation))
        );
    }
    // @formatter:on

    private static int customizeHeld(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        Identifier customId = IdentifierArgumentType.getIdentifier(context, "custom");
        CustomItem customItem = CustomItem.get(customId);

        if (customItem == null) {
            throw CUSTOM_ITEM_NOT_FOUND.create(customId);
        }

        ItemStack stack = player.getStackInHand(Hand.MAIN_HAND);
        if (!stack.isEmpty()) {
            customItem.apply(stack);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int clearAllocation(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        ServerWorld world = source.getWorld();

        BlockPos origin = new BlockPos(100000, 0, 10000);
        ChunkPos originChunk = new ChunkPos(origin);

        ChunkPos minChunk = new ChunkPos(originChunk.x - 32, originChunk.z - 32);
        ChunkPos maxChunk = new ChunkPos(originChunk.x + 32, originChunk.z + 32);

        BedWarsMod.LOGGER.info("Clearing chunks");
        for (int mz = minChunk.z; mz <= maxChunk.z; mz++) {
            int z = mz;
            source.getMinecraftServer().submit(() -> {
                for (int x = minChunk.x; x <= maxChunk.x; x++) {
                    WorldChunk chunk = world.getChunk(x, z);
                    ChunkSection[] sections = chunk.getSectionArray();
                    for (int y = 0; y < sections.length; y++) {
                        sections[y] = new ChunkSection(y << 4);
                    }
                }
            });
        }

        return Command.SINGLE_SUCCESS;
    }
}
