package app;

import java.util.List;

public interface DrawCommandLoader {
    Result<List<DrawCommand>> load(String filename);
}
