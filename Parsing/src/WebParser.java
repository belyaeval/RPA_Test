import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

class WebParser {
    private String source;
    private String target;
    private Document sourceHTML;
    private Document targetHTML;
    private Document internalHTML;

    WebParser(String source, String target) {
        this.source = source;
        this.target = target;
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


    HashMap<String, Integer> parseSource(int i, int j, ArrayList<String> visited, HashMap<String, Integer> knownPaths) {
        HashMap<String, Integer> paths = new HashMap<>();

        if (source.equals(target)) {
            paths.put(source, i);

            return paths;
        }

        i++;
        HashSet<String> sourceLinks = getLinks(source, sourceHTML);

        if (sourceLinks.contains(target)) {
            paths.put(source, i);

            return paths;
        }

        TreeMap<Integer, String> tempPaths = getCoincidence(knownPaths, sourceLinks, i);

        if (!tempPaths.isEmpty()) {
            paths.put(source, tempPaths.firstKey());

            return paths;
        }

        if (!visited.contains(source)) {
            visited.add(source);
            i++;
        }

        for (String link : sourceLinks) {
            HashSet<String> internalLinks = getLinks(link, internalHTML);
            i++;

            if (internalLinks.contains(target)) {
                paths.put(source, i);

                return paths;
            }

            TreeMap<Integer, String> tempInternalPaths = getCoincidence(knownPaths, internalLinks, i);

            if (!tempInternalPaths.isEmpty()) {
                paths.put(source, tempInternalPaths.firstKey());

                return paths;
            }

            visited.addAll(internalLinks);
        }

        i++;

        source = visited.get(j);
        j++;
        return parseSource(i, j, visited, knownPaths);
    }

    HashMap<String, Integer> parseTarget() {
        HashSet<String> targetLinks = getLinks(target, targetHTML);
        HashMap<String, Integer> paths = new HashMap<>();

        int i = 1;

        while (true) {
            for (String link : targetLinks) {
                try {
                    internalHTML = Jsoup.connect(link).userAgent("Chrome/83.0.4103.61").get();
                } catch (IOException e) {
                    e.getMessage();
                }

                Elements internalContent = internalHTML.select("div.mw-parser-output").select("a[href*=/wiki/]").not("[class=image]");

                for (Element elem : internalContent) {
                    if (!elem.text().equals("") && elem.attr("abs:href").contains("ru.wikipedia.org") && !elem.attr("class").contains("mw-magiclink-isbn")) {
                        boolean isExist = false;
                        if (i == 1) {
                            if (elem.attr("abs:href").equals(target)) {
                                isExist = true;
                            }
                        } else {
                            for (String internalLink : paths.keySet()) {
                                if (elem.attr("abs:href").equals(internalLink)) {
                                    isExist = true;
                                    break;
                                }
                            }
                        }
                        if (isExist) {
                            paths.put(link, i);
                            break;
                        }
                    }
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
