package sse.sse.pool;

import java.io.PrintWriter;

public record WriteStream(
        PrintWriter w,
        int userId
) {
}
