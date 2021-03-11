package com.example.demo;

public class TenantPathResolver {

    private static final String QUERY = "?";
    private static final String TENANT = "tenant/";

    public static String extractTenantId(String path) {
        validatePath(path);
        return tenantIdWithoutQuery(extractFromPath(path));
    }

    private static String extractFromPath(String path) {
        return path.substring(path.indexOf(TENANT)).split("/")[1];
    }

    private static void validatePath(String path) {
        if(path == null || !path.contains(TENANT)) {
            throw new IllegalStateException("no tenant-pattern found in the request path!");
        }
    }

    private static String tenantIdWithoutQuery(String realm) {
        if (realm.contains(QUERY)) {
            return realm.split("\\?")[0];
        }
        return realm;
    }
}
