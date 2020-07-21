package net.gegy1000.gl.game.event;

import net.gegy1000.gl.game.Game;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PlayerRemoveListener {
    EventType<PlayerRemoveListener> EVENT = EventType.create(PlayerRemoveListener.class, listeners -> {
        return (game, player) -> {
            for (PlayerRemoveListener listener : listeners) {
                listener.onRemovePlayer(game, player);
            }
        };
    });

    void onRemovePlayer(Game game, ServerPlayerEntity player);
}
