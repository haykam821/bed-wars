package net.gegy1000.bedwars.shop;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;

import java.util.Arrays;
import java.util.function.Consumer;

public final class ShopInventory implements Inventory {
    private static final int WIDTH = 9;
    private static final int PADDING = 1;
    private static final int PADDED_WIDTH = WIDTH - PADDING * 2;

    private final ShopUi.Element[] elements = new ShopUi.Element[this.getInvSize()];

    private final ServerPlayerEntity player;
    private final Consumer<ShopBuilder> builder;

    ShopInventory(ServerPlayerEntity player, Consumer<ShopBuilder> builder) {
        this.player = player;
        this.builder = builder;
        this.buildGrid();
    }

    private void buildGrid() {
        ShopBuilder builder = new ShopBuilder(this.player);
        this.builder.accept(builder);

        this.buildGrid(builder.elements.toArray(new ShopUi.Element[0]));
    }

    private void buildGrid(ShopUi.Element[] elements) {
        Arrays.fill(this.elements, null);

        int rows = MathHelper.ceil((double) elements.length / PADDED_WIDTH);
        for (int row = 0; row < rows; row++) {
            ShopUi.Element[] resolved = this.resolveRow(elements, row);
            int minColumn = (WIDTH - resolved.length) / 2;
            for (int column = 0; column < resolved.length; column++) {
                ShopUi.Element element = resolved[column];
                this.elements[(column + minColumn) + row * WIDTH] = element;
            }
        }
    }

    private ShopUi.Element[] resolveRow(ShopUi.Element[] elements, int row) {
        int minId = Integer.MAX_VALUE;
        int maxId = Integer.MIN_VALUE;
        int rowStart = row * PADDED_WIDTH;
        int rowEnd = Math.min(rowStart + PADDED_WIDTH, elements.length);
        for (int idx = rowStart; idx < rowEnd; idx++) {
            if (elements[idx] != null) {
                if (idx < minId) {
                    minId = idx;
                }
                if (idx > maxId) {
                    maxId = idx;
                }
            }
        }
        ShopUi.Element[] resolved = new ShopUi.Element[(maxId - minId) + 1];
        System.arraycopy(elements, minId, resolved, 0, resolved.length);
        return resolved;
    }

    @Override
    public int getInvSize() {
        return WIDTH * 6;
    }

    @Override
    public boolean isInvEmpty() {
        return false;
    }

    @Override
    public int getInvMaxStackAmount() {
        return 1;
    }

    @Override
    public ItemStack getInvStack(int index) {
        ShopUi.Element element = this.elements[index];
        if (element == null) {
            return ItemStack.EMPTY;
        }
        return element.getIcon(this.player);
    }

    @Override
    public ItemStack takeInvStack(int index, int count) {
        this.handleElementClick(index);
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeInvStack(int index) {
        this.handleElementClick(index);
        return ItemStack.EMPTY;
    }

    private void handleElementClick(int index) {
        this.player.inventory.setCursorStack(ItemStack.EMPTY);
        this.player.method_14241();

        ShopUi.Element element = this.elements[index];
        if (element != null) {
            element.onClick(this.player);
        }

        this.buildGrid();
        this.player.onContainerRegistered(this.player.container, this.player.container.getStacks());
    }

    @Override
    public void setInvStack(int slot, ItemStack stack) {
    }

    @Override
    public void markDirty() {
    }

    @Override
    public boolean canPlayerUseInv(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
    }
}
