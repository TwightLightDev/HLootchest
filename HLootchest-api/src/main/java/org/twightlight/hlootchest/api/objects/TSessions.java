package org.twightlight.hlootchest.api.objects;

public interface TSessions {
    void close();
    boolean isOpening();
    void setNewBox(TBox box);
}
