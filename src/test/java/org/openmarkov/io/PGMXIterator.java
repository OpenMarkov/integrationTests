package org.openmarkov.io;

import org.openmarkov.core.exception.ParserException;

import java.io.IOException;

public interface PGMXIterator {

    PGMXCompound next() throws IOException, ParserException;
    boolean hasNext();

}
