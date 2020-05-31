import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        String source = "https://ru.wikipedia.org/wiki/%D0%91%D0%BE%D0%B9%D0%BB,_%D0%94%D1%8D%D0%BD%D0%BD%D0%B8";
        String target = "https://ru.wikipedia.org/wiki/%D0%A4%D0%B8%D0%BB%D0%BE%D1%81%D0%BE%D1%84%D0%B8%D1%8F";

        WebParser webParser = new WebParser(source, target);

        HashMap<String, Integer> knownPaths = webParser.parseTarget();
        HashMap<String, Integer> paths = webParser.parseSource(0, 1, new ArrayList<>(), knownPaths);

        System.out.println(paths);
    }
}
