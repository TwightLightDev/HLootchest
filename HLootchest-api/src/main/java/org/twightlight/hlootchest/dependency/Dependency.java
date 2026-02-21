package org.twightlight.hlootchest.dependency;

public class Dependency {

    private final String group;
    private final String artifact;
    private final String version;
    private final boolean transitive;

    public Dependency(String group, String artifact, String version, boolean transitive) {
        this.group = group;
        this.artifact = artifact;
        this.version = version;
        this.transitive = transitive;
    }

    public String getGroup() {
        return group;
    }

    public String getArtifact() {
        return artifact;
    }

    public String getVersion() {
        return version;
    }

    public boolean isTransitive() {
        return transitive;
    }

    public String getPath() {
        return group.replace('.', '/') + "/"
                + artifact + "/"
                + version + "/"
                + artifact + "-" + version + ".jar";
    }

    public String getPomPath() {
        return group.replace('.', '/') + "/"
                + artifact + "/"
                + version + "/"
                + artifact + "-" + version + ".pom";
    }

    @Override
    public int hashCode() {
        return (group + artifact + version).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Dependency)) return false;
        Dependency other = (Dependency) obj;
        return group.equals(other.group)
                && artifact.equals(other.artifact)
                && version.equals(other.version);
    }
}