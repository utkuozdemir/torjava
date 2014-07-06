package org.utkuozdemir.torjava;

import org.jsoup.Jsoup;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

import java.io.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Utku on 6.7.2014...
 */
public class Launcher {
	private static final String TR_MAIN_PAGE = "http://www.wattpad.com/41307453-bir-ba%C5%9Flang%C4%B1%C3%A7";

	private static String torbrowserHome;
	private static int count = 1;
	private static int secondsBetweenRuns = 0;

	public static void main(String[] args) throws IOException, InterruptedException {
		if (args.length == 0) {
			System.out.println("Enter Tor Browser path as first argument! - required");
			System.out.println("Enter how many times program will run as second argument. - optional");
			System.out.println("Enter secounds to wait between two runs - optional");
			System.out.println("Example: TorJava \"C:\\Users\\Username\\Desktop\\Tor Browser\" 3 5");
			return;
		}

		killFirefox();
		torbrowserHome = args[0];
		if (args.length == 2) {
			count = Integer.parseInt(args[1]);
		}

		if (args.length > 2) {
			secondsBetweenRuns = Integer.parseInt(args[2]);
		}

		for (int c = 0; c < count; c++) {
			System.out.println("SESSION " + (c+1) + " STARTING!");
			FirefoxDriver driver = initDriver();
			System.out.println("IP ADDRESS: " + getCurrentIp(driver));
			System.out.println("Opening TR Page...");
			driver.get(TR_MAIN_PAGE);

			driver.findElement(By.className("selectBox-label")).click();
			WebElement dropdownMenu = driver.findElement(By.className("selectBox-dropdown-menu"));
			List<WebElement> sectionLis = dropdownMenu.findElements(By.tagName("li"));
			System.out.println("Section count: " + sectionLis.size());

			for (int i = 0; i < sectionLis.size(); i++) {
				// browse all pages
				List<WebElement> nextPageLinks = driver.findElements(By.className("next_page"));
				if (!nextPageLinks.isEmpty()) {
					WebElement nextPageLink = nextPageLinks.get(0);
					String visibility = nextPageLink.getCssValue("visibility");
					boolean nextPageButtonVisible = !"hidden".equals(visibility);
					while (nextPageButtonVisible) {
						nextPageLink.click();
						Thread.sleep(2000);
						nextPageLink = driver.findElement(By.className("next_page"));
						visibility = nextPageLink.getCssValue("visibility");
						nextPageButtonVisible = !"hidden".equals(visibility);
					}
				}

				if (i > 0) {
					driver.findElement(By.className("selectBox-label")).click();
					dropdownMenu = driver.findElement(By.className("selectBox-dropdown-menu"));
					sectionLis = dropdownMenu.findElements(By.tagName("li"));
				}

				sectionLis.get(i).findElement(By.tagName("a")).click();
				Thread.sleep(5000);
			}

			killFirefox();

			if (count > 1 && secondsBetweenRuns > 0) {
				System.out.println("Waiting " + secondsBetweenRuns + " seconds before next run...");
				Thread.sleep(secondsBetweenRuns * 1000);
			}
			System.out.println();
		}
	}

	private static FirefoxDriver initDriver() {
		File torProfileDir
				= new File(torbrowserHome + File.separator + "Data" + File.separator +
				"Browser" + File.separator + "profile.default");
		FirefoxBinary binary = new FirefoxBinary(new File(torbrowserHome + File.separator + "Start Tor Browser.exe"));
		FirefoxProfile torProfile = new FirefoxProfile(torProfileDir);
		torProfile.setPreference("webdriver.load.strategy", "unstable");

		try {
			binary.startProfile(torProfile, torProfileDir, "");
		} catch (IOException e) {
			e.printStackTrace();
		}

		FirefoxProfile profile = new FirefoxProfile();
		profile.setPreference("network.proxy.type", 1);
		profile.setPreference("network.proxy.socks", "127.0.0.1");
		profile.setPreference("network.proxy.socks_port", 9150);
		FirefoxDriver firefoxDriver = new FirefoxDriver(profile);
		firefoxDriver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
		return firefoxDriver;
	}

	private static void killFirefox() {
		Runtime rt = Runtime.getRuntime();

		try {
			rt.exec("taskkill /F /IM firefox.exe");

			while (processIsRunning("firefox.exe")) {
				Thread.sleep(100);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static boolean processIsRunning(String process) {
		boolean firefoxIsRunning = false;
		String line;
		try {
			Process proc = Runtime.getRuntime().exec("wmic.exe");
			BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			OutputStreamWriter oStream = new OutputStreamWriter(proc.getOutputStream());
			oStream.write("process where name='" + process + "'");
			oStream.flush();
			oStream.close();
			while ((line = input.readLine()) != null) {
				if (line.toLowerCase().contains("caption")) {
					firefoxIsRunning = true;
					break;
				}
			}
			input.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		return firefoxIsRunning;
	}

	private static String getCurrentIp(WebDriver webDriver) {
		webDriver.get("http://www.trackip.net/ip");
		return Jsoup.parse(webDriver.getPageSource()).text();
	}
}
