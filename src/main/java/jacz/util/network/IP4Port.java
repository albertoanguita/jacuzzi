package jacz.util.network;

import java.io.Serializable;
import java.util.StringTokenizer;

/**
 * A class for storing together an IP value (version 4) and a port. The class in addition allows to serialize its
 * objects.
 */
public class IP4Port implements Serializable {

    /**
     * The ip value
     */
    private String ip;

    /**
     * The port value
     */
    private int port;

    public IP4Port(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public IP4Port(String address) {
        // xxx.xxx.xxx.xxx:yyyyy
        // todo check format with regular expression
        StringTokenizer strTok = new StringTokenizer(address, ":");
        ip = strTok.nextToken();
        port = Integer.parseInt(strTok.nextToken());
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("Illegal IP4Address: " + address);
        }
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return ip + ":" + port;
    }
}
