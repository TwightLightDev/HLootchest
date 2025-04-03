package org.twightlight.hlootchest.api.interfaces.internal;

import org.twightlight.hlootchest.api.interfaces.lootchest.TBox;

public interface TSession {
    /**
     * Closes the current box or related entity.
     */
    void close();

    /**
     * Checks whether the box is currently in an open state.
     *
     * @return {@code true} if the box is opening, otherwise {@code false}.
     */
    boolean isOpening();

    /**
     * Sets the associated box instance.
     *
     * @param box The {@link TBox} instance to set.
     */
    void setBox(TBox box);
}
