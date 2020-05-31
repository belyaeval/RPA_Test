import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

class WebParser {
    private String source;
    private final String target = "https://ru.wikipedia.org/wiki/%D0%A4%D0%B8%D0%BB%D0%BE%D1%81%D0%BE%D1%84%D0%B8%D1%8F";
    private Document sourceHTML;
    private Document targetHTML;
    private Document internalHTML;

    void setSource(String source) {
        this.source = source;
    }

    private HashSet<String> getLinks(String url, Document html) {
        try {
            html = Jsoup.connect(url).get();
        } catch (IOException e) {
            e.getMessage();
        }

        Elements content = html.select("div.mw-parser-output").select("a[href*=/wiki/]").not("[class=image]");
        HashSet<String> links = new HashSet<>();

        for (Element elem : content) {
            if (!elem.text().equals("") && elem.attr("abs:href").contains("ru.wikipedia.org") && !elem.attr("class").contains("mw-magiclink-isbn")) {
                links.add(elem.attr("abs:href"));
            }
        }

        return links;
    }

    private TreeMap<Integer, String> getCoincidence(HashMap<String, Integer> paths, HashSet<String> links, int i) {
        TreeMap<Integer, String> tempPaths = new TreeMap<>();

        for (String knownPath : paths.keySet()) {
            if (links.contains(knownPath)) {
                int knownLength = paths.get(knownPath);
                tempPaths.put(i + knownLength, source);
            }
        }

        return tempPaths;
    }

    HashMap<String, Integer> parseSource(int i, int j, ArrayList<String> visited, HashMap<String, Integer> knownPaths, String fileName) {
        try (PrintWriter writer = new PrintWriter(fileName)) {
            HashMap<String, Integer> paths = new HashMap<>();

            if (source.equals(target)) {
                paths.put(source, i);
                knownPaths.put(source, i);
                writer.println(paths);

                return paths;
            }

            i++;
            HashSet<String> sourceLinks = getLinks(source, sourceHTML);

            if (sourceLinks.contains(target)) {
                paths.put(source, i);
                knownPaths.put(source, i);
                writer.println(paths);

                return paths;
            }

            TreeMap<Integer, String> tempPaths = getCoincidence(knownPaths, sourceLinks, i);

            if (!tempPaths.isEmpty()) {
                paths.put(source, tempPaths.firstKey());
                knownPaths.put(source, tempPaths.firstKey());
                writer.println(paths);

                return paths;
            }

            if (!visited.contains(source)) {
                visited.add(source);
                i++;
            }

            for (String link : sourceLinks) {
                if (visited.contains(link)) {
                    continue;
                }

                HashSet<String> internalLinks = getLinks(link, internalHTML);
                i++;

                internalLinks.removeAll(visited);

                if (internalLinks.contains(target)) {
                    paths.put(source, i);
                    knownPaths.put(source, i);
                    writer.println(paths);

                    return paths;
                }

                TreeMap<Integer, String> tempInternalPaths = getCoincidence(knownPaths, internalLinks, i);

                if (!tempInternalPaths.isEmpty()) {
                    paths.put(source, tempInternalPaths.firstKey());
                    knownPaths.put(source, i);
                    writer.println(paths);

                    return paths;
                }

                visited.addAll(internalLinks);
            }

            i++;
            source = visited.get(j);
            j++;
        } catch (FileNotFoundException e) {
            e.getMessage();
        }

        return parseSource(i, j, visited, knownPaths, fileName);
    }

    HashMap<String, Integer> parseTarget() {
        HashSet<String> targetLinks = getLinks(target, targetHTML);
        HashMap<String, Integer> paths = new HashMap<>();

        int i = 1;

        while (true) {
            for (String link : targetLinks) {
                HashSet<String> internalLinks = getLinks(link, internalHTML);

                if (internalLinks.contains(target)) {
                    paths.put(link, i);
                }

                TreeMap<Integer, String> tempInternalPaths = getCoincidence(paths, internalLinks, i);

                if (!tempInternalPaths.isEmpty()) {
                    paths.put(link, tempInternalPaths.firstKey());
                }
            }
            int sizeBefore = targetLinks.size();

            targetLinks.removeAll(paths.keySet());
            if (sizeBefore == targetLinks.size()) {
                break;
            }

            i++;
        }

        return paths;
    }
}
