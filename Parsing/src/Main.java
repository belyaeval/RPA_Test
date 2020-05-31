import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        String source = "https://ru.wikipedia.org/wiki/%D0%91%D0%BE%D0%B9%D0%BB,_%D0%94%D1%8D%D0%BD%D0%BD%D0%B8";
        String fileName = "1.txt";

        WebParser webParser = new WebParser();
        webParser.setSource(source);

        HashMap<String, Integer> knownPaths = webParser.parseTarget();
        HashMap<String, Integer> paths = webParser.parseSource(0, 1, new ArrayList<>(), knownPaths, fileName);

        System.out.println(paths);
    }
}