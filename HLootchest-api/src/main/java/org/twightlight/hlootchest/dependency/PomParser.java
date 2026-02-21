package org.twightlight.hlootchest.dependency;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PomParser {

    public static List<Dependency> parse(Repository repo, Dependency dependency) {

        List<Dependency> result = new ArrayList<Dependency>();

        try {
            URL url = new URL(repo.getUrl() + dependency.getPomPath());
            InputStream in = url.openStream();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(in);

            NodeList nodes = doc.getElementsByTagName("dependency");

            for (int i = 0; i < nodes.getLength(); i++) {
                Element element = (Element) nodes.item(i);

                String group = element.getElementsByTagName("groupId")
                        .item(0).getTextContent();

                String artifact = element.getElementsByTagName("artifactId")
                        .item(0).getTextContent();

                String version = element.getElementsByTagName("version")
                        .item(0).getTextContent();

                result.add(new Dependency(group, artifact, version, false));
            }

            in.close();

        } catch (Exception ignored) {}

        return result;
    }
}
