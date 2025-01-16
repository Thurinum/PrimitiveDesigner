package app.commands;

import app.DrawCommand;

import java.awt.*;

public record DrawPointCommand(Point p) implements DrawCommand {
    @Override
    public void execute(Graphics2D g) {
        BasicStroke stroke = (BasicStroke)g.getStroke();
        int thickness = (int)stroke.getLineWidth();
        g.fillOval(p.x, p.y, thickness, thickness);
    }
}
