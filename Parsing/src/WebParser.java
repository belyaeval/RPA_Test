import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

class WebParser {
    private String source;
    private String target;
    private Document sourceHTML;
    private Document targetHTML;
    private Document internalHTML;
    private HashMap<String, Integer> paths;

    WebParser(String source, String target, HashMap<String, Integer> paths) {
        this.source = source;
        this.target = target;
        this.paths = paths;
    }

    void parseSource(int j, HashSet<String> links, ArrayList<String> visited) {
        try {
            sourceHTML = Jsoup.connect(source).get();
        } catch (IOException e) {
            e.getMessage();
        }

        Elements content = sourceHTML.select("div.mw-parser-output").select("a[href*=/wiki/]").not("[class=image]");

        for (Element elem : content) {
            if (!elem.text().equals("") && elem.attr("abs:href").contains("ru.wikipedia.org") && !elem.attr("class").contains("mw-magiclink-isbn")) {
                links.add(elem.attr("abs:href"));
            }
        }

        int i = 1;

        if (links.contains(target)) {
            paths.put(source, i);

            return;
        }

        if (!visited.contains(source)) {
            visited.add(source);
        }

        i++;

        while (true) {
            for (String link : links) {
                try {
                    internalHTML = Jsoup.connect(link).userAgent("Chrome/83.0.4103.61").get();
                } catch (IOException e) {
                    e.getMessage();
                }

                Elements internalContent = internalHTML.select("div.mw-parser-output").select("a[href*=/wiki/]").not("[class=image]");

                for (Element internalElem : internalContent) {
                    if (!internalElem.text().equals("") && internalElem.attr("abs:href").contains("ru.wikipedia.org") && !internalElem.attr("class").contains("mw-magiclink-isbn")) {
                        if (internalElem.attr("abs:href").equals(target)) {
                            paths.put(source, i);

                            return;
                        }

                        if (visited.contains(internalElem.attr("abs:href"))) {
                            continue;
                        }

                        boolean isExist = false;
                        int pathLength = 0;

                        for (String path : paths.keySet()) {
                            if (internalElem.attr("abs:href").equals(path)) {
                                isExist = true;
                                pathLength = paths.get(path);
                                break;
                            }
                        }

                        if (isExist) {
                            paths.put(source, i + pathLength);

                            return;
                        }
                        if (!visited.contains(internalElem.attr("abs:href"))) {
                            visited.add(internalElem.attr("abs:href"));
                        }
                    }
                }
            }
            int sizeBefore = links.size();

            links.removeAll(paths.keySet());
            if (sizeBefore == links.size()) {
                break;
            }

            i++;
        }

        source = visited.get(j);
        j++;

        parseSource(j, links, visited);
    }

    HashMap<String, Integer> parseTarget(HashSet<String> links) {
        try {
            targetHTML = Jsoup.connect(source).get();
        } catch (IOException e) {
            e.getMessage();
        }

        Elements content = targetHTML.select("div.mw-parser-output").select("a[href*=/wiki/]").not("[class=image]");

        for (Element elem : content) {
            if (!elem.text().equals("") && elem.attr("abs:href").contains("ru.wikipedia.org") && !elem.attr("class").contains("mw-magiclink-isbn")) {
                links.add(elem.attr("abs:href"));
            }
        }

        int i = 1;

        while (true) {
            for (String link : links) {
                try {
                    internalHTML = Jsoup.connect(link).userAgent("Chrome/83.0.4103.61").get();
                } catch (IOException e) {
                    e.getMessage();
                }

                Elements internalContent = internalHTML.select("div.mw-parser-output").select("a[href*=/wiki/]").not("[class=image]");

                for (Element elem : internalContent) {
                    if (!elem.text().equals("") && elem.attr("abs:href").contains("ru.wikipedia.org") && !elem.attr("class").contains("mw-magiclink-isbn")) {
                        boolean isExist = false;
                        for (String internalLink : paths.keySet()) {
                            if (i == 1) {
                                if (elem.attr("abs:href").equals(target)) {
                                    isExist = true;
                                    break;
                                }
                            } else {
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
            int sizeBefore = links.size();

            links.removeAll(paths.keySet());
            if (sizeBefore == links.size()) {
                break;
            }

            i++;
        }

        return paths;
    }
}
