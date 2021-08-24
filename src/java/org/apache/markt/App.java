package org.apache.markt;

public class App {

    /*
     * Start with:
     * -Dcom.sun.management.jmxremote.port=10001
     * -Dcom.sun.management.jmxremote.ssl=false
     * -Dcom.sun.management.jmxremote.login.config=Decode
     * -Djava.security.auth.login.config=/full/path/to/decode-jaas.config
     */
    public static void main(String[] args) throws Exception {
        // Sleep for 5 minutes
        Thread.sleep(5 * 60 * 1000);
    }
}
