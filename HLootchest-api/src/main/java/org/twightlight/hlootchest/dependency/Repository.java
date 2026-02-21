package org.twightlight.hlootchest.dependency;

public class Repository {

    private final String url;

    public static final Repository MAVEN_CENTRAL =
            new Repository("https://repo1.maven.org/maven2/");

    public Repository(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}

