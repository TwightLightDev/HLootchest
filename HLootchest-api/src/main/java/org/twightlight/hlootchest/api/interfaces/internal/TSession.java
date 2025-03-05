package org.twightlight.hlootchest.api.interfaces.internal;

import org.twightlight.hlootchest.api.interfaces.lootchest.TBox;

public interface TSession {
    void close();
    boolean isOpening();
    void setBox(TBox box);
}
