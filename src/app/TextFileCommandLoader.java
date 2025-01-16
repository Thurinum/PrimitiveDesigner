package app;

import java.awt.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import app.commands.*;

record Token(
    Operation operation,
    ArrayList<Point> points,
    ArrayList<String> extraData
) {
    boolean isOperation(Operation op) {
        return operation == op;
    }
}
enum Operation {
    DRAW_POINT("point"),
    DRAW_CROSS("cross"),
    DRAW_LINE("line"),
    DRAW_POLYLINE("polyline"),

    SET_COLOR("color"),
    SET_THICKNESS("thickness"),

    INVALID("invalid");

    private final String operation;

    Operation(String operation) {
        this.operation = operation;
    }

    public static Operation fromString(String operation) {
        for (Operation op : Operation.values()) {
            if (op.operation.equals(operation))
                return op;
        }
        return INVALID;
    }

    public boolean isValid() {
        return this != INVALID;
    }

    String[] validOperations() {
        return new String[] {
            DRAW_POINT.operation,
            DRAW_CROSS.operation,
            DRAW_LINE.operation,
            DRAW_POLYLINE.operation,
            SET_COLOR.operation,
            SET_THICKNESS.operation
        };
    }
}

public class TextFileCommandLoader implements DrawCommandLoader {
    private Result<List<DrawCommand>> error(String filename, int lineNum, String msg, Object... args) {
        String formattedMsg = String.format(msg, args);
        return Result.error("%s(%d): %s", filename, lineNum, formattedMsg);
    }

    private Result<Token> tokenize(String line) {
        String[] fragments = line.split("\\s+");

        if (fragments.length == 0)
            return Result.error("Unrecognized token [%s].", line);

        Operation op = Operation.fromString(fragments[0]);
        if (!op.isValid())
            return Result.error("Unknown command [%s].\n\tAvailable commands: [%s]", fragments[0], String.join(", ", op.validOperations()));

        ArrayList<Point> points = new ArrayList<>();
        ArrayList<String> extraData = new ArrayList<>();

        for (int i = 1; i < fragments.length; i++) {
            Optional<Point> point = parsePoint(fragments[i]);
            if (point.isPresent()) {
                points.add(point.get());
            } else {
                String data = fragments[i];
                extraData.add(data);
            }
        }

        Token token = new Token(op, points, extraData);
        return Result.ok(token);
    }

    private Optional<Point> parsePoint(String point) {
        String[] components = point.split(",");
        if (components.length != 2)
            return Optional.empty();

        try {
            int x = Integer.parseInt(components[0]);
            int y = Integer.parseInt(components[1]);
            return Optional.of(new Point(x, y));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    @Override
    public Result<List<DrawCommand>> load(String filePath) {
        List<DrawCommand> commands = new ArrayList<>();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(filePath));
        } catch (FileNotFoundException e) {
            return Result.error("File not found: %s.", filePath);
        }

        // always add a set color command at the beginning
        commands.add(new SetColorCommand(Color.BLACK));

        String[] filenameComponents = filePath.split("[\\\\/]");
        String filename = filenameComponents[filenameComponents.length - 1];
        int lineNumber = 0;
        String line;
        while (true) {
            try {
                line = reader.readLine();
            } catch (IOException e) {
                return Result.error("Error reading file [%s]: %s.", filePath, e.getMessage());
            }

            // end on eof
            if (line == null)
                break;

            lineNumber++;

            if (line.isBlank())
                continue;

            // ignore comments
            char firstChar = line.charAt(0);
            if (firstChar == '#')
                continue;

            // check if color instruction
            Result<Token> token = tokenize(line);
            if (!token.isOk())
                return error(filename, lineNumber, token.getError());

            Token t = token.getResult();
            if (t.isOperation(Operation.SET_COLOR)) {
                if (t.extraData().isEmpty())
                    return error(filename, lineNumber, "Color command must be followed by a color using format [r,g,b].");

                String encodedColor = t.extraData().getFirst();
                String[] colorComponents = encodedColor.split(",");
                if (colorComponents.length != 3)
                    return error(filename, lineNumber, "Color argument must have 3 components (r,g,b).");

                Color color = new Color(
                        Integer.parseInt(colorComponents[0]),
                        Integer.parseInt(colorComponents[1]),
                        Integer.parseInt(colorComponents[2])
                );

                commands.add(new SetColorCommand(color));
            } else if (t.isOperation(Operation.SET_THICKNESS)) {
                if (t.extraData().isEmpty())
                    return error(filename, lineNumber, "Thickness instruction must be followed by integer.");

                int thickness = Integer.parseInt(t.extraData().getFirst());
                commands.add(new SetThicknessCommand(thickness));
            } else {
                if (t.points().isEmpty())
                    return error(filename, lineNumber, "Drawing instruction must contain at least one point. E.g.: 10,10");

                int numPoints = t.points().size();
                switch (t.operation()) {
                    case DRAW_POINT -> commands.add(new DrawPointCommand(t.points().get(0)));
                    case DRAW_CROSS -> commands.add(new DrawCrossCommand(t.points().get(0)));
                    case DRAW_LINE -> {
                        if (numPoints != 2)
                            return error(filename, lineNumber, "Line instruction must have 2 points, found " + numPoints);
                        commands.add(new DrawLineCommand(t.points().get(0), t.points().get(1)));
                    }
                    case DRAW_POLYLINE -> {
                        if (numPoints < 2)
                            return error(filename, lineNumber, "Polyline instruction must have at least 2 points, found " + numPoints, t.extraData());
                        commands.add(new DrawPolylineCommand(t.points()));
                    }
                    default -> {
                        return error(filename, lineNumber, "Unknown operation [%s].", t.operation());
                    }
                }
            }
        }

        try {
            reader.close();
        } catch (IOException e) {
            return error(filename, lineNumber, "Error closing file");
        }
        return Result.ok(commands);
    }
}
