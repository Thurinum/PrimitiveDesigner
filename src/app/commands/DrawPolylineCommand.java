package app.commands;

import app.DrawCommand;

import java.awt.*;
import java.util.List;

public record DrawPolylineCommand(List<Point> points) implements DrawCommand {
    @Override
    public void execute(Graphics2D g) {
        Polygon p = new Polygon();
        for (Point point : points) {
            p.addPoint(point.x, point.y);
        }
        g.drawPolygon(p);
    }
}
