package org.aanguita.jacuzzi.io.stream;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author aanguita
 *         17/05/2017
 */
public class FragmentedInputStream extends InputStream {

    private final List<InputStream> streamSources;

    public FragmentedInputStream(List<InputStream> streamSources) {
        this.streamSources = streamSources;
    }

    public FragmentedInputStream(InputStream... inputStreams) {
        this(new ArrayList<InputStream>(Arrays.asList(inputStreams)));
    }

    @Override
    public int read() throws IOException {
        if (streamSources.isEmpty()) {
            return -1;
        } else {
            int b = streamSources.get(0).read();
            if (b == -1) {
                streamSources.remove(0);
                return read();
            } else {
                return b;
            }
        }
    }
}
