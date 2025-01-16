package app.commands;

import app.DrawCommand;

import java.awt.*;

public record DrawCrossCommand(Point p) implements DrawCommand {
    @Override
    public void execute(Graphics2D g) {
        BasicStroke stroke = (BasicStroke)g.getStroke();
        int thickness = (int)stroke.getLineWidth();
        g.drawLine(p.x - thickness, p.y - thickness, p.x + thickness, p.y + thickness);
        g.drawLine(p.x - thickness, p.y + thickness, p.x + thickness, p.y - thickness);
    }
}
