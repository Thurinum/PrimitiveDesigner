package app;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class App extends Canvas {
    public List<DrawCommand> commandList;

    public static void main(String[] args) {
        App app = new App();
        app.setSize(1280, 720);

        Frame frame = new Frame("The Primitive Designer");
        frame.add(app);
        frame.pack();
        frame.setVisible(true);

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        if (args.length == 0) {
            System.out.println("\u001B[31mPlease provide a path to a commands file in argument.\u001B[0m");
            System.exit(0);
        }

        DrawCommandLoader loader = new TextFileCommandLoader();
        Result<List<DrawCommand>> maybeCommands = loader.load(args[0]);
        if (!maybeCommands.isOk()) {
            System.out.printf("\u001B[31mFAILED TO LOAD COMMANDS:\n%s\u001B[0m%n", maybeCommands.getError());
            System.exit(0);
        }

        app.commandList = maybeCommands.getResult();
        System.out.println("\u001B[36mLOADED COMMANDS SUCCESSFULLY\u001B[0m");
    }

    @Override
    public void paint(Graphics g) {
        for (DrawCommand cmd : commandList) {
            cmd.execute((Graphics2D) g);
        }

        System.out.println("\u001B[36mEXECUTED DRAW COMMANDS\u001B[0m");
    }
}


