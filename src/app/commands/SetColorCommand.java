package app.commands;

import app.DrawCommand;
import java.awt.*;

public record SetColorCommand(Color color) implements DrawCommand {
    @Override
    public void execute(Graphics2D g) {
        g.setColor(color);
    }
}
