package jacz.util.files;

import java.io.File;
import java.util.Map;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 20-nov-2008<br>
 * Last Modified: 20-nov-2008
 */
public class FileUtilExtended {

    /**
     * Transforms a given route using a mapping of directories
     *
     * @param route  the route to modify
     * @param mapDir the mappings of directories
     * @return the new route
     */
    public static String transformRoute(String route, Map<String, String> mapDir) {
        // the method is recursive. It transforms or copies a directory at each step

        // finished processing the route
        if (route.length() == 0) {
            return "";
        }
        // if the route begins with a file separator, copy it and transform the rest
        else if (route.startsWith(File.separator)) {
            route = route.substring(File.separator.length());
            return File.separator + transformRoute(route, mapDir);
        } else {

            int indexOfFS = route.indexOf(File.separator);
            String dir;
            if (indexOfFS > 0) {
                dir = route.substring(0, indexOfFS);
                route = route.substring(indexOfFS);
            } else {
                dir = route;
                route = "";
            }
            if (mapDir.containsKey(dir)) {
                dir = mapDir.get(dir);
            }
            return dir + transformRoute(route, mapDir);
        }
    }
}
