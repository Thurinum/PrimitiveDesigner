package app.commands;

import app.DrawCommand;

import java.awt.*;

public record DrawLineCommand(Point start, Point end) implements DrawCommand {
    @Override
    public void execute(Graphics2D g) {
        g.drawLine(start.x, start.y, end.x, end.y);
    }
}