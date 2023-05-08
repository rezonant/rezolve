package org.torchmc.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.torchmc.events.Event;
import org.torchmc.events.EventType;

import java.util.function.Consumer;

public class Dialog extends ModalWindow {
    public Dialog(Component title) {
        super(title);
    }

    public Dialog(String title) {
        super(title);
    }

    public static final EventType<ResultEvent> RESULT = new EventType<>();

    @Override
    public void onClose() {
        super.onClose();
        if (result == null)
            cancel();
    }

    private Result result;

    public Result getResult() {
        return result;
    }

    public void cancel() {
        finish(CANCELED);
    }

    public void finish(Result result) {
        this.result = result;
        emitEvent(RESULT, new ResultEvent(result));
        removeFromParent();
    }

    public void present(Consumer<Result> handler) {
        present(Minecraft.getInstance().screen, handler);
    }

    public void present(Screen screen, Consumer<Result> handler) {
        listenForNextEvent(RESULT, e -> handler.accept(e.result));
        present(screen);
    }

    @Override
    public void present(Screen screen) {
        this.result = null;
        super.present(screen);
    }

    public static final Result CANCELED = new Result();

    public static class Result {}

    public static class ResultEvent extends Event {
        public ResultEvent(Result result) {
            this.result = result;
        }

        public final Result result;
    }
}
