package org.twightlight.hlootchest.api.interfaces;

public interface TSessions {
    void close();
    boolean isOpening();
    void setNewBox(TBox box);
}
