package sugarizer.olpc_france.org.sugarizeroslibrary.applications;


public class Application {

    public String packageName;
    public String name;
    public String version;
    public String icon;

    public static class Builder {

        private Application application = new Application();

        private Builder() {
        }

        public static Builder create() {
            return new Builder();
        }

        public Builder setPackageName(String packageName) {
            application.packageName = packageName;

            return this;
        }

        public Application build() {
            return application;
        }
    }

}
