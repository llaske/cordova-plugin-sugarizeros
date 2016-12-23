package sugarizer.olpc_france.org.sugarizeroslibrary.applications;

import java.util.List;

public class ApplicationCacher {
    public boolean hasCache() {
        return cache != null;
    }

    private static List<Application> cache;

    public List<Application> getApplications() {
        return cache;
    }

    public void setApplications(List<Application> applications) {
        cache = applications;
    }

    public void invalidateCache() {
        cache = null;
    }
}
