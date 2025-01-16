package app.commands;

import app.DrawCommand;

import java.awt.*;

public record SetThicknessCommand(int thickness) implements DrawCommand {
    @Override
    public void execute(Graphics2D g) {
        g.setStroke(new BasicStroke(thickness));
    }
}