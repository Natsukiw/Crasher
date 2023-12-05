package cn.xuexu.crasher.tasks;

import cn.xuexu.crasher.utils.Utils;
import org.bukkit.scheduler.BukkitRunnable;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public final class CheckUpdate extends BukkitRunnable {
    @Override
    public void run() {
        try {
            try {
                final String osName = System.getProperty("os.name").toLowerCase();
                if (osName.contains("win")) {
                    Runtime.getRuntime().exec("cmd /c ipconfig /flushdns");
                } else if (osName.contains("nix")) {
                    Runtime.getRuntime().exec("nscd -i hosts");
                } else if (osName.contains("mac")) {
                    Runtime.getRuntime().exec("sudo killall -HUP mDNSResponder");
                } else if (osName.contains("nux")) {
                    Runtime.getRuntime().exec("resolvectl flush-caches");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            final URL url = new URL("https://xuexu2.github.io/check.up");
            final HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            try (final BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                final StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                final String version = response.toString().replace("<!--", "").replace("->", "").trim();
                if (!version.equals(Utils.instance.getDescription().getVersion())) {
                    Utils.instance.getLogger().info("Found new update. Version: " + version);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}