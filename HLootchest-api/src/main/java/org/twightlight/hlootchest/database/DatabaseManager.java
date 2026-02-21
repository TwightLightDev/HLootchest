package org.twightlight.hlootchest.database;

import org.twightlight.hlootchest.api.interfaces.internal.TDatabase;

public class DatabaseManager {
    private TDatabase database;

    public void setDatabase(TDatabase database) {
        this.database = database;
    }

    public TDatabase getDatabase() {
        return database;
    }
}
