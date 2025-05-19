package org.twightlight.hlootchest.supports.hooks;

import me.arcaniax.hdb.api.HeadDatabaseAPI;

public class HeadDatabase {

    private final HeadDatabaseAPI headDatabaseAPI;

    public HeadDatabase() {
        headDatabaseAPI = new HeadDatabaseAPI();
    }

    public String getBase64(String id) {
        return headDatabaseAPI.getBase64(id);
    }

    public HeadDatabaseAPI getHeadDatabaseAPI() {
        return headDatabaseAPI;
    }

}
