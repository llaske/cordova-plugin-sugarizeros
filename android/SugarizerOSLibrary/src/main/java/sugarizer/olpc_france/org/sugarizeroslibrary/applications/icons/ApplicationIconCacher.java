package sugarizer.olpc_france.org.sugarizeroslibrary.applications.icons;

import java.util.HashSet;

public class ApplicationIconCacher {

    private static HashSet<String> cache;

    public ApplicationIconCacher() {
        if (cache == null) {
            cache = new HashSet<>();
        }
    }

    public boolean contains(String packageName) {
        return cache.contains(packageName);
    }

    public void add(String filename) {
        cache.add(filename);
    }
}
